"use client";
import React, { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
// import axios from "axios"; // 기존 axios 대신
import api from "../../lib/axios"; // 공통 axios 인스턴스 import

export default function DeleteMemberPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const router = useRouter();

  const handleDelete = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        setError("로그인이 필요합니다.");
        return;
      }

      // 공통 axios(api)를 사용해 DELETE 요청
      await api.delete("/member/delete", {
        headers: { Authorization: `Bearer ${token}` },
        data: { email, password },
      });
      // 탈퇴 성공 시 토큰 제거
      localStorage.removeItem("token");
      router.push("/signup");
    } catch (err: any) {
      setError(err.response?.data || "회원 탈퇴 실패");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded shadow-md w-full max-w-md">
        <h1 className="text-2xl font-bold mb-6 text-center">회원 탈퇴</h1>
        {error && <p className="text-red-500 mb-4">{error}</p>}
        <form onSubmit={handleDelete}>
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
            className="w-full bg-red-500 text-white p-2 rounded hover:bg-red-600 transition-colors"
          >
            탈퇴하기
          </button>
        </form>
        <p className="mt-4 text-center">
          <Link href="/profile" className="text-blue-500 hover:underline">
            프로필로 돌아가기
          </Link>
        </p>
      </div>
    </div>
  );
}
