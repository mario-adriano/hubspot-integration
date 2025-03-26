package com.techcase.hubspot_integration.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HubspotTokenTest {

    @Test
    public void testPrePersist_shouldSetCreatedAtUpdatedAtAndInternalToken() {
        HubspotToken token = new HubspotToken();

        token.prePersist();

        assertNotNull(token.getCreatedAt(), "createdAt deve ser definido");
        assertNotNull(token.getUpdatedAt(), "updatedAt deve ser definido");
        assertNotNull(token.getInternalToken(), "internalToken deve ser gerado");
        assertFalse(token.getInternalToken().isEmpty(), "internalToken não pode ser vazio");
        assertEquals(token.getCreatedAt(), token.getUpdatedAt(), "createdAt e updatedAt devem ser iguais no persist");
    }

    @Test
    public void testPreUpdate_shouldUpdateUpdatedAt() throws InterruptedException {
        HubspotToken token = new HubspotToken();

        LocalDateTime now = LocalDateTime.now();
        token.setCreatedAt(now);
        token.setUpdatedAt(now);

        Thread.sleep(10);

        LocalDateTime previousUpdatedAt = token.getUpdatedAt();

        token.preUpdate();

        assertTrue(token.getUpdatedAt().isAfter(previousUpdatedAt), "updatedAt deve ser atualizado");
        assertTrue(token.getCreatedAt().isEqual(now), "createdAt não deve ser alterado");
    }
}
