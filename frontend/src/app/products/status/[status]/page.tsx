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

export default function ProductsByStatusPage({
  params,
}: {
  params: { status: string };
}) {
  const { status } = params;
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function fetchProducts() {
      try {
        const res = await fetch(
          `http://localhost:8080/items/status/${status}?page=0&size=10`
        );
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
  }, [status]);

  return (
    <div className="min-h-screen bg-gray-100 p-4">
      <h1 className="text-2xl font-bold mb-4">상품 상태: {status}</h1>
      {loading ? (
        <p>로딩 중...</p>
      ) : error ? (
        <p className="text-red-500">{error}</p>
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
              <p className="text-lg font-bold mb-2">가격: {product.price}</p>
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
  );
}
