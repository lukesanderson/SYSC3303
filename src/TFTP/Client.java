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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;

import exceptions.ErrorException;
import exceptions.ReceivedErrorException;
import exceptions.UnknownIDException;

public class Client {
	DatagramPacket sendPacket, receivePacket; // creat two DatagramPacket to
												// send and receive data from
												// and to the ErrSim
	DatagramSocket sendReceiveSocket; // We only need one datagramsocket since
										// we are never //sending and receiving
										// at the same time
	private Scanner input;
	private BufferedInputStream in; // stream to read in file

	public static enum Mode {
		NORMAL, TEST
	}; // enum serving for different mode

	public static enum Decision {
		RRQ, WRQ
	}; // same for decision both enum are inputted in the consol of the client

	private static String fname;

	private static final String CLIENT_DIRECTORY = "C:\\Users\\Public\\";
	private static final int DATA_SIZE = 512;
	private static final int PACKET_SIZE = 516;

	private static final int ILLEGAL_OPER_ERR_CODE = 4;
	private static final int UNKNOWN_TRANSFER_ID_ERR_CODE = 5;

	private static final int SERVER_LISTENER = 69;
	private static final int INTERMEDIARY_LISTENER = 23;

	private boolean transfering = true;
	private boolean isNewData = true;
	private InetAddress serverAddress;
	private int serverPort;

	public Client() {
		try {
			sendReceiveSocket = new DatagramSocket(); // creat the datagram
			sendReceiveSocket.setSoTimeout(100000); // socket
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

		DatagramPacket requestPacket = buildRequest(fname.getBytes());

		// decide if it s a read or a write
		try {

			if (request == Decision.RRQ) {
				System.out.println("Client:" + fname + ", receive in " + mode + " mode.\n");
				read(requestPacket);

			} else if (request == Decision.WRQ) {
				System.out.println("Client:" + fname + ", send in " + mode + " mode.\n");
				write(requestPacket);
			}
		} catch (ErrorException e) {
			// Build the error

			System.out.println(e.getMessage());

			DatagramPacket err = buildError(e.getMessage().getBytes(), e.getErrorCode());

			// set port and address
			err.setAddress(serverAddress);
			err.setPort(serverPort);

			// Send error
			try {
				sendReceiveSocket.send(err);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * builds a request. Opcode must be modified to read or write.
	 * 
	 * @param filename
	 * @return
	 */
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
		requestPacket.setPort(SERVER_LISTENER);

		return requestPacket;
	}

	private DatagramPacket receiveData() throws IOException {

		byte[] data = new byte[PACKET_SIZE];

		DatagramPacket dataPacket = new DatagramPacket(data, data.length);

		sendReceiveSocket.receive(dataPacket);

		return dataPacket;
	}

	private boolean validateData(DatagramPacket dataPacket, int currentBlock) throws ErrorException {

		int opcode = ((dataPacket.getData()[0] & 0xff) << 8) | (dataPacket.getData()[1] & 0xff);
		int dataNumber = ((dataPacket.getData()[2] & 0xff) << 8) | (dataPacket.getData()[3] & 0xff);

		InetAddress dataAddress = dataPacket.getAddress();
		int dataPort = dataPacket.getPort();

		byte[] data = dataPacket.getData();

		// Check its data
		if (opcode == 3) {
			// is data
		} else if (opcode == 5) {
			// received error packet
			throw new ReceivedErrorException(dataPacket);
		} else {
			// Not data or error
			throw new ErrorException("Received an unexpected packet. Opcode: " + opcode, ILLEGAL_OPER_ERR_CODE);
		}

		// Check Address and port
		if (dataPort != serverPort || !dataAddress.equals(serverAddress)) {
			sendUnknownIDError(dataAddress, dataPort);
			return true;
		}

		// check the packet number matches what server is expecting
		if (dataNumber < currentBlock) {
			System.out.println("received duplicate data packet");
			isNewData = false;
			// ignore and send next data
		} else if (dataNumber > currentBlock) {
			System.out.println("received data from the future");
			throw new ErrorException("received data from the future", ILLEGAL_OPER_ERR_CODE);
		}

		System.out.println(data.length);
		if (data[data.length - 1] == (byte) 0) {
			transfering = false;
		}

		isNewData = true;
		return false;

	}

	/**
	 * Read request
	 * 
	 * @param request
	 * @throws IOException
	 * @throws ErrorException
	 */
	private void read(DatagramPacket request) throws IOException, ErrorException {

		request.getData()[1] = (byte) 1;

		try {
			sendReceiveSocket.send(request);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		byte[] incomingData = new byte[PACKET_SIZE];

		DatagramPacket dataPacket = new DatagramPacket(incomingData, incomingData.length);
		DatagramPacket ackPacket = buildAckPacket(0);

		File newFile = new File(CLIENT_DIRECTORY + fname);

		if (!newFile.exists()) {
			newFile.createNewFile();
		}

		BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(newFile));

		int expectedBlockNum = 1;

		// get data1 to save address and port

		dataPacket = receiveData();

		System.out.println("got packet: ");
		for (byte b : dataPacket.getData()) {
			System.out.print(b);
		}
		System.out.println();

		// save port and address of client
		serverAddress = dataPacket.getAddress();
		serverPort = dataPacket.getPort();

		// write data
		if (isNewData) {
			writer.write(incomingData, 4, DATA_SIZE);
		}
		// build and send ack
		ackPacket = buildAckPacket(expectedBlockNum);
		sendReceiveSocket.send(ackPacket);
		System.out.println("sending packet: ");
		for (byte b : ackPacket.getData()) {
			System.out.print(b);
		}
		System.out.println();

		expectedBlockNum++;

		do {
			// receive and validate data
			do {
				try {
					// Receive data packet
					dataPacket = receiveData();
					System.out.println("got packet: ");
					for (byte b : dataPacket.getData()) {
						System.out.print(b);
					}
					System.out.println();
				} catch (SocketTimeoutException e) {
					System.out.println(
							"Timeout receiving ack " + expectedBlockNum + " resending data " + expectedBlockNum);
				}

			} while (validateData(dataPacket, expectedBlockNum));

			serverAddress = dataPacket.getAddress();
			serverPort = dataPacket.getPort();

			incomingData = dataPacket.getData();
			int receivedblockNum = ((incomingData[2] & 0xff) << 8) | (incomingData[3] & 0xff);

			writer.write(incomingData, 4, DATA_SIZE);

			ackPacket = buildAckPacket(receivedblockNum);

			sendReceiveSocket.send(ackPacket);
			System.out.println("sending packet: ");
			for (byte b : ackPacket.getData()) {
				System.out.print(b);
			}
			System.out.println();

			expectedBlockNum++;
		} while (transfering);
		// transfer complete

		System.out.println("read finished");

		writer.close();

	}

	/**
	 * Write request
	 * 
	 * @param request
	 * @throws IOException
	 * @throws ErrorException
	 */
	private void write(DatagramPacket request) throws IOException, ErrorException {

		// Send write request
		request.getData()[1] = (byte) 2;
		sendReceiveSocket.send(request);

		// Receive ack 0
		DatagramPacket ackPacket = receiveAck();

		// check ack 0
		System.out.println("received: ");
		for (byte b : ackPacket.getData()) {
			System.out.print(b);
		}
		System.out.println();

		// Save server address and port
		serverPort = ackPacket.getPort();
		serverAddress = ackPacket.getAddress();

		// Set up data packet and stream to create files.
		byte[] dataForPacket = new byte[516];
		dataForPacket[0] = 0;
		dataForPacket[1] = 3;
		DatagramPacket dataPacket = new DatagramPacket(dataForPacket, dataForPacket.length, serverAddress, serverPort);

		in = new BufferedInputStream(new FileInputStream(CLIENT_DIRECTORY + fname));

		byte[] dataToSend = new byte[512];

		// Data 1 is read
		int sizeOfDataRead = in.read(dataToSend);

		System.out.println("data 1: " + sizeOfDataRead);
		for (byte b : dataToSend) {
			System.out.print(b);
		}
		System.out.println();

		int currentPacketNumber = 1;

		while (transfering) {
			// iterate the file in 512 byte chunks
			// Each iteration send the packet and receive the ack to match block
			// number i

			// Add block number to packet data
			dataForPacket[2] = (byte) ((currentPacketNumber >> 8) & 0xFF);
			dataForPacket[3] = (byte) (currentPacketNumber & 0xFF);

			// Copy the data from the file into the packet data
			System.arraycopy(dataToSend, 0, dataForPacket, 4, dataToSend.length);

			dataPacket.setData(dataForPacket);
			System.out.println("sending data " + currentPacketNumber + " of size: " + sizeOfDataRead);

			sendReceiveSocket.send(dataPacket);

			System.out.println("sent: ");

			for (byte b : dataPacket.getData()) {
				System.out.print(b);
			}

			System.out.println();

			// Receive ack packet

			do {
				try {
					ackPacket = receiveAck();

					System.out.println("received: ");

					for (byte b : ackPacket.getData()) {
						System.out.print(b);
					}

					System.out.println();

				} catch (SocketTimeoutException e) {
					System.out.println(
							"Timeout receiving ack " + currentPacketNumber + " resending data " + currentPacketNumber);
				}

			} while (validateAck(ackPacket, currentPacketNumber));

			System.out.println("received ack " + request.getData()[3]);
			dataToSend = new byte[512];

			currentPacketNumber++;
			sizeOfDataRead = in.read(dataToSend);
			if (sizeOfDataRead == -1) {
				// Trasnfering should end
				transfering = false;
			}
		}
		in.close();
	}

	/**
	 * Receives ack packet
	 * 
	 * @return
	 * @throws IOException
	 */
	private DatagramPacket receiveAck() throws IOException {
		byte[] data = new byte[4];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);

		sendReceiveSocket.receive(receivePacket);

		return receivePacket;
	}

	/**
	 * 
	 * @param ackPacket
	 * @param currentPacketNumber
	 * @return
	 */
	protected boolean validateAck(DatagramPacket ackPacket, int currentPacketNumber) throws ErrorException {

		int opcode = ((ackPacket.getData()[0] & 0xff) << 8) | (ackPacket.getData()[1] & 0xff);
		int ackNumber = ((ackPacket.getData()[2] & 0xff) << 8) | (ackPacket.getData()[3] & 0xff);

		InetAddress ackAddress = ackPacket.getAddress();
		int ackPort = ackPacket.getPort();

		// Check its an ack
		if (opcode == 4) {
			// is ack
		} else if (opcode == 5) {
			// received error packet
			throw new ReceivedErrorException(ackPacket);
		} else {
			// Not an ackPacket
			System.out.println("did not receive ack packet");
			throw new ErrorException("Received an unexpected packet. Opcode: " + opcode, ILLEGAL_OPER_ERR_CODE);
		}

		// Check Address and port
		if (ackPort != serverPort || !ackAddress.equals(serverAddress)) {
			sendUnknownIDError(ackAddress, ackPort);
			return true;
		}

		// check the packet number matches what server is expecting
		if (ackNumber < currentPacketNumber) {
			System.out.println(ackNumber + "  " + currentPacketNumber);
			System.out.println("received duplicate ack packet");
			return true;
			// ignore and send next data
		} else if (ackNumber > currentPacketNumber) {
			System.out.println("received ack from the future");
			throw new ErrorException("received ack from the future", ILLEGAL_OPER_ERR_CODE);
		}

		return false;

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

	protected DatagramPacket buildError(byte[] errMessage, int errCode) {

		byte[] errData = new byte[5 + errMessage.length];

		errData[0] = (byte) 0;
		errData[1] = (byte) 5;
		errData[2] = (byte) ((errCode >> 8) & 0xFF);
		errData[3] = (byte) (errCode & 0xFF);

		System.arraycopy(errMessage, 0, errData, 4, errMessage.length);

		DatagramPacket errPacket = new DatagramPacket(errData, errData.length);

		return errPacket;
	}

	protected void sendUnknownIDError(InetAddress add, int port) {
		UnknownIDException e = new UnknownIDException(add, port);
		DatagramPacket err = buildError(e.getMessage().getBytes(), e.getErrorCode());

		// Set address to other a
		err.setAddress(e.getAddress());
		err.setPort(e.getPort());

		// Send error
		try {
			sendReceiveSocket.send(err);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void exit() {
		sendReceiveSocket.close();
	}
}