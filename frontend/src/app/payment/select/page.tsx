// PaymentSelectionPage.tsx (경로 : app/payment/select/page.tsx)
"use client";
import React, { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import api from "../../../lib/axios";
import Link from "next/link";

// 결제 수단 옵션 (필요에 따라 수정)
const paymentMethods = [
  { value: "CARD", label: "신용카드" },
  { value: "BANK_TRANSFER", label: "계좌이체" },
  { value: "MOBILE", label: "휴대폰 결제" },
];

export default function PaymentSelectionPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const tradeUUID = searchParams.get("tradeUUID") || "";
  const totalPrice = searchParams.get("totalPrice") || "0";

  const [selectedMethod, setSelectedMethod] = useState("CARD");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // 결제 요청 함수: 백엔드의 기존 엔드포인트를 호출(예, /order/process)
  const handlePayment = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem("token");
      if (!token) {
        alert("로그인이 필요합니다.");
        router.push("/login");
        return;
      }
      const payAmount = parseInt(totalPrice, 10);
      // 결제 요청: 기존 컨트롤러에서 portoneService.prePurchase가 호출된 후,
      // 포트원 결제 프로세스가 진행되고, 최종 결제 결과는 포트원 웹훅을 통해 처리됩니다.
      const response = await api.post(
        "/order/process",
        { uuid: tradeUUID, payAmount, paymentMethod: selectedMethod },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert("결제 요청이 완료되었습니다.\n포트원 결제 창이 열릴 수 있습니다.");
      // 실제 결제 창(또는 팝업)이 열리도록 PortOne의 JS SDK(제공된다면)를 호출할 수 있습니다.
      // 여기서는 간단히 주문 목록 페이지로 이동합니다.
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
      <h1 className="text-3xl font-bold mb-4">결제 선택</h1>
      <div className="bg-white p-6 rounded shadow-md w-full max-w-md">
        <p className="mb-2">
          주문 번호: <span className="font-bold">{tradeUUID}</span>
        </p>
        <p className="mb-4">
          총 결제 금액:{" "}
          <span className="font-bold">{parseInt(totalPrice, 10)} 원</span>
        </p>
        <div className="mb-4">
          <h2 className="text-xl mb-2">결제 수단 선택</h2>
          {paymentMethods.map((method) => (
            <label key={method.value} className="block mb-1">
              <input
                type="radio"
                name="paymentMethod"
                value={method.value}
                checked={selectedMethod === method.value}
                onChange={(e) => setSelectedMethod(e.target.value)}
                className="mr-2"
              />
              {method.label}
            </label>
          ))}
        </div>
        {error && (
          <p className="text-red-500 mb-4">
            {typeof error === "string" ? error : JSON.stringify(error)}
          </p>
        )}
        <button
          onClick={handlePayment}
          disabled={loading}
          className="bg-green-500 text-white px-6 py-2 rounded mb-4"
        >
          {loading ? "결제 처리 중..." : "결제하기"}
        </button>
        <Link href="/order" className="text-blue-600 hover:underline">
          주문 목록으로
        </Link>
      </div>
    </div>
  );
}
