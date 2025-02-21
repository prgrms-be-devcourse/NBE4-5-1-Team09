package com.example.cafe.domain.trade.portone.controller;

import com.example.cafe.domain.trade.portone.domain.dto.WebHook;
import com.example.cafe.domain.trade.portone.service.PortoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/portone")
@RequiredArgsConstructor
public class PortoneController {

    private final PortoneService portoneService;

    @PostMapping("/webhook")
    public void webhook(@RequestBody WebHook webHook) {
        log.info("received : {}, {}, {}, {}", webHook.getImp_uid(), webHook.getMerchant_uid(), webHook.getStatus(), webHook.getCancellation_id());
        portoneService.validateWebHook(webHook);
    }

}
