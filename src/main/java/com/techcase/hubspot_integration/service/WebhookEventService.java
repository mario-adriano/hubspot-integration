package com.techcase.hubspot_integration.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techcase.hubspot_integration.model.HubspotToken;
import com.techcase.hubspot_integration.model.WebhookEvent;
import com.techcase.hubspot_integration.repository.HubspotTokenRepository;
import com.techcase.hubspot_integration.repository.WebhookEventRepository;

@Service
public class WebhookEventService {
    private final WebhookEventRepository webhookEventRepository;
    private final HubspotTokenRepository hubspotTokenRepository;
    private final HubspotService hubspotService;
    private final ObjectMapper mapper = new ObjectMapper();

    public WebhookEventService(
                WebhookEventRepository webhookEventRepository,
                HubspotTokenRepository hubspotTokenRepository,
                HubspotService hubspotService) {
        this.hubspotService = hubspotService;
        this.hubspotTokenRepository = hubspotTokenRepository;
        this.webhookEventRepository = webhookEventRepository;
    }

    public boolean save(String payload) {
        try {
            JsonNode jsonNode = mapper.readTree(payload);
            WebhookEvent webhookEvent = new WebhookEvent();
            webhookEvent.setPayload(jsonNode);
            webhookEventRepository.save(webhookEvent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Page<WebhookEvent> listWebhookEvents(int page, int size, String token) {
        HubspotToken hubspotToken = hubspotTokenRepository.findByInternalToken(token)
                                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido ou expirado"));
        if (LocalDateTime.now().isAfter(hubspotToken.getExpiresAt())) {
            hubspotService.refreshAccessToken(hubspotToken);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return webhookEventRepository.findAll(pageable);
    }
}
