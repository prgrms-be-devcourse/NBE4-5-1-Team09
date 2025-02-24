// src/app/account/page.tsx
"use client";
import React, { useEffect, useState } from "react";
import axios from "axios";
import { useRouter } from "next/navigation";

interface UserInfo {
  email: string;
  name: string;
  address: string;
  // 기타 필요한 정보
}

export default function AccountPage() {
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [error, setError] = useState("");
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      router.push("/login");
      return;
    }
    axios
      .get("/member/profile", {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((res) => setUserInfo(res.data))
      .catch((err) => setError(err.response?.data || "회원 정보 조회 실패"));
  }, [router]);

  if (error) {
    return <p className="text-red-500 text-center mt-10">{error}</p>;
  }

  if (!userInfo) {
    return <p className="text-center mt-10">로딩중...</p>;
  }

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="bg-white p-8 rounded shadow-md w-full max-w-md">
        <h1 className="text-2xl font-bold mb-6 text-center">내 정보</h1>
        <p className="mb-2">
          <span className="font-bold">이메일:</span> {userInfo.email}
        </p>
        <p className="mb-2">
          <span className="font-bold">이름:</span> {userInfo.name}
        </p>
        <p className="mb-2">
          <span className="font-bold">주소:</span> {userInfo.address}
        </p>
        {/* 추가 정보 표시 */}
      </div>
    </div>
  );
}
