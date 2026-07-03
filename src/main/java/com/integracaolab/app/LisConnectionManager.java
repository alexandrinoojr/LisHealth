package com.integracaolab.app;

import java.net.Socket;

public class LisConnectionManager {

	private static Socket socket;
	
	public static void setSocket(Socket s) { //Armazena conexão ativa do Socket
		socket = s;
	}
	
	public static Socket getSocket() { //Retorna Socket atual
		return socket;
	}
	
	public static boolean isConnected() { //Existe conexão?
		return socket != null && socket.isConnected();
	}
	
	public static void clear() { //Limpa a var socket
		socket = null;
	}
}
