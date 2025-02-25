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

  // 각 상품의 수량을 관리하는 state (상품 id를 key로 사용)
  const [quantities, setQuantities] = useState<{ [key: number]: number }>({});

  // 메인 페이지 추천 상품은 전체 상품 목록을 불러온 후, 평균 평점 순(내림차순)으로 정렬합니다.
  useEffect(() => {
    async function fetchProducts() {
      try {
        const res = await fetch("http://localhost:8080/items");
        if (!res.ok) throw new Error("상품 목록을 불러오지 못했습니다.");
        const data: Product[] = await res.json();
        // 평균 평점 순(내림차순)으로 정렬, null은 0으로 취급
        const sorted = [...data].sort((a, b) => {
          const ratingA = a.avgRating ?? 0;
          const ratingB = b.avgRating ?? 0;
          return ratingB - ratingA;
        });
        // 최대 6개까지만 선택
        setProducts(sorted.slice(0, 6));
        // 초기 수량을 각 상품마다 1로 설정
        const initQuantities: { [key: number]: number } = {};
        sorted.slice(0, 6).forEach((product) => {
          initQuantities[product.id] = 1;
        });
        setQuantities(initQuantities);
        setLoading(false);
      } catch (err: any) {
        setError(err.message);
        setLoading(false);
      }
    }
    fetchProducts();
  }, []);

  // 수량 입력 필드 변경 시 호출되는 함수
  const handleQuantityChange = (productId: number, value: number) => {
    setQuantities((prev) => ({
      ...prev,
      [productId]: value,
    }));
  };

  // 장바구니에 상품 추가하는 함수
  const handleAddToCart = async (product: Product) => {
    // 입력한 수량과 상품 재고 비교
    const quantity = quantities[product.id] || 1;
    if (quantity > product.stock) {
      alert("재고가 부족합니다.");
      return;
    }
    try {
      const token = localStorage.getItem("token");
      await fetch("http://localhost:8080/cart/add", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ itemId: product.id, quantity }),
      });
      alert("장바구니에 상품이 추가되었습니다.");
    } catch (err: any) {
      alert(err.response?.data || "장바구니 추가에 실패했습니다.");
    }
  };

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
          ) : products.length === 0 ? (
            <div className="text-center">등록된 상품이 없습니다.</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {products.map((product) => (
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
                  <h4 className="text-xl font-bold mb-2">{product.itemName}</h4>
                  <p className="text-gray-600 mb-2">{product.content}</p>
                  <p className="text-lg font-bold mb-2">
                    가격: {product.price} 원
                  </p>
                  <p className="text-yellow-500 mb-2">
                    평점:{" "}
                    {product.avgRating ? product.avgRating.toFixed(1) : "-"}
                  </p>
                  <p className="text-sm text-black">
                    카테고리: {product.category}
                  </p>
                  <p className="text-sm text-black">재고: {product.stock}</p>
                  {/* 수량 입력 필드 */}
                  <div className="flex items-center gap-2 mb-4">
                    <label
                      htmlFor={`quantity-${product.id}`}
                      className="font-medium text-sm"
                    >
                      수량:
                    </label>
                    <input
                      id={`quantity-${product.id}`}
                      type="number"
                      min="1"
                      value={quantities[product.id] || 1}
                      onChange={(e) =>
                        handleQuantityChange(
                          product.id,
                          parseInt(e.target.value, 10)
                        )
                      }
                      className="border rounded p-1 w-12 text-sm"
                    />
                  </div>
                  {/* 버튼 영역을 카드 하단에 고정 */}
                  <div className="flex flex-col gap-2 mt-auto">
                    <Link
                      href={`/products/${product.id}`}
                      className="text-blue-600 hover:underline"
                    >
                      자세히 보기
                    </Link>
                    <button
                      onClick={() => handleAddToCart(product)}
                      className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 text-sm"
                    >
                      장바구니 담기
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-800 text-white p-4 text-center">
        © {new Date().getFullYear()} Code Brew. All rights reserved.
      </footer>
    </div>
  );
}
