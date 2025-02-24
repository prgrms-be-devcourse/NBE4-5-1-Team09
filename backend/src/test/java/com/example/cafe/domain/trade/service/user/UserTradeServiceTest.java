package com.example.cafe.domain.trade.service.user;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.entity.ItemStatus;
import com.example.cafe.domain.item.repository.ItemRepository;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.domain.trade.domain.dto.request.OrderRequestItemDto;
import com.example.cafe.domain.trade.domain.dto.response.OrderResponseDto;
import com.example.cafe.domain.trade.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class UserTradeServiceTest {

    @Autowired
    private UserTradeAtomicUpdateService userTradeService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Test
    public void testHighConcurrencyTradeWithItemInfo() throws InterruptedException {
        // 1. 테스트 환경 셋업: 재고 1000인 상품과 주문할 회원 생성
        Member member = Member.builder()
                .email("loadtest@example.com")
                .password("password123")
                .address("Load Test Address")
                .authority("ROLE_USER")
                .verified(false)
                .build();
        member = memberRepository.save(member);

        Item item = new Item();
        item.setItemName("Load Test Item");
        item.setPrice(100);       // 예시 가격
        item.setStock(1000);      // 초기 재고 1000
        item.setItemStatus(ItemStatus.ON_SALE);
        item = itemRepository.save(item);

        // 2. 동시성 테스트: 1000개의 주문 요청을 동시에 실행
        int requestCount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(200);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(requestCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < requestCount; i++) {
            Member finalMember = member;
            Item finalItem = item;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    OrderRequestItemDto dto = new OrderRequestItemDto(finalItem.getId(), 1);
                    try {
                        OrderResponseDto response = userTradeService.tradeWithItemInfo(finalMember.getId(), dto);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        // 필요한 경우 로그 출력
                        // System.out.println("Order failed: " + e.getMessage());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        doneLatch.await();
        long durationMs = System.currentTimeMillis() - startTime;
        executor.shutdown();

        // 3. 검증: 최종 재고, 성공 주문 건수, Trade 레코드 수 확인
        Item updatedItem = itemRepository.findById(item.getId())
                .orElseThrow(() -> new RuntimeException("Item not found"));
        System.out.println("총 실행 시간: " + durationMs + " ms");
        System.out.println("성공 주문 건수: " + successCount.get());
        System.out.println("실패 주문 건수: " + failureCount.get());
        System.out.println("최종 재고: " + updatedItem.getStock());

        // 성공한 주문 건수는 초기 재고(1000) 이하여야 함
        assertTrue(successCount.get() <= 1000, "성공한 주문 건수가 초기 재고를 초과함");
        // 최종 재고는 초기 재고에서 성공 주문 수만큼 감소했어야 함
        assertEquals(1000 - successCount.get(), updatedItem.getStock(), "재고 차감 로직에 문제가 있음");
        // Trade 레코드 수도 성공 주문 건수와 일치해야 함
        int tradeCount = tradeRepository.findAll().size();
        assertEquals(successCount.get(), tradeCount, "생성된 Trade 수와 성공한 주문 건수가 일치하지 않음");

        double tps = successCount.get() / (durationMs / 1000.0);
        System.out.println("계산된 TPS: " + tps);
    }
}
