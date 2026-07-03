package com.integracaolab.app.api.controller;

import com.integracaolab.app.LisOrder;
import com.integracaolab.app.LisService;
import com.integracaolab.app.api.dto.LisOrderRequest;
import com.integracaolab.app.persistence.entity.Entidade;
import com.integracaolab.app.ClienteContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class LisOrderController {

    private final LisService lisService;

    public LisOrderController(LisService lisService) {
        this.lisService = lisService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestBody LisOrderRequest request) {

        Map<String, Object> response = new LinkedHashMap<>();
        
        // Recupera a entidade já validada pelo Filtro através do Contexto
        Entidade entidade = ClienteContext.get();

        try {
            // Mapeamento dos dados da requisição para o objeto de ordem
            LisOrder order = new LisOrder();
            order.setSampleId(request.getCodeBar());
            order.setPatientName(request.getPatientName());
            order.setPatientBirthDate(request.getPatientBirthDate());
            order.setPatientGender(request.getPatientGender());
            order.setPatientCpf(request.getPatientCpf());
            order.setSampleType(request.getSampleType());
            order.setExams(request.getTests());

            // Processamento da ordem (envio para o banco/socket)
            lisService.processOrder(order, apiKey, request);

            // Retorno de sucesso formatado
            response.put("status", "success");
            response.put("message", "Ordem processada com sucesso");
            response.put("codeBar", request.getCodeBar());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            // Captura falhas de processamento interno
            response.put("status", "error");
            response.put("message", "Erro ao processar ordem: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}