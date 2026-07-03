package com.integracaolab.app;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class LisOrderSender {
	
	public void send(LisOrder order) {
		
	boolean conected =	LisConnectionManager.isConnected();
	
	
	if (!conected) {
		return;
	}
	
	Socket socket = LisConnectionManager.getSocket();
	
	try {
		
		PrintWriter writer = new PrintWriter(socket.getOutputStream(),true);
		LisAsciiFormatter formatter = new LisAsciiFormatter();
		List<String> linhas = formatter.format(order);
		
		for (String linha : linhas) {
			writer.println(linha);
		}
	} catch (Exception e) {
		
		System.out.println("Erro ao enviar ordem LIS: " + e.getMessage());
	}
	
	}

}
