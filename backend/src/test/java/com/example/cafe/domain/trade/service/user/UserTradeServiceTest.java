package com.example.cafe.domain.trade.service.user;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.entity.ItemStatus;
import com.example.cafe.domain.item.repository.ItemRepository;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.domain.trade.domain.dto.request.OrderRequestItemDto;
import com.example.cafe.domain.trade.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserTradeServiceTest {
    @Autowired
    private UserTradeService userTradeService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Test
    public void testConcurrentTradeWithItemInfo() throws InterruptedException {
        // 1. 테스트 환경 셋업 : 재고가 5인 상품과 주문을 요청할 회원 생성
        Member member = Member.builder()
                .email("test@example.com")
                .password("password123") // 실제 암호화된 값 또는 테스트용 문자열 사용
                .address("Test Address")
                .authority("ROLE_USER")  // 필수값: 예) ROLE_USER, ROLE_ADMIN 등
                .verified(false)         // 기본값 false
                .build();
        member = memberRepository.save(member);

        Item item = new Item();
        item.setItemName("Test Item");
        item.setPrice(100); // 예시 가격
        item.setStock(5);   // 재고 5
        item.setItemStatus(ItemStatus.ON_SALE); // 사용 가능한 상태
        item = itemRepository.save(item);

        // 2. 동시성 테스트를 위해 10개의 스레드가 각각 1개씩 주문 요청
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            Member finalMember = member;
            Item finalItem = item;
            executor.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작되도록 대기
                    startLatch.await();
                    OrderRequestItemDto dto = new OrderRequestItemDto(finalMember.getId(), finalItem.getId(), 1);
                    try {
                        userTradeService.tradeWithItemInfo(dto);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        System.out.println("Order failed: " + e.getMessage());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 모든 스레드 시작 신호 전달 후 완료 대기
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // 3. 검증: 최종 재고, 성공한 주문 수, 그리고 Trade 레코드 수가 일치하는지 확인
        Item updatedItem = itemRepository.findById(item.getId())
                .orElseThrow(() -> new RuntimeException("Item not found"));
        System.out.println("성공 주문 건수: " + successCount.get());
        System.out.println("실패 주문 건수: " + failureCount.get());
        System.out.println("최종 재고: " + updatedItem.getStock());

        // 성공한 주문 건수는 초기 재고(5)를 초과할 수 없음
        assertTrue(successCount.get() <= 5, "성공한 주문 건수가 초기 재고를 초과함");

        // 최종 재고는 초기 재고에서 성공 주문 수만큼 감소했어야 함
        assertEquals(5 - successCount.get(), updatedItem.getStock(), "재고 차감 로직에 문제가 있음");

        // DB에 저장된 Trade 건수도 성공한 주문 수와 일치해야 함
        int tradeCount = tradeRepository.findAll().size();
        assertEquals(successCount.get(), tradeCount, "생성된 Trade 수와 성공한 주문 건수가 일치하지 않음");
    }

}