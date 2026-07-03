package com.integracaolab.app;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.integracaolab.app.api.dto.ResultadoDTO;
import com.integracaolab.app.persistence.entity.Entidade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LisConnectionHandler implements Runnable {

    private final Socket socket;
    private final LisService lisService;
    private final Entidade entidade;
    private static final Logger log = LoggerFactory.getLogger(LisConnectionHandler.class);

    public LisConnectionHandler(Socket socket, LisService lisService, Entidade entidade) {
        this.socket = socket;
        this.lisService = lisService;
        this.entidade = entidade;
    }

    @Override
    public void run() {
        ClienteContext.set(entidade); 

        try {
            // Configurações de resiliência da conexão TCP
            socket.setKeepAlive(true); 
            socket.setSoTimeout(0);    
            
            // AJUSTE CRUCIAL: O Dirui envia em UNICODE (UTF-16 Little Endian).
            // O BufferedReader com InputStreamReader garante que os 2 bytes sejam lidos como 1 char.
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_16LE)
            );
            OutputStream out = socket.getOutputStream();
            
            LisMessageBuffer buffer = new LisMessageBuffer();
            StringBuilder sb = new StringBuilder();
            int b;

            log.info("Conexão ativa com {}. Lendo em modo UNICODE (UTF-16LE)...", entidade.getNome());

            while ((b = reader.read()) != -1) {
                // Fim do Bloco MLLP <EB> (ASCII 28 / 0x1C)
                if (b == 0x1C) { 
                    // 1. Resposta IMEDIATA: Envia o ACK (0x06)
                    out.write(0x06); 
                    out.flush();
                    log.info("Sinal ACK enviado para {}.", entidade.getNome());

                    // 2. Descarrega o conteúdo pendente para o buffer
                    if (sb.length() > 0) {
                        buffer.addLine(sb.toString());
                        sb.setLength(0);
                    }
                    
                    List<String> msgParaProcessar = buffer.getMessage();
                    
                    if (!msgParaProcessar.isEmpty()) {
                        try {
                            LisMessageParser parser = new LisMessageParser(msgParaProcessar);
                            String sampleId = parser.parse().getSampleId();
                            List<ResultadoDTO> resultados = parser.parseResultados(sampleId);
                            
                            if (!resultados.isEmpty()) {
                                log.info("Sucesso: {} exames para SampleId: {}", resultados.size(), sampleId);
                                lisService.processResultados(resultados, entidade);
                            } else {
                                // Log de Debug caso o parser falhe mesmo com o encoding certo
                                log.warn("Parser vazio após leitura UNICODE. Conteúdo bruto:");
                                for (int i = 0; i < msgParaProcessar.size(); i++) {
                                    log.warn("Linha {}: [{}]", i, msgParaProcessar.get(i));
                                }
                            }
                        } catch (Exception ex) {
                            log.error("Erro no processamento HL7: {}", ex.getMessage());
                        }
                    }
                    buffer.clear();
                } 
                // Separador de segmentos <CR> (0x0D)
                else if (b == 0x0D) { 
                    if (sb.length() > 0) {
                        buffer.addLine(sb.toString());
                        sb.setLength(0);
                    }
                } 
                // Filtro de caracteres de controle e quebras de linha puras (\n)
                // Ignora também o byte nulo (0x00) se ele aparecer solto
                else if (b != 0x0B && b != 0x0A && b != 0x00) {
                    sb.append((char) b);
                }
            }
        } catch (Exception e) {
            log.error("Conexão encerrada com {}: {}", entidade.getNome(), e.getMessage());
        } finally {
            ClienteContext.clear();
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}