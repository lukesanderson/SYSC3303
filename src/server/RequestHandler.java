package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class RequestHandler implements Runnable {

	private DatagramPacket request;
	private Server parentServer;
	private DatagramSocket inOutSocket;

	private boolean transfering = true;

	private static final String SERVER_DIRECTORY = "A:\\School\\Current Semester\\Sysc 3303\\Project\\SYSC3303_TFTP\\src\\server\\";
	private static final int PACKET_SIZE = 516;
	private static final int DATA_SIZE = 512;

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
			System.out.println(SERVER_DIRECTORY + filename);
			in = new BufferedInputStream(new FileInputStream(SERVER_DIRECTORY + filename));

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

	private void writeRequest() throws IOException {
		System.out.println("handling write request");
		String filename = getFileName(request.getData());
		ArrayList<byte[]> dataBlocks = new ArrayList<byte[]>();

		byte[] incomingData = new byte[PACKET_SIZE];

		DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
		DatagramPacket ackPacket = buildAckPacket(0);
		
		inOutSocket.send(ackPacket);
		
		int expectedBlockNumber = 0;

		do {

			System.out.println("receiving first data packet");
			inOutSocket.receive(incomingPacket);
			if (!validate(incomingPacket.getData())) {
				System.out.println("unexpected");
				break;
			}
			incomingData = incomingPacket.getData();
			int blockNumber = ((incomingData[2] & 0xff) << 8) | (incomingData[3] & 0xff);

			if (blockNumber != expectedBlockNumber) {
				// handle error
			}

			// save block
			dataBlocks.add(incomingData);

			// Build ack
			DatagramPacket ackPack = buildAckPacket(blockNumber);
			inOutSocket.send(ackPack);

		} while (transfering);
		// transfer complete

		BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream(SERVER_DIRECTORY + filename));

		// write to file
		for (byte[] block : dataBlocks) {
			fileWriter.write(block);
		}

		fileWriter.close();

	}

	/**
	 * Validates the data packet. If the packet's data is less than 512 bytes,
	 * TRANSFERING is set to false.
	 * 
	 * @param data
	 * @return false if the packet is not a data packet
	 */
	private boolean validate(byte[] data) {
		if (data[1] != (byte) 3) {
			// this is not a data packet
			return false;
		} else if (data[data.length - 1] == (byte) 0) {
			// end of transfer
			transfering = false;
			return true;
		}

		return false;
	}

	private DatagramPacket buildAckPacket(int blockNumber) {
		byte[] data = new byte[4];

		data[0] = 0;
		data[1] = 4;
		data[2] = (byte) (blockNumber & 0xFF);
		data[3] = (byte) ((blockNumber >> 8) & 0xFF);

		DatagramPacket ackPack = new DatagramPacket(data, data.length);

		ackPack.setAddress(request.getAddress());
		ackPack.setPort(request.getPort());

		return ackPack;
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
			try {
				writeRequest();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Unexpected opcode for request
		else {
			System.out.println("unexpected packet");
		}

		inOutSocket.close();
		parentServer.threadClosed();

	}

}
