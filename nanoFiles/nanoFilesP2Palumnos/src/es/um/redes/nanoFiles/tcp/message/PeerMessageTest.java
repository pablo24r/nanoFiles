package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PeerMessageTest {

	public static void main(String[] args) throws IOException {
		String nombreArchivo = "peermsg.bin";
		DataOutputStream fos = new DataOutputStream(new FileOutputStream(nombreArchivo));
		DataInputStream fis = new DataInputStream(new FileInputStream(nombreArchivo));
		File f = new File("nf-shared/text.txt");
        // Lee el archivo y conviértelo en un array de bytes
        FileInputStream fiss = new FileInputStream(f);
        byte[] bytesArray = new byte[(int) f.length()];
        fiss.read(bytesArray); // Lee el contenido del archivo en el array de bytes
        fiss.close(); // Cierra el FileInputStream
		String hash = "sadf312nd32";
		/*
		 * Probar a crear diferentes tipos de mensajes (con los opcodes válidos
		 * definidos en PeerMessageOps), estableciendo los atributos adecuados a cada
		 * tipo de mensaje. Luego, escribir el mensaje a un fichero con
		 * writeMessageToOutputStream para comprobar que readMessageFromInputStream
		 * construye un mensaje idéntico al original.
		 */
		
		PeerMessage msgOut = new PeerMessage((byte) 2, hash);
		msgOut.writeMessageToOutputStream(fos);

		PeerMessage msgIn = PeerMessage.readMessageFromInputStream((DataInputStream) fis);
		
		
		/*
		 * Comprobar que coinciden los valores de los atributos relevantes al tipo
		 * de mensaje en ambos mensajes (msgOut y msgIn), empezando por el opcode.
		 */
		if (msgOut.getOpcode() != msgIn.getOpcode()) {
			System.err.println("Opcode does not match!");
		}
		System.out.println(msgOut.getOpcode() + "-" + msgIn.getOpcode());

		if (msgOut.getSize() == msgIn.getSize()) {
			System.out.println("Size does match! " + msgOut.getSize()  + " - " + msgIn.getSize());
		}
		else System.err.println("Size does match! " + msgOut.getSize()  + " - " + msgIn.getSize());

		if (msgOut.getFilehash().equals(msgIn.getFilehash() )) {
			System.out.println("Hash does match! " + msgOut.getFilehash() + " - " + msgIn.getFilehash());
		}
		else System.err.println("Hash does match! " + msgOut.getFilehash() + " - " + msgIn.getFilehash());
		
		
		PeerMessage msgOut2 = new PeerMessage((byte) 3, bytesArray);
		msgOut2.writeMessageToOutputStream(fos);
		PeerMessage msgIn2 = PeerMessage.readMessageFromInputStream((DataInputStream) fis);
		
		
		/*
		 * Comprobar que coinciden los valores de los atributos relevantes al tipo
		 * de mensaje en ambos mensajes (msgOut y msgIn), empezando por el opcode.
		 */
		if (msgOut2.getOpcode() != msgIn2.getOpcode()) {
			System.err.println("Opcode does not match!");
		}
		System.out.println(msgOut2.getOpcode() + "-" + msgIn2.getOpcode());

		if (msgOut2.getSize() == msgIn2.getSize()) {
			System.out.println("Size does match! " + msgOut2.getSize()  + " - " + msgIn2.getSize());
		}
		else System.err.println("Size does match! " + msgOut2.getSize()  + " - " + msgIn2.getSize());

		if (msgOut2.getBytes().equals(msgIn2.getBytes() )) {
			System.out.println("Hash does match! " + msgOut2.getBytes() + " - " + msgIn2.getBytes());
		}
		else System.err.println("Hash does match! " + new String(msgOut2.getBytes()) + " - " + new String(msgIn2.getBytes()));

	}

}

