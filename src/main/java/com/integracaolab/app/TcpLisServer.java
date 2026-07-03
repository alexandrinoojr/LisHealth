package com.integracaolab.app;

import com.integracaolab.app.persistence.entity.Entidade;
import com.integracaolab.app.persistence.entity.EntidadeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

@Component
public class TcpLisServer {

    private final LisService lisService;
    private final EntidadeRepository entidadeRepository;
    private static final Logger log = LoggerFactory.getLogger(TcpLisServer.class);

    public TcpLisServer(LisService lisService, EntidadeRepository entidadeRepository) {
        this.lisService = lisService;
        this.entidadeRepository = entidadeRepository;
    }

    public void start() {
        List<Entidade> entidades = entidadeRepository.findAll();
        
        log.info("Iniciando servidores TCP multiclient...");

        for (Entidade entidade : entidades) {
            if (entidade.getAtivo() && entidade.getPorta() != null) {
                new Thread(() -> startListener(entidade)).start();
            }
        }
    }

    private void startListener(Entidade entidade) {
        try (ServerSocket serverSocket = new ServerSocket(entidade.getPorta())) {
            log.info("Cliente [{}] ouvindo na porta: {}", entidade.getNome(), entidade.getPorta());

            while (true) {
                Socket socket = serverSocket.accept();
                log.info("Equipamento conectado na porta {} (Cliente: {})", entidade.getPorta(), entidade.getNome());
                
                LisConnectionHandler handler = new LisConnectionHandler(socket, lisService, entidade);
                new Thread(handler).start();
            }
        } catch (Exception e) {
            log.error("Erro no servidor da porta {}: {}", entidade.getPorta(), e.getMessage());
        }
    }
}