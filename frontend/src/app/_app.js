// 예: _app.tsx
import "../styles/globals.css";
import type { AppProps } from "next/app";
import Script from "next/script";

function MyApp({ Component, pageProps }: AppProps) {
  return (
    <>
      {/* 포트원 스크립트를 beforeInteractive 전략으로 로드 */}
      <Script
        src="https://cdn.portone.co.kr/portone-sdk.js"
        strategy="beforeInteractive"
      />
      <Component {...pageProps} />
    </>
  );
}

export default MyApp;
