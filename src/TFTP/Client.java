package TFTP;
/* Client class for Iteration 1
 * Team 5 - 5000000
 * @author: team 5
 */

/* the following code deals with the client part of this exercise.
 *in the following exercise the client is send a Read Write or Test message to the Errsim which then be sended to the server
 * further explanation about how the connection between the errSim and the server will is explained in the two other classes.*/
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
	DatagramPacket sendPacket, receivePacket; // creat two DatagramPacket to
												// send and receive data from
												// and to the ErrSim
	DatagramSocket sendReceiveSocket; // We only need one datagramsocket since
										// we are never //sending and receiving
										// at the same time
	private DatagramSocket ftSocket;
	private Scanner input;
	private BufferedInputStream in; // stream to read in file

	public static enum Mode {
		NORMAL, TEST
	}; // enum serving for different mode

	public static enum Decision {
		RRQ, WRQ
	}; // same for decision both enum are inputted in the consol of the client

	private static String fname;
	private int packetdata_length = 0;
	private int sendPort = 69;

	private static final String CLIENT_DIRECTORY = "C:\\Users\\Public\\";
	private static final int DATA_SIZE = 512;
	private static final int PACKET_SIZE = 516;

	private boolean transfering = true;
	private InetAddress serverAddress;
	private int serverPort;

	public Client() {
		try {
			sendReceiveSocket = new DatagramSocket(); // creat the datagram
														// socket
		} catch (SocketException se) { // catch Socket exception error if
										// applicable
			se.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String args[]) throws IOException {
		Client c = new Client();
		System.out.println("open Client Program!\n");
		c.inter();
	}

	public void inter() throws IOException {
		String mode = "netascii"; // The used mode
		Decision request = Decision.RRQ; // default decision which is Read
		input = new Scanner(System.in); // run a new scanner to scan the input
										// from the user

		System.out.println("choose (R)ead Request, (W)rite Request, or (Q)uit?");
		String choice = input.nextLine(); // reads the input String

		// it runs threw all the possible answers if none are applicable it
		// recursively go back to inter()
		if (choice.equalsIgnoreCase("R")) {
			request = Decision.RRQ;
			System.out.println("Client: send a read request.");
		} else if (choice.equalsIgnoreCase("W")) {
			request = Decision.WRQ;
			System.out.println("Client:  send a write request.");
		} else if (choice.equalsIgnoreCase("Q")) {
			System.out.println("Goodbye!");
			System.exit(1);
		} else {
			System.out.println("invalid choice.  Please try again...");
			inter();
		}

		// gets a file directory from the user
		System.out.println("Please choose a file to modify.  Type in a file name: ");

		fname = input.nextLine();
		File f = new File(CLIENT_DIRECTORY + fname);
		// tests if the file exists
		// if (f.exists() && !f.isDirectory()) {
		// do something

		DatagramPacket requestPacket = buildRequest(fname.getBytes());

		// decide if it s a read or a write
		if (request == Decision.RRQ) {
			System.out.println("Client:" + fname + ", receive in " + mode + " mode.\n");
			read(requestPacket);

		} else if (request == Decision.WRQ) {
			System.out.println("Client:" + fname + ", send in " + mode + " mode.\n");
			write(requestPacket);
		}

		// } else {
		// // if no file exists with that name ask them to try again
		// System.out.println("That file does not exist.\n");
		// inter();
		// }

	}

	private DatagramPacket buildRequest(byte[] filename) {
		byte[] mode = ("netascii").getBytes();

		byte[] data = new byte[2 + filename.length + 1 + mode.length + 1];
		data[0] = 0;
		data[1] = 2;
		System.arraycopy(filename, 0, data, 2, filename.length);
		data[2 + filename.length] = 0;
		System.arraycopy(mode, 0, data, 3 + filename.length, mode.length);

		DatagramPacket requestPacket = new DatagramPacket(data, data.length);

		try {
			requestPacket.setAddress(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		requestPacket.setPort(69);

		return requestPacket;
	}

	private void read(DatagramPacket request) throws IOException {

		request.getData()[1] = (byte) 1;

		try {
			sendReceiveSocket.send(request);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		serverAddress = request.getAddress();
		serverPort = request.getPort();

		System.out.println("reading");

		byte[] incomingData = new byte[PACKET_SIZE];

		DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
		DatagramPacket ackPacket = buildAckPacket(0);

		File newFile = new File(CLIENT_DIRECTORY + fname);

		if (!newFile.exists()) {
			newFile.createNewFile();
		}

		BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(newFile));

		int blockNumber = 1;

		do {

			sendReceiveSocket.receive(incomingPacket);
			serverAddress = incomingPacket.getAddress();
			serverPort = incomingPacket.getPort();

			incomingData = incomingPacket.getData();
			blockNumber = ((incomingData[2] & 0xff) << 8) | (incomingData[3] & 0xff);

			if (!validate(incomingPacket)) {
				// invalid packet
			}

			writer.write(incomingData, 4, DATA_SIZE);

			ackPacket = buildAckPacket(blockNumber);

			sendReceiveSocket.send(ackPacket);

		} while (transfering);
		// transfer complete

		System.out.println("Finished");
		writer.close();

	}

	public void write(DatagramPacket request) throws IOException {

		request.getData()[1] = (byte) 2;

		try {
			sendReceiveSocket.send(request);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		System.out.println("writing");

		sendReceiveSocket.receive(request);

		int serverPort = request.getPort();
		InetAddress serverAddress = request.getAddress();

		// validate ack

		byte[] dataForPacket = new byte[516];
		dataForPacket[0] = 0;
		dataForPacket[1] = 3;

		DatagramPacket dataPacket = new DatagramPacket(dataForPacket, dataForPacket.length, serverAddress, serverPort);

		byte[] dataToSend = new byte[512];
		int n;
		int i = 1;

		in = new BufferedInputStream(new FileInputStream(CLIENT_DIRECTORY + fname));

		while ((n = in.read(dataToSend)) != -1) {
			// iterate the file in 512 byte chunks
			// Each iteration send the packet and receive the ack to match block
			// number i

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

			sendReceiveSocket.send(dataPacket);

			request = receiveAck();

			System.out.println("received ack " + request.getData()[3]);

			dataToSend = new byte[512];
			i++;
		}

		System.out.println("Finished sending file");
		in.close();
	}

	private byte[] receiveDataPacket() {
		byte[] data = new byte[516];
		ArrayList<Byte> testData = new ArrayList<Byte>();

		System.out.println("Waiting for data packet");
		DatagramPacket receivePacket = null;
		try {
			receivePacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), sendPort);

		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.err.println("ERROR! Failed to create and intialize DatagramPacket for receiving next data packet");

		}

		try {
			ftSocket.receive(receivePacket);
			packetdata_length = receivePacket.getLength();

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("ERROR! Failed to receive data packet.");
		}
		data = receivePacket.getData();

		byte[] trimarr = new byte[packetdata_length];

		for (int j = 0; j < packetdata_length; j++) {
			trimarr[j] = data[j];
		}

		System.out.println("Received Data packet with block #: " + data[3]);
		System.out.println("Containing: ");
		for (int h = 0; h < packetdata_length; h++)
			System.out.print(trimarr[h] + " ");
		System.out.print("\n");

		String received = new String(trimarr, 0, receivePacket.getLength());
		System.out.println(received);

		return trimarr;
	}

	private void sendAck(byte ackCount) {
		byte[] data = { 0, 4, 0, ackCount };
		DatagramPacket sendPacket = null;
		try {
			sendPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), sendPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.err.println("ERROR! DatagramPacket failed to be created and initialized");
		}

		System.out.println("Sending ACK with block #: " + ackCount);
		try {
			ftSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("ERROR! Datagram ACK packet failed to be sent");
		}

		System.out.println("ACK successfully sent");
	}

	private DatagramPacket receiveAck() {
		byte[] data = new byte[4];
		DatagramPacket receivePacket = null;
		receivePacket = new DatagramPacket(data, data.length);
		try {
			sendReceiveSocket.receive(receivePacket);
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

	/**
	 * Validates the data packet. If the packet's data is less than 512 bytes,
	 * TRANSFERING is set to false.
	 * 
	 * @param data
	 * @return false if the packet is not a data packet
	 */
	private boolean validate(DatagramPacket data) {
		if (data.getLength() == serverPort && data.getAddress().equals(serverAddress)) {
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
	 * Builds and returns an ack packet with the block number passed.
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

		ackPack.setAddress(serverAddress);
		ackPack.setPort(serverPort);

		return ackPack;
	}

	private void senderror(int i) {

		DatagramSocket sendErr = null;

		try {
			sendErr = new DatagramSocket();
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		byte[] error = new byte[100], erstring;
		error[0] = 0;
		error[1] = 5;
		error[2] = 0;
		String x = null;

		DatagramPacket errPacket = null;
		try {
			errPacket = new DatagramPacket(error, error.length, InetAddress.getLocalHost(), sendPort);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		switch (i) {
		case 0:
			error[3] = 0;
			break;
		case 1:
			error[3] = 1;

			x = "FILE NOT FOUND error";

			break;
		case 2:
			error[3] = 2;
			x = "ACCESS VIOLATION error ";
			break;
		case 3:
			error[3] = 3;
			x = "DISK FULL ERROR ";
			break;
		case 4:
			error[3] = 4;
			break;
		case 5:
			error[3] = 5;
			break;
		case 6:
			error[3] = 6;
			x = "FILE ALREADY EXISTS.";

			break;

		default:
			break;
		}

		erstring = x.getBytes();

		System.arraycopy(erstring, 0, error, 3, erstring.length);

		error[erstring.length + 4] = 0;

		try {
			errPacket = new DatagramPacket(error, error.length, InetAddress.getLocalHost(), sendPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Sending error packet with contents  #: " + erstring);
		System.out.println("Sending error packet with contents  #: " + new String(errPacket.getData()));
		try {
			sendErr.send(errPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("ERROR! Datagram  packet failed to be sent");
		}

		System.out.println("Error successfully sent");
	}

	public void exit() {
		sendReceiveSocket.close();
	}
}