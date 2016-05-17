package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class RequestHandler implements Runnable {

	private DatagramPacket request;
	private Server parentServer;
	private DatagramSocket inOutSocket;

	private int clientPort;
	private InetAddress clientAddress;

	private boolean transfering = true;

	private static final String SERVER_DIRECTORY = "C:\\Users\\Public\\Server\\";
	private static final int PACKET_SIZE = 516;
	private static final int DATA_SIZE = 512;

	public RequestHandler(DatagramPacket request, Server parent) {
		this.request = request;
		parentServer = parent;

		clientPort = request.getPort();
		clientAddress = request.getAddress();

		try {
			inOutSocket = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("Request Handler: " + "Unable to create a socket to handle request");
			e.printStackTrace();
		}

	}

	/**
	 * Extracts the filename from the data in a request packet
	 * 
	 * @param data
	 * @return filename in request packet
	 */
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

	/**
	 * Handles a read request
	 * 
	 * @throws IOException
	 */
	private void readRequest() throws IOException {

		String filename = getFileName(request.getData());

		byte[] dataForPacket = new byte[516];
		dataForPacket[0] = 0;
		dataForPacket[1] = 3;

		DatagramPacket dataPacket = new DatagramPacket(dataForPacket, dataForPacket.length, clientAddress, clientPort);
		DatagramPacket ackPacket;

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(SERVER_DIRECTORY + filename));

		byte[] dataToSend = new byte[512];
		int n;
		int i = 1;

		while ((n = in.read(dataToSend)) != -1) {

			// Add block number to packet data
			dataForPacket[2] = (byte) ((i >> 8) & 0xFF);
			dataForPacket[3] = (byte) (i & 0xFF);

			// Copy the data from the file into the packet data
			System.arraycopy(dataToSend, 0, dataForPacket, 4, dataToSend.length);

			dataPacket.setData(dataForPacket);
			System.out.println("sending data " + i + " of size: " + n);

			for (byte b : dataForPacket) {
				System.out.print(b);
			}
			System.out.println();

			inOutSocket.send(dataPacket);

			ackPacket = receiveAck();

			if (!(ackPacket.getData()[3] == (byte) i)) {
				System.out.println("received wrong ack packet");
			}

			System.out.println("received ack " + request.getData()[3]);

			dataToSend = new byte[512];
			i++;

		}

		in.close();
	}

	/**
	 * Handles a write request
	 * 
	 * @throws IOException
	 */
	private void writeRequest() throws IOException {
		String filename = getFileName(request.getData());

		byte[] incomingData = new byte[PACKET_SIZE];

		DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
		DatagramPacket ackPacket = buildAckPacket(0);

		inOutSocket.send(ackPacket);

		int expectedBlockNumber = 0;

		System.out.println(filename);

		File newFile = new File(SERVER_DIRECTORY + filename);

		if (!newFile.exists()) {
			newFile.createNewFile();
		}

		BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(newFile));

		do {

			System.out.println("Request Handler: " + "receiving data packet");
			inOutSocket.receive(incomingPacket);
			System.out.println("Request Handler: " + "got packet");

			for (byte b : incomingPacket.getData()) {
				System.out.print(b);
			}

			System.out.println();

			if (!validate(incomingPacket)) {
				System.out.println("Request Handler: " + "unexpected");
				break;
			}
			System.out.println("Request Handler: " + "validated packet");
			incomingData = incomingPacket.getData();
			int blockNumber = ((incomingData[2] & 0xff) << 8) | (incomingData[3] & 0xff);
			System.out.println("Request Handler: " + "packet #" + blockNumber);

			if (blockNumber != expectedBlockNumber) {
				// handle error

			}

			// write block
			System.out.println("Writing data");
			writer.write(incomingData, 4, DATA_SIZE);

			// Build ack
			DatagramPacket ackPack = buildAckPacket(blockNumber);
			inOutSocket.send(ackPack);
			System.out.println("Request Handler: " + "sent ack");

		} while (transfering);
		// transfer complete

		System.out.println("Finished");
		writer.close();

	}

	/**
	 * Validates the data packet. If the packet's data is less than 512 bytes,
	 * TRANSFERING is set to false.
	 * 
	 * @param data
	 * @return false if the packet is not a data packet
	 */
	private boolean validate(DatagramPacket data) {
		if (data.getLength() == clientPort && data.getAddress().equals(clientAddress)) {
			// Packet has a
			System.out.println("Request Handler: " + "Received packet from an unexpected location");
			return false;
		} else if (data.getData()[1] != (byte) 3) {
			// this is not a data packet
			return false;
		} else if (data.getData()[data.getData().length - 1] == (byte) 0) {
			// end of transfer
			System.out.println("Request Handler: " + "data less than 512. ending.");
			transfering = false;
			return true;
		}

		return true;
	}

	/**
	 * Builds an ack packet for the given block number. Uses
	 * 
	 * @param blockNumber
	 * @return
	 */
	private DatagramPacket buildAckPacket(int blockNumber) {
		byte[] data = new byte[4];

		data[0] = 0;
		data[1] = 4;
		data[2] = (byte) ((blockNumber >> 8) & 0xFF);
		data[3] = (byte) (blockNumber & 0xFF);

		DatagramPacket ackPack = new DatagramPacket(data, data.length);

		ackPack.setAddress(clientAddress);
		ackPack.setPort(clientPort);

		return ackPack;
	}

	private DatagramPacket receiveAck() {
		byte[] data = new byte[4];
		DatagramPacket receivePacket = null;
		receivePacket = new DatagramPacket(data, data.length);
		try {
			inOutSocket.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("ERROR! Failed to receive ACK packet");
		}

		data = receivePacket.getData();

		System.out.println("Received an ACK with block #: " + data[3] + "Containing\n: ");
		for (byte b : data)
			System.out.print(b + " ");

		return receivePacket;
	}
	
	
	private void printVerbose(){
		
	}
	
	
	

	@Override
	public void run() {

		byte[] data = request.getData();

		// Read
		if (data[0] == (byte) 0 && data[1] == (byte) 1) {

			try {
				readRequest();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			System.out.println("Request Handler: " + "unexpected packet");
		}

		try {
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		inOutSocket.close();
		parentServer.threadClosed();

	}

}
