package es.um.redes.nanoFiles.logic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.Random;

import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.tcp.server.NFServerSimple;

public class NFControllerLogicP2P {
	/*
	 * Para bgserve, se necesita un atributo NFServer que actuará como
	 * servidor de ficheros en segundo plano de este peer
	 */
	int availablePort;
	NFServer server = null;

	protected NFControllerLogicP2P() {
	}

	/**
	 * Método para arrancar un servidor de ficheros en primer plano.
	 * 
	 */
	protected void foregroundServeFiles() {
		/*
		 * Crear objeto servidor NFServerSimple y ejecutarlo en primer plano.
		 */

		/*
		 * Las excepciones que puedan lanzarse deben ser capturadas y tratadas en este
		 * método. Si se produce una excepción de entrada/salida (error del que no es
		 * posible recuperarse), se debe informar sin abortar el programa
		 */
		try {
			NFServerSimple server = new NFServerSimple();
			availablePort = server.getPort();
			server.run();
		} catch (IOException e) {
			// Imprimir la traza de la excepción
			e.printStackTrace();
			// Informar del error al usuario
			System.err.println("Error al ejecutar el servidor en primer plano: " + e.getMessage());
		}
	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean backgroundServeFiles() {
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en
		 * cuyo caso el servidor ya está en marcha. Si no lo está, crear objeto servidor
		 * NFServer y arrancarlo en segundo plano creando un nuevo hilo. Finalmente,
		 * comprobar que el servidor está escuchando en un puerto válido (>0) e imprimir
		 * mensaje informando sobre el puerto, y devolver verdadero.
		 */
		
		/*
		 * Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */
		try {
			// Comprobar si ya existe un servidor NFServer previamente creado
			if (server == null) {
				// Crear objeto servidor NFServer y arrancarlo en segundo plano creando un nuevo
				// hilo
				server = new NFServer();
				Thread serverThread = new Thread(server);
				serverThread.start();
				// Comprobar si el servidor está escuchando en un puerto válido (>0)
				int serverPort = server.getPort();
				if (serverPort > 0) {
					System.out.println("Background file server started on port " + serverPort);
					return true;
				} else {
					System.err.println("Failed to start background file server");
					return false;
				}
			} else {
				System.err.println("A background file server is already running");
				return false;
			}
		} catch (IOException  e) {
			// Imprimir la traza de la excepción
			e.printStackTrace();
			// Informar del error al usuario
			System.err.println("Error starting background file server: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param fserverAddr    La dirección del servidor al que se conectará
	 * @param targetFileHash El hash del fichero a descargar
	 * @param localFileName  El nombre con el que se guardará el fichero descargado
	 */
	protected boolean downloadFileFromSingleServer(InetSocketAddress fserverAddr, String targetFileHash,
			String localFileName) {
		if (fserverAddr == null) {
			System.err.println("* Cannot start download - No server address provided");
			return false;
		}
		/*
		 * Crear un objeto NFConnector para establecer la conexión con el peer servidor
		 * de ficheros, y usarlo para descargar el fichero mediante su método
		 * "downloadFile". Se debe comprobar previamente si ya existe un fichero con el
		 * mismo nombre en esta máquina, en cuyo caso se informa y no se realiza la
		 * descarga. Si todo va bien, imprimir mensaje informando de que se ha
		 * completado la descarga.
		 * 
		 * Las excepciones que puedan lanzarse deben ser capturadas y tratadas en este
		 * método. Si se produce una excepción de entrada/salida (error del que no es
		 * posible recuperarse), se debe informar sin abortar el programa
		 */
		File file = new File(localFileName);
		if (file.exists()) {
			System.err.println("* File " + localFileName + " already exists.");
			return false;
		}

		NFConnector connector;
		try {
			connector = new NFConnector(fserverAddr);
			boolean downloaded = connector.downloadFile(targetFileHash, file);
			if (downloaded) {
				System.out.println("* File " + localFileName + " downloaded successfully.");
				return true;
			} else {
				System.err.println("* Failed to download file " + localFileName);
				return false;
			}
		} catch (IOException e) {
			System.err.println("* Error creating connector: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList La lista de direcciones de los servidores a los que
	 *                          se conectará
	 * @param targetFileHash    Hash completo del fichero a descargar
	 * @param localFileName     Nombre con el que se guardará el fichero descargado
	 */
	public boolean downloadFileFromMultipleServers(LinkedList<InetSocketAddress> serverAddressList,
			String targetFileHash, String localFileName) {
		boolean downloaded = false;

		if (serverAddressList == null) {
			System.err.println("* Cannot start download - No list of server addresses provided");
			return false;
		}
		/*
		 * TODO: Crear un objeto NFConnector para establecer la conexión con cada
		 * servidor de ficheros, y usarlo para descargar un trozo (chunk) del fichero
		 * mediante su método "downloadFileChunk". Se debe comprobar previamente si ya
		 * existe un fichero con el mismo nombre en esta máquina, en cuyo caso se
		 * informa y no se realiza la descarga. Si todo va bien, imprimir mensaje
		 * informando de que se ha completado la descarga.
		 */
		/*
		 * TODO: Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */

		return downloaded;
	}

	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros en
	 * segundo plano
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	public int getServerPort() {
		int port = 0;
		/*
		 * Devolver el puerto de escucha de nuestro servidor de ficheros en
		 * segundo plano
		 */
		port = server.getPort();

		return port;
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	public void stopBackgroundFileServer() {
		/*
		 * Enviar señal para detener nuestro servidor de ficheros en segundo plano
		 */
		server.stopServer();
		server=null;

	}

}
