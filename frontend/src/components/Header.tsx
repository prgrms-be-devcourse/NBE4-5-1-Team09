"use client";
import React, { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "../context/AuthContext";

/** 필요한 필드를 정의 */
interface JwtPayload {
  authority: string;
  // 필요한 다른 필드...
}

/** 토큰 디코딩 함수 */
function decodeJwt(token: string): JwtPayload {
  try {
    const payloadPart = token.split(".")[1];
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

  // token 상태가 변할 때마다 헤더를 재렌더링
  useEffect(() => {
    if (token) {
      // 토큰이 있으면 로그인 상태
      setLoggedIn(true);

      try {
        const decoded = decodeJwt(token);
        console.log("디코딩된 토큰:", decoded);
        setIsAdmin(decoded.authority === "ADMIN");
      } catch (err) {
        console.error("토큰 디코딩 실패:", err);
        setIsAdmin(false);
      }
    } else {
      // 토큰이 없으면 비로그인 상태
      setLoggedIn(false);
      setIsAdmin(false);
    }
  }, [token]); // <-- 의존성 배열 길이 = 1 (절대 바뀌지 않음)

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("email");

    // AuthContext에서도 토큰 제거
    setToken(null);

    // 홈으로 이동
    router.push("/");
    // 필요 시 전체 새로고침:
    // window.location.reload();
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
