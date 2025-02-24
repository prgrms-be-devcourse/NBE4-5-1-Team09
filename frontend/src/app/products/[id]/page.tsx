"use client";
import React, { useState, useEffect } from "react";
import { useParams } from "next/navigation";
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

export default function ProductDetailPage() {
  const { id } = useParams();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function fetchProduct() {
      try {
        const res = await fetch(`http://localhost:8080/items/${id}`);
        if (!res.ok) throw new Error("상품을 찾지 못했습니다.");
        const data = await res.json();
        setProduct(data);
        setLoading(false);
      } catch (err: any) {
        setError(err.message);
        setLoading(false);
      }
    }
    fetchProduct();
  }, [id]);

  if (loading)
    return (
      <div className="min-h-screen flex items-center justify-center">
        로딩 중...
      </div>
    );
  if (error)
    return (
      <div className="min-h-screen flex items-center justify-center text-red-500">
        에러: {error}
      </div>
    );

  return (
    <div className="min-h-screen bg-gray-100 p-4">
      <h1 className="text-2xl font-bold mb-4">{product?.itemName}</h1>
      <img
        src={product?.imagePath}
        alt={product?.itemName}
        className="w-full h-64 object-cover mb-4"
      />
      <p>{product?.content}</p>
      <p className="font-bold">가격: {product?.price}</p>
      <p className="font-bold">재고: {product?.stock}</p>
      <p>카테고리: {product?.category}</p>
      <p>평점: {product?.avgRating}</p>
      <p>상태: {product?.itemStatus}</p>
      <Link
        href="/products"
        className="text-blue-600 hover:underline mt-4 block"
      >
        목록으로 돌아가기
      </Link>
    </div>
  );
}
