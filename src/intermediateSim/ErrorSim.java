package intermediateSim;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Scanner;

public class ErrorSim implements Runnable {

	private static final int delay = 110000; //very large delay only for testing purposes
	private Scanner reader;
	private DatagramSocket sendReceiveSocket;
	private DatagramPacket initPacket;
	private int clientPort;
	private int serverPort = -1;
	private int blockNum = 0;
	private boolean errorFlag = false, normal = false, duplicate = false, lost = false, errorSent = false; // delay
																											// =

	private Mode mode = Mode.NORMAL;

	public static enum OpCode {
		ACK, DATA
	};

	public static enum Mode {
		NORMAL, DELAY, LOSE, DUPLICATE
	};

	public ErrorSim(DatagramPacket packet, Scanner reader) {
		this.clientPort = packet.getPort();
		this.initPacket = packet;
		this.reader = reader;

		try {
			sendReceiveSocket = new DatagramSocket();

		} catch (SocketException se) {
			System.out.println("Socket Exception on ErrorSim");
			System.exit(1);
		}
	}

	public void Delay(DatagramPacket data) {
		Thread requests = new Thread(new Delay(delay, data, this));
		requests.start();

		// packetDelay = false;
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

	public void menu() throws IOException {

		OpCode request = OpCode.ACK; // default decision which is Read

		reader = new Scanner(System.in); // run a new scanner to scan the input
											// from the user
		System.out.print("\nError Simulator \n");
		System.out.println("choose (A) to alter ACK packet, (D) to alter DATA packet");
		String choice = reader.nextLine(); // reads the input String

		// it runs threw all the possible answers if none are applicable it
		// recursively go back to menu()
		if (choice.equalsIgnoreCase("A")) {
			request = OpCode.ACK;
			System.out.println("ACK packet selected");
		} else if (choice.equalsIgnoreCase("D")) {
			request = OpCode.DATA;
			System.out.println("DATA packet selected");
		} else {
			System.out.println("invalid choice.  Please try again...");
			menu();
		}

		// selects block, loops until a valid number selected
		System.out.println("Please enter the block number");
		while (true) {
			try {
				blockNum = Integer.parseInt(reader.nextLine());
			} catch (NumberFormatException n) {
				System.out.println("Not a number, please try again");
				continue;
			}
			break;
		}

		System.out.println("\nPlease enter the mode\n");
		System.out.println("00: Normal Operation\n");
		System.out.println("01: Lose the packet\n");
		System.out.println("02: Delay a packet\n");
		System.out.println("03: Duplicate a packet\n");
		String modeSelected = reader.nextLine(); // reads the input String

		// it runs threw all the possible answers if none are applicable it
		if (modeSelected.equalsIgnoreCase("00")) {
			mode = Mode.NORMAL;
			System.out.println("Normal mode selected.");
		} else if (modeSelected.equalsIgnoreCase("01")) {
			mode = Mode.LOSE;
			System.out.println("Losing the packet");
		} else if (modeSelected.equalsIgnoreCase("02")) {
			mode = Mode.DELAY;
			System.out.println("Delaying the packet");
		} else if (modeSelected.equalsIgnoreCase("03")) {
			mode = Mode.DUPLICATE;
			System.out.println("Duplicating the packet");
		} else {
			System.out.println("invalid choice. Continuing in normal Mode.");
		}
	}

	public boolean sendReceive() {
		byte data[] = new byte[516];

		// Create a new receive packet
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);

		// Wait until a new packet is received
		try {
			sendReceiveSocket.receive(receivePacket);
		} catch (IOException e) {
			System.out.println("IO exception while attempting to receive packet");
			System.exit(1);
		}

		byte[] packetData = receivePacket.getData();

		int opcode = ((packetData[0] & 0xff) << 8) | (packetData[1] & 0xff);
		int blockNumber = ((packetData[2] & 0xff) << 8) | (packetData[3] & 0xff);

		// Update the port if it hasn't been already
		if (serverPort == -1) {
			serverPort = receivePacket.getPort();
		}

		// If the data packet was received from the server send it to the client
		// and vice versa
		// Builds packet to be passed on
		if (receivePacket.getPort() == serverPort) {
			receivePacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
					receivePacket.getAddress(), clientPort);
		} else {
			receivePacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
					receivePacket.getAddress(), serverPort);
		}

		// data
		if (blockNumber == blockNum && opcode == 3) {
			if (mode == Mode.LOSE) {
				System.out.println("Packet lost.");
			} else if (mode == Mode.DUPLICATE) {
				System.out.println("Duplicate packet sent.");
				sendPacket(receivePacket);
				sendPacket(receivePacket);
			} else if (mode == Mode.DELAY) {
				System.out.println("Delayed the packet for 2.5 seconds");
				Delay(receivePacket);
			} else {
				sendPacket(receivePacket);
			}
		}
		// ack
		else if (blockNumber == blockNum && opcode == 4) {
			if (mode == Mode.LOSE) {
				System.out.println("Packet lost.");
			} else if (mode == Mode.DUPLICATE) {
				System.out.println("Duplicate packet sent.");
				sendPacket(receivePacket);
				sendPacket(receivePacket);
			} else if (mode == Mode.DELAY) {
				System.out.println("Delayed the packet for 2.5 seconds");
				Delay(receivePacket);
			} else {
				sendPacket(receivePacket);
			}
		} else {
			sendPacket(receivePacket);
		}
		//reset the mode to normal
	
		return true;
	}

	// used to send packets from the ErrorSim
	public void sendPacket(DatagramPacket packet) {
		try {
			sendReceiveSocket.send(packet);
		} catch (IOException e) {
			System.out.println("IO exception while attempting to send packet");
		}
	}

	@Override
	public void run() {
		// Initialize the variables
		String input;

		// Get the user input
		try {
			menu();
		
		} catch (IOException e) {
			System.out.println("Error getting user input");
			e.printStackTrace();
		}

		// Create the datagram packet for the request
		DatagramPacket initialRequest = new DatagramPacket(initPacket.getData(), initPacket.getLength(),
				initPacket.getAddress(), 69);

		sendPacket(initialRequest);

		// Loop until the send and receive method is finished
		boolean cont = true;
		while (cont) {
			cont = sendReceive();
		}

		
		// We're finished, so close the sockets.
		sendReceiveSocket.close();
	}

}