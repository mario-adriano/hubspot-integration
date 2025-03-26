package com.techcase.hubspot_integration.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techcase.hubspot_integration.model.HubspotToken;
import com.techcase.hubspot_integration.repository.HubspotTokenRepository;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.Getter;

@Service
public class HubspotService {

    @Value("${hubspot.clientId}")
    private String clientId;

    @Value("${hubspot.clientSecret}")
    private String clientSecret;

    @Value("${hubspot.redirectUri}")
    private String redirectUri;

    @Value("${hubspot.scope}")
    private String scope;

    @Getter
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final HubspotTokenRepository tokenRepository;

    public HubspotService(HubspotTokenRepository tokenRepository, RestTemplate restTemplate) {
        this.tokenRepository = tokenRepository;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = restTemplate;
    }

    public String buildAuthorizationUrl() {
        return UriComponentsBuilder.fromUriString("https://app.hubspot.com/oauth/authorize")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scope)
                .queryParam("response_type", "code")
                .toUriString();
    }

    public String exchangeCodeForToken(String code) {
        String tokenUrl = "https://api.hubapi.com/oauth/v1/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=authorization_code" +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=" + redirectUri +
                "&code=" + code;

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        HubspotToken token = new HubspotToken();

        try {
            JsonNode node = objectMapper.readTree(response.getBody());
            String accessToken = node.get("access_token").asText();
            String refreshToken = node.get("refresh_token").asText();
            long expiresIn = node.get("expires_in").asLong();

            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
            tokenRepository.save(token);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar token HubSpot", e);
        }

        return token.getInternalToken();
    }

    @RateLimiter(name = "hubspot")
    public ResponseEntity<String> createContact(String token, String contactJson) {
        HubspotToken hubspotToken = tokenRepository.findByInternalToken(token)
                                        .orElseThrow(() -> new RuntimeException("Não encontrou token de acesso"));

        if (LocalDateTime.now().isAfter(hubspotToken.getExpiresAt())) {
            hubspotToken = refreshAccessToken(hubspotToken);
        }

        return createContactInternal(hubspotToken, contactJson, 0);
    }

    private ResponseEntity<String> createContactInternal(HubspotToken token, String contactJson, int retryCount) {
        String url = "https://api.hubapi.com/crm/v3/objects/contacts";
        RestTemplate restTemplate = this.restTemplate;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(contactJson, headers);

        return restTemplate.postForEntity(url, request, String.class);
    }

    public HubspotToken refreshAccessToken(HubspotToken token) {
        String tokenUrl = "https://api.hubapi.com/oauth/v1/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=refresh_token" +
                "&refresh_token=" + token.getRefreshToken() +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret;

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = this.restTemplate;

        ResponseEntity<String> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        try {
            JsonNode node = objectMapper.readTree(response.getBody());
            String newAccessToken = node.get("access_token").asText();
            long expiresIn = node.get("expires_in").asLong();

            token.setAccessToken(newAccessToken);
            token.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));

            return tokenRepository.save(token);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar token de acesso", e);
        }
    }
}
