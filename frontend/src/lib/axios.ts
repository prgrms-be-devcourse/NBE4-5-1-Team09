import axios from "axios";

// axios 기본 설정
const instance = axios.create({
  baseURL: "http://localhost:8080", // 서버 주소
  withCredentials: true, // 쿠키 자동 전송 (리프레시 토큰 쿠키 사용 시)
});

// 필요하다면 요청/응답 인터셉터 등을 설정할 수 있음
// 예: 401 발생 시 재발급 로직, 로깅 등

export default instance;
