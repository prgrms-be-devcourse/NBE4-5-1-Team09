# NBE4-5-1-Team09
프로그래머스 백엔드 데브코스 4기 5회차 9팀 예외처리반 1차 프로젝트 - 카페 메뉴 관리 서비스 제작

# 프로젝트 기간
2025/02/19 12:00 ~ 2025/02/27 12:00

# 구성원
[장무영](https://github.com/wkdan), [신동훈](https://github.com/SDHSeoulTech), [신윤호](https://github.com/messiteacher), [최재우](https://github.com/cjw0324)

# 컨벤션
[🔗 컨벤션 Wiki 바로가기](https://github.com/prgrms-be-devcourse/NBE4-5-1-Team09/wiki/%EC%BB%A8%EB%B2%A4%EC%85%98)

# 프로젝트 필수 요구 사항
### HTTP 메서드 POST를 이용해 Create, GET을 이용해 Read, PUT을 이용해 Update, DELETE를 이용해 Delete 기능을 구현해주세요.

- PUT : 해당하는 id에 해당하는 데이터를 갱신하는 기능을 구현합니다.
- DELETE : 해당하는 id에 해당하는 데이터를 삭제하는 기능을 구현합니다.
- POST: 요청이 들어올 때마다 id가 하나씩 증가하여 menu 리스트에 추가될 수 있도록 코드를 추가 구현합니다.
- SQL과 ORM 중 하나를 선택하여 데이터 베이스를 구현하여 제작합니다.
- 구현한 데이터베이스 연동을 구현합니다.

# 시스템 명세서

[🔗 시스템 명세서 Wiki 바로가기](https://github.com/prgrms-be-devcourse/NBE4-5-1-Team09/wiki/%EC%8B%9C%EC%8A%A4%ED%85%9C-%EB%AA%85%EC%84%B8%EC%84%9C)

# DB ERD (ver.0.1)
https://www.erdcloud.com/team/kKm3uNXtgGF56Jrxx
![image](https://github.com/user-attachments/assets/11f772e0-22ac-4b8b-a661-33c7c4ca98a3)

# 시스템 아키텍처
![image](https://github.com/user-attachments/assets/592ddbd8-6536-4651-900e-92b299322766)


# 시스템 흐름도 초안

```mermaid
graph LR
    %% 액터 영역
    subgraph 사용자
      U1[사용자]
    end

    subgraph 관리자
      A1[관리자]
    end

    %% 프론트엔드 영역
    subgraph 프론트엔드
      F1["상품 상세 페이지 (상품 주문 요청, 리뷰 작성)"]
      F2["장바구니 페이지 (수량 수정, 주문 요청)"]
      F3["주문/결제 페이지 (결제 진행)"]
      F4["상품 관리 페이지 (상품 등록/수정/삭제)"]
      F5["리뷰 페이지 (리뷰 조회, 정렬)"]
      F6["회원 관리 페이지 (회원가입, 로그인 등)"]
    end

    %% 백엔드 서비스 영역
    subgraph 백엔드
      S1["주문 관리 서비스"]
      S2["상품 관리 서비스"]
      S3["리뷰 관리 서비스"]
      S4["유저 관리 서비스"]
    end

    %% 데이터베이스 영역
    subgraph DB
      DB1[(주문 테이블)]
      DB2[(상품 테이블)]
      DB3[(리뷰 테이블)]
      DB4[(회원 테이블)]
    end

    %% 사용자 흐름
    U1 --> F1
    U1 --> F2
    U1 --> F3
    U1 --> F5
    U1 --> F6

    %% 관리자 흐름
    A1 --> F4
    A1 --> F6

    %% 프론트엔드와 백엔드 연결
    F1 --> S2
    F1 --> S3
    F2 --> S1
    F3 --> S1
    F4 --> S2
    F5 --> S3
    F6 --> S4

    %% 백엔드와 데이터베이스 연결
    S1 --> DB1
    S2 --> DB2
    S3 --> DB3
    S4 --> DB4

    %% 관리자 주문 승인/거절 흐름
    A1 ---|주문 승인/거절 오후2시 기준| S1

```

# 시스템 IA

```mermaid
mindmap
  root((시스템 IA))
    주문 및 주문 상품
      주문 프로세스
        상품 주문 요청
        장바구니 관리
        결제 진행
        관리자 승인
        배송 준비 및 진행
        주문 상태
        장바구니 담기
        구매 요청 후 결제 전
        결제 완료
        배송 대기
        배송 준비 중
        배송 중
        배송 완료
        주문 취소
    상품 관리
      상품 목록 조회
      상품 상세 정보 조회
      상품 등록
      상품 수정
      상품 삭제
    후기 관리
      리뷰 작성
      리뷰 수정
      리뷰 삭제
      상품별 리뷰 조회
      평균 평점 조회
      예외 처리
    유저 관리
      회원 가입
        일반 회원 가입
        관리자 회원 가입
      로그인
        일반 회원 로그인
        관리자 로그인
      비밀번호 재설정
      회원 탈퇴
      로그아웃
      이메일 인증
```
