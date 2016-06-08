package intermediateSim;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Initializes all new Error simulator threads after receiving an initial
 * packet. The new ErrorSim threads are passed the initial packet, and an
 * instance of the user input and the scanner.
 * 
 * 
 * @author Luke Sanderson - Team 5 Systems and Computer Engineering, Carleton
 *         University
 * @version 2.0
 *
 */
public class ErrorInit {

	private DatagramSocket receiveSocket;
	private static final Scanner READER = new Scanner(System.in);
	private static final int DEFAULT_PACKET_SIZE = 516;
	private static final int INITIAL_SOCKET = 23;

	public ErrorInit() {

		// Setup Port 2300 to receive initial packets for client request
		try {
			receiveSocket = new DatagramSocket(INITIAL_SOCKET);
		} catch (SocketException se) {
			System.out.println("Socket Exception on port: " + INITIAL_SOCKET);
			System.exit(1);
		}
	}

	/**
	 * Listens on the initial socket, waiting for packets from client. Creates
	 * an ErrorSim thread when received.
	 */
	public void receiveAndEcho() {
		int clientPort;
		/**
		ErrorSelect eS = new ErrorSelect(); // The user input used to select
											// which kind of error the user
											// wants to simulate

		try {
			eS.menu();
		} catch (IOException e1) {
			System.out.println("IOException while trying to receive user input");
			e1.printStackTrace();
		}
		VerboseQuiet vQ = new VerboseQuiet(eS.verboseMode);
		vQ.printThis(true, "\nError Simulator started. Waiting to receive packet on port: " + INITIAL_SOCKET);
		**/
		
		
			ErrorSelect eS = new ErrorSelect(); // The user input used to select
			// which kind of error the user
			// wants to simulate

			try {
				eS.menu();
			} catch (IOException e1) {
				System.out.println("IOException while trying to receive user input");
				e1.printStackTrace();
			}
			VerboseQuiet vQ = new VerboseQuiet(eS.verboseMode);
			vQ.printThis(true, "\nError Simulator started. Waiting to receive packet on port: " + INITIAL_SOCKET);
			
			
			// Create the receive packet for the request
			byte data[] = new byte[DEFAULT_PACKET_SIZE];
			DatagramPacket receivePacket = new DatagramPacket(data, data.length);

			// Block until a datagram packet is received from receiveSocket.
			try {
				receiveSocket.receive(receivePacket);
			} catch (IOException e) {
				System.out.println("Exception on receive socket");
				
			}
			// Process the received datagram.
			vQ.printThis(false, "\nPacket received from host.");
			vQ.printThis(false, "\nHost address: " + receivePacket.getAddress());
			clientPort = receivePacket.getPort();
			vQ.printThis(false, "\nHost port: " + clientPort);
			vQ.printThis(false, "\nLength: " + receivePacket.getLength());
			vQ.printThis(false, "\nContaining: \n");
			// vQ.printThis(false, new String(receivePacket.getData()));

			if (eS.verboseMode) {
				for (int j = 0; j < receivePacket.getLength(); j++) {
					System.out.print(data[j]);
				}
			}

			// New Thread for Error Sim
			System.out.println("\nCreating new Error Simulator Thread to handle request");
			Thread t = new Thread(new ErrorSim(receivePacket, READER, eS));
			t.start();
		
			
		

		READER.close();
		receiveSocket.close();
	}

	// Create a ErrorInit thread to handle initial setup
	public static void main(String args[]) {
		ErrorInit e = new ErrorInit();
		e.receiveAndEcho();
	}
	
	
}