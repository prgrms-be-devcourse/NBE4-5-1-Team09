"use client";
import React, { useState, useEffect, useCallback } from "react";
import api from "../../lib/axios";
import Link from "next/link";
import { useRouter } from "next/navigation";

interface OrderItem {
  itemId: number;
  quantity: number;
  itemName: string;
}

interface OrderItemsGroup {
  tradeUUID: string;
  orderItemDtoList: OrderItem[];
}

interface OrdersResponse {
  buyList: OrderItemsGroup[];
  payList: OrderItemsGroup[];
  prepareDeliveryList: OrderItemsGroup[];
  beforeDeliveryList: OrderItemsGroup[];
  inDeliveryList: OrderItemsGroup[];
  postDeliveryList: OrderItemsGroup[];
  refusedList: OrderItemsGroup[];
  refundList: OrderItemsGroup[];
}

export default function OrderPage() {
  const router = useRouter();
  const [orders, setOrders] = useState<OrdersResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>("");
  const [isAdmin, setIsAdmin] = useState<boolean>(false);

  const fetchOrders = useCallback(async () => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        setError("로그인이 필요합니다.");
        setLoading(false);
        return;
      }
      const endpoint = isAdmin ? "/admin/trade/all-trades" : "/order/show";
      const response = await api.get(endpoint, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setOrders(response.data);
    } catch (err: any) {
      setError(err.response?.data?.msg || "주문 목록을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }, [isAdmin]);

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      try {
        const base64Url = token.split(".")[1];
        const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
        const jsonPayload = decodeURIComponent(
          atob(base64)
            .split("")
            .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
            .join("")
        );
        const payload = JSON.parse(jsonPayload);
        setIsAdmin(payload.authority === "ADMIN");
      } catch (e) {
        console.error("토큰 디코딩 중 오류 발생", e);
      }
    }
  }, []);

  useEffect(() => {
    if (localStorage.getItem("token")) {
      fetchOrders();
    }
  }, [fetchOrders]);

  const updateOrderStatus = async (tradeUUID: string, endpoint: string) => {
    try {
      const token = localStorage.getItem("token");
      await api.post(`/admin/trade/${endpoint}`, null, {
        params: { tradeUUID },
        headers: { Authorization: `Bearer ${token}` },
      });
      alert("주문 상태가 변경되었습니다.");
      fetchOrders();
    } catch (error) {
      alert("주문 상태 변경에 실패했습니다.");
      console.error(error);
    }
  };

  const handleCancelOrder = async (
    tradeUUID: string,
    orderItems: OrderItem[]
  ) => {
    const cancelItemList = orderItems
      .map((item) => {
        const input = window.prompt(
          `상품: ${item.itemName}\n주문 수량: ${item.quantity}\n취소할 수량을 입력하세요 (0 입력 시 취소 안함):`,
          "0"
        );
        const qty = parseInt(input || "0", 10);
        return { itemId: item.itemId, quantity: qty };
      })
      .filter((ci) => ci.quantity > 0);

    if (cancelItemList.length === 0) return;

    try {
      const token = localStorage.getItem("token");
      await api.post(
        "/order/cancel",
        { tradeUUID, cancelItemList },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert("취소 요청이 완료되었습니다.");
      fetchOrders();
    } catch (err: any) {
      alert("취소 요청에 실패했습니다.");
      console.error(err);
    }
  };

  // 사용자 주문 취소 함수 (BUY)
  const handleCancelBuyOrder = async (
    tradeUUID: string,
    orderItems: OrderItem[]
  ) => {
    // 각 주문 항목별로 취소할 수량을 입력받음
    const cancelItemList = orderItems
      .map((item) => {
        const input = window.prompt(
          `상품: ${item.itemName}\n주문 수량: ${item.quantity}\n취소할 수량을 입력하세요 (0 입력 시 취소 안함):`,
          "0"
        );
        const qty = parseInt(input || "0", 10);
        return { itemId: item.itemId, quantity: qty };
      })
      .filter((ci) => ci.quantity > 0);
    if (cancelItemList.length === 0) return;
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("로그인이 필요합니다.");
        return;
      }
      await api.post(
        "/order/cancel",
        { tradeUUID, cancelItemList },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert("취소 요청이 완료되었습니다.");
      fetchOrders();
    } catch (err: any) {
      alert("취소 요청에 실패했습니다.");
      console.error(err);
    }
  };

  // 사용자 주문 취소 함수 (PAY)
  const handleCancelPayOrder = async (
    tradeUUID: string,
    orderItems: OrderItem[]
  ) => {
    const cancelItemList = orderItems
      .map((item) => {
        const input = window.prompt(
          `상품: ${item.itemName}\n주문 수량: ${item.quantity}\n취소할 수량을 입력하세요 (0 입력 시 취소 안함):`,
          "0"
        );
        const qty = parseInt(input || "0", 10);
        return { itemId: item.itemId, quantity: qty };
      })
      .filter((ci) => ci.quantity > 0);
    if (cancelItemList.length === 0) return;
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("로그인이 필요합니다.");
        return;
      }

      await api.post(
        "/order/cancel",
        { tradeUUID, cancelItemList },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert("취소 요청이 완료되었습니다.");
      fetchOrders();
    } catch (err: any) {
      alert("취소 요청에 실패했습니다.");
      console.error(err);
    }
  };

  const handlePayment = async (tradeUUID: string) => {
    try {
      const token = localStorage.getItem("token");
      const memberEmail = localStorage.getItem("email");

      if (!token || !memberEmail) {
        alert("로그인이 필요합니다.");
        router.push("/login");
        return;
      }

      // 결제 요청을 위해 해당 아이템과 수량을 전달
      const response = await api.post(
        "/order/pay-retry",
        { tradeUUID },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const { totalPrice } = response.data;
      router.push(`/payment?tradeUUID=${tradeUUID}&totalPrice=${totalPrice}`);
    } catch (err: any) {
      alert("결제 요청에 실패했습니다.");
      console.error(err);
    }
  };

  const renderOrderGroup = (orderGroups: OrderItemsGroup[], status: string) => {
    if (orderGroups.length === 0) {
      return <p>해당 상태의 주문이 없습니다.</p>;
    }
    return orderGroups.map((group) => (
      <div key={group.tradeUUID} className="border p-2 rounded mb-2">
        <p>거래 ID: {group.tradeUUID}</p>
        {group.orderItemDtoList.map((item) => (
          <div key={item.itemId} className="ml-4">
            <p>상품명: {item.itemName}</p>
            <p>수량: {item.quantity}</p>
          </div>
        ))}
        {isAdmin && status === "payList" && (
          <button
            onClick={() => updateOrderStatus(group.tradeUUID, "confirm")}
            className="bg-blue-500 text-white px-3 py-1 rounded mt-2"
          >
            확인
          </button>
        )}
        {!isAdmin && status === "buyList" && (
          <div className="mt-2 flex gap-2">
            <button
              onClick={() =>
                handleCancelOrder(group.tradeUUID, group.orderItemDtoList)
              }
              className="bg-red-500 text-white px-3 py-1 rounded"
            >
              주문 취소
            </button>
            <button
              onClick={() => handlePayment(group.tradeUUID)} // item과 tradeUUID 전달
              className="bg-green-500 text-white px-3 py-1 rounded"
            >
              결제하기
            </button>
          </div>
        )}
        {!isAdmin && status === "payList" && (
          <button
            onClick={() =>
              handleCancelPayOrder(group.tradeUUID, group.orderItemDtoList)
            }
            className="bg-red-500 text-white px-3 py-1 rounded mt-2"
          >
            주문 취소
          </button>
        )}
        {isAdmin && status === "prepareDeliveryList" && (
          <button
            onClick={() => updateOrderStatus(group.tradeUUID, "prepare")}
            className="bg-yellow-500 text-white px-3 py-1 rounded mt-2"
          >
            배송 준비
          </button>
        )}
        {isAdmin && status === "beforeDeliveryList" && (
          <button
            onClick={() => updateOrderStatus(group.tradeUUID, "in-delivery")}
            className="bg-green-500 text-white px-3 py-1 rounded mt-2"
          >
            배송 시작
          </button>
        )}
        {isAdmin && status === "inDeliveryList" && (
          <button
            onClick={() => updateOrderStatus(group.tradeUUID, "post-delivery")}
            className="bg-purple-500 text-white px-3 py-1 rounded mt-2"
          >
            배송 완료
          </button>
        )}
      </div>
    ));
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-blue-600 text-white p-4 flex justify-between items-center">
        <h1 className="text-xl font-bold">
          내 주문 목록 {isAdmin ? "(관리자)" : ""}
        </h1>
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
            <section className="mb-6">
              <h3 className="text-xl font-semibold mb-2">주문 대기 (BUY)</h3>
              {renderOrderGroup(orders.buyList, "buyList")}
            </section>
            <section className="mb-6">
              <h3 className="text-xl font-semibold mb-2">결제 완료 (PAY)</h3>
              {renderOrderGroup(orders.payList, "payList")}
            </section>
            {/* 그 외 상태들 (배송 준비중, 배송 전, 배송중 등) */}
            <section className="mb-6">
              <h3 className="text-xl font-semibold mb-2">
                배송 준비중 (PREPARE_DELIVERY)
              </h3>
              {renderOrderGroup(
                orders.prepareDeliveryList,
                "prepareDeliveryList"
              )}
            </section>
            <section className="mb-6">
              <h3 className="text-xl font-semibold mb-2">
                배송 전 (BEFORE_DELIVERY)
              </h3>
              {renderOrderGroup(
                orders.beforeDeliveryList,
                "beforeDeliveryList"
              )}
            </section>
            <section className="mb-6">
              <h3 className="text-xl font-semibold mb-2">
                배송중 (IN_DELIVERY)
              </h3>
              {renderOrderGroup(orders.inDeliveryList, "inDeliveryList")}
            </section>
            <section className="mb-6">
              <h3 className="text-xl font-semibold mb-2">
                배송 완료 (POST_DELIVERY)
              </h3>
              {renderOrderGroup(orders.postDeliveryList, "postDeliveryList")}
            </section>
            <section className="mb-6">
              <h3 className="text-xl font-semibold mb-2">주문 취소 (CANCEL)</h3>
              {renderOrderGroup(orders.refusedList, "refusedList")}
            </section>
          </div>
        ) : null}
      </main>
    </div>
  );
}
