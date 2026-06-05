package com.example.skillora_platform.commerce.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.skillora_platform.config.MomoProperties;
import com.example.skillora_platform.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DefaultMomoClient implements MomoClient {

    private final RestClient.Builder restClientBuilder;
    private final MomoProperties momoProperties;

    @Override
    public MomoCreatePaymentResult createPayment(MomoCreatePaymentPayload payload) {
        try {
            return restClientBuilder.build()
                    .post()
                    .uri(momoProperties.resolvedEndpoint())
                    .body(payload)
                    .retrieve()
                    .body(MomoCreatePaymentResult.class);
        } catch (Exception ex) {
            throw new BusinessException("MoMo payment gateway request failed", HttpStatus.BAD_GATEWAY);
        }
    }
}
