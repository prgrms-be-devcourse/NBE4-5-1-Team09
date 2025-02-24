package com.example.cafe.domain.item.controller;

import com.example.cafe.domain.item.dto.ItemRequestDto;
import com.example.cafe.domain.item.dto.ItemResponseDto;
import com.example.cafe.domain.item.entity.ItemCategory;
import com.example.cafe.domain.item.entity.ItemStatus;
import com.example.cafe.domain.item.service.ItemService;
import com.example.cafe.global.annotation.CheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @CheckPermission("ADMIN")
    @Operation(summary = "상품 생성(관리자만 가능)")
    @PostMapping
    public ResponseEntity<ItemResponseDto> createItem(@RequestBody ItemRequestDto itemRequestDto) {

        ItemResponseDto savedItem = itemService.createItem(itemRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    @CheckPermission("ADMIN")
    @Operation(summary = "상품 생성(관리자만 가능) 이미지 기능 추가")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponseDto> createItemImage(
            @RequestPart("item") @Valid ItemRequestDto itemRequestDto,
            @RequestPart("image") MultipartFile imageFile) throws IOException {

        ItemResponseDto savedItem = itemService.createItemImage(itemRequestDto, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    @CheckPermission("ADMIN")
    @Operation(summary = "상품 수정(관리자만 가능)")
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long id, @RequestBody @Valid ItemRequestDto itemRequestDto) {

        ItemResponseDto updatedItem = itemService.updateItem(id, itemRequestDto);
        return ResponseEntity.ok(updatedItem);
    }

    @CheckPermission("ADMIN")
    @Operation(summary = "상품 수정(관리자만 가능) 이미지 기능 추가")
    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponseDto> updateItemImage(
            @PathVariable Long id,
            @RequestPart("item") @Valid ItemRequestDto itemRequestDto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        ItemResponseDto updatedItem = itemService.updateItemImage(id, itemRequestDto, imageFile);
        return ResponseEntity.ok(updatedItem);
    }

    @CheckPermission("ADMIN")
    @Operation(summary = "상품 삭제(관리자만 가능)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {

        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @CheckPermission("ADMIN")
    @Operation(summary = "상품 삭제(관리자만 가능) 이미지 기능 추가")
    @DeleteMapping("/{id}/image")
    public ResponseEntity<Void> deleteItemImage(@PathVariable Long id) throws IOException {

        itemService.deleteItemImage(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상품 검색 및 필터링")
    @GetMapping("/search")
    public ResponseEntity<List<ItemResponseDto>> searchItems(@RequestParam(required = false) String keyword,
                                                             @RequestParam(required = false) ItemCategory category,
                                                             @RequestParam(required = false) Integer minPrice,
                                                             @RequestParam(required = false) Integer maxPrice,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(itemService.searchItems(keyword, category, minPrice, maxPrice, page, size));
    }

    @Operation(summary = "카테고리별 상품 조회")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ItemResponseDto>> getItemsByCategory(
            @PathVariable ItemCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(itemService.getItemsByCategory(category, page, size));
    }

    @Operation(summary = "상품 상태별 조회")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ItemResponseDto>> getItemsByStatus(
            @PathVariable ItemStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(itemService.getItemsByStatus(status, page, size));
    }

    @Operation(summary = "평점 높은 상품 조회")
    @GetMapping("/top-rated")
    public ResponseEntity<List<ItemResponseDto>> getTopRatedItems(
            @RequestParam(defaultValue = "5") int limit) {

        return ResponseEntity.ok(itemService.getTopRatedItems(limit));
    }
}
