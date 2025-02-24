"use client";
import React, { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "../context/AuthContext";

interface JwtPayload {
  authority: string;
  // 필요한 다른 필드 추가 가능
}

// 간단한 JWT 디코딩 함수
function decodeJwt(token: string): JwtPayload {
  try {
    const payloadPart = token.split(".")[1];
    // Base64 URL 디코딩: '+'와 '/'를 원래 문자로 변환하고, '=' 패딩 추가
    const base64 = payloadPart.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(jsonPayload);
  } catch (err) {
    throw new Error("토큰 디코딩 실패");
  }
}

export default function Header() {
  const router = useRouter();
  const { token, setToken } = useAuth();
  const [isAdmin, setIsAdmin] = useState(false);
  const [loggedIn, setLoggedIn] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem("token");
    setLoggedIn(!!token);
    if (token) {
      try {
        const decoded = decodeJwt(token);
        console.log("디코딩된 토큰:", decoded);
        setIsAdmin(decoded.authority === "ADMIN");
      } catch (err) {
        console.error("토큰 디코딩 실패:", err);
        setIsAdmin(false);
      }
    } else {
      setIsAdmin(false);
    }
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    setToken(null);
    router.push("/");
    window.location.reload();
  };

  return (
    <header className="bg-blue-600 text-white p-4 m-0">
      <div className="container mx-auto flex justify-between items-center px-4">
        <h1 className="text-2xl font-bold m-0">카페</h1>
        <nav>
          <ul className="flex space-x-4 m-0 items-center">
            <li>
              <Link href="/" className="hover:underline">
                홈
              </Link>
            </li>
            {isAdmin && (
              <li>
                <Link href="/admin" className="hover:underline">
                  관리자 페이지
                </Link>
              </li>
            )}
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
