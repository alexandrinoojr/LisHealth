package com.integracaolab.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppApplication implements CommandLineRunner {

    private final TcpLisServer tcpLisServer;

    public AppApplication(TcpLisServer tcpLisServer) {
        this.tcpLisServer = tcpLisServer;
    }

    @Override
    public void run(String... args) {
        tcpLisServer.start();
    }

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

}
