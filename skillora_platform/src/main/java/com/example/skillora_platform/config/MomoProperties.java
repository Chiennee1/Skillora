package com.example.skillora_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skillora.payment.momo")
public record MomoProperties(
        String partnerCode,
        String accessKey,
        String secretKey,
        String endpoint,
        String requestType,
        String partnerName,
        String storeId
) {

    private static final String DEFAULT_ENDPOINT = "https://test-payment.momo.vn/v2/gateway/api/create";
    private static final String DEFAULT_REQUEST_TYPE = "payWithMethod";
    private static final String DEFAULT_PARTNER_NAME = "Skillora";
    private static final String DEFAULT_STORE_ID = "SkilloraStore";

    public String resolvedEndpoint() {
        if (endpoint == null || endpoint.isBlank()) {
            return DEFAULT_ENDPOINT;
        }
        return endpoint;
    }

    public String resolvedRequestType() {
        if (requestType == null || requestType.isBlank()) {
            return DEFAULT_REQUEST_TYPE;
        }
        return requestType;
    }

    public String resolvedPartnerName() {
        if (partnerName == null || partnerName.isBlank()) {
            return DEFAULT_PARTNER_NAME;
        }
        return partnerName;
    }

    public String resolvedStoreId() {
        if (storeId == null || storeId.isBlank()) {
            return DEFAULT_STORE_ID;
        }
        return storeId;
    }

    public boolean configured() {
        return partnerCode != null && !partnerCode.isBlank()
                && accessKey != null && !accessKey.isBlank()
                && secretKey != null && !secretKey.isBlank();
    }
}
