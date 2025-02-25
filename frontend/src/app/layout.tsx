// src/app/layout.tsx
import "./globals.css";
import { Inter } from "next/font/google";
import Header from "../components/Header";
import { AuthProvider } from "../context/AuthContext";

const inter = Inter({ subsets: ["latin"] });

export const metadata = {
  title: "카페",
  description: "Next.js 카페 프로젝트",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <head>
        {/* 아임포트 결제 스크립트 추가 */}
        <script src="https://service.iamport.kr/js/iamport.payment-1.2.0.js" />
      </head>
      <body className={inter.className}>
        <AuthProvider>
          <Header />
          <main>{children}</main>
        </AuthProvider>
      </body>
    </html>
  );
}
