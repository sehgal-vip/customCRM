package com.turno.crm.repository;

import com.turno.crm.model.entity.WebhookApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebhookApiKeyRepository extends JpaRepository<WebhookApiKey, Long> {

    Optional<WebhookApiKey> findByKeyHashAndActiveTrue(String keyHash);
}
