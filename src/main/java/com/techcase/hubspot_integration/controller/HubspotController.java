package com.techcase.hubspot_integration.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.techcase.hubspot_integration.model.WebhookEvent;
import com.techcase.hubspot_integration.service.HubspotService;
import com.techcase.hubspot_integration.service.WebhookEventService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/hubspot")
public class HubspotController {

    private final HubspotService hubspotService;
    private final WebhookEventService webhookEventService;

    public HubspotController(HubspotService hubspotService, WebhookEventService webhookEventService) {
        this.hubspotService = hubspotService;
        this.webhookEventService = webhookEventService;
    }

    @GetMapping("/authorize")
    public ResponseEntity<String> getAuthorizationUrl() {
        return ResponseEntity.ok(hubspotService.buildAuthorizationUrl());
    }

    @PostMapping("/contacts")
    public ResponseEntity<String> createContact(@RequestHeader("Authorization") String bearerToken,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do contato a ser criado", required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Contato Padrão", value = """
                    {
                    "properties": {
                        "email": "example@example.com",
                        "firstname": "example",
                        "lastname": "example",
                        "phone": "(11) 99999-9999",
                        "company": "TechCase",
                        "website": "techcase.com.br",
                        "lifecyclestage": "lead"
                    }
                    }
                    """))) @RequestBody String contactRequest) {
        return hubspotService.createContact(bearerToken.replace("Bearer ", ""), contactRequest);
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @Parameter(
                description = "Código de autorização retornado pela API do HubSpot",
                example = "authorization-code",
                required = true
            )
            @RequestParam("code") String code) {
        return ResponseEntity.ok(hubspotService.exchangeCodeForToken(code));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        if (webhookEventService.save(payload)) {
            return ResponseEntity.ok("Webhook recebido com sucesso.");
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar o webhook.");
    }

    @GetMapping("/webhook-events")
    public ResponseEntity<Page<WebhookEvent>> getWebhookEvents(
        @RequestHeader("Authorization") String bearerToken,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(webhookEventService.listWebhookEvents(page, size, bearerToken.replace("Bearer ", "")));
    }
}
