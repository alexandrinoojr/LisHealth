package com.integracaolab.app.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.integracaolab.app.ClienteContext;
import com.integracaolab.app.persistence.entity.Entidade;
import com.integracaolab.app.persistence.entity.EntidadeRepository;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);

    @Autowired
    private EntidadeRepository entidadeRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-KEY");

        // 1. Validação de presença da Key
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("API Key ausente na requisição");
            enviarErroJson(response, "Header X-API-KEY ausente");
            return;
        }

        // 2. Validação no Banco de Dados
        Optional<Entidade> entidadeOpt = entidadeRepository.findByApiKey(apiKey);

        if (entidadeOpt.isEmpty() || !entidadeOpt.get().getAtivo()) {
            log.warn("Tentativa de acesso com API Key inválida ou inativa: {}", apiKey);
            enviarErroJson(response, "API Key inválida ou entidade inativa");
            return;
        }

        // 3. Sucesso: Define o contexto e prossegue
        Entidade entidade = entidadeOpt.get();
        ClienteContext.set(entidade);
        log.info("Requisição autenticada. Cliente={}", entidade.getNome());

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Importante: limpa o contexto após a execução para evitar vazamento entre threads
            ClienteContext.clear();
        }
    }

    /**
     * Método para forçar a escrita do JSON no corpo da resposta quando o filtro barra a requisição.
     */
    private void enviarErroJson(HttpServletResponse response, String mensagem) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = String.format("{\"status\": \"error\", \"message\": \"%s\"}", mensagem);

        response.getWriter().write(json);
        response.getWriter().flush();
    }
}