package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {




	private byte opcode;
	private String  nickname, filehash;
	private long size;
	private File file;
	private byte[] bytes;

	/*
	 * TODO: Añadir atributos y crear otros constructores específicos para crear
	 * mensajes con otros campos (tipos de datos)
	 * 
	 */




	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}
	
	// op 2
	public PeerMessage(byte op, String  hash) {
		this.opcode = op;
		this.filehash = hash;		
	}
	
	// op 3
	public PeerMessage(byte op, byte[] f) {
		this.opcode = op;
		this.bytes = f;		
	}

	/*
	 * TODO: Crear métodos getter y setter para obtener valores de nuevos atributos,
	 * comprobando previamente que dichos atributos han sido establecidos por el
	 * constructor (sanity checks)
	 */
	public byte getOpcode() {
		return opcode;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nick) {
		this.nickname = nick;
	}

	public String getFilehash() {
		return filehash;
	}

	public void setFilehash(String filehash) {
		this.filehash = filehash;
	}

	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * TODO: En función del tipo de mensaje, leer del socket a través del "dis" el
		 * resto de campos para ir extrayendo con los valores y establecer los atributos
		 * del un objeto DirMessage que contendrá toda la información del mensaje, y que
		 * será devuelto como resultado. NOTA: Usar dis.readFully para leer un array de
		 * bytes, dis.readInt para leer un entero, etc.
		 */
		PeerMessage message = new PeerMessage();
		byte opcode = dis.readByte();
		switch (opcode) {
		case 1:{
			message.setOpcode(opcode);
			System.err.println("FILE NOT FOUND. "
					+ PeerMessageOps.opcodeToOperation(opcode));
			break;
		}
		case 2:{
			message.setOpcode(opcode);
			message.setSize(dis.readLong());
			message.setFilehash(dis.readUTF());
			break;
		}
		case 3:{
			message.setOpcode(opcode);
			message.setSize(dis.readLong());
			byte datos[] = new byte[(int) message.getSize()];
			dis.readFully(datos);
			message.setBytes(datos);
			// System.out.println(new String(message.getBytes()));
			break;
		}
		case 4:{
			message.setOpcode(opcode);
			System.err.println("There is at least 2 files which start with that hash."
					+ PeerMessageOps.opcodeToOperation(opcode));
			break;
		}


		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * TODO: Escribir los bytes en los que se codifica el mensaje en el socket a
		 * través del "dos", teniendo en cuenta opcode del mensaje del que se trata y
		 * los campos relevantes en cada caso. NOTA: Usar dos.write para leer un array
		 * de bytes, dos.writeInt para escribir un entero, etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
		case 1:{
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
			break;
		}
		case 2:{
			setSize(getFilehash().length());
			dos.writeLong(getSize());;
			dos.writeUTF(getFilehash());;
			break;
		}
		case 3:{
			setSize(getBytes().length);
			dos.writeLong(getSize());
			dos.write(getBytes());
			break;
		}
		case 4:{
			break;
		}



		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}





}
