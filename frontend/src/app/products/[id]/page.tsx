"use client";
import React, { useState, useEffect } from "react";
import api from "../../../lib/axios";
import Link from "next/link";
import { useRouter } from "next/navigation";

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

interface Review {
  reviewId: number;
  memberId: number;
  memberEmail: string;
  reviewContent: string;
  rating: number;
  createdAt: string;
}

export default function ProductDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const router = useRouter();
  const [id, setId] = useState<string | null>(null);
  const [product, setProduct] = useState<Product | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [sortType, setSortType] = useState("LATEST");
  const [showModal, setShowModal] = useState(false);
  const [reviewContent, setReviewContent] = useState("");
  const [rating, setRating] = useState(1);
  const [myReviews, setMyReviews] = useState<Review[]>([]);
  const [editingReviewId, setEditingReviewId] = useState<number | null>(null);
  const [isAdmin, setIsAdmin] = useState(false);
  // 구매 수량 상태 (초기값 1)
  const [buyQuantity, setBuyQuantity] = useState<number>(1);

  // URL 파라미터에서 ID 가져오기
  useEffect(() => {
    (async () => {
      const resolvedParams = await params;
      setId(resolvedParams.id);
    })();
  }, [params]);

  // 상품 & 리뷰 가져오기
  const fetchProductData = async (id: string) => {
    try {
      const response = await api.get(`/items/${id}`);
      setProduct(response.data);
      setLoading(false);
      // 초기 구매 수량 1로 설정
      setBuyQuantity(1);
    } catch (err) {
      setError("상품 정보를 불러오는데 실패했습니다.");
      setLoading(false);
    }
  };

  const fetchReviews = async (id: string, sortType: string) => {
    try {
      const memberEmail = localStorage.getItem("email");
      const response = await api.get(
        `/reviews/item/${id}/${memberEmail}?sortType=${sortType}`
      );
      setReviews(response.data);
      setLoading(false);
    } catch (err) {
      setError("리뷰 정보를 불러오는데 실패했습니다.");
      setLoading(false);
    }
  };

  const fetchMyReview = async (id: string) => {
    const memberEmail = localStorage.getItem("email");
    if (!memberEmail) return;
    try {
      const response = await api.get(`/reviews/my/item/${id}/${memberEmail}`);
      if (Array.isArray(response.data)) {
        setMyReviews(response.data);
      }
    } catch (err) {
      console.error("내 리뷰 정보를 불러오는데 실패했습니다.");
    }
  };

  // 토큰에서 authority 정보를 디코딩하여 관리자 여부 설정
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
    if (!id) return;
    fetchProductData(id);
    fetchReviews(id, sortType);
    fetchMyReview(id);
  }, [id, sortType]);

  const handleReviewSubmit = async () => {
    if (!id) return;
    try {
      const token = localStorage.getItem("token");
      const memberEmail = localStorage.getItem("email");

      if (!token || !memberEmail) {
        alert("로그인이 필요합니다.");
        router.push("/login");
        return;
      }

      const reviewData = { memberEmail, itemId: id, reviewContent, rating };

      if (editingReviewId) {
        await api.put(`/reviews/update/${editingReviewId}`, reviewData, {
          headers: { Authorization: `Bearer ${token}` },
        });
      } else {
        await api.post("/reviews/create", reviewData, {
          headers: { Authorization: `Bearer ${token}` },
        });
      }

      // 성공 시 상태 초기화
      setShowModal(false);
      setReviewContent("");
      setRating(1);
      setEditingReviewId(null);

      // 리뷰 갱신
      fetchReviews(id, sortType);
      fetchMyReview(id);
      fetchProductData(id);
    } catch (err: any) {
      if (err.response) {
        // 서버에서 보낸 에러 메시지를 가져옴
        const errorMessage =
          err.response.data?.msg ||
          err.response.data?.message ||
          err.response.data?.resultCode ||
          "리뷰 작성 중 오류가 발생했습니다.";

        alert(`에러: ${errorMessage}`);
        setError(errorMessage);
      } else if (err.request) {
        alert("서버 응답이 없습니다. 네트워크 상태를 확인해주세요.");
        setError("서버 응답이 없습니다.");
      } else {
        alert(`요청 중 오류 발생: ${err.message}`);
        setError(`요청 중 오류 발생: ${err.message}`);
      }
    }
  };

  const handleReviewDelete = async (reviewId: number) => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("로그인이 필요합니다.");
        router.push("/login");
        return;
      }
      await api.delete(`/reviews/delete/${reviewId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      fetchReviews(id!, sortType);
      fetchMyReview(id!);
      fetchProductData(id!);
    } catch (err) {
      console.error("리뷰 삭제에 실패했습니다.");
    }
  };

  const handleEditButtonClick = (review: Review) => {
    setEditingReviewId(review.reviewId);
    setReviewContent(review.reviewContent);
    setRating(review.rating);
    setShowModal(true);
  };

  const handleSortChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const newSortType = event.target.value;
    setSortType(newSortType);
    fetchReviews(id!, newSortType);
  };

  // 구매하기: 단건 주문 API 호출 후, 결제 옵션 페이지(PaymentSelectionPage)로 이동
  const handleBuyNow = async () => {
    try {
      const token = localStorage.getItem("token");
      const memberEmail = localStorage.getItem("email");
      console.log("handleBuyNow - token:", token); // 토큰 값 디버깅
      if (!token || !memberEmail || !product) {
        alert("로그인이 필요합니다.");
        router.push("/login");
        return;
      }
      const response = await api.post(
        "/order/item",
        { memberEmail, itemId: product.id, quantity: buyQuantity },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      const { tradeUUID, totalPrice } = response.data;
      router.push(`/payment?tradeUUID=${tradeUUID}&totalPrice=${totalPrice}`);
    } catch (err: any) {
      const errorData = err.response?.data;
      const errorMsg =
        typeof errorData === "object"
          ? JSON.stringify(errorData)
          : errorData || "구매 주문에 실패했습니다.";
      alert(errorMsg);
    }
  };

  // 장바구니 담기: 장바구니 추가 API 호출
  const handleAddToCart = async () => {
    try {
      const token = localStorage.getItem("token");
      const memberEmail = localStorage.getItem("email");
      if (!token || !memberEmail || !product) {
        alert("로그인이 필요합니다.");
        router.push("/login");
        return;
      }
      await api.post(
        "/cart/add",
        { memberEmail, itemId: product.id, quantity: buyQuantity },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert("상품이 장바구니에 추가되었습니다.");
    } catch (err: any) {
      alert(err.response?.data || "장바구니 추가에 실패했습니다.");
    }
  };

  if (loading) {
    return <div className="text-center my-8">로딩 중...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* 상품 상세 섹션 */}
      <section className="bg-white py-6">
        <div className="container mx-auto">
          <div className="flex gap-6">
            <div className="w-1/2">
              {product && (
                <div className="w-full h-96 flex justify-center items-center bg-gray-100">
                  <img
                    src={`http://localhost:8080${product.imagePath}`}
                    alt={product.itemName}
                    className="w-full h-full object-contain"
                  />
                </div>
              )}
            </div>
            <div className="w-1/2">
              <h2 className="text-3xl font-bold mb-4">{product?.itemName}</h2>
              <p className="text-lg mb-4">{product?.content}</p>
              <p className="text-xl font-bold text-green-600 mb-4">
                가격:{" "}
                {product?.price !== undefined
                  ? new Intl.NumberFormat("ko-KR").format(product?.price)
                  : "가격 정보 없음"}{" "}
                원
              </p>
              <p className="text-sm text-black mb-4">
                카테고리: {product?.category}
              </p>
              <p className="text-sm text-black">재고: {product?.stock}</p>
              <p className="text-yellow-500 mb-4">
                평점:{" "}
                {product?.avgRating !== null
                  ? product?.avgRating.toFixed(1)
                  : "-"}
              </p>
              <p
                className={`text-lg font-semibold ${
                  product?.itemStatus === "ON_SALE"
                    ? "text-green-500"
                    : "text-red-500"
                }`}
              >
                {product?.itemStatus === "ON_SALE" ? "구매 가능" : "품절"}
              </p>
              {product?.itemStatus === "ON_SALE" && (
                <div className="mt-4">
                  {/* 구매 수량 조절 입력 필드 */}
                  <div className="flex items-center gap-2 mb-4">
                    <label className="font-medium">구매 수량:</label>
                    <button
                      onClick={() =>
                        setBuyQuantity((prev) => Math.max(prev - 1, 1))
                      }
                      className="bg-gray-300 text-black px-2 py-1 rounded"
                    >
                      -
                    </button>
                    <input
                      type="number"
                      min="1"
                      value={buyQuantity}
                      onChange={(e) =>
                        setBuyQuantity(parseInt(e.target.value, 10) || 1)
                      }
                      className="border rounded p-1 w-16 text-center"
                    />
                    <button
                      onClick={() => setBuyQuantity((prev) => prev + 1)}
                      className="bg-gray-300 text-black px-2 py-1 rounded"
                    >
                      +
                    </button>
                  </div>
                  {/* 구매하기와 장바구니 담기 버튼 */}
                  <div className="flex gap-4">
                    <button
                      onClick={handleBuyNow}
                      className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
                    >
                      구매하기
                    </button>
                    <button
                      onClick={handleAddToCart}
                      className="bg-yellow-500 text-white px-4 py-2 rounded hover:bg-yellow-600"
                    >
                      장바구니 담기
                    </button>
                  </div>
                </div>
              )}
              <div className="mt-4">
                <Link
                  href="/products"
                  className="text-blue-600 hover:underline inline-block"
                >
                  돌아가기
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 리뷰 섹션 */}
      <section className="py-12">
        <div className="container mx-auto">
          <h3 className="text-3xl font-bold mb-6 text-center">리뷰</h3>
          {myReviews.length > 0 && (
            <div className="bg-blue-100 p-4 rounded shadow mb-6">
              <h4 className="text-xl font-semibold mb-2">내가 작성한 리뷰</h4>
              {myReviews.map((review) => (
                <div key={review.reviewId} className="mb-4">
                  <p className="font-semibold">평점: {review.rating}</p>
                  <p>{review.reviewContent}</p>
                  <p className="text-sm text-gray-500">
                    작성일: {review.createdAt.slice(0, 10)}
                  </p>
                  <div className="flex gap-2 mt-2">
                    <button
                      onClick={() => handleEditButtonClick(review)}
                      className="text-blue-500"
                    >
                      수정
                    </button>
                    <button
                      onClick={() => handleReviewDelete(review.reviewId)}
                      className="text-red-500"
                    >
                      삭제
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
          {showModal && (
            <div className="fixed top-0 left-0 w-full h-full bg-gray-800 bg-opacity-50 flex justify-center items-center">
              <div className="bg-white p-6 rounded shadow-md w-96">
                <h3 className="text-2xl font-bold mb-4">
                  {editingReviewId ? "리뷰 수정" : "리뷰 작성"}
                </h3>
                <textarea
                  className="w-full p-2 border border-gray-300 rounded mb-4"
                  placeholder="리뷰 내용을 작성하세요."
                  value={reviewContent}
                  onChange={(e) => setReviewContent(e.target.value)}
                />
                <div className="flex items-center mb-4">
                  <div className="flex items-center gap-2 mb-4">
                    <span>평점 : {rating}</span>
                    <input
                      type="range"
                      min="1"
                      max="5"
                      step="0.5"
                      value={rating}
                      onChange={(e) => setRating(Number(e.target.value))}
                      className="w-full"
                    />
                  </div>
                </div>
                <button
                  onClick={handleReviewSubmit}
                  className="bg-blue-500 text-white px-4 py-2 rounded"
                >
                  {editingReviewId ? "수정" : "작성"} 완료
                </button>
                <button
                  onClick={() => setShowModal(false)}
                  className="ml-4 text-gray-500"
                >
                  취소
                </button>
              </div>
            </div>
          )}
          <div className="text-right mb-4">
            <button
              onClick={() => setShowModal(true)}
              className="bg-green-500 text-white px-4 py-2 rounded"
            >
              리뷰 작성
            </button>
          </div>
          <div className="text-right mb-4">
            <label className="mr-2">정렬 기준: </label>
            <select
              value={sortType}
              onChange={handleSortChange}
              className="p-2 border border-gray-300 rounded"
            >
              <option value="LATEST">최신순</option>
              <option value="HIGHEST_RATING">평점 높은 순</option>
              <option value="LOWEST_RATING">평점 낮은 순</option>
            </select>
          </div>
          {reviews.length === 0 ? (
            <p className="text-center">리뷰가 없습니다.</p>
          ) : (
            <div>
              {reviews.map((review) => (
                <div key={review.reviewId} className="mb-6 border-b pb-4">
                  <p className="font-semibold">평점: {review.rating}</p>
                  <p>{review.reviewContent}</p>
                  <p className="text-sm text-gray-500">
                    작성일: {review.createdAt.slice(0, 10)}
                  </p>
                  <div className="flex justify-between mt-2">
                    {isAdmin && <span>관리자</span>}
                    {(isAdmin ||
                      localStorage.getItem("email") === review.memberEmail) && (
                      <button
                        onClick={() => handleReviewDelete(review.reviewId)}
                        className="text-red-500"
                      >
                        삭제
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
