// src/app/page.tsx
"use client";
import Link from "next/link";
import React from "react";

export default function HomePage() {
  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-blue-600 text-white p-4">
        <div className="container mx-auto flex justify-between items-center">
          <h1 className="text-2xl font-bold">카페</h1>
          <nav>
            <ul className="flex space-x-4">
              <li>
                <Link href="/">홈</Link>
              </li>
              <li>
                <Link href="/login">로그인</Link>
              </li>
              <li>
                <Link href="/products">상품</Link>
              </li>
              <li>
                <Link href="/cart">장바구니</Link>
              </li>
              <li>
                <Link href="/orders">주문/결제</Link>
              </li>
              <li>
                <Link href="/reviews">리뷰</Link>
              </li>
            </ul>
          </nav>
        </div>
      </header>

      {/* Hero Section */}
      <section className="bg-white py-12">
        <div className="container mx-auto text-center">
          <h2 className="text-4xl font-bold mb-4">최고의 상품을 만나보세요!</h2>
          <p className="text-lg text-gray-600 mb-8">
            다양한 상품과 특별한 할인 혜택이 기다리고 있습니다.
          </p>
          <Link
            href="/products"
            className="bg-blue-600 text-white px-6 py-3 rounded hover:bg-blue-700 transition-colors"
          >
            지금 쇼핑하기
          </Link>
        </div>
      </section>

      {/* Featured Products Section */}
      <section className="py-12">
        <div className="container mx-auto">
          <h3 className="text-3xl font-bold mb-6">추천 상품</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* 상품 카드 예시 */}
            <div className="bg-white p-4 rounded shadow">
              <img
                src="/images/product1.jpg"
                alt="상품 1"
                className="w-full h-48 object-cover mb-4"
              />
              <h4 className="text-xl font-bold mb-2">상품 1</h4>
              <p className="text-gray-600 mb-2">상품 1 설명...</p>
              <Link
                href="/products/1"
                className="text-blue-600 hover:underline"
              >
                자세히 보기
              </Link>
            </div>
            <div className="bg-white p-4 rounded shadow">
              <img
                src="/images/product2.jpg"
                alt="상품 2"
                className="w-full h-48 object-cover mb-4"
              />
              <h4 className="text-xl font-bold mb-2">상품 2</h4>
              <p className="text-gray-600 mb-2">상품 2 설명...</p>
              <Link
                href="/products/2"
                className="text-blue-600 hover:underline"
              >
                자세히 보기
              </Link>
            </div>
            <div className="bg-white p-4 rounded shadow">
              <img
                src="/images/product3.jpg"
                alt="상품 3"
                className="w-full h-48 object-cover mb-4"
              />
              <h4 className="text-xl font-bold mb-2">상품 3</h4>
              <p className="text-gray-600 mb-2">상품 3 설명...</p>
              <Link
                href="/products/3"
                className="text-blue-600 hover:underline"
              >
                자세히 보기
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-800 text-white p-4 text-center">
        © {new Date().getFullYear()} 카페. All rights reserved.
      </footer>
    </div>
  );
}
