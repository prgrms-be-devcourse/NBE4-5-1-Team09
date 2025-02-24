import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  swcMinify: true,
  async rewrites() {
    return [
      {
        source: "/member/:path*",
        destination: "http://localhost:8080/member/:path*", // 백엔드 서버 주소
      },
    ];
  },
};

export default nextConfig;
