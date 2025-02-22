package com.example.cafe.domain.item.controller;

import com.example.cafe.domain.item.dto.ItemRequestDto;
import com.example.cafe.domain.item.dto.ItemResponseDto;
import com.example.cafe.domain.item.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Item API", description = "관리자에게는 상품에 대한 CRUD 기능을, 회원에게는 상품 조회 기능을 제공합니다.")
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @Operation(summary = "상품 전체 조회")
    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @Operation(summary = "상품 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItem(id));
    }

    @Operation(summary = "상품 생성(관리자만 가능)")
    @PostMapping
    public ResponseEntity<ItemResponseDto> createItem(@RequestBody ItemRequestDto itemRequestDto) {

        ItemResponseDto savedItem = itemService.createItem(itemRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    @Operation(summary = "상품 수정(관리자만 가능)")
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long id, @RequestBody @Valid ItemRequestDto itemRequestDto) {

        ItemResponseDto updatedItem = itemService.updateItem(id, itemRequestDto);
        return ResponseEntity.ok(updatedItem);
    }

    @Operation(summary = "상품 삭제(관리자만 가능)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {

        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
