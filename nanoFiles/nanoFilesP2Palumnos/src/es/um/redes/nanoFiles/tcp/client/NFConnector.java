package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDigest;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;
	private DataOutputStream dos;
	private DataInputStream dis;


	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		serverAddr = fserverAddr;
		/*
		 *Se crea el socket a partir de la dirección del servidor (IP, puerto). La
		 * creación exitosa del socket significa que la conexión TCP ha sido
		 * establecida.
		 */
		socket = new Socket(serverAddr.getAddress(),serverAddr.getPort());
		/*
		 * Se crean los DataInputStream/DataOutputStream a partir de los streams de
		 * entrada/salida del socket creado. Se usarán para enviar (dos) y recibir (dis)
		 * datos del servidor.
		 */
	    dos = new DataOutputStream(socket.getOutputStream());
	    dis = new DataInputStream(socket.getInputStream());


	}

	/**
	 * Método para descargar un fichero a través del socket mediante el que estamos
	 * conectados con un peer servidor.
	 * 
	 * @param targetFileHashSubstr Subcadena del hash del fichero a descargar
	 * @param file                 El objeto File que referencia el nuevo fichero
	 *                             creado en el cual se escribirán los datos
	 *                             descargados del servidor
	 * @return Verdadero si la descarga se completa con éxito, falso en caso
	 *         contrario.
	 * @throws IOException Si se produce algún error al leer/escribir del socket.
	 */
	public boolean downloadFile(String targetFileHashSubstr, File file) throws IOException {
		boolean downloaded = false;
		/* Boletín 7
		int num=new Random().nextInt(100);
		dos.writeInt(num);
		System.out.println("Envío -> " + num);
		
		int numRec = dis.readInt();
		System.out.println("Recibo <- " + numRec);
		if (num==numRec)
			return true;
		*/
		
		/*
		 * Construir objetos PeerMessage que modelen mensajes con los valores
		 * adecuados en sus campos (atributos), según el protocolo diseñado, y enviarlos
		 * al servidor a través del "dos" del socket mediante el método
		 * writeMessageToOutputStream.
		 */
		PeerMessage message = new PeerMessage((byte) 2, targetFileHashSubstr);
		message.writeMessageToOutputStream(dos);
		
		/*
		 * Recibir mensajes del servidor a través del "dis" del socket usando
		 * PeerMessage.readMessageFromInputStream, y actuar en función del tipo de
		 * mensaje recibido, extrayendo los valores necesarios de los atributos del
		 * objeto (valores de los campos del mensaje).
		 */
		PeerMessage menRecibido = PeerMessage.readMessageFromInputStream((DataInputStream) dis);
		switch (menRecibido.getOpcode()){
		case 1:{
			// System.err.println("File with hash "+  targetFileHashSubstr + " not found.");
			break;
		}
		case 3:{
			/*
			 * Para escribir datos de un fichero recibidos en un mensaje, se puede
			 * crear un FileOutputStream a partir del parámetro "file" para escribir cada
			 * fragmento recibido (array de bytes) en el fichero mediante el método "write".
			 * Cerrar el FileOutputStream una vez se han escrito todos los fragmentos.
			 */
			
			FileOutputStream fos = new FileOutputStream(file.getPath());
			fos.write(menRecibido.getBytes());
			fos.close();
			
			/*
			 * Finalmente, comprobar la integridad del fichero creado para comprobar
			 * que es idéntico al original, calculando el hash a partir de su contenido con
			 * FileDigest.computeFileChecksumString y comparándolo con el hash completo del
			 * fichero solicitado. Para ello, es necesario obtener del servidor el hash
			 * completo del fichero descargado, ya que quizás únicamente obtuvimos una
			 * subcadena del mismo como parámetro.
			 */
			PeerMessage checkHash = PeerMessage.readMessageFromInputStream((DataInputStream) dis);
			downloaded = checkHash.getFilehash().equals(FileDigest.computeFileChecksumString(file.getPath()));
			// System.out.println(checkHash.getFilehash() + " - " + FileDigest.computeFileChecksumString(file.getPath()));
			break;
		}
		case 4:{
			// System.err.println("Mas de uno");
			break;
		}
		default:
			break;
		}
		

		/*
		 * NOTA: Hay que tener en cuenta que puede que la subcadena del hash pasada como
		 * parámetro no identifique unívocamente ningún fichero disponible en el
		 * servidor (porque no concuerde o porque haya más de un fichero coincidente con
		 * dicha subcadena)
		 */






		return downloaded;
	}





	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
