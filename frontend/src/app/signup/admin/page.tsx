"use client";
import React, { useState } from "react";
import axios from "axios";
import { useRouter } from "next/navigation";
import Link from "next/link";

export default function AdminSignupPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [address, setAddress] = useState("");
  const [adminCode, setAdminCode] = useState("");
  const [errorMessage, setErrorMessage] = useState<string>("");
  const [successMessage, setSuccessMessage] = useState<string>("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await axios.post("/member/join/admin", {
        email,
        password,
        address,
        adminCode,
      });
      // 가입 성공 시 메시지 출력 후 로그인 페이지로 이동
      setSuccessMessage(response.data);
      setTimeout(() => {
        router.push("/login");
      }, 2000);
    } catch (err: any) {
      let errMsg = "회원가입 실패";
      if (err.response?.data) {
        if (typeof err.response.data === "string") {
          errMsg = err.response.data;
        } else if (typeof err.response.data === "object") {
          errMsg = err.response.data.msg || JSON.stringify(err.response.data);
        }
      }
      setErrorMessage(errMsg);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded shadow-md w-full max-w-md">
        <h1 className="text-2xl font-bold mb-6 text-center">관리자 회원가입</h1>
        {errorMessage && <p className="text-red-500 mb-4">{errorMessage}</p>}
        {successMessage && (
          <p className="text-green-500 mb-4">{successMessage}</p>
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
          <div className="mb-4">
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
          <div className="mb-4">
            <label htmlFor="address" className="block text-gray-700">
              주소
            </label>
            <input
              type="text"
              id="address"
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              className="mt-1 p-2 border rounded w-full"
              required
            />
          </div>
          <div className="mb-6">
            <label htmlFor="adminCode" className="block text-gray-700">
              관리자 코드
            </label>
            <input
              type="text"
              id="adminCode"
              value={adminCode}
              onChange={(e) => setAdminCode(e.target.value)}
              className="mt-1 p-2 border rounded w-full"
              required
            />
          </div>
          <button
            type="submit"
            className="w-full bg-green-500 text-white p-2 rounded hover:bg-green-600 transition-colors"
          >
            관리자 회원가입
          </button>
        </form>
        <div className="mt-4 text-center">
          <p>
            이미 계정이 있으신가요?{" "}
            <Link href="/login" className="text-blue-500 hover:underline">
              로그인하기
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
