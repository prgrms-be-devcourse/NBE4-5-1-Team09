package com.example.cafe.domain.trade.portone.service;

import com.example.cafe.domain.trade.domain.entity.Trade;
import com.example.cafe.domain.trade.domain.entity.TradeStatus;
import com.example.cafe.domain.trade.portone.domain.dto.WebHook;
import com.example.cafe.domain.trade.repository.TradeRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.request.PrepareData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.siot.IamportRestClient.response.Prepare;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PortoneService {
    private final TradeRepository tradeRepository;

    @Value("${imp_key}")
    private String IMP_KEY;

    @Value("${imp_secret}")
    private String IMP_SECRET;

    private IamportClient iamportClient;

    @PostConstruct
    public void initializeImportClient() {
        iamportClient = new IamportClient(IMP_KEY, IMP_SECRET);
    }

    public void prePurchase(String uuid, BigDecimal amount) throws IamportResponseException, IOException {
        log.info("merchantUid = {}", uuid);

        IamportResponse<Prepare> prepareIamportResponse = iamportClient.postPrepare(new PrepareData(uuid, amount));

        log.info("return code : {}", prepareIamportResponse.getCode());
        log.info("return message : {}", prepareIamportResponse.getMessage());

        if (prepareIamportResponse.getCode() != 0) {
            throw new RuntimeException(prepareIamportResponse.getMessage());
        }
    }


    public void refund(Long tradeId, BigDecimal amount) {
        Trade refundTrade = tradeRepository.findById(tradeId).orElseThrow(() -> new RuntimeException("환불 요청에 대한 해당 거래를 찾을 수 없음"));
        String merchantUid = refundTrade.getTradeUUID();
        try {
            CancelData cancelData = new CancelData(merchantUid, false, amount);
            cancelData.setChecksum(new BigDecimal(refundTrade.getTotalPrice()));
            IamportResponse<Payment> iamportResponse = iamportClient.cancelPaymentByImpUid(cancelData);
            log.info("cancel amount : {}", iamportResponse.getResponse().getCancelAmount());
            log.info("original amount : {}", iamportResponse.getResponse().getAmount());
            refundTrade.setTotalPrice(refundTrade.getTotalPrice() - amount.intValue());
        } catch (IamportResponseException e) {
            throw new RuntimeException("port one 결제 취소 실패");
        } catch (IOException e) {
            throw new RuntimeException("취소 하고자 하는 거래 데이터를 찾을 수 없음");
        }
    }

    @Transactional
    public void validateWebHook(WebHook webHook) {
        IamportResponse<Payment> paymentIamportResponse;

        try {
            paymentIamportResponse = iamportClient.paymentByImpUid(webHook.getImp_uid());
        } catch (IamportResponseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Trade findTrade = tradeRepository.findByTradeUUID(paymentIamportResponse.getResponse().getMerchantUid()).orElseThrow(() -> new RuntimeException("WEB HOOK 과 일치하는 거래 내역을 찾을 수 없습니다."));

        if (webHook.getStatus().equals("cancelled")) {
            findTrade.setTradeStatus(TradeStatus.REFUND);
            log.info("response.cancelAmount:{}", paymentIamportResponse.getResponse().getCancelAmount());
            findTrade.setTotalPrice(paymentIamportResponse.getResponse().getAmount().intValue() - paymentIamportResponse.getResponse().getCancelAmount().intValue());
        }

        if (webHook.getStatus().equals("paid")) {
            findTrade.setTradeStatus(TradeStatus.PAY);
            log.info("response.amount:{}", paymentIamportResponse.getResponse().getAmount());
        }

        log.info("web hook 인증 결과 이상 없음.");
    }
}