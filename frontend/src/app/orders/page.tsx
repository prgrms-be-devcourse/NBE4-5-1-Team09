"use client";
import React, { useEffect, useState } from "react";
// import axios from "axios"; // 기존 axios 대신
import api from "../../lib/axios"; // 공통 axios 인스턴스
import Link from "next/link";

interface OrderItem {
  itemId: number;
  quantity: number;
  itemName: string;
}

interface OrdersResponse {
  buyList: OrderItem[];
  payList: OrderItem[];
  prepareDeliveryList: OrderItem[];
  beforeDeliveryList: OrderItem[];
  inDeliveryList: OrderItem[];
  postDeliveryList: OrderItem[];
  refusedList: OrderItem[];
  refundList: OrderItem[];
}

export default function OrderPage() {
  const [orders, setOrders] = useState<OrdersResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>("");

  useEffect(() => {
    async function fetchOrders() {
      try {
        const token = localStorage.getItem("token");
        if (!token) {
          setError("로그인이 필요합니다.");
          setLoading(false);
          return;
        }
        // 공통 axios 인스턴스 사용
        const response = await api.get("/order/show", {
          headers: { Authorization: `Bearer ${token}` },
        });
        setOrders(response.data);
        setLoading(false);
      } catch (err: any) {
        const errMsg =
          err.response?.data?.msg || "주문 목록을 불러오지 못했습니다.";
        setError(errMsg);
        setLoading(false);
      }
    }
    fetchOrders();
  }, []);

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-blue-600 text-white p-4 flex justify-between items-center">
        <h1 className="text-xl font-bold">내 주문 목록</h1>
        <Link href="/" className="underline">
          메인 페이지로
        </Link>
      </header>
      <main className="container mx-auto py-8">
        {loading ? (
          <div className="text-center">주문 목록 로딩 중...</div>
        ) : error ? (
          <div className="text-center text-red-500">{error}</div>
        ) : orders ? (
          <div>
            <h2 className="text-2xl font-bold mb-4">주문 현황</h2>

            {/* BUY: 주문 대기 */}
            <section className="mb-6">
              <h3 className="text-xl font-semibold mb-2">주문 대기 (BUY)</h3>
              {orders.buyList.length === 0 ? (
                <p>주문 대기중인 상품이 없습니다.</p>
              ) : (
                orders.buyList.map((item, index) => (
                  <div key={index} className="border p-2 rounded mb-2">
                    <p>상품명: {item.itemName}</p>
                    <p>수량: {item.quantity}</p>
                  </div>
                ))
              )}
            </section>

            {/* PAY: 결제 완료 */}
            <section className="mb-6">
              <h3 className="text-xl font-semibold mb-2">결제 완료 (PAY)</h3>
              {orders.payList.length === 0 ? (
                <p>결제 완료된 주문이 없습니다.</p>
              ) : (
                orders.payList.map((item, index) => (
                  <div key={index} className="border p-2 rounded mb-2">
                    <p>상품명: {item.itemName}</p>
                    <p>수량: {item.quantity}</p>
                  </div>
                ))
              )}
            </section>

            {/* 배송 준비중 */}
            <section className="mb-6">
              <h3 className="text-xl font-semibold mb-2">배송 준비중</h3>
              {orders.prepareDeliveryList.length === 0 ? (
                <p>배송 준비중인 주문이 없습니다.</p>
              ) : (
                orders.prepareDeliveryList.map((item, index) => (
                  <div key={index} className="border p-2 rounded mb-2">
                    <p>상품명: {item.itemName}</p>
                    <p>수량: {item.quantity}</p>
                  </div>
                ))
              )}
            </section>

            {/* 필요에 따라 다른 상태들 (배송 전, 배송중, 배송 완료, 취소, 환불 등) 추가 */}
          </div>
        ) : null}
      </main>
      <footer className="bg-gray-800 text-white p-4 text-center">
        © {new Date().getFullYear()} 카페. All rights reserved.
      </footer>
    </div>
  );
}
