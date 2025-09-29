package com.springboot.demo.service.impl;

import com.springboot.demo.service.AccountService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordRequest;

@Service
public class AccountServiceImpl implements AccountService {

    private final CognitoIdentityProviderClient cognito;

    public AccountServiceImpl(CognitoIdentityProviderClient cognito) {
        this.cognito = cognito;
    }

    @Override
    public void changePassword(Jwt jwt, String currentPassword, String newPassword, String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Missing accessToken");
        }
        ChangePasswordRequest req = ChangePasswordRequest.builder()
                .previousPassword(currentPassword)
                .proposedPassword(newPassword)
                .accessToken(accessToken)
                .build();
        cognito.changePassword(req);
    }
}
