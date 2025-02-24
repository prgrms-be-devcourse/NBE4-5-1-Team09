"use client";
import React, { useState, useEffect } from "react";
import Link from "next/link";

interface Product {
  id: number;
  itemName: string;
  content: string;
  imagePath: string;
  price: number;
  stock: number;
  category: string;
  avgRating: number | null;
  itemStatus: string;
}

export default function HomePage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function fetchProducts() {
      try {
        const res = await fetch("http://localhost:8080/items");
        if (!res.ok) throw new Error("상품 목록을 불러오지 못했습니다.");
        const data = await res.json();
        setProducts(data);
        setLoading(false);
      } catch (err: any) {
        setError(err.message);
        setLoading(false);
      }
    }
    fetchProducts();
  }, []);

  return (
    <div className="min-h-screen bg-gray-100">
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

      {/* 추천 상품 Section */}
      <section className="py-12">
        <div className="container mx-auto">
          <h3 className="text-3xl font-bold mb-6">추천 상품</h3>
          {loading ? (
            <div className="text-center">상품 로딩 중...</div>
          ) : error ? (
            <div className="text-center text-red-500">{error}</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {products.map((product) => (
                <div key={product.id} className="bg-white p-4 rounded shadow">
                  <img
                    src={product.imagePath}
                    alt={product.itemName}
                    className="w-full h-48 object-cover mb-4"
                  />
                  <h4 className="text-xl font-bold mb-2">{product.itemName}</h4>
                  <p className="text-gray-600 mb-2">{product.content}</p>
                  <p className="text-lg font-bold mb-2">
                    가격: {product.price}
                  </p>
                  <Link
                    href={`/products/${product.id}`}
                    className="text-blue-600 hover:underline"
                  >
                    자세히 보기
                  </Link>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-800 text-white p-4 text-center">
        © {new Date().getFullYear()} 카페. All rights reserved.
      </footer>
    </div>
  );
}
