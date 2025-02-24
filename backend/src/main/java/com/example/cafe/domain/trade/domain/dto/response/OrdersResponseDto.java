package com.example.cafe.domain.trade.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdersResponseDto {

    private List<OrderItemsDto> buyList = new ArrayList<>();
    private List<OrderItemsDto> payList = new ArrayList<>();
    private List<OrderItemsDto> prepareDeliveryList = new ArrayList<>();
    private List<OrderItemsDto> beforeDeliveryList = new ArrayList<>();
    private List<OrderItemsDto> inDeliveryList = new ArrayList<>();
    private List<OrderItemsDto> postDeliveryList = new ArrayList<>();
    private List<OrderItemsDto> refusedList = new ArrayList<>();
    private List<OrderItemsDto> refundList = new ArrayList<>();


    @Data
    @AllArgsConstructor
    public static class OrderItemsDto {
        private Long itemId;
        private int quantity;
        private String itemName;
    }
}
