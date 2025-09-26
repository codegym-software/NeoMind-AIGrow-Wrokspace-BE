package com.springboot.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordResponse;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final CognitoIdentityProviderClient cognito;

    public void changePassword(Jwt jwt, String currentPassword, String newPassword, String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Missing accessToken");
        }
        ChangePasswordRequest req = ChangePasswordRequest.builder()
                .previousPassword(currentPassword)
                .proposedPassword(newPassword)
                .accessToken(accessToken)
                .build();
        ChangePasswordResponse resp = cognito.changePassword(req);
    }
}
