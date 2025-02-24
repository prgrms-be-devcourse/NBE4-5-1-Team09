// src/components/Header.tsx
"use client";
import React from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "../context/AuthContext";

export default function Header() {
  const router = useRouter();
  const { token, setToken } = useAuth();
  const loggedIn = Boolean(token);

  const handleLogout = () => {
    setToken(null);
    localStorage.removeItem("email"); // 필요한 경우
    router.push("/");
  };

  return (
    <header className="bg-blue-600 text-white p-4 m-0">
      <div className="container mx-auto flex justify-between items-center px-4">
        <h1 className="text-2xl font-bold m-0">카페</h1>
        <nav>
          <ul className="flex space-x-4 m-0">
            <li>
              <Link href="/" className="hover:underline">
                홈
              </Link>
            </li>
            <li>
              <Link href="/account" className="hover:underline">
                내 정보 조회
              </Link>
            </li>
            <li>
              <Link href="/products" className="hover:underline">
                상품
              </Link>
            </li>
            <li>
              <Link href="/cart" className="hover:underline">
                장바구니
              </Link>
            </li>
            <li>
              <Link href="/orders" className="hover:underline">
                주문/결제
              </Link>
            </li>
            <li>
              <Link href="/reviews" className="hover:underline">
                리뷰
              </Link>
            </li>
            <li>
              {loggedIn ? (
                <button onClick={handleLogout} className="hover:underline">
                  로그아웃
                </button>
              ) : (
                <Link href="/login" className="hover:underline">
                  로그인
                </Link>
              )}
            </li>
          </ul>
        </nav>
      </div>
    </header>
  );
}
