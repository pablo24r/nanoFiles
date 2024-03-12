package es.um.redes.nanoFiles.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	/**
	 * Puerto en el que atienden los servidores de directorio
	 */
	private static final int DIRECTORY_PORT = 6868;
	/**
	 * Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
	 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
	 * recuperar el control
	 */
	private static final int TIMEOUT = 1000;
	/**
	 * Número de intentos máximos para obtener del directorio una respuesta a una
	 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
	 * cuenta como un intento.
	 */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;

	/**
	 * Valor inválido de la clave de sesión, antes de ser obtenida del directorio al
	 * loguearse
	 */
	public static final int INVALID_SESSION_KEY = -1;

	/**
	 * Socket UDP usado para la comunicación con el directorio
	 */
	private DatagramSocket socket;
	/**
	 * Dirección de socket del directorio (IP:puertoUDP)
	 */
	private InetSocketAddress directoryAddress;

	private int sessionKey = INVALID_SESSION_KEY;


	public DirectoryConnector(String address) throws IOException {
		/*
		 * Convertir el nombre de host 'address' a InetAddress y guardar la dirección de
		 * socket (address:DIRECTORY_PORT) del directorio en el atributo
		 * directoryAddress, para poder enviar datagramas a dicho destino.
		 */
		InetAddress ip = InetAddress.getByName(address);
		this.directoryAddress = new InetSocketAddress(ip, DIRECTORY_PORT);
		/*
		 * Crea el socket UDP en cualquier puerto para enviar datagramas al directorio
		 */
		this.socket = new DatagramSocket();

	}

	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 * @throws IOException
	 */
	private byte[] sendAndReceiveDatagrams(byte[] requestData) throws IOException {
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		byte response[] = null;
		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"directoryAddress\"");
			System.exit(-1);
		}
		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"socket\"");
			System.exit(-1);
		}
		/*
		 * Enviar datos en un datagrama al directorio y recibir una respuesta. El array
		 * devuelto debe contener únicamente los datos recibidos, *NO* el búfer de
		 * recepción al completo.
		 */

		// Inicializar contador de intentos
		int attempts = 0;

		do {
			try {
				// Configurar el tiempo de espera para recibir una respuesta
				socket.setSoTimeout(TIMEOUT);

				// Enviar datos en un datagrama al directorio
				DatagramPacket packetToServer = new DatagramPacket(requestData, requestData.length, directoryAddress);
				socket.send(packetToServer);

				// Crear un datagrama asociado al búfer de recepción
				DatagramPacket packetFromServer = new DatagramPacket(responseData, responseData.length);

				// Tratar de recibir la respuesta
				socket.receive(packetFromServer);

				// Obtener los datos del paquete
				byte[] receivedData = packetFromServer.getData();
				int length = packetFromServer.getLength();

				// Convertir los datos a una cadena
				String messageFromServer = new String(receivedData, 0, length);

				// Almacenar en el response
				response = messageFromServer.getBytes();

			} catch (SocketTimeoutException e) {
				// Incrementar el contador de intentos en caso de timeout
				attempts++;

				// Mostrar información sobre el reenvío
				System.out.println("Timeout #" + attempts + ". Retrying...");

				// Manejar el caso de exceder el número máximo de intentos
				if (attempts >= MAX_NUMBER_OF_ATTEMPTS) {
					System.err.println("Exceeded maximum number of attempts. Aborting.");
					System.exit(-1);
				}
			}
		} while (response == null && attempts < MAX_NUMBER_OF_ATTEMPTS);

		return response;

	}

	/**
	 * Método para probar la comunicación con el directorio mediante el envío y
	 * recepción de mensajes sin formatear ("en crudo")
	 * 
	 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
	 * @throws IOException
	 */
	public boolean testSendAndReceive() throws IOException {
		/*
		 * Probar el correcto funcionamiento de sendAndReceiveDatagrams. Se debe enviar
		 * un datagrama con la cadena "login" y comprobar que la respuesta recibida es
		 * "loginok". En tal caso, devuelve verdadero, falso si la respuesta no contiene
		 * los datos esperados.
		 */
		String string = "login";
		byte[] menssage = string.getBytes();
		byte[] respuesta = sendAndReceiveDatagrams(menssage);

		String messageFromServer = new String(respuesta, 0, respuesta.length);
		//System.out.println("MENSAJE SER->CLI ES:" + messageFromServer);
		return messageFromServer.equals("loginok");
	}

	public InetSocketAddress getDirectoryAddress() {
		return directoryAddress;
	}

	public int getSessionKey() {
		return sessionKey;
	}

	/**
	 * Método para "iniciar sesión" en el directorio, comprobar que está operativo y
	 * obtener la clave de sesión asociada a este usuario.
	 * 
	 * @param nickname El nickname del usuario a registrar
	 * @return La clave de sesión asignada al usuario que acaba de loguearse, o -1
	 *         en caso de error
	 * @throws IOException
	 */
	public boolean logIntoDirectory(String nickname) throws IOException {
		assert (sessionKey == INVALID_SESSION_KEY);
		boolean success = false;
		// 1.Crear el mensaje a enviar (objeto DirMessage) con atributos adecuados
		// (operation, etc.) NOTA: Usar como operaciones las constantes definidas en la
		// clase DirMessageOps
		DirMessage message = new DirMessage(DirMessageOps.OPERATION_LOGIN);
		message.setNickname(nickname);
		
		// 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		String loginMessage = message.toString();
		
		// 3.Crear un datagrama con los bytes en que se codifica la cadena
		byte[] menssageToServer = loginMessage.getBytes();

		// 4.Enviar datagrama y recibir una respuesta (sendAndReceiveDatagrams).
		try {
			byte[] responseData = sendAndReceiveDatagrams(menssageToServer);
			// 5.Convertir respuesta recibida en un objeto DirMessage (método
			// DirMessage.fromString)
			String messageFromServer = new String(responseData, 0, responseData.length);			
			DirMessage response = DirMessage.fromString(messageFromServer);
			// 6.Extraer datos del objeto DirMessage y procesarlos (p.ej., sessionKey)
			
			switch(response.getOperation()) {
			case DirMessageOps.OPERATION_LOGIN_OK:
				if(response.getSuccess().equals("true"))
					success = true;
				else
					System.out.println(response.getSuccess());
				this.sessionKey = Integer.parseInt(response.getSessionKey()); 
	            System.out.println("SUCCESS: " + success);
	            System.out.println("SESSION KEY: " + sessionKey);
				break;
			default:
				System.out.println(response.getOperation());
				System.out.println("Respuesta no entendida");
			}
		
		} catch (IOException e) {
			System.err.println("Error durante el login");
		}
		// 7.Devolver éxito/fracaso de la operación
		return success;
	}

	/**
	 * Método para obtener la lista de "nicknames" registrados en el directorio.
	 * Opcionalmente, la respuesta puede indicar para cada nickname si dicho peer
	 * está sirviendo ficheros en este instante.
	 * 
	 * @return La lista de nombres de usuario registrados, o null si el directorio
	 *         no pudo satisfacer nuestra solicitud
	 */
	public String[] getUserList() {
		String[] userlist = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar

		return userlist;
	}

	/**
	 * Método para "cerrar sesión" en el directorio
	 * 
	 * @return Verdadero si el directorio eliminó a este usuario exitosamente
	 */
	public boolean logoutFromDirectory() {
		assert (sessionKey == INVALID_SESSION_KEY);
		boolean success = false;
		// 1.Crear el mensaje a enviar (objeto DirMessage) con atributos adecuados
		// (operation, etc.) NOTA: Usar como operaciones las constantes definidas en la
		// clase DirMessageOps
		DirMessage message = new DirMessage(DirMessageOps.OPERATION_LOGOUT);
		message.setSessionKey(this.sessionKey+"");
		
		// 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		String loginMessage = message.toString();
		
		// 3.Crear un datagrama con los bytes en que se codifica la cadena
		byte[] menssageToServer = loginMessage.getBytes();

		// 4.Enviar datagrama y recibir una respuesta (sendAndReceiveDatagrams).
		try {
			byte[] responseData = sendAndReceiveDatagrams(menssageToServer);
			// 5.Convertir respuesta recibida en un objeto DirMessage (método
			// DirMessage.fromString)
			String messageFromServer = new String(responseData, 0, responseData.length);			
			DirMessage response = DirMessage.fromString(messageFromServer);
			// 6.Extraer datos del objeto DirMessage y procesarlos (p.ej., sessionKey)
			switch(response.getOperation()) {
			case DirMessageOps.OPERATION_LOGOUT_OK:
				if(response.getSuccess().equals("true"))
					success = true;
				else
					System.out.println(response.getSuccess());
				this.sessionKey = -1; 
	            System.out.println("SUCCESS: " + success);
				break;
			default:
				System.out.println(response.getOperation());
				System.out.println("Respuesta no entendida");
			}
		
		} catch (IOException e) {
			System.err.println("Error durante el logout");
		}
		finally {
			// Restablecer las credenciales independientemente de si la operación fue exitosa o no
	        sessionKey = INVALID_SESSION_KEY;
	        
		}
		// 7.Devolver éxito/fracaso de la operación
		return success;
	}

	/**
	 * Método para obtener la lista de usuarios conectados
	 * 
	 * @return Verdadero si la operación fue exitosa
	 */
	public boolean getUserlistFromDirectory() {
		boolean success = false;
		// 1.Crear el mensaje a enviar (objeto DirMessage) con atributos adecuados
		// (operation, etc.) NOTA: Usar como operaciones las constantes definidas en la
		// clase DirMessageOps
		DirMessage message = new DirMessage(DirMessageOps.OPERATION_USERLIST);
		message.setSessionKey(this.sessionKey+"");
		// 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		String getUsers = message.toString();
		// 3.Crear un datagrama con los bytes en que se codifica la cadena
		byte[] menssageToServer = getUsers.getBytes();
		System.out.println("Cliente envía: " + getUsers);
		// 4.Enviar datagrama y recibir una respuesta (sendAndReceiveDatagrams).
		try {
			byte[] responseData = sendAndReceiveDatagrams(menssageToServer);
			// 5.Convertir respuesta recibida en un objeto DirMessage (método
			// DirMessage.fromString)
			String messageFromServer = new String(responseData, 0, responseData.length);			
			DirMessage response = DirMessage.fromString(messageFromServer);
			// 6.Extraer datos del objeto DirMessage y procesarlos (p.ej., sessionKey)
			switch(response.getOperation()) {
			case DirMessageOps.OPERATION_USERLIST_OK:
				System.out.println(response.getUserlist());
				break;
			default:
				System.out.println(response.getOperation());
				System.out.println("Respuesta no entendida");
			}
		
		} catch (IOException e) {
			System.err.println("Error con la lista de usuarios.");
		}
		// 7.Devolver éxito/fracaso de la operación
		return success;
	}
	
	/**
	 * Método para dar de alta como servidor de ficheros en el puerto indicado a
	 * este peer.
	 * 
	 * @param serverPort El puerto TCP en el que este peer sirve ficheros a otros
	 * @return Verdadero si el directorio acepta que este peer se convierta en
	 *         servidor.
	 */
	public boolean registerServerPort(int serverPort) {
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar
		boolean success = false;

		return success;
	}

	/**
	 * Método para obtener del directorio la dirección de socket (IP:puerto)
	 * asociada a un determinado nickname.
	 * 
	 * @param nick El nickname del servidor de ficheros por el que se pregunta
	 * @return La dirección de socket del servidor en caso de que haya algún
	 *         servidor dado de alta en el directorio con ese nick, o null en caso
	 *         contrario.
	 */
	public InetSocketAddress lookupServerAddrByUsername(String nick) {
		InetSocketAddress serverAddr = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar

		return serverAddr;
	}

	/**
	 * Método para publicar ficheros que este peer servidor de ficheros están
	 * compartiendo.
	 * 
	 * @param files La lista de ficheros que este peer está sirviendo.
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y acepta la lista de ficheros, falso en caso contrario.
	 */
	public boolean publishLocalFiles(FileInfo[] files) {
		boolean success = false;

		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar

		return success;
	}

	/**
	 * Método para obtener la lista de ficheros que los peers servidores han
	 * publicado al directorio. Para cada fichero se debe obtener un objeto FileInfo
	 * con nombre, tamaño y hash. Opcionalmente, puede incluirse para cada fichero,
	 * su lista de peers servidores que lo están compartiendo.
	 * 
	 * @return Los ficheros publicados al directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 */
	public FileInfo[] getFileList() {
		FileInfo[] filelist = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar

		return filelist;
	}

	/**
	 * Método para obtener la lista de nicknames de los peers servidores que tienen
	 * un fichero identificado por su hash. Opcionalmente, puede aceptar también
	 * buscar por una subcadena del hash, en vez de por el hash completo.
	 * 
	 * @return La lista de nicknames de los servidores que han publicado al
	 *         directorio el fichero indicado. Si no hay ningún servidor, devuelve
	 *         una lista vacía.
	 */
	public String[] getServerNicknamesSharingThisFile(String fileHash) {
		String[] nicklist = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar

		return nicklist;
	}

}
