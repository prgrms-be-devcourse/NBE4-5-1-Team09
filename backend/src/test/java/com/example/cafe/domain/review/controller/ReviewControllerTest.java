package com.example.cafe.domain.review.controller;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.review.dto.ReviewRequestDto;
import com.example.cafe.domain.review.entity.Review;
import com.example.cafe.domain.review.entity.ReviewSortType;
import com.example.cafe.domain.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private Member testMember;
    private Item testItem;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();

        // member와 item을 미리 준비
        testMember = new Member();
        testMember.setId(1L);

        // Item 객체 초기화
        testItem = new Item();
        testItem.setId(1L);
    }

    @Test
    public void testCreateReview() throws Exception {
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(1L, 1L, "Great product!", 5.0);
        Review review = new Review(1L, testMember, testItem, "Great product!", 5.0, LocalDateTime.now(), LocalDateTime.now());

        when(reviewService.createReview(1L, 1L, "Great product!", 5.0)).thenReturn(review);

        mockMvc.perform(post("/reviews/create")
                        .contentType("application/json")
                        .content("{\"memberId\":1,\"itemId\":1,\"reviewContent\":\"Great product!\",\"rating\":5.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.reviewContent").value("Great product!"))
                .andExpect(jsonPath("$.rating").value(5.0));
    }

    @Test
    public void testUpdateReview() throws Exception {
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(1L, 1L, "Updated review", 4.0);
        Review review = new Review(1L, testMember, testItem, "Updated review", 4.0, LocalDateTime.now(), LocalDateTime.now());

        when(reviewService.updateReview(1L, "Updated review", 4.0)).thenReturn(review);

        mockMvc.perform(put("/reviews/update/1")
                        .contentType("application/json")
                        .content("{\"reviewContent\":\"Updated review\",\"rating\":4.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.reviewContent").value("Updated review"))
                .andExpect(jsonPath("$.rating").value(4.0));
    }

    @Test
    public void testDeleteReview() throws Exception {
        doNothing().when(reviewService).deleteReview(1L);

        mockMvc.perform(delete("/reviews/delete/1"))
                .andExpect(status().isNoContent());

        verify(reviewService, times(1)).deleteReview(1L);
    }

    @Test
    public void testGetReviewsByItem() throws Exception {
        Review review1 = new Review(1L, testMember, testItem, "Good product!", 4.0, LocalDateTime.now(), LocalDateTime.now());
        Review review2 = new Review(2L, testMember, testItem, "Not bad", 3.0, LocalDateTime.now(), LocalDateTime.now());

        List<Review> reviews = List.of(review1, review2);
        when(reviewService.getReviewsByItem(1L, ReviewSortType.LATEST)).thenReturn(reviews);

        mockMvc.perform(get("/reviews/item/1?sortType=LATEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewContent").value("Good product!"))
                .andExpect(jsonPath("$[1].reviewContent").value("Not bad"));
    }

    @Test
    public void testGetAverageRating() throws Exception {
        when(reviewService.getAverageRating(1L)).thenReturn(4.0);

        mockMvc.perform(get("/reviews/average/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4.0));
    }

    @Test
    public void testGetAllReviews() throws Exception {
        Review review1 = new Review(1L, testMember, testItem, "Review 1", 4.0, LocalDateTime.now(), LocalDateTime.now());
        Review review2 = new Review(2L, testMember, testItem, "Review 2", 3.0, LocalDateTime.now(), LocalDateTime.now());

        List<Review> reviews = List.of(review1, review2);
        when(reviewService.findAllReviews()).thenReturn(reviews);

        mockMvc.perform(get("/reviews/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewContent").value("Review 1"))
                .andExpect(jsonPath("$[1].reviewContent").value("Review 2"));
    }
}
