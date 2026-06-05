package com.example.skillora_platform.commerce.service;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class PaymentSignatureUtils {

    private PaymentSignatureUtils() {
    }

    static String hmacSha512(String secret, String data) {
        return hmac("HmacSHA512", secret, data);
    }

    static String hmacSha256(String secret, String data) {
        return hmac("HmacSHA256", secret, data);
    }

    private static String hmac(String algorithm, String secret, String data) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign payment payload", ex);
        }
    }

    static boolean matches(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);
        if (expectedBytes.length != actualBytes.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < expectedBytes.length; i++) {
            diff |= expectedBytes[i] ^ actualBytes[i];
        }
        return diff == 0;
    }
}
