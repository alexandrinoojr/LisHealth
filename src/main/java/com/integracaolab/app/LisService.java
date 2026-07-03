package com.integracaolab.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.integracaolab.app.api.dto.ResultadoDTO;
import com.integracaolab.app.persistence.entity.*;
import com.integracaolab.app.persistence.repository.ResultadoRepository;
import com.integracaolab.app.persistence.entity.EntidadeRepository;
import com.integracaolab.app.persistence.entity.OrderRepository;    
import com.integracaolab.app.api.dto.LisOrderRequest;

@Service
public class LisService {

    private static final Logger log = LoggerFactory.getLogger(LisService.class);

    private final ResultadoRepository resultadoRepository;
    private final EntidadeRepository entidadeRepository;
    private final OrderRepository orderRepository;

    public LisService(ResultadoRepository resultadoRepository,
                      EntidadeRepository entidadeRepository,
                      OrderRepository orderRepository) {
        this.resultadoRepository = resultadoRepository;
        this.entidadeRepository = entidadeRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void processOrder(LisOrder order, String apiKey, LisOrderRequest request) {
        if (order.getSampleId() == null || order.getSampleId().isBlank()) {
            throw new IllegalArgumentException("code_bar (sampleId) é obrigatório");
        }

        Entidade entidade = entidadeRepository
                .findByApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("Entidade não encontrada para API Key"));

        OrderEntity entity = new OrderEntity();
        entity.setEntidade(entidade);
        entity.setSampleId(order.getSampleId());
        entity.setPatientName(request.getPatientName());
        entity.setPatientBirthDate(request.getPatientBirthDate());
        entity.setPatientGender(request.getPatientGender());
        entity.setPatientCpf(request.getPatientCpf());
        entity.setSampleType(request.getSampleType());
        entity.setExamsRaw(String.join(",", request.getTests()));
        entity.setCreatedAt(LocalDateTime.now());

        orderRepository.save(entity);
    }

    @Transactional
    public void processResultados(List<ResultadoDTO> resultados, Entidade entidade) {
        for (ResultadoDTO dto : resultados) {
            try {
    
                ResultadoEntity entity = new ResultadoEntity();
                entity.setEntidade(entidade); // Preenche ent_cod
                entity.setSampleId(dto.getSampleId()); // Preenche sample_id
                entity.setExame(dto.getExame()); // Preenche exame
                entity.setValor(dto.getValor());
                entity.setUnidade(dto.getUnidade());
                
                LocalDateTime agora = LocalDateTime.now();
                entity.setDataResultado(agora);
                entity.setCreatedAt(agora);

                resultadoRepository.save(entity);
                resultadoRepository.flush(); // FORÇA a inserção agora para logar erro se houver
                
                log.info("Sucesso Local: {} - {}", dto.getSampleId(), dto.getExame());

                try {
                    atualizarBancoCliente(dto, entidade);
                } catch (Exception e) {
                    log.error("Falha ao atualizar banco EXTERNO para {}: {}", dto.getSampleId(), e.getMessage());
                }

            } catch (Exception e) {
                log.error("ERRO CRÍTICO ao salvar no banco LOCAL: {}", e.getMessage());
            }
        }
    }

    private void atualizarBancoCliente(ResultadoDTO dto, Entidade entidade) {
        String sql = """
           Query de atualização no banco de dados de destino
        """;

        try (Connection conn = criarConexaoCliente(entidade);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setQueryTimeout(5); // Define timeout de 5s para não travar a aplicação
            ps.setString(1, dto.getValor());
            ps.setString(2, dto.getSampleId());
            ps.setString(3, dto.getExame());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                log.info("Banco Externo atualizado: {} - {}", dto.getSampleId(), dto.getExame());
            } else {
                log.warn("Nenhum registro encontrado no cliente para: {} - {}", dto.getSampleId(), dto.getExame());
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Connection criarConexaoCliente(Entidade entidade) throws Exception {
        if (entidade.getDbUsername() == null || entidade.getDbUsername().isBlank()) {
            return DriverManager.getConnection(entidade.getDbUrl());
        }
        return DriverManager.getConnection(
                entidade.getDbUrl(),
                entidade.getDbUsername(),
                entidade.getDbPassword());
    }
}