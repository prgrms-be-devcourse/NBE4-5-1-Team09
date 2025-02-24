"use client";
import React, { useState, useEffect } from "react";
import axios from "axios";
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
  reviewContent: string;
  rating: number;
  createdAt: string;
}
// Next.js App Router에서 동적 라우트 params를 Promise로 받는 경우를 가정
export default function ProductDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const router = useRouter();
  // URL 파라미터에서 ID 가져오기
  const [id, setId] = useState<string | null>(null);
  useEffect(() => {
    const fetchParams = async () => {
      const resolvedParams = await params;
      setId(resolvedParams.id);
    };
    fetchParams();
  }, [params]);
  // 상태 변수들
  const [product, setProduct] = useState<Product | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  // 정렬 상태
  const [sortType, setSortType] = useState("LATEST");
  // 리뷰 작성 모달 상태
  const [showModal, setShowModal] = useState(false);
  const [reviewContent, setReviewContent] = useState("");
  const [rating, setRating] = useState(1);
  // 내가 작성한 리뷰가 있는지 체크(필요시)
  const [myReview, setMyReview] = useState<Review | null>(null);
  // 상품 & 리뷰 가져오기
  useEffect(() => {
    if (!id) return;
    const fetchProductData = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/items/${id}`);
        setProduct(response.data);
        setLoading(false);
      } catch (err) {
        setError("상품 정보를 불러오는데 실패했습니다.");
        setLoading(false);
      }
    };
    const fetchReviews = async () => {
      try {
        const response = await axios.get(
          `http://localhost:8080/reviews/item/${id}?sort=${sortType}`
        );
        setReviews(Array.isArray(response.data) ? response.data : []);
        setLoading(false);
      } catch (err) {
        setError("리뷰 정보를 불러오는데 실패했습니다.");
        setLoading(false);
      }
    };
    fetchProductData();
    fetchReviews();
  }, [id, sortType]);
  // 리뷰 작성 함수
  const handleReviewSubmit = async () => {
    if (!id) return;
    try {
      // 로컬 스토리지에서 토큰과 이메일 추출
      const token = localStorage.getItem("token");
      const memberEmail = localStorage.getItem("memberEmail"); // 이메일을 로컬 스토리지에서 받아옴
  
      if (!token || !memberEmail) {
        alert("로그인이 필요합니다.");
        router.push("/login");
        return;
      }
  
      const response = await axios.post(
        "http://localhost:8080/reviews/create",
        {
          memberEmail,  // 이메일 포함
          itemId: id,
          reviewContent,
          rating,
        },
        {
          headers: {
            // 토큰을 Authorization 헤더에 설정
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      // 리뷰 배열에 새 리뷰를 추가
      setReviews([...reviews, response.data]);
      setMyReview(response.data);
      setShowModal(false); // 모달 닫기
      setReviewContent("");
      setRating(1);
    } catch (err) {
      console.error(err);
      setError("리뷰 작성에 실패했습니다.");
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
                <img
                  src={product.imagePath}
                  alt={product.itemName}
                  className="w-full h-auto"
                />
              )}
            </div>
            <div className="w-1/2">
              <h2 className="text-3xl font-bold mb-4">{product?.itemName}</h2>
              <p className="text-lg mb-4">{product?.content}</p>
              <p className="text-xl font-bold text-green-600 mb-4">
                가격: {product?.price}
              </p>
              <p className="text-sm text-black mb-4">
                카테고리: {product?.category}
              </p>
              <p className="text-yellow-500 mb-4">
                평점: {product?.avgRating ?? "-"}
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
              <Link
                href="/products"
                className="text-blue-600 hover:underline mt-4 inline-block"
              >
                돌아가기
              </Link>
            </div>
          </div>
        </div>
      </section>
      {/* 리뷰 섹션 */}
      <section className="py-12">
        <div className="container mx-auto">
          <h3 className="text-3xl font-bold mb-6 text-center">리뷰</h3>
          <div className="flex justify-between mb-4">
            {/* 정렬 select */}
            <select
              value={sortType}
              onChange={(e) => setSortType(e.target.value)}
              className="border p-2 rounded"
            >
              <option value="LATEST">최신순</option>
              <option value="HIGHEST">높은 평점순</option>
              <option value="LOWEST">낮은 평점순</option>
            </select>
            {/* 리뷰 작성 버튼 */}
            <div>
              {!myReview && (
                <button
                  onClick={() => setShowModal(true)}
                  className="bg-blue-500 text-white px-4 py-2 rounded"
                >
                  리뷰 작성
                </button>
              )}
            </div>
          </div>
          {/* 리뷰 작성 모달 */}
          {showModal && (
            <div className="fixed inset-0 flex justify-center items-center bg-gray-500 bg-opacity-50 z-50">
              <div className="bg-white p-6 rounded shadow-lg">
                <h3 className="text-2xl font-bold mb-4">리뷰 작성</h3>
                <textarea
                  value={reviewContent}
                  onChange={(e) => setReviewContent(e.target.value)}
                  placeholder="리뷰 내용을 입력하세요."
                  className="w-full h-32 border p-2 mb-4 rounded"
                />
                <div className="mb-4">
                  <label>평점:</label>
                  <input
                    type="number"
                    min="1"
                    max="5"
                    value={rating}
                    onChange={(e) => setRating(Number(e.target.value))}
                    className="w-16 border p-2 rounded"
                  />
                </div>
                <button
                  onClick={handleReviewSubmit}
                  className="bg-blue-500 text-white px-4 py-2 rounded mr-2"
                >
                  작성하기
                </button>
                <button
                  onClick={() => setShowModal(false)}
                  className="bg-gray-500 text-white px-4 py-2 rounded"
                >
                  취소
                </button>
              </div>
            </div>
          )}
          {/* 에러 메시지 */}
          {error && <div className="text-center text-red-500 mb-4">{error}</div>}
          {/* 리뷰 목록 */}
          {reviews.length === 0 ? (
            <div className="text-center">리뷰가 없습니다.</div>
          ) : (
            <div>
              {reviews.map((review) => (
                <div
                  key={review.reviewId}
                  className="bg-white p-4 rounded shadow mb-4"
                >
                  <p className="font-semibold">
                    회원 ID: {review.memberId} | 평점: {review.rating}
                  </p>
                  <p>{review.reviewContent}</p>
                  <p className="text-sm text-gray-500">
                    작성일: {review.createdAt}
                  </p>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>
    </div>
  );
}