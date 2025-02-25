"use client";

import React from "react";
import { useSearchParams, useRouter } from "next/navigation";
import styled from "styled-components";
import { Button } from "antd";
import {
  CheckCircleFilled,
  ExclamationCircleFilled,
  ArrowLeftOutlined,
} from "@ant-design/icons";

const PaymentResultPage: React.FC = () => {
  const router = useRouter();
  // useSearchParams로 쿼리 파라미터 접근
  const searchParams = useSearchParams();

  // 아임포트 응답에서 전달된 쿼리 파라미터들
  const merchant_uid = searchParams.get("merchant_uid");
  const error_msg = searchParams.get("error_msg");
  const imp_uid = searchParams.get("imp_uid");
  const imp_success = searchParams.get("imp_success");
  const success = searchParams.get("success");

  // 성공 여부 판별
  const successStr = imp_success || success;
  const isSuccessed = successStr === "true";

  return (
    <Wrapper>
      <Container colorType={isSuccessed ? "#52c41a" : "#f5222d"}>
        {isSuccessed ? <CheckCircleFilled /> : <ExclamationCircleFilled />}
        <p>{`결제에 ${isSuccessed ? "성공" : "실패"}하였습니다`}</p>
        <ul>
          <li>
            <span>주문번호</span>
            <span>{merchant_uid}</span>
          </li>
          {isSuccessed ? (
            <li>
              <span>아임포트 번호</span>
              <span>{imp_uid}</span>
            </li>
          ) : (
            <li>
              <span>에러 메시지</span>
              <span>{error_msg}</span>
            </li>
          )}
        </ul>
        <Button size="large" onClick={() => router.push("/")}>
          <ArrowLeftOutlined />
          돌아가기
        </Button>
      </Container>
    </Wrapper>
  );
};

export default PaymentResultPage;

// ---------- styled-components ----------
const Wrapper = styled.div`
  position: relative;
  width: 100%;
  min-height: 100vh;
`;

const Container = styled.div<{ colorType: string }>`
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  background-color: #fff;
  border-radius: 4px;
  margin: 2rem;
  padding: 2rem;

  > svg {
    font-size: 6rem;
    margin-bottom: 2rem;
    color: ${(props) => props.colorType};
  }
  p {
    font-size: 2rem;
    font-weight: bold;
    margin-bottom: 2rem;
  }
  ul {
    list-style: none;
    padding: 0;
    margin-bottom: 3rem;
    li {
      display: flex;
      line-height: 2;
      span:first-child {
        width: 8rem;
        color: #888;
      }
      span:last-child {
        width: calc(100% - 8rem);
        color: #333;
      }
    }
  }
  button {
    border-color: ${(props) => props.colorType};
    color: ${(props) => props.colorType};
  }
  button:hover {
    opacity: 0.7;
  }
`;
