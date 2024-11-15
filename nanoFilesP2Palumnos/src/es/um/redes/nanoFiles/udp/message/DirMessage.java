package es.um.redes.nanoFiles.udp.message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	/*
	 * Definir de manera simbólica los nombres de todos los campos que pueden
	 * aparecer en los mensajes de este protocolo (formato campo:valor)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	private static final String FIELDNAME_NICKNAME = "nickname";
	private static final String FIELDNAME_SUCCESS = "success";
	private static final String FIELDNAME_SESSIONKEY = "sessionkey";
	private static final String FIELDNAME_USERLIST = "userlist";
	private static final String FIELDNAME_NEWSERVER = "newserver";
	private static final String FIELDNAME_PORT = "port";

	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/*
	 * TODO: Crear un atributo correspondiente a cada uno de los campos de los
	 * diferentes mensajes de este protocolo.
	 */
	private String nickname;
	private String success;
	private String sessionKey;
	private String userlist;
	private String port;

	public DirMessage(String op) {
		operation = op;
	}

	/*
	 * TODO Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */
	public DirMessage() {

	}

	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * TODO: Usar un bucle para parsear el mensaje línea a línea, extrayendo para
		 * cada línea el nombre del campo y el valor, usando el delimitador DELIMITER, y
		 * guardarlo en variables locales.
		 */

		// System.out.println("DirMessage read from socket:");
		// System.out.println(message);
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;

		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();

			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			case FIELDNAME_NICKNAME:
				assert (m != null);
				m.setNickname(value);
				break;
			case FIELDNAME_SUCCESS:
				assert (m != null);
				m.setSuccess(value);
				break;
			case FIELDNAME_SESSIONKEY:
				assert (m != null);
				m.setSessionKey(value);
				break;
			case FIELDNAME_USERLIST:
				assert (m != null);
				m.setUserlist(value);
				break;
			case FIELDNAME_NEWSERVER:
				assert (m != null);
				m.setSuccess(value);
				break;
			case FIELDNAME_PORT:
				assert (m != null);
				m.setPort(value);
				break;

			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}

		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		/*
		 * TODO: En función del tipo de mensaje, crear una cadena con el tipo y
		 * concatenar el resto de campos necesarios usando los valores de los atributos
		 * del objeto.
		 */
		String field, field1;
		String value, value1;

		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN:
			field = FIELDNAME_NICKNAME;
			value = getNickname();
			sb.append(field + DELIMITER + value + END_LINE);
			break;

		case DirMessageOps.OPERATION_LOGIN_OK:
			field = FIELDNAME_SUCCESS;
			value = getSuccess();
			sb.append(field + DELIMITER + value + END_LINE);
			field1 = FIELDNAME_SESSIONKEY;
			value1 = getSessionKey();
			sb.append(field1 + DELIMITER + value1 + END_LINE);
			break;

		case DirMessageOps.OPERATION_LOGOUT:
			field1 = FIELDNAME_SESSIONKEY;
			value1 = getSessionKey();
			sb.append(field1 + DELIMITER + value1 + END_LINE);
			break;

		case DirMessageOps.OPERATION_LOGOUT_OK:
			field1 = FIELDNAME_SUCCESS;
			value1 = getSuccess();
			sb.append(field1 + DELIMITER + value1 + END_LINE);
			break;

		case DirMessageOps.OPERATION_USERLIST:
			field1 = FIELDNAME_SESSIONKEY;
			value1 = getSessionKey();
			sb.append(field1 + DELIMITER + value1 + END_LINE);
			break;

		case DirMessageOps.OPERATION_USERLIST_OK:
			field1 = FIELDNAME_USERLIST;
			value1 = getUserlist();
			sb.append(field1 + DELIMITER + value1 + END_LINE);
			break;

		case DirMessageOps.OPERATION_NEWSERVER:
			field = FIELDNAME_SESSIONKEY;
			value = getSessionKey();
			sb.append(field + DELIMITER + value + END_LINE);
			field1 = FIELDNAME_PORT;
			value1 = getPort();
			sb.append(field1 + DELIMITER + value1 + END_LINE);
			break;
			
		case DirMessageOps.OPERATION_NEWSERVER_OK:
			field = FIELDNAME_SUCCESS;
			value = getSuccess();
			sb.append(field + DELIMITER + value + END_LINE);
			break;
			
		case DirMessageOps.OPERATION_GETADDRESS:
			field = FIELDNAME_SESSIONKEY;
			value = getSessionKey();
			sb.append(field + DELIMITER + value + END_LINE);
			field1 = FIELDNAME_NICKNAME;
			value1 = getNickname();
			sb.append(field1 + DELIMITER + value1 + END_LINE);
			break;

		case DirMessageOps.OPERATION_GETADDRESS_OK:
			field = FIELDNAME_PORT;
			value = getPort();
			sb.append(field + DELIMITER + value + END_LINE);
			break;
		}

		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}

	// GETTERS AND SETTERS
	public String getOperation() {
		return operation;
	}

	public void setNickname(String nick) {

		nickname = nick;
	}

	public String getNickname() {

		return nickname;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String value) {
		this.sessionKey = value;
	}

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	public String getUserlist() {
		return userlist;
	}

	public void setUserlist(String userlist) {
		this.userlist = userlist;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

}
