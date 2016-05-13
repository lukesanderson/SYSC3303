package TFTP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Client {

	private DatagramSocket sock;

	public Client() {

		try {
			// Creates socket and sets timeout to 5 seconds
			sock = new DatagramSocket();
			sock.setSoTimeout(5000);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Creates a packet and sends it to port 23 Prints the data being sent
	 * 
	 * @param data
	 */
	private void send(byte[] data) {

		try {
			DatagramPacket pack = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 69);

			System.out.println("sending: ");
			for (byte g : pack.getData()) { // print data in bytes
				System.out.print((int) g);
			}
			System.out.println();
			for (byte g : pack.getData()) { // Print data in string
				System.out.print((char) g);
			}

			sock.send(pack);

		} catch (IOException e) {
			System.out.println("Error sending request");
		}

	}

	/**
	 * Receive response from server
	 * 
	 * @return the packet received
	 * @throws IOException
	 */
	private DatagramPacket receive() throws IOException {
		byte[] data = new byte[4];
		DatagramPacket serverResponse = new DatagramPacket(data, data.length);
		sock.receive(serverResponse);
		return serverResponse;
	}

	/**
	 * Closes the datagramSocket
	 */
	private void exit() {
		sock.close();
	}

	public static void main(String[] args) {

		Client cli = new Client();

		byte[] fileName = "test.txt".getBytes();
		byte[] text = "octet".getBytes();

		// Create data for read and write in byte arrays
		byte[] writeData = new byte[2 + fileName.length + 1 + text.length + 1];
		writeData[0] = 0;
		writeData[1] = 2;
		System.arraycopy(fileName, 0, writeData, 2, fileName.length);
		writeData[2 + fileName.length] = 0;
		System.arraycopy(text, 0, writeData, 3 + fileName.length, text.length);
		byte[] readData = writeData.clone();
		readData[1] = (byte) 1;
		
		
		
		
		cli.send(writeData);
		

//		// Send 10 read and write requests and one invalid request
//		for (int i = 0; i < 10; i++) {
//			if (i < 5) {
//				cli.send(readData);
//			} else {
//				cli.send(writeData);
//			}
//
//			DatagramPacket serverResponse = null;
//			try {
//				serverResponse = cli.receive();
//			} catch (IOException e) {
//				System.out.println("No response received");
//				break;
//			}
//
//			// Print server's response
//			byte[] data = serverResponse.getData();
//			System.out.print("Server responded with: ");
//			for (byte g : data) {
//				System.out.print((int) g);
//			}
//			System.out.println();
//		}
//
//		System.out.println("sending invalid request");
//		cli.send(fileName);
//		System.out.println();
//		try {
//			cli.receive();
//		} catch (IOException e) {
//			System.out.println("No response");
//		}

		cli.exit();

	}

}
