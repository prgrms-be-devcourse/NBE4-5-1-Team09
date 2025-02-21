package com.example.cafe.global.exception;

public class ItemNotFoundException extends RuntimeException{

    public ItemNotFoundException(Long id) {
        super("아이디 %d에 해당하는 아이템을 찾을 수 없습니다.".formatted(id));
    }

    public ItemNotFoundException(String message) {
        super(message);
    }
}
