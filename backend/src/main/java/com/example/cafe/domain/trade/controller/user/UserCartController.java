package com.example.cafe.domain.trade.controller.user;

import com.example.cafe.domain.trade.domain.dto.request.ItemCartRequestDto;
import com.example.cafe.domain.trade.domain.dto.response.CartListResponseDto;
import com.example.cafe.domain.trade.domain.dto.response.ItemCartResponseDto;
import com.example.cafe.domain.trade.service.user.UserCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class UserCartController {
    private final UserCartService service;

    @GetMapping
    public ResponseEntity<CartListResponseDto> showCart(@RequestParam("memberId") Long memberId) {
        return ResponseEntity.ok(service.showCart(memberId));
    }

    @PostMapping("/add")
    public ResponseEntity<ItemCartResponseDto> addCart(@RequestBody ItemCartRequestDto addItem) {
        return ResponseEntity.ok(service.addItemToCart(addItem));
    }

    @PostMapping("/edit")
    public ResponseEntity<ItemCartResponseDto> editCart(@RequestBody ItemCartRequestDto editItem) {
        return ResponseEntity.ok(service.editItemToCart(editItem));
    }
}
