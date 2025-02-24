'use client'
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

export default function ProductDetailPage({ params }: { params: Promise<{ id: string }> }) {
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
  const [editingReviewId, setEditingReviewId] = useState<number | null>(null); // 수정 중인 리뷰 ID
  const [isAdmin, setIsAdmin] = useState(false); // 관리자 여부

  // URL 파라미터에서 ID 가져오기
  useEffect(() => {
    const fetchParams = async () => {
      const resolvedParams = await params;
      setId(resolvedParams.id);
    };
    fetchParams();
  }, [params]);

  // 상품 & 리뷰 가져오기
  const fetchProductData = async (id: string) => {
    try {
      const response = await axios.get(`http://localhost:8080/items/${id}`);
      setProduct(response.data);
      setLoading(false);
    } catch (err) {
      setError("상품 정보를 불러오는데 실패했습니다.");
      setLoading(false);
    }
  };

  const fetchReviews = async (id: string, sortType: string) => {
    console.log(`Fetching reviews for item ${id} with sort type: ${sortType}`); // 로그 추가
    try {
      const memberEmail = localStorage.getItem("email");
      const response = await axios.get(
        `http://localhost:8080/reviews/item/${id}/${memberEmail}?sortType=${sortType}`
      );
      console.log(response.data);
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
      const response = await axios.get(
        `http://localhost:8080/reviews/my/item/${id}/${memberEmail}`
      );
      if (Array.isArray(response.data)) {
        setMyReviews(response.data);
      }
    } catch (err) {
      console.error("내 리뷰 정보를 불러오는데 실패했습니다.");
    }
  };

  useEffect(() => {
    const checkAdmin = () => {
      const userRole = localStorage.getItem("role");
      if (userRole === "ADMIN") {
        setIsAdmin(true);
      } else {
        setIsAdmin(false); // ADMIN이 아닌 경우 false로 설정
      }
    };
    checkAdmin();
  }, []);
  console.log("isAdmin:", isAdmin);

  useEffect(() => {
    if (!id) return;
    fetchProductData(id);
    fetchReviews(id, sortType);
    fetchMyReview(id);
  }, [id, sortType]);
  

  // 리뷰 작성 함수
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

      if (editingReviewId) {
        // 수정 모드일 때는 PUT 요청을 보냄
        await axios.put(
          `http://localhost:8080/reviews/update/${editingReviewId}`,
          {
            reviewContent,
            rating,
          },
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
      } else {
        // 새 리뷰 작성은 POST 요청
        await axios.post(
          "http://localhost:8080/reviews/create",
          {
            memberEmail,
            itemId: id,
            reviewContent,
            rating,
          },
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
      }

      setShowModal(false);
      setReviewContent("");
      setRating(1);
      setEditingReviewId(null); // 수정 모드 종료
      fetchReviews(id, sortType); // 리뷰 목록 갱신
      fetchMyReview(id); // 내 리뷰 목록 갱신
      fetchProductData(id); // 상품 정보 갱신 (평균 평점 갱신)
    } catch (err) {
      console.error(err);
      setError("리뷰 작성에 실패했습니다.");
    }
  };

  // 리뷰 삭제 함수
  const handleReviewDelete = async (reviewId: number) => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("로그인이 필요합니다.");
        router.push("/login");
        return;
      }

      await axios.delete(
        `http://localhost:8080/reviews/delete/${reviewId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      fetchReviews(id!, sortType); // 리뷰 목록 갱신
      fetchMyReview(id!); // 내 리뷰 목록 갱신
      fetchProductData(id!); // 상품 정보 갱신 (평균 평점 갱신)
    } catch (err) {
      console.error("리뷰 삭제에 실패했습니다.");
    }
  };

  const handleEditButtonClick = (review: Review) => {
    setEditingReviewId(review.reviewId);
    setReviewContent(review.reviewContent);
    setRating(review.rating);
    setShowModal(true); // 수정 모달 띄우기
  };

  const handleSortChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const newSortType = event.target.value;
    console.log(`Sort type changed to: ${newSortType}`); // 로그 추가
    setSortType(newSortType);
    fetchReviews(id!, newSortType); // 이 부분이 호출되는지 확인
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
                평점: {product?.avgRating !== null ? product?.avgRating.toFixed(1) : "-"}
              </p>
              <p
                className={`text-lg font-semibold ${product?.itemStatus === "ON_SALE" ? "text-green-500" : "text-red-500"}`}
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

          {/* 내가 작성한 리뷰 목록 */}
          {myReviews.length > 0 && (
            <div className="bg-blue-100 p-4 rounded shadow mb-6">
              <h4 className="text-xl font-semibold mb-2">내가 작성한 리뷰</h4>
              {myReviews.map((review) => (
                <div key={review.reviewId} className="mb-4">
                  <p className="font-semibold">평점: {review.rating}</p>
                  <p>{review.reviewContent}</p>
                  <p className="text-sm text-gray-500">작성일: {review.createdAt.slice(0, 10)}</p>
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

          {/* 리뷰 작성 모달 */}
          {showModal && (
            <div className="fixed top-0 left-0 w-full h-full bg-gray-800 bg-opacity-50 flex justify-center items-center">
              <div className="bg-white p-6 rounded shadow-md w-96">
                <h3 className="text-2xl font-bold mb-4">{editingReviewId ? "리뷰 수정" : "리뷰 작성"}</h3>
                <textarea
                  className="w-full p-2 border border-gray-300 rounded mb-4"
                  placeholder="리뷰 내용을 작성하세요."
                  value={reviewContent}
                  onChange={(e) => setReviewContent(e.target.value)}
                />
                <div className="flex items-center mb-4">
                  {/* 평점 슬라이더 */}
                  <div className="flex items-center gap-2 mb-4">
                  <span>평점 :&nbsp;{rating}</span>
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

          {/* 리뷰 작성 버튼 추가 */}
<div className="text-right mb-4">
  <button
    onClick={() => setShowModal(true)}
    className="bg-green-500 text-white px-4 py-2 rounded"
  >
    리뷰 작성
  </button>
</div>

{/* 리뷰 정렬 및 표시 */}
<div className="text-right mb-4">
  <label className="mr-2">정렬 기준: </label>
  <select
  value={sortType}
  onChange={(e) => handleSortChange(e)}  // 수정된 부분
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
    <p className="text-sm text-gray-500">작성일: {review.createdAt.slice(0, 10)}</p>
    <div className="flex justify-between mt-2">
      <span>{isAdmin && "관리자"}</span>
      {/* 관리자인 경우에만 삭제 버튼을 표시 */}
      {isAdmin && (
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
