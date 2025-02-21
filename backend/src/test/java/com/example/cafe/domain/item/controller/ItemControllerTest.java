package com.example.cafe.domain.item.controller;

import com.example.cafe.domain.item.dto.ItemRequestDto;
import com.example.cafe.domain.item.entity.ItemCategory;
import com.example.cafe.domain.item.entity.ItemStatus;
import com.example.cafe.domain.item.repository.ItemRepository;
import com.example.cafe.domain.item.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemController itemController;
    @Autowired
    private ObjectMapper objectMapper;

    private Long existingItemId1;
    private Long existingItemId2;
    private Path testImageDirectory;

    @BeforeAll
    void init() throws Exception {

        // 테스트용 임시 디렉토리 생성
        testImageDirectory = Files.createTempDirectory("test-images-" + UUID.randomUUID());

        // ItemService의 이미지 저장 경로를 테스트 디렉토리로 설정
        itemService.setImageDirectory(testImageDirectory.toString());

        ItemRequestDto requestDto1 = new ItemRequestDto();
        requestDto1.setItemName("프리미엄 아라비카 원두");
        requestDto1.setPrice(10000);
        requestDto1.setStock(100);
        requestDto1.setContent("최상급 아라비카 원두입니다.");
        requestDto1.setCategory(ItemCategory.ARABICA);

        ItemRequestDto requestDto2 = new ItemRequestDto();
        requestDto2.setItemName("로부스타 원두");
        requestDto2.setPrice(8000);
        requestDto2.setStock(50);
        requestDto2.setContent("진한 맛의 로부스타 원두입니다.");
        requestDto2.setCategory(ItemCategory.ROBUSTA);

        MockMultipartFile imageFile1 = new MockMultipartFile(
                "image",
                "arabica.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        MockMultipartFile itemJson1 = new MockMultipartFile(
                "item",
                "",
                "application/json",
                objectMapper.writeValueAsString(requestDto1).getBytes()
        );

        MockMultipartFile imageFile2 = new MockMultipartFile(
                "image",
                "robusta.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        MockMultipartFile itemJson2 = new MockMultipartFile(
                "item",
                "",
                "application/json",
                objectMapper.writeValueAsString(requestDto2).getBytes()
        );

        String response1 = mockMvc.perform(multipart("/items")
                        .file(imageFile1)
                        .file(itemJson1))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        existingItemId1 = objectMapper.readTree(response1).get("id").asLong();

        String response2 = mockMvc.perform(multipart("/items")
                        .file(imageFile2)
                        .file(itemJson2))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        existingItemId2 = objectMapper.readTree(response2).get("id").asLong();
    }

    @AfterAll
    void cleanup() throws IOException {
        // 테스트 종료 후 디렉토리 내 모든 파일과 디렉토리 삭제
        if (testImageDirectory != null) {
            Files.walk(testImageDirectory)
                    .sorted((p1, p2) -> -p1.compareTo(p2)) // 역순으로 정렬하여 파일부터 삭제
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + path + " : " + e.getMessage());
                        }
                    });
        }
    }

    @Test
    @DisplayName("아이템 생성")
    void createItem() throws Exception {

        ItemRequestDto requestDto = new ItemRequestDto();

        requestDto.setItemName("프리미엄 아라비카 원두");
        requestDto.setPrice(10000);
        requestDto.setStock(100);
        requestDto.setContent("최상급 아라비카 원두입니다.");
        requestDto.setCategory(ItemCategory.ARABICA);

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "arabica.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        MockMultipartFile itemJson = new MockMultipartFile(
                "item",
                "",
                "application/json",
                objectMapper.writeValueAsString(requestDto).getBytes()
        );

        MvcResult mvcResult = mockMvc.perform(multipart("/items")
                        .file(imageFile)
                        .file(itemJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.itemName").value("프리미엄 아라비카 원두"))
                .andExpect(jsonPath("$.price").value(10000))
                .andExpect(jsonPath("$.stock").value(100))
                .andExpect(jsonPath("$.imagePath").exists())
                .andExpect(jsonPath("$.content").value("최상급 아라비카 원두입니다."))
                .andExpect(jsonPath("$.category").value(ItemCategory.ARABICA.name()))
                .andExpect(jsonPath("$.avgRating").value(0.0))
                .andExpect(jsonPath("$.itemStatus").value(ItemStatus.ON_SALE.name()))
                .andReturn();
    }

    @Test
    @DisplayName("모든 아이템 조회")
    void getAllItems() throws Exception {

        mockMvc.perform(get("/items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    @DisplayName("아이템 단건 조회")
    void getItem() throws Exception {
        mockMvc.perform(get("/items/{id}", existingItemId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemName").value("프리미엄 아라비카 원두"))
                .andExpect(jsonPath("$.price").value(10000))
                .andExpect(jsonPath("$.stock").value(100))
                .andExpect(jsonPath("$.content").value("최상급 아라비카 원두입니다."))
                .andExpect(jsonPath("$.category").value(ItemCategory.ARABICA.name()));
    }

    @Test
    @DisplayName("아이템 수정")
    void updateItem() throws Exception {

        ItemRequestDto updateRequestDto = new ItemRequestDto();
        updateRequestDto.setItemName("업데이트된 아라비카 원두");
        updateRequestDto.setPrice(12000);
        updateRequestDto.setStock(80);
        updateRequestDto.setContent("업데이트된 최상급 아라비카 원두입니다.");
        updateRequestDto.setCategory(ItemCategory.ARABICA);

        MockMultipartFile newImageFile = new MockMultipartFile(
                "image",
                "updated_arabica.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "updated test image content".getBytes()
        );

        MockMultipartFile updateJson = new MockMultipartFile(
                "item",
                "",
                "application/json",
                objectMapper.writeValueAsString(updateRequestDto).getBytes()
        );

        // 기존 이미지 경로 가져오기
        String beforeUpdateResponse = mockMvc.perform(get("/items/{id}", existingItemId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagePath").exists())
                .andReturn().getResponse().getContentAsString();

        String oldImagePath = objectMapper.readTree(beforeUpdateResponse).get("imagePath").asText();

        // 업데이트 요청
        String afterUpdateResponse = mockMvc.perform(multipart(HttpMethod.PUT, "/items/{id}", existingItemId1)
                        .file(newImageFile)
                        .file(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagePath").exists())
                .andExpect(jsonPath("$.itemName").value(updateRequestDto.getItemName()))
                .andExpect(jsonPath("$.price").value(updateRequestDto.getPrice()))
                .andExpect(jsonPath("$.stock").value(updateRequestDto.getStock()))
                .andExpect(jsonPath("$.content").value(updateRequestDto.getContent()))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @DisplayName("아이템 삭제")
    void deleteItem() throws Exception {

        mockMvc.perform(delete("/items/{id}", existingItemId1))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/items/{id}", existingItemId1))
                .andExpect(status().isNotFound());
    }
}