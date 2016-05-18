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

public class Client {
	DatagramPacket sendPacket, receivePacket; // creat two DatagramPacket to
												// send and receive data from
												// and to the ErrSim
	DatagramSocket sendReceiveSocket; // We only need one datagramsocket since
										// we are never //sending and receiving
										// at the same time
	private Scanner input;
	private BufferedInputStream in; // stream to read in file
	private static int Past_dataBlockNumber = 0;
	private static int pastBlockNumber = 0;

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

	private boolean transfering = true;
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
		if (request == Decision.RRQ) {
			System.out.println("Client:" + fname + ", receive in " + mode + " mode.\n");
			read(requestPacket);

		} else if (request == Decision.WRQ) {
			System.out.println("Client:" + fname + ", send in " + mode + " mode.\n");
			write(requestPacket);
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
		requestPacket.setPort(23);

		return requestPacket;
	}

	/**
	 * Read request
	 * 
	 * @param request
	 * @throws IOException
	 */
	private void read(DatagramPacket request) throws IOException {

		request.getData()[1] = (byte) 1;

		try {
			sendReceiveSocket.send(request);
		} catch (IOException e2) {
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

			if (!(blockNumber == pastBlockNumber + 1)) {

				if (blockNumber == pastBlockNumber) { // we got a duplicate
					// packet

					input = new Scanner(System.in); // run a new scanner to scan
													// the
					// input
					System.out.println(
							"A duplicate block has been detected\n would you like to continue the transfer: [y/N]");
					String choice = input.nextLine(); // reads the input String
					if (choice == "y" || choice == "Y")
						continue;
					else {
						System.out.println(
								"Data transfer has stopped.\n\n Would you like to start a new data transfer: [y/N]");
						choice = input.nextLine();
						if (choice == "y" || choice == "Y") {
							inter();
						} else {
							System.out.println("Thank you!!!");
							System.exit(0);
						}
					}
					// from the user
				} else {
					input = new Scanner(System.in); // run a new scanner to scan
													// the
					// input
					System.out.println(
							"A Lost packet has been detected\n would you like to continue the transfer: [y/N]");
					String choice = input.nextLine(); // reads the input String
					if (choice.equalsIgnoreCase("y")) {
						System.out.println("Continuing Data transfer.\nLoading....\n");
						continue;
					} else {
						System.out.println(
								"Data transfer has stopped.\n\n Would you like to start a new	 data transfer: [y/N]");
						choice = input.nextLine();
						if (choice.equalsIgnoreCase("y")) {
							inter();
						} else {
							System.out.println("Thank you!!!");
							System.exit(0);

						}
					}
				}

			}
			if (!validate(incomingPacket)) {
				// invalid packet
			}

			writer.write(incomingData, 4, DATA_SIZE);

			ackPacket = buildAckPacket(blockNumber);

			sendReceiveSocket.send(ackPacket);
			pastBlockNumber = blockNumber;
		} while (transfering);
		// transfer complete

		System.out.println("Finished");
		writer.close();

	}

	/**
	 * Write request
	 * 
	 * @param request
	 * @throws IOException
	 */
	private void write(DatagramPacket request) throws IOException {

		request.getData()[1] = (byte) 2;

		try {
			sendReceiveSocket.send(request);
		} catch (IOException e2) {
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

		in = new BufferedInputStream(new FileInputStream(CLIENT_DIRECTORY + fname));

		byte[] dataToSend = new byte[512];
		int n = in.read(dataToSend);
		int i = 1;

		do {
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

			try {
				request = receiveAck();
			} catch (SocketTimeoutException e) {
				System.out.println("timed out. resending data " + i);
				continue;
			}

			System.out.println("received ack " + request.getData()[3]);

			dataToSend = new byte[512];
			i++;
		} while ((n = in.read(dataToSend)) != -1);

		System.out.println("Finished sending file");
		in.close();
	}

	/**
	 * Receives an ack packet and validates it
	 * 
	 * @return
	 * @throws IOException
	 */
	private DatagramPacket receiveAck() throws IOException {
		byte[] data = new byte[4];
		Byte B;
		DatagramPacket receivePacket = null;
		receivePacket = new DatagramPacket(data, data.length);

		sendReceiveSocket.receive(receivePacket);

		data = receivePacket.getData();

		B = new Byte(data[3]);

		if (!(B == Past_dataBlockNumber + 1)) {
			if (B.intValue() == Past_dataBlockNumber) {
				input = new Scanner(System.in); // run a new scanner to scan the
				// input
				System.out.println(
						"A duplicate block has been detected\n would you like to continue the transfer: [y/N]");
				String choice = input.nextLine(); // reads the input String
				if (choice.equalsIgnoreCase("y")) {
					;
				} else {
					System.out.println(
							"Data transfer has stopped.\n\n Would you like to start a new data transfer: [y/N]");
					choice = input.nextLine();
					if (choice.equalsIgnoreCase("y")) {
						try {
							inter();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						System.out.println("Thank you!!!");
						System.exit(0);

					}
				}

			} // end of check if duplicate

			else {

				input = new Scanner(System.in); // run a new scanner to scan the
				// input
				System.out.println("A Lost packet has been detected\n would you like to continue the transfer: [y/N]");
				String choice = input.nextLine(); // reads the input String
				if (choice.equalsIgnoreCase("y"))
					System.out.println("Continuing Data transfer.\nLoading....\n");
				else {
					System.out.println(
							"Data transfer has stopped.\n\n Would you like to start a new data transfer: [y/N]");
					choice = input.nextLine();
					if (choice.equalsIgnoreCase("y")) {
						try {
							inter();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						System.out.println("Thank you!!!");
						System.exit(0);

					}
				}
			}

		}

		Past_dataBlockNumber = B.intValue();

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

	public void exit() {
		sendReceiveSocket.close();
	}
}