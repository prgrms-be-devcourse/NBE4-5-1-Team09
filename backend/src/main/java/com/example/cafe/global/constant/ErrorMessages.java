package com.example.cafe.global.constant;

public final class ErrorMessages {
    public static final String EMAIL_REQUIRED = "이메일을 입력해 주세요.";
    public static final String PASSWORD_REQUIRED = "비밀번호를 입력해 주세요.";
    public static final String ADDRESS_REQUIRED = "주소를 입력해 주세요.";
    public static final String ADMIN_CODE_REQUIRED = "관리자 코드를 입력해 주세요.";
    public static final String VERIFICATION_CODE_REQUIRED = "이메일로 발송 된 인증 코드를 입력해 주세요.";
    public static final String ALREADY_REGISTERED_EMAIL = "이미 등록된 이메일입니다.";
    public static final String MEMBER_NOT_FOUND = "회원 정보가 없습니다.";
    public static final String PASSWORD_MISMATCH = "비밀번호가 일치하지 않습니다.";
    public static final String EMAIL_NOT_VERIFIED = "이메일 인증이 완료되지 않았습니다.";
    public static final String INVALID_EMAIL_FORMAT = "올바른 이메일 형식이 아닙니다.";
    public static final String INVALID_ADMIN_CODE = "관리자 인증 코드가 올바르지 않습니다.";
    public static final String INVALID_VERIFICATION_CODE = "이메일로 발송 된 인증 코드가 일치하지 않습니다.";
    public static final String MAIL_SENDING_FAILED = "메일 발송 중 오류가 발생했습니다.";

    private ErrorMessages() {
    }
}