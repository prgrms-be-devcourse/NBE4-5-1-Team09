"use client";
import Link from "next/link";
import { useParams } from "next/navigation";
import React, { useEffect, useState } from "react";

interface Product {
  id: number;
  name: string;
  description: string;
  image: string;
  price: string;
  rating: number;
  reviews: string[];
}

export default function ProductDetailPage() {
  const { id } = useParams();
  const [product, setProduct] = useState<Product | null>(null);

  useEffect(() => {
    // 상품 단건 조회 API 호출
    const fetchProduct = async () => {
      const response = await fetch(`http://localhost:8080/items/${id}`); // 상품 단건 조회 API
      const data = await response.json();
      setProduct(data);
    };

    fetchProduct();
  }, [id]);

  if (!product) {
    return <div className="text-center py-10">상품을 찾을 수 없습니다.</div>;
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-blue-600 text-white p-4">
        <div className="container mx-auto flex justify-between items-center">
          <h1 className="text-2xl font-bold">{product.name}</h1>
          <nav>
            <ul className="flex space-x-4">
              <li><Link href="/">홈</Link></li>
              <li><Link href="/login">로그인</Link></li>
              <li><Link href="/cart">장바구니</Link></li>
              <li><Link href="/orders">주문/결제</Link></li>
            </ul>
          </nav>
        </div>
      </header>

      <section className="container mx-auto py-12">
        <div className="bg-white p-6 rounded shadow max-w-2xl mx-auto">
          <img src={product.image} alt={product.name} className="w-full h-64 object-cover mb-4" />
          <h2 className="text-2xl font-bold mb-2">{product.name}</h2>
          <p className="text-gray-600 mb-2">{product.description}</p>
          <p className="text-lg font-bold mb-2">{product.price}</p>
          <p className="text-yellow-500">⭐ {product.rating} / 5.0</p>
          <h3 className="text-xl font-bold mt-6">리뷰</h3>
          <ul className="mt-2">
            {product.reviews.map((review, index) => (
              <li key={index} className="bg-gray-200 p-2 rounded my-1">{review}</li>
            ))}
          </ul>
        </div>
      </section>

      <footer className="bg-gray-800 text-white p-4 text-center">
        © {new Date().getFullYear()} 카페. All rights reserved.
      </footer>
    </div>
  );
}
