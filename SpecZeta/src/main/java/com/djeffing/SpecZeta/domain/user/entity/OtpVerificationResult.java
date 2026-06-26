package com.djeffing.SpecZeta.domain.user.entity;

import com.djeffing.SpecZeta.domain.user.dto.AuthResponse;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
@Data
public class OtpVerificationResult {
    private final boolean success;
    private final String errorMessage;

    //private
    private final AuthResponse authResponse;
    public static OtpVerificationResult success(AuthResponse authResponse) {
        return new OtpVerificationResult(true, "", authResponse);
    }

    public static OtpVerificationResult failure(String message) {
        return new OtpVerificationResult(false, message, null);
    }
}
