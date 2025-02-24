"use client";
import React, { useState, useEffect } from "react";
import axios from "axios";
import { useRouter } from "next/navigation";
import { useAuth } from "../../context/AuthContext";

interface Profile {
  email: string;
  address: string;
  authority: string;
}

export default function AccountPage() {
  const router = useRouter();
  const { token, setToken } = useAuth();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [profileError, setProfileError] = useState<string>("");

  // 비밀번호 변경 상태
  const [oldPassword, setOldPassword] = useState<string>("");
  const [newPassword, setNewPassword] = useState<string>("");
  const [passwordMessage, setPasswordMessage] = useState<string>("");
  const [passwordError, setPasswordError] = useState<string>("");

  // 회원 탈퇴 상태
  const [delPassword, setDelPassword] = useState<string>("");
  const [delMessage, setDelMessage] = useState<string>("");
  const [delError, setDelError] = useState<string>("");

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      router.push("/login");
      return;
    }
    axios
      .get("/member/profile", {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((res) => {
        setProfile(res.data);
        setLoading(false);
      })
      .catch((err) => {
        setProfileError("프로필 조회에 실패했습니다.");
        setLoading(false);
      });
  }, [router]);

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    try {
      await axios.post(
        "/member/change-password",
        { oldPassword, newPassword },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setPasswordMessage("비밀번호 변경 성공");
      setPasswordError("");
      setOldPassword("");
      setNewPassword("");
    } catch (err: any) {
      let errMsg = "비밀번호 변경 실패";
      if (err.response?.data) {
        if (typeof err.response.data === "string") {
          errMsg = err.response.data;
        } else if (typeof err.response.data === "object") {
          errMsg = err.response.data.msg || JSON.stringify(err.response.data);
        }
      }
      setPasswordError(errMsg);
      setPasswordMessage("");
    }
  };

  const handleDeleteAccount = async (e: React.FormEvent) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    try {
      await axios.delete("/member/delete", {
        headers: { Authorization: `Bearer ${token}` },
        data: { email: profile?.email, password: delPassword },
      });
      setDelMessage("회원 탈퇴 성공");
      setDelError("");
      // 글로벌 상태와 로컬 스토리지에서 토큰 제거
      setToken(null);
      localStorage.removeItem("token");
      localStorage.removeItem("email");
      // 즉시 Header가 업데이트됨
      router.refresh();
    } catch (err: any) {
      let errMsg = "회원 탈퇴 실패";
      if (err.response?.data) {
        if (typeof err.response.data === "string") {
          errMsg = err.response.data;
        } else if (typeof err.response.data === "object") {
          errMsg = err.response.data.msg || JSON.stringify(err.response.data);
        }
      }
      setDelError(errMsg);
      setDelMessage("");
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
      <div className="container mx-auto mt-4">
        {profileError && <p className="text-red-500 mb-4">{profileError}</p>}

        {/* 프로필 정보 */}
        {profile && (
          <div className="bg-white p-4 rounded shadow mb-6">
            <h2 className="text-xl font-bold mb-2">프로필 정보</h2>
            <p>
              <strong>이메일:</strong> {profile.email}
            </p>
            <p>
              <strong>주소:</strong> {profile.address}
            </p>
            <p>
              <strong>권한:</strong> {profile.authority}
            </p>
          </div>
        )}

        {/* 비밀번호 변경 */}
        <div className="bg-white p-4 rounded shadow mb-6">
          <h2 className="text-xl font-bold mb-2">비밀번호 변경</h2>
          {passwordMessage && (
            <p className="text-green-500 mb-2">{passwordMessage}</p>
          )}
          {passwordError && (
            <p className="text-red-500 mb-2">{passwordError}</p>
          )}
          <form onSubmit={handleChangePassword}>
            <div className="mb-4">
              <label htmlFor="oldPassword" className="block text-gray-700">
                현재 비밀번호
              </label>
              <input
                type="password"
                id="oldPassword"
                value={oldPassword}
                onChange={(e) => setOldPassword(e.target.value)}
                className="mt-1 p-2 border rounded w-full"
                required
              />
            </div>
            <div className="mb-4">
              <label htmlFor="newPassword" className="block text-gray-700">
                새 비밀번호
              </label>
              <input
                type="password"
                id="newPassword"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className="mt-1 p-2 border rounded w-full"
                required
              />
            </div>
            <button
              type="submit"
              className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
            >
              비밀번호 변경
            </button>
          </form>
        </div>

        {/* 회원 탈퇴 */}
        <div className="bg-white p-4 rounded shadow mb-6">
          <h2 className="text-xl font-bold mb-2">회원 탈퇴</h2>
          {delMessage && <p className="text-green-500 mb-2">{delMessage}</p>}
          {delError && <p className="text-red-500 mb-2">{delError}</p>}
          <form onSubmit={handleDeleteAccount}>
            <div className="mb-4">
              <label htmlFor="delPassword" className="block text-gray-700">
                비밀번호
              </label>
              <input
                type="password"
                id="delPassword"
                value={delPassword}
                onChange={(e) => setDelPassword(e.target.value)}
                className="mt-1 p-2 border rounded w-full"
                required
              />
            </div>
            <button
              type="submit"
              className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
            >
              회원 탈퇴
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
