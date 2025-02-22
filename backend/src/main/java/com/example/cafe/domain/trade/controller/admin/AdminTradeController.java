package com.example.cafe.domain.trade.controller.admin;

import com.example.cafe.domain.trade.domain.dto.request.AdminConfirmRequestDto;
import com.example.cafe.domain.trade.domain.dto.response.OrderResponseDto;
import com.example.cafe.domain.trade.service.admin.AdminTradeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/trade")
@Tag(name = "Admin Trade API", description = "판매자가 거래에 대한 상태를 변경 가능합니다.")
public class AdminTradeController {

    private final AdminTradeService service;

    @PostMapping("/confirm")
    public ResponseEntity<OrderResponseDto> confirm(AdminConfirmRequestDto requestDto) {
        return ResponseEntity.ok(service.adminConfirm(requestDto));
    }

    @PostMapping("/prepare")
    public ResponseEntity<OrderResponseDto> prepare(AdminConfirmRequestDto requestDto) {
        return ResponseEntity.ok(service.adminPrepareDelivery(requestDto));
    }

    @PostMapping("/in-delivery")
    public ResponseEntity<OrderResponseDto> inDelivery(AdminConfirmRequestDto requestDto) {
        return ResponseEntity.ok(service.adminSetInDelivery(requestDto));
    }

    @PostMapping("/post-delivery")
    public ResponseEntity<OrderResponseDto> postDelivery(AdminConfirmRequestDto requestDto) {
        return ResponseEntity.ok(service.adminSetPostDelivery(requestDto));
    }
}
