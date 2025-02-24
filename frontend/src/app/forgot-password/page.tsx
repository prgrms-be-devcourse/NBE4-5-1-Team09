"use client";
import React, { useState } from "react";
import axios from "axios";
import { useRouter } from "next/navigation";
import Link from "next/link";

export default function ForgotPasswordPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const handleRequest = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      // 이메일을 보내 재설정 코드 요청
      await axios.post("/member/forgot-password", { email });
      setMessage(
        "비밀번호 재설정 이메일을 전송했습니다. 재설정 페이지로 이동합니다."
      );
      // 필요 시 이메일을 localStorage에도 저장 가능
      localStorage.setItem("resetEmail", email);
      setTimeout(() => {
        // 재설정 페이지로 이동 (쿼리로 전달하거나, localStorage로 전달 가능)
        router.push(`/reset-password?email=${encodeURIComponent(email)}`);
      }, 2000);
    } catch (err: any) {
      let errMsg = "비밀번호 재설정 요청 실패";
      if (err.response?.data) {
        if (typeof err.response.data === "string") {
          errMsg = err.response.data;
        } else if (typeof err.response.data === "object") {
          errMsg = err.response.data.msg || JSON.stringify(err.response.data);
        }
      }
      setError(errMsg);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded shadow-md w-full max-w-md">
        <h1 className="text-2xl font-bold mb-6 text-center">
          비밀번호 재설정 요청
        </h1>
        {message && <p className="text-green-500 mb-4">{message}</p>}
        {error && <p className="text-red-500 mb-4">{error}</p>}
        <form onSubmit={handleRequest}>
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
          <button
            type="submit"
            className="w-full bg-blue-500 text-white p-2 rounded hover:bg-blue-600 transition-colors"
          >
            재설정 요청
          </button>
        </form>
        <div className="mt-4 text-center">
          <Link href="/login" className="text-blue-500 hover:underline">
            로그인 페이지로 이동
          </Link>
        </div>
      </div>
    </div>
  );
}
