"use client";
import React, { useState, useEffect } from "react";
import axios from "axios";
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

  // 검색 및 필터 상태
  const [keyword, setKeyword] = useState("");
  const [category, setCategory] = useState("");
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [sort, setSort] = useState(""); // 예: "priceAsc", "priceDesc", "ratingDesc"

  // 검색/필터/정렬 적용 API 호출 함수
  const fetchProducts = async () => {
    try {
      const params = new URLSearchParams();
      if (keyword) params.append("keyword", keyword);
      if (category) params.append("category", category);
      if (minPrice) params.append("minPrice", minPrice);
      if (maxPrice) params.append("maxPrice", maxPrice);
      if (sort) params.append("sort", sort);
      params.append("page", "0");
      params.append("size", "10");

      const { data } = await axios.get(
        `http://localhost:8080/items/search?${params.toString()}`
      );

      // 클라이언트 측 정렬 처리
      let sortedData = data;
      if (sort) {
        sortedData = [...data].sort((a, b) => {
          if (sort === "priceAsc") {
            return a.price - b.price;
          } else if (sort === "priceDesc") {
            return b.price - a.price;
          } else if (sort === "ratingDesc") {
            const ratingA = a.avgRating ?? 0;
            const ratingB = b.avgRating ?? 0;
            return ratingB - ratingA;
          }
          return 0;
        });
      }

      setProducts(sortedData);
      setLoading(false);
    } catch (err: any) {
      setError(err.message);
      setLoading(false);
    }
  };

  // 초기 상품 목록 호출 (검색 조건 없을 때)
  useEffect(() => {
    fetchProducts();
  }, []);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    fetchProducts();
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* 검색, 필터링, 정렬 섹션 */}
      <section className="bg-white py-6">
        <div className="container mx-auto">
          <form
            onSubmit={handleSearch}
            className="flex flex-col md:flex-row gap-4 items-end"
          >
            <div className="flex-1">
              <label className="block text-gray-700">키워드</label>
              <input
                type="text"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                className="border p-2 rounded w-full"
                placeholder="상품명, 설명 등"
              />
            </div>
            <div className="flex-1">
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
            <div className="flex-1">
              <label className="block text-gray-700">최소 가격</label>
              <input
                type="number"
                value={minPrice}
                onChange={(e) => setMinPrice(e.target.value)}
                className="border p-2 rounded w-full"
                placeholder="0"
              />
            </div>
            <div className="flex-1">
              <label className="block text-gray-700">최대 가격</label>
              <input
                type="number"
                value={maxPrice}
                onChange={(e) => setMaxPrice(e.target.value)}
                className="border p-2 rounded w-full"
                placeholder="예: 100000"
              />
            </div>
            <div className="flex-1">
              <label className="block text-gray-700">정렬</label>
              <select
                value={sort}
                onChange={(e) => setSort(e.target.value)}
                className="border p-2 rounded w-full"
              >
                <option value="">기본 정렬</option>
                <option value="priceAsc">가격 낮은 순</option>
                <option value="priceDesc">가격 높은 순</option>
                <option value="ratingDesc">평점 높은 순</option>
              </select>
            </div>
            <div>
              <button
                type="submit"
                className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
              >
                검색
              </button>
            </div>
          </form>
        </div>
      </section>

      {/* 상품 목록 Section */}
      <section className="py-12">
        <div className="container mx-auto">
          <h3 className="text-3xl font-bold mb-6 text-center text-black">
            상품 목록
          </h3>
          {loading ? (
            <div className="text-center">상품 로딩 중...</div>
          ) : error ? (
            <div className="text-center text-red-500">{error}</div>
          ) : products.length === 0 ? (
            <div className="text-center">검색 조건에 맞는 상품이 없습니다.</div>
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
                  <p className="text-black mb-2">{product.content}</p>
                  <p className="text-lg font-bold mb-2">
                    가격: {product.price}
                  </p>
                  <p className="text-sm text-black">
                    카테고리: {product.category}
                  </p>
                  <p className="text-yellow-500">
                    평점: {product.avgRating ?? "-"}
                  </p>

                  {/* 구매 가능 여부 표시 */}
                  <p
                    className={`text-lg font-semibold ${
                      product.itemStatus === "ON_SALE"
                        ? "text-green-500"
                        : "text-red-500"
                    }`}
                  >
                    {product.itemStatus === "ON_SALE" ? "구매 가능" : "품절"}
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
