package intermediateSim;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

public class ErrorSim implements Runnable {

	private static final int delay = 110000; //very large delay only for testing purposes
	private DatagramSocket sendReceiveSocket;
	private DatagramSocket errorSocket;
	private DatagramPacket initPacket;
	private int clientPort;
	private int serverPort = -1;
	private int blockNum;
	private int opCode;
	private int mode;
	
	
	
	//private boolean errorFlag = false, normal = false, duplicate = false, lost = false, errorSent = false; // delay
																											
	public ErrorSim(DatagramPacket packet, Scanner reader, ErrorSelect eS) {
		this.clientPort = packet.getPort();
		this.initPacket = packet;
		this.blockNum = eS.blockNum;
		this.opCode = eS.OpCode;
		this.mode = eS.mode;
		
		try {
			sendReceiveSocket = new DatagramSocket();
			errorSocket = new DatagramSocket();
			
		} catch (SocketException se) {
			System.out.println("Socket Exception on ErrorSim");
			System.exit(1);
		}
		
	}

	public void Delay(DatagramPacket data) {
		Thread requests = new Thread(new Delay(delay, data, this));
		requests.start();
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
		
		if (blockNumber == blockNum && opcode == 3 && opCode == 02) {
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
				receivePacket = newMode(receivePacket);
				sendPacket(receivePacket);
				System.out.println("Opcode has been altered");
			}else if(mode == 05){
				sendErrorPacket(receivePacket);
				System.out.println("Sent from an invalid ID");
			}else { //send normally
				sendPacket(receivePacket);
			}
		
		}
		// ack
		else if (blockNumber == blockNum && opcode == 4 && opCode == 01) {
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
				receivePacket = newMode(receivePacket);
				sendPacket(receivePacket);
				System.out.println("Opcode has been altered");
			}else if(mode == 05){
				sendErrorPacket(receivePacket);
				System.out.println("Sent from an invalid ID");
			} else { //send normally
				sendPacket(receivePacket);
			}
		} else {
			sendPacket(receivePacket);
		}
		
		
		return true;
	}
	private DatagramPacket newMode(DatagramPacket receivedPacket){
		byte[] packetData = receivedPacket.getData();
		packetData[0] = 9;
		packetData[1] = 9;
		receivedPacket.setData(packetData);
		return receivedPacket;
	}
	
	private void sendErrorPacket(DatagramPacket packet){
		try {
			errorSocket.send(packet);
		} catch (IOException e) {
			System.out.println("IO exception while attempting to send error packet");
		}
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
			initPacket.getAddress(), 5000);
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