"use client";
import React, { useState, useEffect } from "react";
// import axios from "axios";  // 기존 axios import 제거
import api from "../../lib/axios"; // 공통 axios 인스턴스 import
import { useRouter } from "next/navigation";

interface Item {
  id: number;
  itemName: string;
  price: number;
  stock: number;
  imagePath: string;
  content: string;
  category: string; // 백엔드 enum: "ARABICA", "ROBUSTA", "LIBERICA", "DECAF"
  avgRating: number | null;
  itemStatus: string;
}

export default function AdminPage() {
  const router = useRouter();
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>("");

  // 신규 상품 등록 폼 상태
  const [newItem, setNewItem] = useState({
    itemName: "",
    price: "",
    stock: "",
    imagePath: "",
    content: "",
    category: "",
  });

  // 수정 중인 상품 상태 (null이면 수정 모드 아님)
  const [editingItem, setEditingItem] = useState<Item | null>(null);

  // 메시지 상태
  const [updateMessage, setUpdateMessage] = useState<string>("");
  const [deleteMessage, setDeleteMessage] = useState<string>("");

  // 상품 목록 불러오기
  const fetchItems = async () => {
    try {
      const response = await api.get("/items"); // <-- api 인스턴스 사용
      setItems(response.data);
      setLoading(false);
    } catch (err) {
      setError("상품 목록을 불러오지 못했습니다.");
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchItems();
  }, []);

  // 신규 상품 등록 처리
  const handleCreateItem = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem("token");
      // price, stock은 Number로 변환
      const payload = {
        itemName: newItem.itemName,
        price: Number(newItem.price),
        stock: Number(newItem.stock),
        imagePath: newItem.imagePath,
        content: newItem.content,
        category: newItem.category,
      };
      await api.post("/items", payload, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setNewItem({
        itemName: "",
        price: "",
        stock: "",
        imagePath: "",
        content: "",
        category: "",
      });
      fetchItems();
    } catch (err) {
      setError("상품 생성에 실패했습니다.");
    }
  };

  // 수정 폼 제출 처리
  const handleUpdateItem = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingItem) return;
    try {
      const token = localStorage.getItem("token");
      await api.put(`/items/${editingItem.id}`, editingItem, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setUpdateMessage("수정이 완료되었습니다.");
      setEditingItem(null);
      fetchItems();
      // 3초 후 메시지 제거
      setTimeout(() => setUpdateMessage(""), 3000);
    } catch (err) {
      setError("상품 수정에 실패했습니다.");
    }
  };

  // 상품 삭제 처리
  const handleDeleteItem = async (id: number) => {
    try {
      const token = localStorage.getItem("token");
      await api.delete(`/items/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setDeleteMessage("삭제가 완료되었습니다.");
      fetchItems();
      setTimeout(() => setDeleteMessage(""), 3000);
    } catch (err) {
      setError("상품 삭제에 실패했습니다.");
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        로딩 중...
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 p-4">
      <h1 className="text-2xl font-bold mb-4 text-center">관리자 페이지</h1>

      {updateMessage && (
        <p className="text-green-500 text-center mb-2">{updateMessage}</p>
      )}
      {deleteMessage && (
        <p className="text-green-500 text-center mb-2">{deleteMessage}</p>
      )}
      {error && <p className="text-red-500 text-center mb-2">{error}</p>}

      {/* 신규 상품 등록 섹션 */}
      <section className="mb-8">
        <h2 className="text-xl font-bold mb-2">상품 등록</h2>
        <form
          onSubmit={handleCreateItem}
          className="bg-white p-4 rounded shadow"
        >
          <div className="mb-2">
            <label className="block text-gray-700">상품 이름</label>
            <input
              type="text"
              value={newItem.itemName}
              onChange={(e) =>
                setNewItem({ ...newItem, itemName: e.target.value })
              }
              className="border p-2 rounded w-full"
              required
            />
          </div>
          <div className="mb-2">
            <label className="block text-gray-700">상품 설명</label>
            <textarea
              value={newItem.content}
              onChange={(e) =>
                setNewItem({ ...newItem, content: e.target.value })
              }
              className="border p-2 rounded w-full"
              required
            />
          </div>
          <div className="mb-2">
            <label className="block text-gray-700">상품 이미지 URL</label>
            <input
              type="text"
              value={newItem.imagePath}
              onChange={(e) =>
                setNewItem({ ...newItem, imagePath: e.target.value })
              }
              className="border p-2 rounded w-full"
            />
          </div>
          <div className="mb-2">
            <label className="block text-gray-700">상품 가격</label>
            <input
              type="number"
              value={newItem.price}
              onChange={(e) =>
                setNewItem({ ...newItem, price: e.target.value })
              }
              onBlur={(e) =>
                setNewItem({
                  ...newItem,
                  price: String(Number(e.target.value)),
                })
              }
              className="border p-2 rounded w-full"
              required
            />
          </div>
          <div className="mb-2">
            <label className="block text-gray-700">재고 수량</label>
            <input
              type="number"
              value={newItem.stock}
              onChange={(e) =>
                setNewItem({ ...newItem, stock: e.target.value })
              }
              onBlur={(e) =>
                setNewItem({
                  ...newItem,
                  stock: String(Number(e.target.value)),
                })
              }
              className="border p-2 rounded w-full"
              required
            />
          </div>
          <div className="mb-2">
            <label className="block text-gray-700">상품 카테고리</label>
            <select
              value={newItem.category}
              onChange={(e) =>
                setNewItem({ ...newItem, category: e.target.value })
              }
              className="border p-2 rounded w-full"
              required
            >
              <option value="">카테고리를 선택하세요</option>
              <option value="ARABICA">아라비카</option>
              <option value="ROBUSTA">로부스타</option>
              <option value="LIBERICA">리베리카</option>
              <option value="DECAF">디카페인</option>
            </select>
          </div>
          <button
            type="submit"
            className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
          >
            상품 등록
          </button>
        </form>
      </section>

      {/* 상품 목록 및 수정/삭제 섹션 */}
      <section>
        <h2 className="text-xl font-bold mb-2">상품 목록</h2>
        {items.map((item) => (
          <div key={item.id} className="bg-white p-4 rounded shadow mb-2">
            {editingItem && editingItem.id === item.id ? (
              <form onSubmit={handleUpdateItem}>
                <div className="mb-2">
                  <label className="block text-gray-700">상품 이름</label>
                  <input
                    type="text"
                    value={editingItem.itemName}
                    onChange={(e) =>
                      setEditingItem({
                        ...editingItem,
                        itemName: e.target.value,
                      })
                    }
                    className="border p-2 rounded w-full"
                    required
                  />
                </div>
                <div className="mb-2">
                  <label className="block text-gray-700">상품 설명</label>
                  <textarea
                    value={editingItem.content}
                    onChange={(e) =>
                      setEditingItem({
                        ...editingItem,
                        content: e.target.value,
                      })
                    }
                    className="border p-2 rounded w-full"
                    required
                  />
                </div>
                <div className="mb-2">
                  <label className="block text-gray-700">상품 이미지 URL</label>
                  <input
                    type="text"
                    value={editingItem.imagePath}
                    onChange={(e) =>
                      setEditingItem({
                        ...editingItem,
                        imagePath: e.target.value,
                      })
                    }
                    className="border p-2 rounded w-full"
                  />
                </div>
                <div className="mb-2">
                  <label className="block text-gray-700">상품 가격</label>
                  <input
                    type="number"
                    value={editingItem.price}
                    onChange={(e) =>
                      setEditingItem({
                        ...editingItem,
                        price: Number(e.target.value),
                      })
                    }
                    className="border p-2 rounded w-full"
                    required
                  />
                </div>
                <div className="mb-2">
                  <label className="block text-gray-700">재고 수량</label>
                  <input
                    type="number"
                    value={editingItem.stock}
                    onChange={(e) =>
                      setEditingItem({
                        ...editingItem,
                        stock: Number(e.target.value),
                      })
                    }
                    className="border p-2 rounded w-full"
                    required
                  />
                </div>
                <div className="mb-2">
                  <label className="block text-gray-700">상품 카테고리</label>
                  <select
                    value={editingItem.category}
                    onChange={(e) =>
                      setEditingItem({
                        ...editingItem,
                        category: e.target.value,
                      })
                    }
                    className="border p-2 rounded w-full"
                    required
                  >
                    <option value="">카테고리를 선택하세요</option>
                    <option value="ARABICA">아라비카</option>
                    <option value="ROBUSTA">로부스타</option>
                    <option value="LIBERICA">리베리카</option>
                    <option value="DECAF">디카페인</option>
                  </select>
                </div>
                <button
                  type="submit"
                  className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 mr-2"
                >
                  수정 완료
                </button>
                <button
                  type="button"
                  onClick={() => setEditingItem(null)}
                  className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
                >
                  취소
                </button>
              </form>
            ) : (
              <div className="flex justify-between items-center">
                <div>
                  <h3 className="font-bold">{item.itemName}</h3>
                  <p>{item.content}</p>
                  <p className="font-bold">가격: {item.price}</p>
                  <p className="font-bold">재고: {item.stock}</p>
                  <p className="text-sm text-gray-600">
                    카테고리: {item.category}
                  </p>
                </div>
                <div className="flex space-x-2">
                  <button
                    onClick={() => setEditingItem(item)}
                    className="bg-blue-500 text-white px-2 py-1 rounded hover:bg-blue-600"
                  >
                    수정
                  </button>
                  <button
                    onClick={() => handleDeleteItem(item.id)}
                    className="bg-red-500 text-white px-2 py-1 rounded hover:bg-red-600"
                  >
                    삭제
                  </button>
                </div>
              </div>
            )}
          </div>
        ))}
      </section>
    </div>
  );
}
