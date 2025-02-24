"use client";
import React, { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import api from "../../lib/axios";
import Link from "next/link";

export default function PaymentPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const tradeUUID = searchParams.get("tradeUUID") || "";
  const totalPrice = searchParams.get("totalPrice") || "0";

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<any>("");

  // 결제 처리 함수: 백엔드의 /order/process 엔드포인트 호출
  const handleProcessPayment = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem("token");
      if (!token) {
        alert("로그인이 필요합니다.");
        router.push("/login");
        return;
      }
      const payAmount = parseInt(totalPrice, 10);
      const response = await api.post(
        "/order/process",
        { uuid: tradeUUID, payAmount },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert("결제가 완료되었습니다.");
      router.push("/order");
    } catch (err: any) {
      const errorData = err.response?.data;
      let errorMsg = "결제 처리 중 오류가 발생했습니다.";
      if (errorData) {
        if (typeof errorData === "object" && errorData.msg) {
          errorMsg = errorData.msg;
        } else if (typeof errorData === "string") {
          errorMsg = errorData;
        } else {
          errorMsg = JSON.stringify(errorData);
        }
      }
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col items-center justify-center p-4">
      <h1 className="text-3xl font-bold mb-4">결제 페이지</h1>
      <div className="bg-white p-6 rounded shadow-md w-full max-w-md">
        <p className="mb-2">
          주문 번호: <span className="font-bold">{tradeUUID}</span>
        </p>
        <p className="mb-4">
          총 결제 금액:{" "}
          <span className="font-bold">{parseInt(totalPrice, 10)} 원</span>
        </p>
        {error && (
          <p className="text-red-500 mb-4">
            {typeof error === "string" ? error : JSON.stringify(error)}
          </p>
        )}
        <button
          onClick={handleProcessPayment}
          className="bg-green-500 text-white px-6 py-2 rounded mb-4"
          disabled={loading}
        >
          {loading ? "결제 처리 중..." : "결제하기"}
        </button>
        <Link href="/" className="text-blue-600 hover:underline">
          메인 페이지로
        </Link>
      </div>
    </div>
  );
}
