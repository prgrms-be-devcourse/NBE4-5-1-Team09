// "use client";

// import React, { useState, useEffect } from "react";
// import { useRouter } from "next/navigation";
// import { Form, Select, Input, Switch, Button } from "antd";
// import styled from "styled-components";
// import queryString from "query-string";

// import {
//   PGS,
//   METHODS_FOR_INICIS,
//   QUOTAS_FOR_INICIS_AND_KCP,
// } from "./constants";
// import { getMethods, getQuotas } from "./utils";

// const { Option } = Select;

// const PaymentPage: React.FC = () => {
//   const router = useRouter();
//   const [form] = Form.useForm();

//   // 상태값
//   const [methods, setMethods] = useState(METHODS_FOR_INICIS);
//   const [quotas, setQuotas] = useState(QUOTAS_FOR_INICIS_AND_KCP);
//   const [isQuotaRequired, setIsQuotaRequired] = useState(true);
//   const [isDigitalRequired, setIsDigitalRequired] = useState(false);
//   const [isVbankDueRequired, setIsVbankDueRequired] = useState(false);
//   const [isBizNumRequired, setIsBizNumRequired] = useState(false);

//   // userAgent 체크 (React Native 환경 가정)
//   const [userAgent, setUserAgent] = useState("");

//   useEffect(() => {
//     if (typeof window !== "undefined") {
//       setUserAgent(navigator.userAgent);
//     }
//   }, []);

//   const isReactNative = () => {
//     return /Android|iPhone|iPad/i.test(userAgent);
//   };

//   // PG 변경
//   const onChangePg = (value: string) => {
//     const newMethods = getMethods(value);
//     setMethods(newMethods);
//     form.setFieldsValue({ pay_method: newMethods[0].value });

//     const { pay_method } = form.getFieldsValue();
//     handleQuotas(value, pay_method);

//     let bizRequired = false;
//     let vbankRequired = false;
//     if (pay_method === "vbank") {
//       if (value === "danal_tpay") {
//         bizRequired = true;
//       }
//       vbankRequired = true;
//     }
//     setIsBizNumRequired(bizRequired);
//     setIsVbankDueRequired(vbankRequired);
//   };

//   // 결제수단 변경
//   const onChangePayMethod = (value: string) => {
//     const { pg } = form.getFieldsValue();
//     let quotaRequired = false;
//     let digitalRequired = false;
//     let vbankRequired = false;
//     let bizRequired = false;

//     switch (value) {
//       case "card":
//         quotaRequired = true;
//         break;
//       case "phone":
//         digitalRequired = true;
//         break;
//       case "vbank":
//         if (pg === "danal_tpay") {
//           bizRequired = true;
//         }
//         vbankRequired = true;
//         break;
//       default:
//         break;
//     }
//     setIsQuotaRequired(quotaRequired);
//     setIsDigitalRequired(digitalRequired);
//     setIsVbankDueRequired(vbankRequired);
//     setIsBizNumRequired(bizRequired);

//     handleQuotas(pg, value);
//   };

//   // 할부개월수 설정
//   const handleQuotas = (pg: string, pay_method: string) => {
//     const { isQuotaRequired, quotas } = getQuotas(pg, pay_method);
//     setIsQuotaRequired(isQuotaRequired);
//     setQuotas(quotas);
//     form.setFieldsValue({ card_quota: quotas[0].value });
//   };

//   // 폼 제출
//   const onFinish = (values: any) => {
//     const userCode = process.env.NEXT_PUBLIC_USER_CODE; // 아임포트 식별코드
//     if (!userCode) {
//       alert("아임포트 식별코드가 설정되지 않았습니다.");
//       return;
//     }

//     const {
//       pg,
//       pay_method,
//       merchant_uid,
//       name,
//       amount,
//       buyer_name,
//       buyer_tel,
//       buyer_email,
//       escrow,
//       card_quota,
//       biz_num,
//       vbank_due,
//       digital,
//     } = values;

//     const data: any = {
//       pg,
//       pay_method,
//       merchant_uid,
//       name,
//       amount,
//       buyer_name,
//       buyer_tel,
//       buyer_email,
//       escrow,
//     };

//     if (pay_method === "vbank") {
//       data.vbank_due = vbank_due;
//       if (pg === "danal_tpay") {
//         data.biz_num = biz_num;
//       }
//     }
//     if (pay_method === "card") {
//       if (card_quota !== 0) {
//         data.digital = { card_quota: card_quota === 1 ? [] : card_quota };
//       }
//     }
//     if (pay_method === "phone") {
//       data.digital = digital;
//     }

//     if (isReactNative()) {
//       // (예) React Native 환경
//       const params = { userCode, data, type: "payment" };
//       (window as any).ReactNativeWebView?.postMessage(JSON.stringify(params));
//     } else {
//       // 웹 환경
//       const { IMP } = window as any;
//       IMP.init(userCode);
//       IMP.request_pay(data, (response: any) => {
//         // 결제 완료 후 result 페이지로 이동
//         const query = queryString.stringify(response);
//         router.push(`/payment/result?${query}`);
//       });
//     }
//   };

//   return (
//     <Wrapper>
//       <Header>아임포트 결제 테스트</Header>
//       <FormContainer
//         form={form}
//         layout="vertical"
//         onFinish={onFinish}
//         initialValues={{
//           pg: "html5_inicis",
//           pay_method: "card",
//           name: "아임포트 결제 데이터 분석",
//           amount: "39000",
//           merchant_uid: "",
//           buyer_name: "홍길동",
//           buyer_tel: "01012341234",
//           buyer_email: "example@example.com",
//           escrow: false,
//           card_quota: 0,
//         }}
//       >
//         <Form.Item label="PG사" name="pg">
//           <Select onChange={onChangePg}>
//             {PGS.map((pgItem) => (
//               <Option value={pgItem.value} key={pgItem.value}>
//                 {pgItem.label}
//               </Option>
//             ))}
//           </Select>
//         </Form.Item>

//         <Form.Item label="결제수단" name="pay_method">
//           <Select onChange={onChangePayMethod}>
//             {methods.map((method) => (
//               <Option value={method.value} key={method.value}>
//                 {method.label}
//               </Option>
//             ))}
//           </Select>
//         </Form.Item>

//         {isQuotaRequired && (
//           <Form.Item label="할부개월수" name="card_quota">
//             <Select>
//               {quotas.map((quota) => (
//                 <Option value={quota.value} key={quota.value}>
//                   {quota.label}
//                 </Option>
//               ))}
//             </Select>
//           </Form.Item>
//         )}

//         {isVbankDueRequired && (
//           <Form.Item
//             label="입금기한"
//             name="vbank_due"
//             rules={[{ required: true, message: "입금기한은 필수입력입니다" }]}
//           >
//             <Input placeholder="YYYYMMDDhhmm" />
//           </Form.Item>
//         )}

//         {isBizNumRequired && (
//           <Form.Item
//             label="사업자번호"
//             name="biz_num"
//             rules={[{ required: true, message: "사업자번호는 필수입력입니다" }]}
//           >
//             <Input />
//           </Form.Item>
//         )}

//         {isDigitalRequired && (
//           <Form.Item label="실물여부" name="digital" valuePropName="checked">
//             <Switch />
//           </Form.Item>
//         )}

//         <Form.Item label="에스크로" name="escrow" valuePropName="checked">
//           <Switch />
//         </Form.Item>

//         <Form.Item
//           label="주문명"
//           name="name"
//           rules={[{ required: true, message: "주문명은 필수입력입니다" }]}
//         >
//           <Input />
//         </Form.Item>

//         <Form.Item
//           label="결제금액"
//           name="amount"
//           rules={[{ required: true, message: "결제금액은 필수입력입니다" }]}
//         >
//           <Input type="number" />
//         </Form.Item>

//         <Form.Item
//           label="주문번호"
//           name="merchant_uid"
//           rules={[{ required: true, message: "주문번호는 필수입력입니다" }]}
//         >
//           <Input placeholder="주문번호를 입력해주세요" />
//         </Form.Item>

//         <Form.Item
//           label="이름"
//           name="buyer_name"
//           rules={[{ required: true, message: "구매자 이름은 필수입력입니다" }]}
//         >
//           <Input />
//         </Form.Item>

//         <Form.Item
//           label="전화번호"
//           name="buyer_tel"
//           rules={[
//             { required: true, message: "구매자 전화번호는 필수입력입니다" },
//           ]}
//         >
//           <Input type="number" />
//         </Form.Item>

//         <Form.Item
//           label="이메일"
//           name="buyer_email"
//           rules={[
//             { required: true, message: "구매자 이메일은 필수입력입니다" },
//           ]}
//         >
//           <Input />
//         </Form.Item>

//         <Form.Item>
//           <Button type="primary" htmlType="submit" size="large">
//             결제하기
//           </Button>
//         </Form.Item>
//       </FormContainer>
//     </Wrapper>
//   );
// };

// export default PaymentPage;

// // ---------- styled-components ----------
// const Wrapper = styled.div`
//   padding: 5rem 0;
//   display: flex;
//   align-items: center;
//   justify-content: center;
//   flex-direction: column;
// `;

// const Header = styled.h1`
//   font-weight: bold;
//   text-align: center;
//   padding: 2rem;
//   padding-top: 0;
//   font-size: 3rem;
// `;

// const FormContainer = styled(Form)`
//   width: 350px;
//   border-radius: 3px;

//   .ant-form-item {
//     margin-bottom: 1rem;
//   }
//   button[type="submit"] {
//     width: 100%;
//     height: 3rem;
//     font-size: 1.2rem;
//     margin-top: 2rem;
//   }
// `;
"use client";

import React, { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import queryString from "query-string";

export default function PaymentPage() {
  const router = useRouter();
  const searchParams = useSearchParams();

  // URL 파라미터에서 tradeUUID, totalPrice 추출
  const tradeUUID = searchParams.get("tradeUUID") || "defaultTradeUUID";
  const totalPrice = searchParams.get("totalPrice") || "0";

  useEffect(() => {
    // 아임포트 가맹점 식별코드 (NEXT_PUBLIC_ 접두어 사용)
    const userCode = process.env.NEXT_PUBLIC_USER_CODE;
    if (!userCode) {
      alert("아임포트 식별코드가 설정되지 않았습니다.");
      return;
    }

    // IMP 객체 준비
    const { IMP } = window as any;
    IMP.init(userCode);

    // 결제 요청에 필요한 데이터
    const data = {
      pg: "kakaopay", // 신 - 카카오페이
      pay_method: "card", // 카드 결제로 가정
      merchant_uid: tradeUUID, // 주문번호
      name: "결제 테스트 상품", // 상품명(원하는 값으로 설정)
      amount: parseInt(totalPrice), // 결제금액
      buyer_name: "홍길동", // 예시용
      buyer_tel: "01012341234", // 예시용
      buyer_email: "example@example.com", // 예시용
    };

    // 결제창 호출
    IMP.request_pay(data, (rsp: any) => {
      // 결제 완료(또는 실패) 후 응답값을 쿼리스트링으로 변환
      const query = queryString.stringify(rsp);
      // 결제 결과 페이지로 이동
      router.push(`/payment/result?${query}`);
    });
  }, [router, searchParams]);

  return (
    <div style={{ padding: "3rem", textAlign: "center" }}>
      <h1>결제 요청 중...</h1>
      <p>잠시만 기다려 주세요.</p>
    </div>
  );
}
