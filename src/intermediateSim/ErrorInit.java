package intermediateSim;

import java.io.*; 
import java.net.*;
import java.util.Scanner;

/**
 * Error simulator that listens on port 23 for new client requests, then creates the new ERRSIM threads for the
 * requests.
 *
 */
public class ErrorInit {
	
	private DatagramSocket receiveSocket;
	private static final Scanner READER = new Scanner(System.in);

	public ErrorInit() {
 
        //Setup Port 23 to receive initial packets for client request
		try {
			receiveSocket = new DatagramSocket(23);
		} catch (SocketException se) {
			System.out.println("Socket Exception on port 23");
			System.exit(1);
		} 
	}
	
	/**
	 * Listens for new client requests and creates an ErrorSimThread thread
     * to deal with each new request.
	 */
	public void receiveAndEcho() {
		int clientPort, j=0;
		System.out.println("Error Simulator started. Waiting to receive packet...");
      
		while(true){
            // Create the receive packet for the request
            byte data[] = new byte[516];
			DatagramPacket receivePacket = new DatagramPacket(data, data.length);

			// Block until a datagram packet is received from receiveSocket.
			try {        
				receiveSocket.receive(receivePacket);
			} catch (IOException e) {
				System.out.println("Exception on receive socket");
				break;
			}

			// Need to print out data in received packet**
			// Process the received datagram.
            System.out.println("Simulator: Packet received:");
            System.out.println("From host: " + receivePacket.getAddress());
            clientPort = receivePacket.getPort();
            System.out.println("Host port: " + clientPort);
            System.out.println("Length: " + receivePacket.getLength());
            System.out.println("Containing: " );
            
         // print the bytes
            /*
            for (j=0;j<receivePacket.getLength();j++) {
                System.out.print("byte " + j + " " + data[j]);
            }
            */
            for (byte b : receivePacket.getData()) {
				System.out.print(b);
			}

            // Form a String from the byte array, and print the string.
            String received = new String(data,0,receivePacket.getLength());
            System.out.println(received);

            // New Thread for Error Sim
			System.out.println("Creating new Error Simulator Thread");
			Thread t = new Thread(new ErrorSim(receivePacket, READER));
			t.start();
		}

        READER.close();
		receiveSocket.close();
	}
	
	public static void main(String args[]) {
		ErrorInit e = new ErrorInit();
		e.receiveAndEcho();
	}
}