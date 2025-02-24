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

export default function ProductSearchPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [keyword, setKeyword] = useState("");
  const [category, setCategory] = useState("");
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchProducts = async () => {
    try {
      const params = new URLSearchParams();
      if (keyword) params.append("keyword", keyword);
      if (category) params.append("category", category);
      if (minPrice) params.append("minPrice", minPrice);
      if (maxPrice) params.append("maxPrice", maxPrice);
      params.append("page", "0");
      params.append("size", "10");
      const res = await fetch(
        `http://localhost:8080/items/search?${params.toString()}`
      );
      if (!res.ok) throw new Error("검색 결과를 불러오지 못했습니다.");
      const data = await res.json();
      setProducts(data);
      setLoading(false);
    } catch (err: any) {
      setError(err.message);
      setLoading(false);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    fetchProducts();
  };

  return (
    <div className="min-h-screen bg-gray-100 p-4">
      <h1 className="text-2xl font-bold mb-4">상품 검색</h1>
      <form
        onSubmit={handleSearch}
        className="mb-4 bg-white p-4 rounded shadow"
      >
        <div className="mb-2">
          <label className="block text-gray-700">키워드</label>
          <input
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            className="border p-2 rounded w-full"
            placeholder="상품명, 설명 등"
          />
        </div>
        <div className="mb-2">
          <label className="block text-gray-700">카테고리</label>
          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            className="border p-2 rounded w-full"
          >
            <option value="">전체</option>
            <option value="ARABICA">아라비카</option>
            <option value="ROBUSTA">로부스타</option>
            <option value="LIBERICA">리베리카</option>
            <option value="DECAF">디카페인</option>
          </select>
        </div>
        <div className="mb-2 flex space-x-2">
          <div className="w-1/2">
            <label className="block text-gray-700">최소 가격</label>
            <input
              type="number"
              value={minPrice}
              onChange={(e) => setMinPrice(e.target.value)}
              className="border p-2 rounded w-full"
            />
          </div>
          <div className="w-1/2">
            <label className="block text-gray-700">최대 가격</label>
            <input
              type="number"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
              className="border p-2 rounded w-full"
            />
          </div>
        </div>
        <button
          type="submit"
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          검색
        </button>
      </form>
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
