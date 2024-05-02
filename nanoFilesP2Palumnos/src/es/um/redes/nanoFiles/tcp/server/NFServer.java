package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Servidor que se ejecuta en un hilo propio. Creará objetos
 * {@link NFServerThread} cada vez que se conecte un cliente.
 */
public class NFServer implements Runnable {

	private ServerSocket serverSocket = null;
	private boolean stopServer = false;
	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	private int availablePort = 10000;

	public NFServer() throws IOException {
		/*
		 * Crear un socket servidor y ligarlo a cualquier puerto disponible
		 */
		availablePort = findAvailablePort(availablePort);
		InetSocketAddress serverSocketAddress = new InetSocketAddress(availablePort);
		
		serverSocket = new ServerSocket();
		serverSocket.bind(serverSocketAddress);
		// System.out.println("\nServer is listening on port " + availablePort);
	}

	/**
	 * Método que crea un socket servidor y ejecuta el hilo principal del servidor,
	 * esperando conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		/*
		 * Usar el socket servidor para esperar conexiones de otros peers que
		 * soliciten descargar ficheros
		 */
		while (!stopServer) {
			try {
				// Esperar conexiones de clientes
				Socket socket = serverSocket.accept();
				System.out.println(
						"\nNew client connected: " + socket.getInetAddress().toString() + ":" + socket.getPort());

				/*
				 * Al establecerse la conexión con un peer, la comunicación con dicho
				 * cliente se hace en el método NFServerComm.serveFilesToClient(socket), al cual
				 * hay que pasarle el socket devuelto por accept
				 */
				// NFServerComm.serveFilesToClient(socket);

				/*
				 * (Opcional) Crear un hilo nuevo de la clase NFServerThread, que llevará
				 * a cabo la comunicación con el cliente que se acaba de conectar, mientras este
				 * hilo vuelve a quedar a la escucha de conexiones de nuevos clientes (para
				 * soportar múltiples clientes). Si este hilo es el que se encarga de atender al
				 * cliente conectado, no podremos tener más de un cliente conectado a este
				 * servidor.
				 */
				NFServerThread clientThread = new NFServerThread(socket);
				clientThread.start();

			} catch (IOException ex) {
				System.out.println("Server exception: " + ex.getMessage());
				// ex.printStackTrace();
			}
		}

	}

	/**
	 * Añadir métodos a esta clase para: 1) Arrancar el servidor en un hilo
	 * nuevo que se ejecutará en segundo plano 2) Detener el servidor (stopserver)
	 * 3) Obtener el puerto de escucha del servidor etc.
	 */

	/**
	 * Método para arrancar el servidor en un hilo nuevo que se ejecutará en segundo
	 * plano.
	 */
	public void startServer() {
		Thread serverThread = new Thread(this);
		serverThread.start();
	}

	/**
	 * Método para detener el servidor.
	 */
	public void stopServer() {
		stopServer = true;
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Método para obtener el puerto de escucha del servidor.
	 */

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
