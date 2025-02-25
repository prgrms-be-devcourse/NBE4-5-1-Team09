"use client";
import React, { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation"; // App Router 환경에서는 next/navigation 사용
import api from "../../lib/axios"; // 공통 axios 인스턴스 import

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
  const router = useRouter();

  const [allProducts, setAllProducts] = useState<Product[]>([]);
  const [displayProducts, setDisplayProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // 검색, 필터 상태
  const [keyword, setKeyword] = useState("");
  const [category, setCategory] = useState("");
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [sort, setSort] = useState(""); // 예: "priceAsc", "priceDesc", "ratingDesc"

  // 페이징 상태
  const pageSize = 9;
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);

  // 검색/필터/정렬 적용 API 호출 함수 (백엔드에서 단순 배열(Product[])을 반환한다고 가정)
  const fetchProducts = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      if (keyword) params.append("keyword", keyword);
      if (category) params.append("category", category);
      if (minPrice) params.append("minPrice", minPrice);
      if (maxPrice) params.append("maxPrice", maxPrice);
      if (sort) params.append("sort", sort);
      // 여기서 page와 size 파라미터를 명시적으로 추가합니다.
      params.append("page", "0");
      params.append("size", "30"); // 충분한 크기로 요청

      const res = await fetch(
        `http://localhost:8080/items/search?${params.toString()}`
      );
      if (!res.ok) throw new Error("상품 목록을 불러오지 못했습니다.");
      const data: Product[] = await res.json();

      // 클라이언트 측 정렬 (옵션)
      let sortedData = data;
      if (sort) {
        sortedData = [...data].sort((a, b) => {
          if (sort === "priceAsc") return a.price - b.price;
          else if (sort === "priceDesc") return b.price - a.price;
          else if (sort === "ratingDesc") {
            const ratingA = a.avgRating ?? 0;
            const ratingB = b.avgRating ?? 0;
            return ratingB - ratingA;
          }
          return 0;
        });
      }

      setAllProducts(sortedData);
      setTotalPages(Math.ceil(sortedData.length / pageSize));
      setCurrentPage(0); // 새로운 검색 시 첫 페이지로
      setLoading(false);
    } catch (err: any) {
      setError(err.message);
      setLoading(false);
    }
  };

  // 페이지 변경 시 화면에 보여줄 상품 목록 업데이트
  useEffect(() => {
    const start = currentPage * pageSize;
    const end = start + pageSize;
    setDisplayProducts(allProducts.slice(start, end));
  }, [allProducts, currentPage]);

  // 첫 로딩 시 상품 목록 호출
  useEffect(() => {
    fetchProducts();
  }, []);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    fetchProducts();
  };

  // 페이지 변경 핸들러
  const handlePageChange = (page: number) => {
    if (page < 0 || page >= totalPages) return;
    setCurrentPage(page);
  };

  // 페이징 UI 렌더링
  const renderPagination = () => {
    const pages = [];
    for (let i = 0; i < totalPages; i++) {
      pages.push(
        <button
          key={i}
          onClick={() => handlePageChange(i)}
          className={`px-3 py-1 border mx-1 rounded ${
            i === currentPage
              ? "bg-blue-500 text-white"
              : "bg-white text-blue-500 hover:bg-blue-100"
          }`}
        >
          {i + 1}
        </button>
      );
    }
    return (
      <div className="flex justify-center mt-8">
        <button
          onClick={() => handlePageChange(currentPage - 1)}
          disabled={currentPage === 0}
          className="px-3 py-1 border mx-1 rounded disabled:opacity-50"
        >
          Prev
        </button>
        {pages}
        <button
          onClick={() => handlePageChange(currentPage + 1)}
          disabled={currentPage === totalPages - 1}
          className="px-3 py-1 border mx-1 rounded disabled:opacity-50"
        >
          Next
        </button>
      </div>
    );
  };

  // 장바구니에 상품 추가하는 함수 (공통 axios 인스턴스 사용)
  const handleAddToCart = async (productId: number) => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("로그인이 필요합니다.");
        return;
      }
      await api.post(
        "/cart/add",
        { itemId: productId, quantity: 1 },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert("장바구니에 상품이 추가되었습니다.");
    } catch (err: any) {
      const errorMsg = err.response?.data || "장바구니 추가에 실패했습니다.";
      setError(errorMsg);
      alert(errorMsg);
    }
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
          ) : allProducts.length === 0 ? (
            <div className="text-center">검색 조건에 맞는 상품이 없습니다.</div>
          ) : (
            <>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {displayProducts.map((product) => (
                  <div
                    key={product.id}
                    className="bg-white p-4 rounded shadow flex flex-col h-full"
                  >
                    <div className="w-full h-48 flex items-center justify-center overflow-hidden">
                      <img
                        src={`http://localhost:8080${product.imagePath}`}
                        alt={product.itemName}
                        className="max-w-full max-h-full object-cover"
                      />
                    </div>
                    <h4 className="text-xl font-bold mb-2">
                      {product.itemName}
                    </h4>
                    <p className="text-black mb-2">{product.content}</p>
                    <p className="text-lg font-bold mb-2">
                      가격: ₩{Number(product.price).toLocaleString("ko-KR")}
                    </p>
                    <p className="text-sm text-black">
                      카테고리: {product.category}
                    </p>
                    <p className="text-sm text-black">재고: {product.stock}</p>
                    <p className="text-yellow-500">
                      평점:{" "}
                      {product.avgRating ? product.avgRating.toFixed(1) : "-"}
                    </p>
                    <div className="flex flex-col gap-2 mt-auto">
                      <Link
                        href={`/products/${product.id}`}
                        className="text-blue-600 hover:underline"
                      >
                        자세히 보기
                      </Link>
                      <button
                        onClick={() => handleAddToCart(product.id)}
                        className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
                      >
                        장바구니 담기
                      </button>
                    </div>
                  </div>
                ))}
              </div>
              {renderPagination()}
            </>
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
