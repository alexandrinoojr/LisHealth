package com.integracaolab.app.persistence.entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EntidadeRepository extends JpaRepository<Entidade, Long> {

    Optional<Entidade> findByApiKey(String apiKey);
}
