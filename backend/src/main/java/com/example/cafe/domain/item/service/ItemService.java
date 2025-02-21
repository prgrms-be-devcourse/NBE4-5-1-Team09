package com.example.cafe.domain.item.service;

import com.example.cafe.domain.item.dto.ItemRequestDto;
import com.example.cafe.domain.item.dto.ItemResponseDto;
import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.entity.ItemStatus;
import com.example.cafe.domain.item.repository.ItemRepository;
import com.example.cafe.global.exception.ItemNotFoundException;
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
    private String uploadDir = "src/main/resources/static/images";

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

        Item savedProduct = itemRepository.save(item);
        return new ItemResponseDto(savedProduct);
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

        return new ItemResponseDto(item);
    }

    public void deleteItem(Long id) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다. id: " + id));

        itemRepository.delete(item);
    }

    private String saveImage(MultipartFile imageFile) throws IOException {

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

        // 저장된 파일의 경로 반환
        return filePath.toString();
    }
}
