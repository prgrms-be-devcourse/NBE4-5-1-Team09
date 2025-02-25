"use client";
import React, { useState, useEffect, useRef } from "react";
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

interface JwtPayload {
  authority: string;
}

/** 토큰 디코딩 (간단한 예시) */
function decodeJwt(token: string): JwtPayload | null {
  try {
    const payloadPart = token.split(".")[1];
    const base64 = payloadPart.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(jsonPayload);
  } catch (err) {
    console.error("JWT 디코딩 실패:", err);
    return null;
  }
}

export default function AdminPage() {
  const router = useRouter();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const editFileInputRef = useRef<HTMLInputElement>(null);

  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>("");
  const [uploadStatus, setUploadStatus] = useState<string>("");

  // 신규 상품 등록 폼 상태
  const [newItem, setNewItem] = useState({
    itemName: "",
    price: "",
    stock: "",
    imagePath: "",
    content: "",
    category: "",
  });

  // 이미지 파일 상태
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [editSelectedFile, setEditSelectedFile] = useState<File | null>(null);

  // 수정 중인 상품 상태 (null이면 수정 모드 아님)
  const [editingItem, setEditingItem] = useState<Item | null>(null);

  // 메시지 상태
  const [updateMessage, setUpdateMessage] = useState<string>("");
  const [deleteMessage, setDeleteMessage] = useState<string>("");

  // 권한 검사 useEffect
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      // 토큰이 없다면 관리자 페이지 접근 불가 → 로그인 or 홈으로 이동
      router.push("/");
      return;
    }

    // 토큰 디코딩
    const decoded = decodeJwt(token);
    if (!decoded || decoded.authority !== "ADMIN") {
      // 관리자 권한이 아니면 홈으로 이동
      router.push("/");
      return;
    }

    // 여기까지 왔다면 관리자 권한이 맞음 → 상품 목록 불러오기
    fetchItems();
  }, [router]);

  // 상품 목록 불러오기
  const fetchItems = async () => {
    try {
      const response = await api.get("/items");
      setItems(response.data);
      setLoading(false);
    } catch (err) {
      setError("상품 목록을 불러오지 못했습니다.");
      setLoading(false);
    }
  };

  // 신규 상품 등록 처리
  const handleCreateItem = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        setError("인증되지 않았습니다.");
        return;
      }

      // price, stock은 Number로 변환
      const itemData = {
        itemName: newItem.itemName,
        price: Number(newItem.price),
        stock: Number(newItem.stock),
        imagePath: newItem.imagePath, // 파일 없는 경우 URL 경로 사용
        content: newItem.content,
        category: newItem.category,
      };

      // FormData 생성
      const formData = new FormData();

      // 상품 정보를 JSON 문자열로 변환하여 추가
      formData.append(
        "item",
        new Blob([JSON.stringify(itemData)], { type: "application/json" })
      );

      let response;

      if (selectedFile) {
        // 파일이 있는 경우 이미지 포함 API 호출
        formData.append("image", selectedFile);
        setUploadStatus("업로드 중...");

        response = await api.post("/items/image", formData, {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "multipart/form-data",
          },
        });

        setUploadStatus("업로드 완료!");
        setTimeout(() => setUploadStatus(""), 3000);
      } else {
        // 파일이 없는 경우 기존 API 호출
        response = await api.post("/items", itemData, {
          headers: { Authorization: `Bearer ${token}` },
        });
      }

      setNewItem({
        itemName: "",
        price: "",
        stock: "",
        imagePath: "",
        content: "",
        category: "",
      });
      setSelectedFile(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
      fetchItems();
    } catch (err) {
      setError("상품 생성에 실패했습니다.");
      setUploadStatus("");
    }
  };

  // 수정 폼 제출 처리
  const handleUpdateItem = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingItem) return;
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        setError("인증되지 않았습니다.");
        return;
      }

      if (editSelectedFile) {
        // 파일이 있는 경우 FormData 사용
        const formData = new FormData();

        // 상품 정보를 JSON으로 변환하여 추가
        formData.append(
          "item",
          new Blob([JSON.stringify(editingItem)], { type: "application/json" })
        );
        formData.append("image", editSelectedFile);

        setUploadStatus("업로드 중...");

        await api.put(`/items/${editingItem.id}/image`, formData, {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "multipart/form-data",
          },
        });

        setUploadStatus("업로드 완료!");
        setTimeout(() => setUploadStatus(""), 3000);
      } else {
        // 파일이 없는 경우 일반 JSON 요청
        await api.put(`/items/${editingItem.id}`, editingItem, {
          headers: { Authorization: `Bearer ${token}` },
        });
      }

      setUpdateMessage("수정이 완료되었습니다.");
      setEditingItem(null);
      setEditSelectedFile(null);
      fetchItems();
      // 3초 후 메시지 제거
      setTimeout(() => setUpdateMessage(""), 3000);
    } catch (err) {
      setError("상품 수정에 실패했습니다.");
      setUploadStatus("");
    }
  };

  // 상품 삭제 처리
  const handleDeleteItem = async (id: number) => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        setError("인증되지 않았습니다.");
        return;
      }
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

  // 파일 선택 처리
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      const file = e.target.files[0];
      setSelectedFile(file);
      // 파일 이름을 imagePath에 표시
      setNewItem({ ...newItem, imagePath: file.name });
    }
  };

  // 수정 폼 파일 선택 처리
  const handleEditFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0 && editingItem) {
      const file = e.target.files[0];
      setEditSelectedFile(file);
      // 파일 이름을 imagePath에 표시
      setEditingItem({ ...editingItem, imagePath: file.name });
    }
  };

  // 파일 선택 버튼 클릭 핸들러
  const handleFileButtonClick = () => {
    if (fileInputRef.current) {
      fileInputRef.current.click();
    }
  };

  // 수정 폼 파일 선택 버튼 클릭 핸들러
  const handleEditFileButtonClick = () => {
    if (editFileInputRef.current) {
      editFileInputRef.current.click();
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
      {uploadStatus && (
        <p className="text-blue-500 text-center mb-2">{uploadStatus}</p>
      )}

      {/* 신규 상품 등록 섹션 */}
      <section className="mb-8">
        <h2 className="text-xl font-bold mb-2">상품 등록</h2>
        <form
          onSubmit={handleCreateItem}
          className="bg-white p-4 rounded shadow"
          encType="multipart/form-data"
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
            <label className="block text-gray-700">상품 이미지</label>
            <div className="flex">
              <input
                type="text"
                value={newItem.imagePath}
                onChange={(e) =>
                  setNewItem({ ...newItem, imagePath: e.target.value })
                }
                className="border p-2 rounded w-full mr-2"
                placeholder="파일 이름 또는 URL"
                readOnly={!!selectedFile}
              />
              <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileChange}
                className="hidden"
                accept="image/*"
              />
              <button
                type="button"
                onClick={handleFileButtonClick}
                className="bg-gray-200 text-gray-700 px-4 py-2 rounded hover:bg-gray-300"
              >
                파일 선택
              </button>
            </div>
            {selectedFile && (
              <p className="text-sm text-green-600 mt-1">
                선택된 파일: {selectedFile.name} (
                {Math.round(selectedFile.size / 1024)} KB)
              </p>
            )}
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
              <form onSubmit={handleUpdateItem} encType="multipart/form-data">
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
                  <label className="block text-gray-700">상품 이미지</label>
                  <div className="flex">
                    <input
                      type="text"
                      value={editingItem.imagePath}
                      onChange={(e) =>
                        setEditingItem({
                          ...editingItem,
                          imagePath: e.target.value,
                        })
                      }
                      className="border p-2 rounded w-full mr-2"
                      placeholder="파일 이름 또는 URL"
                      readOnly={!!editSelectedFile}
                    />
                    <input
                      type="file"
                      ref={editFileInputRef}
                      onChange={handleEditFileChange}
                      className="hidden"
                      accept="image/*"
                    />
                    <button
                      type="button"
                      onClick={handleEditFileButtonClick}
                      className="bg-gray-200 text-gray-700 px-4 py-2 rounded hover:bg-gray-300"
                    >
                      파일 선택
                    </button>
                  </div>
                  {editSelectedFile && (
                    <p className="text-sm text-green-600 mt-1">
                      선택된 파일: {editSelectedFile.name} (
                      {Math.round(editSelectedFile.size / 1024)} KB)
                    </p>
                  )}
                  {!editSelectedFile && item.imagePath && (
                    <p className="text-sm text-blue-600 mt-1">
                      현재 이미지: {item.imagePath.split("/").pop()}
                    </p>
                  )}
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
                  onClick={() => {
                    setEditingItem(null);
                    setEditSelectedFile(null);
                  }}
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
                  {item.imagePath && (
                    <p className="text-sm text-gray-600">
                      이미지: {item.imagePath.split("/").pop()}
                    </p>
                  )}
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
