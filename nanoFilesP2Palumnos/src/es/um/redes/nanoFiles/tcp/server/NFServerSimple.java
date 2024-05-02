package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;

public class NFServerSimple {

	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	private static final String STOP_SERVER_COMMAND = "fgstop";
	private static final int PORT = 10000;
	private ServerSocket serverSocket = null;
	private int availablePort = 10000;

	public NFServerSimple() throws IOException {
		/*
		 * Crear una direción de socket a partir del puerto especificado
		 */
		availablePort = findAvailablePort(PORT);
		InetSocketAddress serverSocketAddress = new InetSocketAddress(availablePort);
		
		/*
		 * Crear un socket servidor y ligarlo a la dirección de socket anterior
		 */
		serverSocket = new ServerSocket();
		
		// After creating the server socket, bind to the listening port
		serverSocket.bind(serverSocketAddress);
		System.out.println("\nServer is listening on port " + availablePort);
	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación a menos que se implemente la funcionalidad de
	 * detectar el comando STOP_SERVER_COMMAND (opcional)
	 * 
	 */
	public void run() {
		/*
		 * Comprobar que el socket servidor está creado y ligado
		 */
		if(serverSocket != null && serverSocket.isBound()) {
			/*
			 * Usar el socket servidor para esperar conexiones de otros peers que
			 * soliciten descargar ficheros
			 */
			while(true) {
				try {
					// Waiting for new connection requests
					Socket socket = serverSocket.accept();
					
					// Created the socket for the connection with the client
					System.out.println("\nNew client connected: " +
						socket.getInetAddress().toString() + ":" + socket.getPort());
					/*
					 * Al establecerse la conexión con un peer, la comunicación con dicho
					 * cliente se hace en el método NFServerComm.serveFilesToClient(socket), al cual
					 * hay que pasarle el socket devuelto por accept
					 */
					NFServerComm.serveFilesToClient(socket);
				} catch (IOException ex) {
					System.out.println("Server exception: " + ex.getMessage());
					ex.printStackTrace();
				}

			}
		}
		
		System.out.println("NFServerSimple stopped. Returning to the nanoFiles shell...");
	}
	
    // Método para encontrar un puerto disponible
    private int findAvailablePort(int initialPort) {
        int port = initialPort;
        while (!isPortAvailable(port)) {
            port++;
        }
        return port;
    }
    
    // Método para verificar si un puerto está disponible
    private boolean isPortAvailable(int port) {
        try {
            new ServerSocket(port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public int getPort() {
    	return availablePort;
    }
    
}
