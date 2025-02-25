"use client";
import React, { useState, useEffect } from "react";
import api from "../../lib/axios"; // 공통 axios 인스턴스
import Link from "next/link";
import { useRouter } from "next/navigation";

interface CartItem {
  itemId: number;
  itemName: string;
  price: number;
  quantity: number;
}

interface CartResponse {
  cartId: number;
  memberId: number;
  totalPrice: number;
  items: CartItem[];
}

export default function CartPage() {
  const router = useRouter();
  const [cart, setCart] = useState<CartResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>("");
  const [quantities, setQuantities] = useState<{ [key: number]: number }>({});

  const fetchCart = async () => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        setError("로그인이 필요합니다.");
        setLoading(false);
        return;
      }
      const response = await api.get("/cart", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setCart(response.data);
      const initQuantities: { [key: number]: number } = {};
      response.data.items.forEach((item: CartItem) => {
        initQuantities[item.itemId] = item.quantity;
      });
      setQuantities(initQuantities);
      setLoading(false);
    } catch (err: any) {
      const errorData = err.response?.data;
      setError(
        typeof errorData === "object"
          ? JSON.stringify(errorData)
          : errorData || "장바구니 정보를 불러오지 못했습니다."
      );
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, []);

  const handleUpdateCart = async (itemId: number) => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        setError("로그인이 필요합니다.");
        return;
      }
      const newQuantity = quantities[itemId];
      await api.post(
        "/cart/edit",
        { itemId, quantity: newQuantity },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert("수량이 수정되었습니다.");
      fetchCart();
    } catch (err: any) {
      const errorData = err.response?.data;
      alert(
        typeof errorData === "object"
          ? JSON.stringify(errorData)
          : errorData || "수정에 실패했습니다."
      );
    }
  };

  // 장바구니 주문 후 결제 선택 페이지로 이동
  const handleCartOrder = async () => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("로그인이 필요합니다.");
        router.push("/login");
        return;
      }
      const response = await api.post("/order/cart", null, {
        headers: { Authorization: `Bearer ${token}` },
      });
      // OrderResponseDto에는 tradeUUID와 totalPrice가 포함됨
      const { tradeUUID, totalPrice } = response.data;
      // PaymentSelectionPage로 이동 (쿼리 파라미터로 전달)
      router.push(`/payment?tradeUUID=${tradeUUID}&totalPrice=${totalPrice}`);
    } catch (err: any) {
      const errorData = err.response?.data;
      alert(
        typeof errorData === "object"
          ? JSON.stringify(errorData)
          : errorData || "주문에 실패했습니다."
      );
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-blue-600 text-white p-4 flex justify-between items-center">
        <h1 className="text-xl font-bold">내 장바구니</h1>
        <Link href="/" className="underline">
          메인 페이지로
        </Link>
      </header>
      <main className="container mx-auto py-8">
        {loading ? (
          <div className="text-center">장바구니 로딩 중...</div>
        ) : error ? (
          <div className="text-center text-red-500">{error}</div>
        ) : cart && cart.items.length === 0 ? (
          <div className="text-center">장바구니가 비어 있습니다.</div>
        ) : (
          cart && (
            <div>
              <h2 className="text-2xl font-bold mb-4">
                총 금액:{" "}
                {new Intl.NumberFormat("ko-KR", {
                  style: "currency",
                  currency: "KRW",
                })
                  .format(cart.totalPrice)
                  .replace("₩", "")}{" "}
                원
              </h2>
              <div className="grid grid-cols-1 gap-4">
                {cart.items.map((item) => (
                  <div
                    key={item.itemId}
                    className="bg-white p-4 rounded shadow flex flex-col md:flex-row md:items-center md:justify-between"
                  >
                    <div>
                      <h3 className="text-xl font-bold">{item.itemName}</h3>
                      <p>
                        가격:{" "}
                        {new Intl.NumberFormat("ko-KR", {
                          style: "currency",
                          currency: "KRW",
                        })
                          .format(item.price)
                          .replace("₩", "")}{" "}
                        원
                      </p>
                    </div>
                    <div className="flex items-center gap-2 mt-4 md:mt-0">
                      <label
                        htmlFor={`quantity-${item.itemId}`}
                        className="font-medium text-sm"
                      >
                        수량:
                      </label>
                      <input
                        id={`quantity-${item.itemId}`}
                        type="number"
                        min="0"
                        value={quantities[item.itemId] ?? 1}
                        onChange={(e) =>
                          setQuantities((prev) => ({
                            ...prev,
                            [item.itemId]: parseInt(e.target.value, 10),
                          }))
                        }
                        className="border rounded p-1 w-12 text-sm"
                      />
                      <button
                        onClick={() => handleUpdateCart(item.itemId)}
                        className="bg-blue-500 text-white px-3 py-1 rounded text-sm"
                      >
                        수정
                      </button>
                    </div>
                  </div>
                ))}
              </div>
              <div className="mt-8 flex justify-end">
                <button
                  onClick={handleCartOrder}
                  className="bg-green-500 text-white px-6 py-2 rounded"
                >
                  결제하기
                </button>
              </div>
            </div>
          )
        )}
      </main>
      <footer className="bg-gray-800 text-white p-4 text-center">
        © {new Date().getFullYear()} 카페. All rights reserved.
      </footer>
    </div>
  );
}
