package com.techcase.hubspot_integration.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.techcase.hubspot_integration.service.HubspotService;

@RestController
@RequestMapping("/hubspot")
public class HubspotController {

    private final HubspotService hubspotService;

    public HubspotController(HubspotService hubspotService) {
        this.hubspotService = hubspotService;
    }

    @GetMapping("/authorize")
    public ResponseEntity<String> getAuthorizationUrl() {
        return ResponseEntity.ok(hubspotService.buildAuthorizationUrl());
    }

    @PostMapping("/contacts")
    public ResponseEntity<String> createContact(@RequestHeader("Authorization") String bearerToken,
                                                @RequestBody String contactRequest) {
        return hubspotService.createContact(bearerToken.replace("Bearer ", ""), contactRequest);
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam("code") String code) {
        return ResponseEntity.ok(hubspotService.exchangeCodeForToken(code));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        System.out.println(payload);
        return ResponseEntity.ok("Webhook recebido com sucesso.");
    }
}

