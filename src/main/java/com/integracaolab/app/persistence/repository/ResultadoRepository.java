package com.integracaolab.app.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.integracaolab.app.persistence.entity.ResultadoEntity;

public interface ResultadoRepository
        extends JpaRepository<ResultadoEntity, Long> {
}
