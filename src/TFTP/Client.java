package TFTP;
/* Client class for Iteration 1
 * Team 5 - 5000000
 * @author: team 5
 */

/* the following code deals with the client part of this exercise.
 *in the following exercise the client is send a Read Write or Test message to the Errsim which then be sended to the server
 * further explanation about how the connection between the errSim and the server will is explained in the two other classes.*/
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
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

	private static String s;
	private static String fname;
	private int errorno, packetdata_length = 0;
	private int sendPort = 69;
	
	private static final String CLIENT_DIRECTORY = "C:\\Users\\Public\\";
	

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
		if (f.exists() && !f.isDirectory()) {
			// do something

			DatagramPacket requestPacket = buildRequest(fname.getBytes());

			// decide if it s a read or a write
			if (request == Decision.RRQ) {
				System.out.println("Client:" + fname + ", receive in " + mode + " mode.\n");
				read();

			} else if (request == Decision.WRQ) {
				System.out.println("Client:" + fname + ", send in " + mode + " mode.\n");
				write(requestPacket);
			}

		} else {
			// if no file exists with that name ask them to try again
			System.out.println("That file does not exist.\n");
			inter();
		}

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

	private void read() {
		ArrayList<Byte> finalData = new ArrayList<Byte>();
		OutputStream os = null;
		byte[] data, data_noopcode;

		File file = new File("client_files\\" + fname);

		try {
			os = new FileOutputStream(file);
		} catch (FileNotFoundException e) {

			System.err.println("ERROR! Failed to initialize OutputStream Object");
		}
		int x = 0;
		for (;;) {

			data = receiveDataPacket();
			data_noopcode = Arrays.copyOfRange(data, 4, packetdata_length);

			for (byte b : data_noopcode)
				finalData.add(b);

			sendAck(data[3]++);
			System.out.println("THE PACKETDATA_LENGTH VALUE IS: " + packetdata_length);
			if (packetdata_length < 512) {
				break;
			}
		}

		byte[] finalData_barr = new byte[finalData.size()];
		int z = 0;

		for (byte b : finalData) {
			finalData_barr[z] = b;
			z++;
		}

		try {
			os.write(finalData_barr, 0, finalData_barr.length);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to write to the OutputStream");
		}

		System.out.println("Done. File was successfully read");
		try {
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("ERROR! Failed to close the OutputStream.");
		}
		ftSocket.close();
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


		in = new BufferedInputStream(new FileInputStream(CLIENT_DIRECTORY+ fname));

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
			
			for(byte b : dataForPacket){
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
		

		// int ssPort = ack0.getPort();
		// InetAddress ssAddress = ack0.getAddress();
		InputStream reader = null;

		// byte[] stuff = new byte[516];
		//
		// for (int i = 3; i < 512; i++) {
		// stuff[i] = (byte) i;
		// }
		//
		// stuff[0] = 0;
		// stuff[1] = 3;
		// stuff[2] = 0;
		// stuff[3] = 1;
		//
		// DatagramPacket data1 = new DatagramPacket(stuff, stuff.length);
		//
		// data1.setPort(ssPort);
		// data1.setAddress(ssAddress);
		//
		// try {
		// sendReceiveSocket.send(data1);
		// System.out.println("Sent data");
		//
		// sendReceiveSocket.receive(ack0);
		// System.out.println("got ack # " + ack0.getData()[3]);
		//
		// } catch (IOException e2) {
		// // TODO Auto-generated catch block
		// e2.printStackTrace();
		// }
		//
		// File file = new File("client_files\\" + fname);
		//
		// if (!file.canRead()) {
		//
		// errorno = 2;
		// senderror(errorno);
		//
		// }
		// try {
		// reader = new FileInputStream(file);
		// } catch (FileNotFoundException e1) {
		// //
		// }
		//
		// // Create a new ArrayList of Bytes to store our data
		// // Create n integer to indicate how many bytes we have read from the
		// // file.
		// // Create and initialize dataop ( Data packet opcode array) to size 4
		// // Create and initialize byte array data to a size of 512 bytes.
		// Byte[] arr = null;
		// int n, i = 0;
		// byte[] dataop = new byte[4];
		// dataop[0] = 0;
		// dataop[1] = 3;
		// dataop[2] = 0;
		// dataop[3] = 1;
		//
		// try {
		// for (;;) {
		// ArrayList<Byte> bl = new ArrayList<Byte>();
		// byte[] data = new byte[512];
		//
		// if ((n = reader.read(data)) == -1)
		// break;
		//
		// for (byte b : dataop) {
		// bl.add(b);
		// i++;
		// }
		//
		// for (int k = 0; k < n; k++) {
		// bl.add(data[k]);
		// i++;
		// }
		//
		// arr = bl.toArray(new Byte[i]);
		// sendDataPacket(arr);
		// receiveAck();
		// dataop[3]++;
		// i = 0;
		// }
		//
		// } catch (IOException e) {
		//
		// e.printStackTrace();
		// System.err.println("Failed to complete write request.");
		// Thread.currentThread().interrupt();
		// System.err.println("Thread was interrupted due to failed write");
		// }
		//
		// if (arr.length == 516) {
		// Byte[] zeropacket = { 0, 3, 0, dataop[3]++, 0 };
		// sendDataPacket(zeropacket);
		// receiveAck();
		// }
		//
		// try {
		// reader.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// System.err.println("Failed to close the input stream.");
		// }
		// System.out.println("File has been successully sent.");
		// ftSocket.close();
	}

	private void sendDataPacket(Byte[] d) {
		byte[] data = new byte[d.length];
		int i;

		for (i = 0; i < d.length; i++)
			data[i] = d[i].byteValue();
		DatagramPacket sendPacket = null;
		try {

			sendPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), this.sendPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		System.out.println("\nSending data packet to port : " + this.sendPort + " Containing: ");
		for (byte b : data)
			System.out.print(b + " ");

		try {
			System.out.println("port id =" + sendPort);
			System.out.println("data length =" + data.length);
			System.out.println(new String(data));
			ftSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Packet failed to send to port: " + this.sendPort);
		}
		System.out.println("\nPacket sent to port: " + this.sendPort);
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
		try {
			receivePacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), sendPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.err.println("ERROR! Failed to create and initialize DatagramPacket to receive ACKs");
		}
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