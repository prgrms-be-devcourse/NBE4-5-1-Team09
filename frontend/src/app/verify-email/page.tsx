"use client";
import React, { useState, useEffect } from "react";
import axios from "axios";
import { useRouter } from "next/navigation";

export default function VerifyEmailPage() {
  const [email, setEmail] = useState<string>("");
  const [code, setCode] = useState<string>("");
  const [message, setMessage] = useState<string>("");
  const [error, setError] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(true);
  const router = useRouter();

  useEffect(() => {
    // 로컬 스토리지에서 email 정보를 가져옴
    const storedEmail = localStorage.getItem("email");
    if (!storedEmail) {
      router.replace("/login");
      return;
    }
    setEmail(storedEmail);
    setLoading(false);
  }, [router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await axios.post("/member/verify-email", { email, code });
      if (res.data === "이메일 인증 성공") {
        setMessage("이메일 인증 성공! 홈 페이지로 이동합니다.");
        setTimeout(() => {
          router.push("/");
        }, 2000);
      } else {
        setError("이메일 인증 실패");
      }
    } catch (err: any) {
      let errMsg: string = "이메일 인증 중 오류 발생";
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

  if (loading) return null;

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded shadow-md w-full max-w-md">
        <h1 className="text-2xl font-bold mb-6 text-center">이메일 인증</h1>
        {message && <p className="text-green-500 mb-4">{message}</p>}
        {error && <p className="text-red-500 mb-4">{error}</p>}
        <div className="mb-4">
          <p className="text-gray-700">
            인증할 이메일: <span className="font-bold">{email}</span>
          </p>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="mb-6">
            <label htmlFor="code" className="block text-gray-700">
              인증 코드
            </label>
            <input
              type="text"
              id="code"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              className="mt-1 p-2 border rounded w-full"
              required
            />
          </div>
          <button
            type="submit"
            className="w-full bg-blue-500 text-white p-2 rounded hover:bg-blue-600 transition-colors"
          >
            인증하기
          </button>
        </form>
      </div>
    </div>
  );
}
