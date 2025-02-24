// src/app/profile/page.tsx
"use client";
import React, { useEffect, useState } from "react";
import axios from "axios";
import { useRouter } from "next/navigation";

interface Profile {
  email: string;
  address: string;
  authority: string;
}

export default function ProfilePage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const router = useRouter();

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const token = localStorage.getItem("token");
        const response = await axios.get("/member/profile", {
          headers: { Authorization: `Bearer ${token}` },
        });
        setProfile(response.data);
      } catch (err) {
        router.push("/login");
      }
    };
    fetchProfile();
  }, [router]);

  if (!profile) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>로딩중...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col items-center justify-center">
      <div className="bg-white p-8 rounded shadow-md w-full max-w-md">
        <h1 className="text-2xl font-bold mb-6 text-center">프로필</h1>
        <div className="mb-4">
          <p className="text-gray-700">
            <span className="font-bold">이메일:</span> {profile.email}
          </p>
        </div>
        <div className="mb-4">
          <p className="text-gray-700">
            <span className="font-bold">주소:</span> {profile.address}
          </p>
        </div>
        <div className="mb-6">
          <p className="text-gray-700">
            <span className="font-bold">권한:</span> {profile.authority}
          </p>
        </div>
      </div>
    </div>
  );
}
