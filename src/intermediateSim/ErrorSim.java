package intermediateSim;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
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
		this.initPacket = packet;
		this.blockNum = eS.blockNum; //Holds the block which the user wishes to alter
		this.opCode = eS.OpCode; //Holds the user input for Ack or DATA
		this.mode = eS.mode; //Holds the type of error the user wants to create
		this.corruption = eS.corruption;
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
		} catch (IOException e) {
			System.out.println("IO exception while attempting to receive packet");
			System.exit(1);
		
		}
		//Creates a byte array to parse the packet for opcode and block number
		byte[] packetData = receivePacket.getData();
		
		int opcode = ((packetData[0] & 0xff) << 8) | (packetData[1] & 0xff);
		int blockNumber = ((packetData[2] & 0xff) << 8) | (packetData[3] & 0xff);

		// If the server port is unknown, update it
		if (serverPort == -1) {
			serverPort = receivePacket.getPort();
		}

		// Passes the packet from server to client, and client to server.
		// Creates a new packet with the correct destination address
		if (receivePacket.getPort() == serverPort) {
			receivePacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
					receivePacket.getAddress(), clientPort);
		} else {
			receivePacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
					receivePacket.getAddress(), serverPort);
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
			} else if(mode == 04){
				receivePacket = newMode(receivePacket, corruption); //Change the mode to simulate an invalid opcode
				sendPacket(receivePacket);
				System.out.println("Opcode has been altered");
			}else if(mode == 05){
				sendErrorPacket(receivePacket);
				System.out.println("Sent from an invalid ID"); //Send from a different socket
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
			} else if(mode == 04){
				receivePacket = newMode(receivePacket, corruption);
				sendPacket(receivePacket);
				System.out.println("Opcode has been altered.");
			}else if(mode == 05){
				sendErrorPacket(receivePacket);
				System.out.println("Sent from an invalid ID.");
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
		if(corruption == 1){
		packetData[0] = 9;
		packetData[1] = 9;
		}
		//else if(corruption == 2){
			//System.arraycopy(packetData, 3, packetData, 50, 600);
		//}
		else if(corruption == 3){
			if (packetData[1] == 3){
				packetData[1] = 4;
			}
			else if(packetData[1] == 4){
				packetData[1] = 3;
			}
		}
		//else if(corruption == 4){
			//packetData = null;
		//}
		receivedPacket.setData(packetData);
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
		
		errorSocket.close();
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

		// Create the datagram packet for the request
	   DatagramPacket initialRequest = new DatagramPacket(initPacket.getData(), initPacket.getLength(),
			initPacket.getAddress(), INITIAL_SERVER_PORT);
		sendPacket(initialRequest);

		// Loop until the send and receive method is finished
		boolean cont = true;
		while (cont) {
			cont = sendReceive();
		}

		
		// Operation finished, close the socket.
		sendReceiveSocket.close();
		
	}

}