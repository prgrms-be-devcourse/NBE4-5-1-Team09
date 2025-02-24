//package com.example.cafe.domain.review.service;
//
//import com.example.cafe.domain.item.entity.Item;
//import com.example.cafe.domain.item.repository.ItemRepository;
//import com.example.cafe.domain.member.entity.Member;
//import com.example.cafe.domain.member.repository.MemberRepository;
//import com.example.cafe.domain.review.entity.Review;
//import com.example.cafe.domain.review.entity.ReviewSortType;
//import com.example.cafe.domain.review.repository.ReviewRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class ReviewServiceTest {
//
//    @Mock
//    private ReviewRepository reviewRepository;
//
//    @Mock
//    private MemberRepository memberRepository;
//
//    @Mock
//    private ItemRepository itemRepository;
//
//    @InjectMocks
//    private ReviewService reviewService;
//
//    private Member testMember;
//    private Item testItem;
//
//    @BeforeEach
//    void setUp() {
//        testMember = new Member();
//        testMember.setId(1L);
//        testMember.setEmail("test@example.com");
//
//        testItem = new Item();
//        testItem.setId(1L);
//    }
//
//    @Test
//    void testCreateReview() {
//        // Arrange
//        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testMember));
//        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
//
//        Review review = new Review();
//        review.setMember(testMember);
//        review.setItem(testItem);
//        review.setReviewContent("Great product!");
//        review.setRating(5.0);
//        when(reviewRepository.save(any(Review.class))).thenReturn(review);
//
//        // Act
//        Review result = reviewService.createReview("test@example.com", 1L, "Great product!", 5.0);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("Great product!", result.getReviewContent());
//        assertEquals(5.0, result.getRating());
//        verify(memberRepository).findByEmail("test@example.com");
//        verify(itemRepository).findById(1L);
//        verify(reviewRepository).save(any(Review.class));
//    }
//
//    @Test
//    void testUpdateReview() {
//        // Arrange
//        Review existingReview = new Review();
//        existingReview.setId(1L);
//        existingReview.setMember(testMember);
//        existingReview.setItem(testItem);
//        existingReview.setReviewContent("Old review");
//        existingReview.setRating(3.0);
//
//        when(reviewRepository.findById(1L)).thenReturn(Optional.of(existingReview));
//        when(reviewRepository.save(any(Review.class))).thenReturn(existingReview);
//
//        // Act
//        Review result = reviewService.updateReview(1L, "Updated review", 4.0);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("Updated review", result.getReviewContent());
//        assertEquals(4.0, result.getRating());
//        verify(reviewRepository).findById(1L);
//        verify(reviewRepository).save(any(Review.class));
//    }
//
//    @Test
//    void testDeleteReview() {
//        // Arrange
//        Review existingReview = new Review();
//        existingReview.setId(1L);
//
//        when(reviewRepository.findById(1L)).thenReturn(Optional.of(existingReview));
//
//        // Act
//        reviewService.deleteReview(1L);
//
//        // Assert
//        verify(reviewRepository).delete(existingReview);
//    }
//
//    @Test
//    void testGetReviewsByItem() {
//        // Arrange
//        Review review1 = new Review();
//        review1.setReviewContent("Good product!");
//        review1.setRating(4.0);
//        Review review2 = new Review();
//        review2.setReviewContent("Not bad");
//        review2.setRating(3.0);
//
//        when(reviewRepository.findByItem_IdAndMember_EmailNotOrderByCreatedAtDesc(1L, "test@example.com"))
//                .thenReturn(List.of(review1, review2));
//
//        // Act
//        List<Review> reviews = reviewService.getReviewsByItem(1L, "test@example.com", ReviewSortType.LATEST);
//
//        // Assert
//        assertEquals(2, reviews.size());
//        assertEquals("Good product!", reviews.get(0).getReviewContent());
//        assertEquals("Not bad", reviews.get(1).getReviewContent());
//        verify(reviewRepository).findByItem_IdAndMember_EmailNotOrderByCreatedAtDesc(1L, "test@example.com");
//    }
//
//    @Test
//    void testGetAverageRating() {
//        // Arrange
//        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
//        when(reviewRepository.findByItemId(1L)).thenReturn(List.of(new Review(testMember, testItem, "Good", 4.0),
//                new Review(testMember, testItem, "Great", 5.0)));
//
//        // Act
//        double averageRating = reviewService.getAverageRating(1L);
//
//        // Assert
//        assertEquals(4.5, averageRating);
//        verify(itemRepository).findById(1L);
//        verify(reviewRepository).findByItemId(1L);
//    }
//
//    @Test
//    void testFindAllReviews() {
//        // Arrange
//        Review review1 = new Review();
//        review1.setReviewContent("Review 1");
//        review1.setRating(4.0);
//        Review review2 = new Review();
//        review2.setReviewContent("Review 2");
//        review2.setRating(3.0);
//
//        when(reviewRepository.findAll()).thenReturn(List.of(review1, review2));
//
//        // Act
//        List<Review> reviews = reviewService.findAllReviews();
//
//        // Assert
//        assertEquals(2, reviews.size());
//        assertEquals("Review 1", reviews.get(0).getReviewContent());
//        assertEquals("Review 2", reviews.get(1).getReviewContent());
//        verify(reviewRepository).findAll();
//    }
//}
