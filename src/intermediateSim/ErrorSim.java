package intermediateSim;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * TFTP Error Simulator that can affects the selected ACK or DATA packet, then
 * sends it to its respective destination.
 * 
 * Possible Errors that can be simulated are: -Losing a packet -Delaying a
 * packet -Duplicating a packet -Invalid Opcode -Unknown transfer ID
 * 
 * @author Luke Sanderson - Team 5
 *
 *         Systems and Computer Engineering, Carleton University
 * @version 2.0
 *
 */
public class ErrorSim implements Runnable {

	private static final int DELAY = 2500; //2.5 second delay
	private static final int DEFAULT_PACKET_SIZE = 516;
	private static final int INITIAL_SERVER_PORT = 69;
	private static final int DATA = 3;
	private static final int ACK = 4;
	private DatagramSocket sendReceiveSocket;
	private DatagramSocket errorSocket;
	private DatagramPacket initPacket;
	private int clientPort;
	private int serverPort = -1;
	private int blockNum;
	private int opCode;
	private int mode;
	private int corruption;
	private boolean newError = true;
	private InetAddress serverIP;
	private InetAddress clientIP;
	private boolean corruptFirst = false;
	private ErrorSelect eS;
	/**
	 * Constructor for the ErrorSim that is passed the initial packet and client port,
	 * as well as the user input need to select which error to create.	
	 * 
	 * @param packet
	 * @param reader
	 * @param eS
	 */
	public ErrorSim(DatagramPacket packet, Scanner reader, ErrorSelect eS) {
		this.clientPort = packet.getPort();
		this.clientIP = packet.getAddress();
		this.initPacket = packet;
		this.blockNum = eS.blockNum; //Holds the block which the user wishes to alter
		this.opCode = eS.OpCode; //Holds the user input for Ack or DATA
		this.mode = eS.mode; //Holds the type of error the user wants to create
		this.corruption = eS.corruption;
		this.serverIP = eS.serverAddress;
		this.corruptFirst = eS.firstPacket;
		this.eS = eS;
		
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {
			System.out.println("Socket Exception on ErrorSim");
			System.exit(1);
		}
		
		
	}

	/**
	 * Creates a new thread for the requested packet delay
	 * 
	 * @param data
	 */
	public void Delay(DatagramPacket data) {
		Thread requests = new Thread(new Delay(DELAY, data, this));
		requests.start();
	}

	
	/**
	 * 
	 * Passes packets between the client and server, and 
	 * generates an error on the packet chosen by the user input, when the packet is received.
	 * 
	 * @return boolean
	 */
	public boolean sendReceive() {
		
		byte data[] = new byte[DEFAULT_PACKET_SIZE];
		// Create a new receive packet
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);
		// Waits until a new packet is received
		try {
			sendReceiveSocket.receive(receivePacket);
			sendReceiveSocket.setSoTimeout(6500);
		} catch (IOException e) {
			if(e instanceof SocketTimeoutException){
				System.out.println("No more packets detected.");
				return false;
			}else{
			System.out.println("IO exception while attempting to receive packet");
			}
			
		}
		//Creates a byte array to parse the packet for opcode and block number
		byte[] packetData = receivePacket.getData();
	
		int opcode = ((packetData[0] & 0xff) << 8) | (packetData[1] & 0xff);
		int blockNumber = ((packetData[2] & 0xff) << 8) | (packetData[3] & 0xff);

		// If the server port is unknown, update it
		if (serverPort == -1) {
			serverPort = receivePacket.getPort();
		}
		
		String currentPort;
		// Passes the packet from server to client, and client to server.
		// Creates a new packet with the correct destination address
		if (receivePacket.getPort() == serverPort) {
			receivePacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
					clientIP, clientPort);
			currentPort = "server";
		} else {
			receivePacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
					serverIP, serverPort);
			currentPort = "client";
		}
		if(eS.verboseMode){
		System.out.println("received packet from " + currentPort + " containing:");
		for (byte b : receivePacket.getData()) {
			System.out.print(b);
		}
		System.out.println();
		}
		//Checks if the packet is Data packet, and the block number we want,
		//then generates the selected error if we wanted to.
		if (blockNumber == blockNum && opcode == DATA && opCode == 02 && newError) {
			if (mode == 01) {
				System.out.println("Packet lost."); //Dont send anything to simulate losing a packet
			} else if (mode == 03) {
				System.out.println("Duplicate packet sent."); //Send the packet twice to simulate duplicating
				sendPacket(receivePacket);
				sendPacket(receivePacket);
			} else if (mode == 02) {
				System.out.println("Delayed the packet for 2.5 seconds"); //Delay the packet 2.5 seconds
				Delay(receivePacket);
			} else if(mode == 04 && corruption != 4){
				System.out.println("Corrupting the packet of block#: " + blockNum);
				receivePacket = newMode(receivePacket, corruption); //Change the mode to simulate an invalid opcode
				sendPacket(receivePacket);
			}else if(mode == 05){
				sendErrorPacket(receivePacket);
				System.out.println("/nSent from an invalid ID"); //Send from a different socket
			}else { //send normally
				sendPacket(receivePacket);
			}
			newError = false;
		
		}
		//If the packet is an ACK packet, and the block number we want,
		//then generates the selected error if we wanted to.
		else if (blockNumber == blockNum && opcode == ACK && opCode == 01 && newError) {
			if (mode == 01) {
				System.out.println("Packet lost.");
			} else if (mode == 03) {
				System.out.println("Duplicate packet sent.");
				sendPacket(receivePacket);
				sendPacket(receivePacket);
			} else if (mode == 02) {
				System.out.println("Delayed the packet for 2.5 seconds");
				Delay(receivePacket);
			} else if(mode == 04 && corruption != 4){
				System.out.println("Corrupting the packet of block#: " + blockNum);
				receivePacket = newMode(receivePacket, corruption);
				sendPacket(receivePacket);
			}else if(mode == 05){
				sendErrorPacket(receivePacket);
				System.out.println("/nSent from an invalid ID.");
			} else { //send normally
				sendPacket(receivePacket);
			}
			newError = false;
			
		} else { //If it is not the packet we are looking to change, send it normally
			sendPacket(receivePacket);
		}
		return true;
	}

	
	/**
	 * Changes the opcode to an invalid operation.
	 * Note: There are multiple ways to generate an invalid operation, 
	 * but currently we generate the error only by invalidating the opcode.
	 * 
	 * @param receivedPacket
	 * @param corruption
	 * @return receivedPacket
	 */
	private DatagramPacket newMode(DatagramPacket receivedPacket, int corruption){
		byte[] packetData = receivedPacket.getData();
		if(corruption == 1){ //opcode
		packetData[0] = 9;
		packetData[1] = 9;
		}
		else if(corruption == 2){ //block num
		packetData[2] = -1;
		packetData[3] = 9;
		}
		else if(corruption == 3){ //change mode
			if (packetData[1] == 3){
				packetData[1] = 4;
			}
			else if(packetData[1] == 4){
				packetData[1] = 3;
			}
		}
		else if(corruption == 4){
			
			for (int i = 2; i < packetData.length; i++) {
				if (packetData[i] == (byte) 0) {
					int x = i + 1;
					for (int y = x; y < packetData.length; y++) {
						if (packetData[y] == (byte) 0) {
							break;
						}
						packetData[y] = 9;
						}
					}
				}
		}
		receivedPacket.setData(packetData);//length of packet	
		return receivedPacket;
	}
	
	/**
	 * Sends the packet from a different socket, to simulate an unknown transfer ID
	 * 
	 * @param packet
	 */
	private void sendErrorPacket(DatagramPacket packet){
		try {
			errorSocket = new DatagramSocket(); //Used to send from an unknown ID
		} catch (SocketException se) {
			System.out.println("Socket Exception on Error socket");
			System.exit(1);
		}
		try {
			errorSocket.send(packet);
		} catch (IOException e) {
			System.out.println("IO exception while attempting to send error packet");
		}
		try{
			errorSocket.receive(packet);
			if(eS.verboseMode){
			System.out.println("received error packet containing: ");
			for (byte b : packet.getData()) {
				System.out.print(b);
			}
			}
		}catch (IOException e){
			System.out.println("IO exception while attempting to receive error packet");
		}
		errorSocket.close();
	}
	String sentPacket = "server";
	// used to send packets from the ErrorSim
	public void sendPacket(DatagramPacket packet) {
		
		if(packet.getPort() == serverPort){
			sentPacket = "server";
		}else if(packet.getPort() == clientPort){
			sentPacket = "client";
		}
		try {
			sendReceiveSocket.send(packet);
			if(eS.verboseMode){
			System.out.println("sent packet to " + sentPacket);
			}
		} catch (IOException e) {
			System.out.println("IO exception while attempting to send packet");
		}
	}

	@Override
	public void run() {
	
		// Create the datagram packet for the request
	   DatagramPacket initialRequest = new DatagramPacket(initPacket.getData(), initPacket.getLength(),
			serverIP, INITIAL_SERVER_PORT);
	   if(corruptFirst){
		   System.out.println("corrupting initial packet");
		   initialRequest = newMode(initialRequest, 4);
		   sendPacket(initialRequest);
	   }
	   else{
		   sendPacket(initialRequest);
	   }
		// Loop until the send and receive method is finished
		boolean cont = true;
		while (cont) {
			cont = sendReceive();
		}

		
		// Operation finished, close the socket.
		sendReceiveSocket.close();
		System.out.println("Transfer has finished.");
		
		
	}

}