package com.techcase.hubspot_integration.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.techcase.hubspot_integration.service.HubspotService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

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
        System.out.println(payload);
        return ResponseEntity.ok("Webhook recebido com sucesso.");
    }
}
