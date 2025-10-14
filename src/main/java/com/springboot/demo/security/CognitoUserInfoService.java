package com.springboot.demo.security;

import com.springboot.demo.config.CognitoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CognitoUserInfoService {

    private final CognitoProperties props;
    private final WebClient webClient = WebClient.builder().build();

    public Map<String, Object> fetch(String accessToken) {
        return webClient.get()
                .uri(props.getUserinfoUrl())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        resp -> resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new IllegalStateException(
                                        "Cognito userinfo error: " + resp.statusCode() + " " + body)))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(5));
    }
}
