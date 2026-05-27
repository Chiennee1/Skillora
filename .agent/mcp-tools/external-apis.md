# MCP Tools: External APIs

> External API integration tools for Skillora.

## Google Gemini AI (AI Studio)

### Configuration
```yaml
gemini:
  api-key: ${GEMINI_API_KEY}
  model: gemini-2.5-flash
  base-url: https://generativelanguage.googleapis.com/v1beta
```

### Rate Limits (Free Tier)
| Constraint | Limit |
|-----------|-------|
| Requests Per Day (RPD) | 1,500 |
| Requests Per Minute (RPM) | 10-15 |
| Tokens Per Minute (TPM) | 1,000,000 |

### API Call Pattern
```java
WebClient.builder()
    .baseUrl(geminiConfig.getBaseUrl())
    .build()
    .post()
    .uri("/models/{model}:generateContent?key={apiKey}", model, apiKey)
    .bodyValue(requestBody)
    .retrieve()
    .bodyToMono(GeminiResponse.class);
```

## VNPay (Primary Payment)

### Configuration
```yaml
vnpay:
  tmn-code: ${VNPAY_TMN_CODE}
  hash-secret: ${VNPAY_HASH_SECRET}
  url: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
  api-url: https://sandbox.vnpayment.vn/merchant_webapi/api/transaction
  return-url: ${APP_URL}/api/v1/payments/vnpay/return
```

### Integration Flow
1. Create order in DB → generate unique `vnp_TxnRef`
2. Build VNPay URL with HMAC-SHA512 signature
3. Redirect user to VNPay payment page
4. Handle return URL (verify signature) → update order status
5. Handle IPN callback (server-to-server) → confirm payment

## MoMo (Secondary Payment)

### Configuration
```yaml
momo:
  partner-code: ${MOMO_PARTNER_CODE}
  access-key: ${MOMO_ACCESS_KEY}
  secret-key: ${MOMO_SECRET_KEY}
  endpoint: https://test-payment.momo.vn/v2/gateway/api/create
  ipn-url: ${APP_URL}/api/v1/payments/momo/ipn
  redirect-url: ${FRONTEND_URL}/payment/result
```

## Cloudinary (Image Upload)

### Configuration
```yaml
cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME}
  api-key: ${CLOUDINARY_API_KEY}
  api-secret: ${CLOUDINARY_API_SECRET}
```

### Usage
- Course thumbnails
- User avatars
- Lesson resource images

## Bunny Stream (Video Hosting)

### Configuration
```yaml
bunny:
  api-key: ${BUNNY_API_KEY}
  library-id: ${BUNNY_LIBRARY_ID}
  hostname: ${BUNNY_CDN_HOSTNAME}
```

### Integration Flow
1. Frontend requests presigned upload URL from backend
2. Frontend uploads video directly to Bunny Stream
3. Bunny processes video (HLS transcoding)
4. Backend stores `videoId` in `lesson_videos.asset_id`
5. Playback via signed token URL
