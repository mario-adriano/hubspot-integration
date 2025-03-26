package com.techcase.hubspot_integration.service;

import com.techcase.hubspot_integration.model.HubspotToken;
import com.techcase.hubspot_integration.repository.HubspotTokenRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class HubspotServiceTest {

    @Mock
    private HubspotTokenRepository tokenRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private HubspotService hubspotService;

    private HubspotToken token;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        hubspotService = new HubspotService(tokenRepository, restTemplate);
    }

    @Test
    public void testCreateContact_withValidToken_shouldSucceed() {
        String internalToken = "internal-token";
        String contactJson = "{\"email\": \"test@example.com\"}";

        token = new HubspotToken();
        token.setInternalToken(internalToken);
        token.setAccessToken("valid-access-token");
        token.setRefreshToken("refresh-token");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByInternalToken(internalToken)).thenReturn(Optional.of(token));

        ResponseEntity<String> mockResponse = new ResponseEntity<>("ok", HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);

        ResponseEntity<String> response = hubspotService.createContact(internalToken, contactJson);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ok", response.getBody());
    }

    @Test
    public void testCreateContact_withExpiredToken_shouldRefreshAndSucceed() throws Exception {
        String internalToken = "internal-token";
        String contactJson = "{\"email\": \"test@example.com\"}";

        token = new HubspotToken();
        token.setInternalToken(internalToken);
        token.setAccessToken("expired-access-token");
        token.setRefreshToken("refresh-token");
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(tokenRepository.findByInternalToken(internalToken)).thenReturn(Optional.of(token));

        String refreshResponse = "{\"access_token\":\"new-token\",\"expires_in\":1800}";
        ResponseEntity<String> refreshEntity = new ResponseEntity<>(refreshResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(refreshEntity);

        when(tokenRepository.save(any(HubspotToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<String> contactResponse = new ResponseEntity<>("created", HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(contactResponse);

        ResponseEntity<String> response = hubspotService.createContact(internalToken, contactJson);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("created", response.getBody());

        verify(restTemplate).postForEntity(eq("https://api.hubapi.com/crm/v3/objects/contacts"), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testExchangeCodeForToken_shouldReturnInternalToken() throws Exception {
        String code = "auth-code";
        String mockApiResponse = "{\"access_token\":\"acc123\",\"refresh_token\":\"ref123\",\"expires_in\":3600}";

        ResponseEntity<String> response = new ResponseEntity<>(mockApiResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        ArgumentCaptor<HubspotToken> captor = ArgumentCaptor.forClass(HubspotToken.class);
        when(tokenRepository.save(captor.capture())).thenAnswer(invocation -> {
            HubspotToken token = invocation.getArgument(0);
            token.prePersist();
            return token;
        });

        String internalToken = hubspotService.exchangeCodeForToken(code);

        assertNotNull(internalToken);
        assertEquals("acc123", captor.getValue().getAccessToken());
        assertEquals("ref123", captor.getValue().getRefreshToken());
    }
}

