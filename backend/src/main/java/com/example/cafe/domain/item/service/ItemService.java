package com.example.cafe.domain.item.service;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.respository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Item getItem(Long id) {
        return itemRepository.findById(id).get();
    }
}
