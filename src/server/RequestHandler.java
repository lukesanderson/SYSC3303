package server;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RequestHandler implements Runnable {

	private DatagramPacket request;
	private Server parentServer;
	private DatagramSocket inOutSocket;

	private static final String DEFAULT_DIRECTORY = "A:\\School\\Current Semester\\Sysc 3303\\Project\\SYSC3303_TFTP\\src\\server\\";

	public RequestHandler(DatagramPacket request, Server parent) {
		this.request = request;
		parentServer = parent;

		try {
			inOutSocket = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("Unable to create a socket to handle request");
			e.printStackTrace();
		}

	}

	private DatagramPacket buildDataPacket(byte[] data) {

		return null;

	}

	private String getFileName(byte[] data) {

		String filename = "";

		for (int i = 2; i < data.length; i++) {
			if (data[i] == (byte) 0) {
				break;
			}

			char g = (char) data[i];
			filename += g;
		}

		return filename;
	}

	private void readRequest() {

		String filename = getFileName(request.getData());
		BufferedInputStream in = null;
		try {
			System.out.println(DEFAULT_DIRECTORY + filename);
			in = new BufferedInputStream(new FileInputStream(DEFAULT_DIRECTORY + filename));

			byte[] g = new byte[1000];
			in.read(g);

			for (byte d : g) {
				System.out.print((char) d);
			}

		} catch (FileNotFoundException e) {
			System.out.println(filename + " not found");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void writeRequest() {

		String filename = getFileName(request.getData());

	}

	@Override
	public void run() {

		byte[] data = request.getData();

		// Read
		if (data[0] == (byte) 0 && data[1] == (byte) 1) {
			readRequest();
		}

		// Write
		else if (data[0] == (byte) 0 && data[1] == (byte) 2) {
			writeRequest();
		}

		// Unexpected opcode for request
		else {
			System.out.println("unexpected packet");
		}

		inOutSocket.close();
		parentServer.threadClosed();

	}

}
