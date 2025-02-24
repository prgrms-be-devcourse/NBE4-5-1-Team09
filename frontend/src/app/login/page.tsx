// src/app/login/page.tsx
"use client";
import React, { useState } from "react";
import axios from "axios";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "../../context/AuthContext";

export default function LoginPage() {
  const { setToken } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState<string>("");
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await axios.post("/member/login", { email, password });
      const { token, email: returnedEmail } = response.data;
      // 전역 상태 업데이트
      setToken(token);
      localStorage.setItem("email", returnedEmail);
      router.push("/");
    } catch (err: any) {
      let errMsg = "로그인 실패";
      if (err.response?.data) {
        if (typeof err.response.data === "string") {
          errMsg = err.response.data;
        } else if (typeof err.response.data === "object") {
          errMsg = err.response.data.msg || JSON.stringify(err.response.data);
        }
      }
      if (errMsg === "이메일 인증이 완료되지 않았습니다.") {
        localStorage.setItem("email", email);
      }
      setErrorMessage(errMsg);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded shadow-md w-full max-w-md">
        <h1 className="text-2xl font-bold mb-6 text-center">로그인</h1>
        {errorMessage && (
          <p className="text-red-500 mb-4">
            {errorMessage}{" "}
            {errorMessage === "이메일 인증이 완료되지 않았습니다." && (
              <Link href="/verify-email" className="underline text-blue-500">
                인증하러 가기
              </Link>
            )}
          </p>
        )}
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label htmlFor="email" className="block text-gray-700">
              이메일
            </label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="mt-1 p-2 border rounded w-full"
              required
            />
          </div>
          <div className="mb-6">
            <label htmlFor="password" className="block text-gray-700">
              비밀번호
            </label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="mt-1 p-2 border rounded w-full"
              required
            />
          </div>
          <button
            type="submit"
            className="w-full bg-blue-500 text-white p-2 rounded hover:bg-blue-600 transition-colors"
          >
            로그인
          </button>
        </form>
        <div className="mt-4 text-center">
          <p>
            아직 계정이 없으신가요?{" "}
            <Link href="/signup" className="text-blue-500 hover:underline">
              회원가입
            </Link>
          </p>
          <p className="mt-2">
            비밀번호를 잊으셨나요?{" "}
            <Link
              href="/forgot-password"
              className="text-blue-500 hover:underline"
            >
              비밀번호 재설정
            </Link>
          </p>
          <p className="mt-2">
            관리자로 회원가입하시겠습니까?{" "}
            <Link
              href="/signup/admin"
              className="text-blue-500 hover:underline"
            >
              관리자 회원가입
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
