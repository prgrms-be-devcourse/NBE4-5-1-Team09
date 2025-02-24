// src/pages/index.tsx
import Link from "next/link";
import React from "react";

const Home: React.FC = () => {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-100">
      <h1 className="text-4xl font-bold mb-8">My App Home</h1>
      <ul className="space-y-4 text-lg">
        <li>
          <Link href="/login" className="text-blue-500 hover:underline">
            로그인
          </Link>
        </li>
        <li>
          <Link href="/signup" className="text-blue-500 hover:underline">
            회원가입
          </Link>
        </li>
        <li>
          <Link href="/admin-signup" className="text-blue-500 hover:underline">
            관리자 회원가입
          </Link>
        </li>
        <li>
          <Link href="/admin-login" className="text-blue-500 hover:underline">
            관리자 로그인
          </Link>
        </li>
        <li>
          <Link href="/profile" className="text-blue-500 hover:underline">
            프로필 조회
          </Link>
        </li>
        <li>
          <Link
            href="/profile-update"
            className="text-blue-500 hover:underline"
          >
            프로필 수정
          </Link>
        </li>
        <li>
          <Link
            href="/change-password"
            className="text-blue-500 hover:underline"
          >
            비밀번호 변경
          </Link>
        </li>
        <li>
          <Link
            href="/forgot-password"
            className="text-blue-500 hover:underline"
          >
            비밀번호 재설정 요청
          </Link>
        </li>
        <li>
          <Link
            href="/reset-password"
            className="text-blue-500 hover:underline"
          >
            비밀번호 재설정
          </Link>
        </li>
        <li>
          <Link href="/verify-email" className="text-blue-500 hover:underline">
            이메일 인증
          </Link>
        </li>
        <li>
          <Link href="/delete-member" className="text-blue-500 hover:underline">
            회원 탈퇴
          </Link>
        </li>
      </ul>
    </div>
  );
};

export default Home;
