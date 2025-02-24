"use client";
import Link from "next/link";
import React, { useEffect, useState } from "react";

interface Product {
  id: number;
  name: string;
  description: string;
  image: string;
  price: string;
}

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);

  useEffect(() => {
    // 서버에서 상품 목록 가져오기
    const fetchProducts = async () => {
      const response = await fetch('http://localhost:8080/items'); // 상품 전체 조회 API
      const data = await response.json();
      setProducts(data);
    };

    fetchProducts();
  }, []);

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-blue-600 text-white p-4">
        <div className="container mx-auto flex justify-between items-center">
          <h1 className="text-2xl font-bold">상품 목록</h1>
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

      {/* Product List */}
      <section className="py-12">
        <div className="container mx-auto">
          <h2 className="text-3xl font-bold mb-6 text-center">모든 상품</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {products.map((product) => (
              <div key={product.id} className="bg-white p-4 rounded shadow">
                <img
                  src={product.image}
                  alt={product.name}
                  className="w-full h-48 object-cover mb-4"
                />
                <h4 className="text-xl font-bold mb-2">{product.name}</h4>
                <p className="text-gray-600 mb-2">{product.description}</p>
                <p className="text-lg font-bold mb-2">{product.price}</p>
                <Link href={`/products/${product.id}`} className="text-blue-600 hover:underline">
                  자세히 보기
                </Link>
              </div>
            ))}
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
