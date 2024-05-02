package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServerComm {

	public static void serveFilesToClient(Socket socket) throws IOException {
		/*
		 * Crear dis/dos a partir del socket
		 */
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());

		/*
		 * Boletín 7 // Recibir el entero enviado por el cliente int numeroRecibido =
		 * dis.readInt();
		 * 
		 * // Enviarlo de vuelta dos.writeInt(numeroRecibido);
		 */

		/*
		 * Mientras el cliente esté conectado, leer mensajes de socket, convertirlo a un
		 * objeto PeerMessage y luego actuar en función del tipo de mensaje recibido,
		 * enviando los correspondientes mensajes de respuesta.
		 */
		while (socket.isConnected()) {
			try {
				PeerMessage message = PeerMessage.readMessageFromInputStream((DataInputStream) dis);
				switch (message.getOpcode()) {
				case 2: {
					/*
					 * TODO: Para servir un fichero, hay que localizarlo a partir de su hash (o
					 * subcadena) en nuestra base de datos de ficheros compartidos. Los ficheros
					 * compartidos se pueden obtener con NanoFiles.db.getFiles(). El método
					 * FileInfo.lookupHashSubstring es útil para buscar coincidencias de una
					 * subcadena del hash. El método NanoFiles.db.lookupFilePath(targethash)
					 * devuelve la ruta al fichero a partir de su hash completo.
					 */
					String hash = message.getFilehash();
					FileInfo[] matches = FileInfo.lookupHashSubstring(NanoFiles.db.getFiles(), hash);
					if (matches.length == 0) {
						PeerMessage FileNotFound = new PeerMessage((byte) 1);
						FileNotFound.writeMessageToOutputStream(dos);
					} else if (matches.length == 1) {
						String ruta = NanoFiles.db.lookupFilePath(matches[0].fileHash);
						File file = new File(ruta);

						// Lee el archivo y lo convierte en un array de bytes
						FileInputStream fis = new FileInputStream(file);
						byte[] bytesArray = new byte[(int) file.length()];
						fis.read(bytesArray); // Lee el contenido del archivo en el array de bytes
						fis.close();
						PeerMessage DownloadOK = new PeerMessage((byte) 3, bytesArray);
						DownloadOK.writeMessageToOutputStream(dos);
						
						PeerMessage CheckHash = new PeerMessage((byte) 2, matches[0].fileHash);
						CheckHash.writeMessageToOutputStream(dos);

					} else {
						PeerMessage MultiFiles = new PeerMessage((byte) 4);
						MultiFiles.writeMessageToOutputStream(dos);
					}

					break;
				}
				default:
					break;
				}

			} catch (EOFException e) {
				System.err.println("El cliente cerró la conexión.");
				socket.close();
				// e.printStackTrace();
				break;
			}

		}

	}

}
