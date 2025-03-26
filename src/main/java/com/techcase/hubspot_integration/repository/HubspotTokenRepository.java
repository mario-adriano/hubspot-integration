package com.techcase.hubspot_integration.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techcase.hubspot_integration.model.HubspotToken;

@Repository
public interface HubspotTokenRepository extends JpaRepository<HubspotToken, Long> {
    Optional<HubspotToken> findByRefreshToken(String refreshToken);
}
