package com.example.cafe.domain.item.service;

import com.example.cafe.domain.item.dto.ItemRequestDto;
import com.example.cafe.domain.item.dto.ItemResponseDto;
import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.entity.ItemStatus;
import com.example.cafe.domain.item.respository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public List<ItemResponseDto> getAllItems() {
        return itemRepository.findAll().stream().map(ItemResponseDto::new).toList();
    }

    public ItemResponseDto getItem(Long id) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다. id: " + id));

        return new ItemResponseDto(item);
    }

    @Transactional
    public ItemResponseDto createItem(ItemRequestDto itemRequestDto, MultipartFile imageFile) throws IOException {

        String imagePath = saveImage(imageFile);  // 이미지 경로 생성

        // 이미지 경로를 ItemRequestDto에 설정
        itemRequestDto.setImagePath(imagePath);

        // Item 객체 생성
        Item item = new Item();
        item.setItemName(itemRequestDto.getItemName());
        item.setPrice(itemRequestDto.getPrice());
        item.setStock(itemRequestDto.getStock());
        item.setContent(itemRequestDto.getContent());
        item.setCategory(itemRequestDto.getCategory());
        item.setAvgRating(0.0);
        item.setItemStatus(ItemStatus.ON_SALE);
        item.setImagePath(imagePath);  // 서비스에서 처리된 이미지 경로 저장

        // 아이템 저장
        Item savedItem = itemRepository.save(item);

        return new ItemResponseDto(savedItem);
    }

    @Transactional
    public ItemResponseDto updateItem(Long id, ItemRequestDto itemRequestDto) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다. id: " + id));

        item.setItemName(itemRequestDto.getItemName());
        item.setPrice(itemRequestDto.getPrice());
        item.setStock(itemRequestDto.getStock());
        item.setImagePath(itemRequestDto.getImagePath());
        item.setContent(itemRequestDto.getContent());
        item.setCategory(itemRequestDto.getCategory());

        return new ItemResponseDto(item);
    }

    public void deleteItem(Long id) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다. id: " + id));

        itemRepository.delete(item);
    }

    private String saveImage(MultipartFile imageFile) throws IOException {

        // 저장할 디렉토리 경로 설정
        String uploadDir = "src/main/resources/static/images";  // 적절한 디렉토리 경로로 수정
        Path uploadPath = Paths.get(uploadDir);

        // 디렉토리가 없으면 생성
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 원본 파일 이름에서 확장자 추출
        String originalFileName = imageFile.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));

        // UUID를 이용한 새로운 파일 이름 생성
        String newFileName = UUID.randomUUID().toString() + fileExtension;

        // 새로운 파일 경로 생성
        Path filePath = uploadPath.resolve(newFileName);

        // 파일 복사
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 저장된 파일의 경로 반환 (새로운 파일 이름)
        return filePath.toString();
    }
}
