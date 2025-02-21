package com.example.cafe.domain.trade.controller.user;

import com.example.cafe.domain.trade.domain.dto.request.OrderRequestItemDto;
import com.example.cafe.domain.trade.domain.dto.response.OrderResponseDto;
import com.example.cafe.domain.trade.service.user.UserTradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class UserTradeController {
    private final UserTradeService service;

    @PostMapping("/cart")
    public ResponseEntity<OrderResponseDto> orderWithCart(@RequestParam("memberId") Long memberId) {
        return ResponseEntity.ok(service.tradeWithCart(memberId));
    }

    @PostMapping("/item")
    public ResponseEntity<OrderResponseDto> orderWithItem(@RequestBody OrderRequestItemDto requestItemDto) {
        return ResponseEntity.ok(service.tradeWithItemInfo(requestItemDto));
    }
}
