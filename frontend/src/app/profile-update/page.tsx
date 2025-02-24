// src/app/profile-update/page.tsx
"use client";
import React, { useState, useEffect } from "react";
import axios from "axios";
import { useRouter } from "next/navigation";
import Link from "next/link";

interface Profile {
  email: string;
  address: string;
  authority: string;
}

export default function ProfileUpdatePage() {
  const [address, setAddress] = useState("");
  const [profile, setProfile] = useState<Profile | null>(null);
  const [error, setError] = useState("");
  const router = useRouter();

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const token = localStorage.getItem("token");
        const response = await axios.get("/member/profile", {
          headers: { Authorization: `Bearer ${token}` },
        });
        setProfile(response.data);
        setAddress(response.data.address);
      } catch (err) {
        router.push("/login");
      }
    };
    fetchProfile();
  }, [router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem("token");
      await axios.put(
        "/member/profile",
        { address },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      router.push("/profile");
    } catch (err: any) {
      setError(err.response?.data || "프로필 수정 실패");
    }
  };

  if (!profile) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>로딩중...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded shadow-md w-full max-w-md">
        <h1 className="text-2xl font-bold mb-6 text-center">프로필 수정</h1>
        {error && <p className="text-red-500 mb-4">{error}</p>}
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block text-gray-700">이메일</label>
            <input
              type="text"
              value={profile.email}
              disabled
              className="mt-1 p-2 border rounded w-full bg-gray-200"
            />
          </div>
          <div className="mb-6">
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
          <button
            type="submit"
            className="w-full bg-green-500 text-white p-2 rounded hover:bg-green-600 transition-colors"
          >
            수정 완료
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
