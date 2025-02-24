package com.example.cafe.domain.item.service;

import com.example.cafe.domain.item.dto.ItemRequestDto;
import com.example.cafe.domain.item.dto.ItemResponseDto;
import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.entity.ItemCategory;
import com.example.cafe.domain.item.entity.ItemStatus;
import com.example.cafe.domain.item.repository.ItemRepository;
import com.example.cafe.domain.review.repository.ReviewRepository;
import com.example.cafe.global.exception.ItemNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ReviewRepository reviewRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public void setImageDirectory(String directory) {
        this.uploadDir = directory;
    }

    public List<ItemResponseDto> getAllItems() {
        return itemRepository.findAll().stream().map(ItemResponseDto::new).toList();
    }

    public ItemResponseDto getItem(Long id) {

        return itemRepository.findById(id)
                .map(ItemResponseDto::new)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }

    @Transactional
    public ItemResponseDto createItem(ItemRequestDto itemRequestDto) {

        Item item = new Item();
        item.setItemName(itemRequestDto.getItemName());
        item.setPrice(itemRequestDto.getPrice());
        item.setStock(itemRequestDto.getStock());
        item.setImagePath(itemRequestDto.getImagePath());
        item.setContent(itemRequestDto.getContent());
        item.setCategory(itemRequestDto.getCategory());
        item.setAvgRating(0.0);
        item.setItemStatus(ItemStatus.ON_SALE);
        item.autoCheckQuantityForSetStatus();

        Item savedItem = itemRepository.save(item);

        return new ItemResponseDto(savedItem);
    }

    @Transactional
    public ItemResponseDto createItemImage(ItemRequestDto itemRequestDto, MultipartFile imageFile) throws IOException {

        String imagePath = saveImage(imageFile);
        itemRequestDto.setImagePath(imagePath);

        // Item 엔티티 생성 및 저장
        Item item = new Item();

        item.setItemName(itemRequestDto.getItemName());
        item.setPrice(itemRequestDto.getPrice());
        item.setStock(itemRequestDto.getStock());
        item.setImagePath(itemRequestDto.getImagePath());
        item.setContent(itemRequestDto.getContent());
        item.setCategory(itemRequestDto.getCategory());
        item.setAvgRating(0.0);
        item.setItemStatus(ItemStatus.ON_SALE);
        item.autoCheckQuantityForSetStatus();

        Item savedItem = itemRepository.save(item);

        // 저장된 아이템 반환
        return new ItemResponseDto(savedItem);
    }

    @Transactional
    public ItemResponseDto updateItemImage(Long id, ItemRequestDto itemRequestDto, MultipartFile imageFile) throws IOException {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        // 기존 이미지 경로 유지
        String imagePath = item.getImagePath();

        // 새로운 이미지가 있을 경우
        if (imageFile != null && !imageFile.isEmpty()) {
            // 기존 이미지 삭제
            if (imagePath != null) {
                deleteImage(imagePath);
            }

            // 새 이미지 저장
            imagePath = saveImage(imageFile);  // 새 이미지 저장 후 경로 반환
        }

        item.setItemName(itemRequestDto.getItemName());
        item.setPrice(itemRequestDto.getPrice());
        item.setStock(itemRequestDto.getStock());
        item.setImagePath(imagePath);  // 새 이미지 경로 또는 기존 경로
        item.setContent(itemRequestDto.getContent());
        item.setCategory(itemRequestDto.getCategory());

        item.autoCheckQuantityForSetStatus();  // 재고에 따른 상태 자동 설정

        Item savedItem = itemRepository.save(item);

        return new ItemResponseDto(savedItem);
    }

    @Transactional
    public ItemResponseDto updateItem(Long id, ItemRequestDto itemRequestDto) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        item.setItemName(itemRequestDto.getItemName());
        item.setPrice(itemRequestDto.getPrice());
        item.setStock(itemRequestDto.getStock());
        item.setImagePath(itemRequestDto.getImagePath());
        item.setContent(itemRequestDto.getContent());
        item.setCategory(itemRequestDto.getCategory());

        item.autoCheckQuantityForSetStatus();

        return new ItemResponseDto(item);
    }

    public void deleteItem(Long id) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다. id: " + id));

        itemRepository.delete(item);
    }

    @Transactional
    public void deleteItemImage(Long id) throws IOException {
        // 상품 조회
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다. id: " + id));

        // 상품에 연결된 이미지 파일 경로 가져오기
        String imagePath = item.getImagePath();

        // 상품에 이미지가 있으면 삭제
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            deleteImage(imagePath);
        }

        // 상품 삭제
        itemRepository.delete(item);
    }

    public List<ItemResponseDto> searchItems(String keyword, ItemCategory category, Integer minPrice, Integer maxPrice, int page, int size) {

        // 기본값 설정 (null이면 0 또는 최대값)
        int safeMinPrice = (minPrice != null) ? minPrice : 0;
        int safeMaxPrice = (maxPrice != null) ? maxPrice : Integer.MAX_VALUE;

        Pageable pageable = PageRequest.of(page, size);

        return itemRepository.searchItems(keyword, category, minPrice, maxPrice, ItemStatus.ON_SALE, pageable)
                .stream()
                .map(ItemResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<ItemResponseDto> getItemsByCategory(ItemCategory category, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return itemRepository.findByCategory(category, pageable)
                .stream()
                .map(ItemResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<ItemResponseDto> getItemsByStatus(ItemStatus status, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return itemRepository.findByItemStatus(status, pageable)
                .stream()
                .map(ItemResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<ItemResponseDto> getTopRatedItems(int limit) {

        Pageable pageable = PageRequest.of(0, limit, Sort.by("avgRating").descending());

        return itemRepository.findByItemStatus(ItemStatus.ON_SALE, pageable)
                .stream()
                .map(ItemResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Double getAverageRating(Long itemId) {

        Double avgRating = reviewRepository.findAverageRatingByItemId(itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다. id: " + itemId));

        item.setAvgRating(avgRating);
        itemRepository.save(item);

        return avgRating;
    }

    private String saveImage(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return null; // 이미지가 없으면 null 반환
        }

        // 디렉토리 생성 (없을 경우)
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 고유한 파일명 생성
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);

        // 이미지 저장
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/images/" + fileName; // 저장된 파일의 상대 경로 반환
    }

    private void deleteImage(String imagePath) throws IOException {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return;
        }

        // "/images/" 로 시작하는 상대 경로에서 실제 파일명만 추출
        String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);

        // 실제 파일 경로 생성
        Path fullPath = Paths.get(uploadDir, fileName);
        File file = fullPath.toFile();

        if (file.exists() && !file.delete()) {
            throw new IOException("이미지 파일 삭제 실패: " + fullPath);
        }
    }
}
