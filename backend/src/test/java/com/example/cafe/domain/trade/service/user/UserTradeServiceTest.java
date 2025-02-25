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
        // 1. 테스트 환경 셋업: 재고 100인 상품과 주문할 회원 생성
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
        item.setPrice(100);
        item.setStock(100);  // 초기 재고 100
        item.setItemStatus(ItemStatus.ON_SALE);
        item = itemRepository.save(item);

        // 2. 1000 TPS 수준의 주문 요청을 동시에 실행
        int requestCount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(200);  // 동시 실행 스레드 수는 200 정도로 설정
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(requestCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

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
                        // e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        long startTime = System.currentTimeMillis();
        startLatch.countDown();
        doneLatch.await();
        long durationMs = System.currentTimeMillis() - startTime;

        System.out.println("총 실행 시간: " + durationMs + " ms");
        System.out.println("성공 주문 건수: " + successCount.get());
        System.out.println("실패 주문 건수: " + failureCount.get());

        // 성공한 주문 건수는 초기 재고(100)를 초과할 수 없어야 함
        assertTrue(successCount.get() <= 100, "성공한 주문 건수가 초기 재고를 초과함");

        double tps = successCount.get() / (durationMs / 1000.0);
        System.out.println("계산된 TPS: " + tps);

        executor.shutdown();
    }
}
