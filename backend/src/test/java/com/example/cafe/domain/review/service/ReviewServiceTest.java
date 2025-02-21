package com.example.cafe.domain.review.service;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.respository.ItemRepository;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.domain.review.entity.Review;
import com.example.cafe.domain.review.entity.ReviewSortType;
import com.example.cafe.domain.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private MemberRepository memberRepository;

    private Member testMember;
    private Item testItem;
    private Review testReview1;
    private Review testReview2;
    private Review testReview3;
    private Review testReview4;
    private Review testReview5;

    @BeforeEach
    void setUp() {
        // Member 객체 초기화
        testMember = new Member();
        testMember.setId(1L);

        // Item 객체 초기화
        testItem = new Item();
        testItem.setId(1L);

        // 여러 개의 Review 객체 초기화
        testReview1 = new Review();
        testReview1.setId(1L);
        testReview1.setMember(testMember);
        testReview1.setItem(testItem);
        testReview1.setReview_content("좋은 제품입니다.");
        testReview1.setRating(4.5);
        testReview1.setCreatedAt(LocalDateTime.now());
        testReview1.setModifiedAt(LocalDateTime.now());

        testReview2 = new Review();
        testReview2.setId(2L);
        testReview2.setMember(testMember);
        testReview2.setItem(testItem);
        testReview2.setReview_content("보통이에요.");
        testReview2.setRating(3.0);
        testReview2.setCreatedAt(LocalDateTime.now());
        testReview2.setModifiedAt(LocalDateTime.now());

        testReview3 = new Review();
        testReview3.setId(3L);
        testReview3.setMember(testMember);
        testReview3.setItem(testItem);
        testReview3.setReview_content("별로였어요.");
        testReview3.setRating(1.5);
        testReview3.setCreatedAt(LocalDateTime.now());
        testReview3.setModifiedAt(LocalDateTime.now());

        testReview4 = new Review();
        testReview4.setId(4L);
        testReview4.setMember(testMember);
        testReview4.setItem(testItem);
        testReview4.setReview_content("가격대비 괜찮습니다.");
        testReview4.setRating(3.8);
        testReview4.setCreatedAt(LocalDateTime.now());
        testReview4.setModifiedAt(LocalDateTime.now());

        testReview5 = new Review();
        testReview5.setId(5L);
        testReview5.setMember(testMember);
        testReview5.setItem(testItem);
        testReview5.setReview_content("완전 마음에 들어요!");
        testReview5.setRating(5.0);
        testReview5.setCreatedAt(LocalDateTime.now());
        testReview5.setModifiedAt(LocalDateTime.now());
    }


    /** ✅ 리뷰 작성 테스트 */
    @Test
    void createReview_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setCreatedAt(LocalDateTime.now());
            review.setModifiedAt(LocalDateTime.now());
            review.setModifiedAt(LocalDateTime.now());
            return review;
        });

        Review createdReview = reviewService.createReview(1L, 1L, "좋은 제품입니다.", 4.5);

        assertNotNull(createdReview);
        assertEquals("좋은 제품입니다.", createdReview.getReview_content()); // 필드명: review_content
        assertEquals(4.5, createdReview.getRating());
        assertNotNull(createdReview.getCreatedAt());
        assertNotNull(createdReview.getModifiedAt());
    }

    /** ✅ 리뷰 수정 테스트 */
    @Test
    void updateReview_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview1));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setModifiedAt(LocalDateTime.now());
            return review;
        });

        LocalDateTime beforeUpdate = testReview1.getModifiedAt();
        Review updatedReview = reviewService.updateReview(1L, "업데이트된 리뷰", 5.0);

        assertEquals("업데이트된 리뷰", updatedReview.getReview_content()); // 필드명: review_content
        assertEquals(5.0, updatedReview.getRating());
        assertTrue(updatedReview.getModifiedAt().isAfter(beforeUpdate));
    }

    /** ✅ 리뷰 삭제 테스트 */
    @Test
    void deleteReview_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview1));
        doNothing().when(reviewRepository).delete(testReview1);

        assertDoesNotThrow(() -> reviewService.deleteReview(1L));
        verify(reviewRepository, times(1)).delete(testReview1);
    }

    /** ✅ 상품별 리뷰 조회 테스트 */
    @Test
    void getReviewsByItem_Success() {
        // 여러 개의 리뷰를 반환하도록 수정
        when(reviewRepository.findByItem_IdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(testReview1, testReview2, testReview3, testReview4, testReview5));

        List<Review> reviews = reviewService.getReviewsByItem(1L, ReviewSortType.LATEST);

        assertFalse(reviews.isEmpty());
        assertEquals(5, reviews.size());  // 리뷰가 5개로 늘어났으므로, 5개를 확인
    }

    /** ✅ 평균 평점 조회 테스트 */
    @Test
    void getAverageRating_Success() {
        // 실제 리뷰 객체 5개를 사용하여 평균 평점 계산
        double expectedAverage = (testReview1.getRating() + testReview2.getRating() + testReview3.getRating() +
                testReview4.getRating() + testReview5.getRating()) / 5.0;

        // findAverageRatingByItem_Id 메서드가 평균 평점을 반환하도록 설정
        when(reviewRepository.findAverageRatingByItem_Id(1L))
                .thenReturn(expectedAverage);

        // 서비스 메서드 호출
        Double avgRating = reviewService.getAverageRating(1L);

        // 예상 평균 평점과 서비스에서 반환된 평균 평점이 일치하는지 확인
        assertEquals(expectedAverage, avgRating);
    }

    /** ✅ 전체 리뷰 조회 테스트 */
    @Test
    void findAllReviews_Success() {
        // 여러 개의 리뷰를 반환하도록 수정
        when(reviewRepository.findAll()).thenReturn(Arrays.asList(testReview1, testReview2, testReview3, testReview4, testReview5));

        List<Review> reviews = reviewService.findAllReviews();

        assertFalse(reviews.isEmpty());
        assertEquals(5, reviews.size());  // 리뷰가 5개로 늘어났으므로, 5개를 확인
    }

    /** ✅ 데이터베이스 오류 발생 시 재시도 확인 */
    @Test
    void getAverageRating_ShouldRetryOnDatabaseError() {
        when(reviewRepository.findAverageRatingByItem_Id(1L))
                .thenThrow(new DataAccessException("DB 연결 오류") {})
                .thenThrow(new DataAccessException("DB 연결 오류") {})
                .thenReturn(4.2);  // 3번째 호출에서 정상값 반환

        Double avgRating = reviewService.getAverageRating(1L);  // 외부에서 호출

        assertEquals(4.2, avgRating);
        verify(reviewRepository, times(3)).findAverageRatingByItem_Id(1L);  // 세 번 호출되었는지 검증
    }

    /** ✅ 예외 발생 테스트: 리뷰 내용이 비어있을 때 */
    @Test
    void createReview_ShouldThrowException_WhenReviewContentIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(1L, 1L, "", 4.5));
    }

    /** ✅ 예외 발생 테스트: 리뷰 내용이 null일 때 */
    @Test
    void createReview_ShouldThrowException_WhenReviewContentIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(1L, 1L, null, 4.5));
    }

    /** ✅ 예외 발생 테스트: 평점이 null일 때 */
    @Test
    void createReview_ShouldThrowException_WhenRatingIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(1L, 1L, "좋은 제품", null));
    }

    /** ✅ 예외 발생 테스트: 리뷰 수정 시 존재하지 않는 경우 */
    @Test
    void updateReview_ShouldThrowException_WhenReviewNotFound() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reviewService.updateReview(1L, "업데이트된 리뷰", 5.0));
    }

    /** ✅ 예외 발생 테스트: 리뷰 삭제 시 존재하지 않는 경우 */
    @Test
    void deleteReview_ShouldThrowException_WhenReviewNotFound() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reviewService.deleteReview(1L));
    }
}
