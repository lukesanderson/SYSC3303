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
import java.io.FileNotFoundException;
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
	DatagramPacket sendPacket, receivePacket; // create two DatagramPacket to
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
	}; // same for decision both enum are input in the console of the client

	private static String fname;
	private static String client_directory = System.getProperty("user.dir") + File.separator + "src"
			+ File.separator + "TFTP" + File.separator;
	
	private static final int DATA_SIZE = 512;
	private static final int PACKET_SIZE = 516;
	private static int timeoutLim = 3;
	private int timeout = 0;
	private boolean resending = false;
	private int currentBlock = 1;
	private boolean duplicateAck = false;

	
	
	
	

	// Error codes
	private static final int FILE_NOT_FOUND_CODE = 1;
	private static final int ACCESS_DENIED_CODE = 2;
	private static final int DISK_FULL_ERROR_CODE = 3;
	private static final int ILLEGAL_OPER_ERR_CODE = 4;
	private static final int UNKNOWN_TRANSFER_ID_ERR_CODE = 5;
	private static final int FILE_EXISTS_CODE = 6;

	private static final int SERVER_LISTENER = 69;
	private static final int INTERMEDIARY_LISTENER = 23;
	private int dataSize = 512;
	private boolean transfering = true;
	private boolean isNewData = true;
	private InetAddress serverAddress;
	private int serverPort;
	private boolean invalidFile = true;
	private boolean readOrWrite = false;
	private boolean testingMode = false;
	private int portSelected;
	private boolean newDir = false;
	
	public Client() {
		try {
			sendReceiveSocket = new DatagramSocket(); // create the datagram
			sendReceiveSocket.setSoTimeout(2000); // socket
		} catch (SocketException se) { // catch Socket exception error if
										// applicable
			se.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String args[]) throws IOException {
		Client c = new Client();
		System.out.println("TFTP Client is running.\n");
		c.inter();
	}

	public void inter() throws IOException {
		String mode = "netascii"; // The used mode
		Decision request = Decision.RRQ; // default decision which is Read
		input = new Scanner(System.in); // run a new scanner to scan the input
										// from the user
		
		
		while(testingMode == false){
			System.out.println("Would you like to proceed in (N)ormal or (T)est mode?");
			String testMode = input.nextLine();
			if(testMode.equalsIgnoreCase("T")){
				System.out.println("Testing Mode Selected.");
				portSelected = INTERMEDIARY_LISTENER;
				testingMode = true;
			}else if (testMode.equalsIgnoreCase("N")){
				System.out.println("Normal Mode Selected.");
				portSelected = SERVER_LISTENER;
				testingMode = true;
			}
			else{
				System.out.println("Invalid mode, please provide valid input.");
				testingMode = false;
			}
		}
		
		while(readOrWrite == false){
			System.out.println("choose (R)ead Request, (W)rite Request, or (Q)uit?");
			String choice = input.nextLine(); // reads the input String
		if (choice.equalsIgnoreCase("R")) {
			request = Decision.RRQ;
			System.out.println("Client: send a read request.");
			readOrWrite = true;
		} else if (choice.equalsIgnoreCase("W")) {
			request = Decision.WRQ;
			System.out.println("Client:  send a write request.");
			readOrWrite = true;
		} else if (choice.equalsIgnoreCase("Q")) {
			System.out.println("Goodbye!");
			readOrWrite = true;
			System.exit(1);
		} else {
			readOrWrite = false;
			System.out.println("invalid choice.  Please try again...");
	
		}
		}
		
		System.out.println("Your current directory is: " + client_directory);
		
		while(newDir == false){
			System.out.println("Please enter the name of the directory you would like to switch to.");
			String directory = input.nextLine(); // reads the input String
			File dir = new File(directory);
			if(dir.isDirectory() && dir.exists()){
				if(dir.canWrite() && !directory.equals("\\")){
				client_directory = directory + File.separator;
				newDir = true;
				System.out.println("Your new directory is: " + client_directory);
				
				if (request == Decision.RRQ) {
					try{
						File.createTempFile("test", null, dir).deleteOnExit();
						newDir = true;
					}
					catch(IOException noA){
						System.out.println("Access to this directory is denied: Access violation");
						newDir = false;
					}
				}
				
				}else{
				System.out.println("You can't write to this directory");
				newDir = false;
				}
			}else{
				System.out.println("Directory does not exist, please try another.");
				newDir = false;
			}
		}
		
		

		// gets a file directory from the user
		System.out.println("Please choose a file to modify.  Type in a file name: ");
		
		
		//Checks for file size, and if file exists in directory
		if (request == Decision.WRQ) {
			while (invalidFile) {
				fname = input.nextLine();
				try {
					FileInputStream localFile = new FileInputStream(client_directory + fname);
					BufferedInputStream in = new BufferedInputStream(localFile);
					if(in.available() >= 1000000){
						System.out.println("Your selected file is too big.");
						System.out.println("Please select a file less than 1 mB");
						invalidFile = true;
					}else{
						invalidFile = false;
					}					

				} catch (FileNotFoundException nF) {
					System.out.println("The file " + fname + " does not exist in your directory:" + client_directory);
					System.out.println("Please choose a correct file to send.");
					invalidFile = true;
				}
			}
			
		}else{
			fname = input.nextLine();
		}

		DatagramPacket requestPacket = buildRequest(fname.getBytes());

		// decide if it s a read or a write
		try

		{

			if (request == Decision.RRQ) {
				System.out.println("Client:" + fname + ", receive in " + mode + " mode.\n");
				read(requestPacket);

			} else if (request == Decision.WRQ) {
				System.out.println("Client:" + fname + ", send in " + mode + " mode.\n");
				write(requestPacket);
			}
		} catch (ReceivedErrorException e)

		{
			System.out.println("\nReceived error: "); // fix this
			System.out.println(e.getMessage());/**
												 * TODO DELETE FILE IF ERROR
												 * 
												 */

		}

		catch (ErrorException e) {
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
		requestPacket.setPort(portSelected);

		return requestPacket;
	}

	private DatagramPacket receiveData() throws IOException {

		byte[] data = new byte[PACKET_SIZE];

		DatagramPacket dataPacket = new DatagramPacket(data, data.length);

		sendReceiveSocket.receive(dataPacket);

		return dataPacket;
	}

	private boolean validateData(DatagramPacket dataPacket) throws ErrorException {

		int opcode = ((dataPacket.getData()[0] & 0xff) << 8) | (dataPacket.getData()[1] & 0xff);
		int dataNumber = ((dataPacket.getData()[2] & 0xff) << 8) | (dataPacket.getData()[3] & 0xff);

		byte[] data = dataPacket.getData();

		InetAddress dataAddress = dataPacket.getAddress();
		int dataPort = dataPacket.getPort();

		// Check its data
		if (opcode == 3) {
			// is data
		} else if (opcode == 5) {
			// received error packet
			throw new ReceivedErrorException(dataPacket);
		} else {
			// Not data or error
			throw new ErrorException("\nReceived an unexpected packet. Opcode: " + opcode, ILLEGAL_OPER_ERR_CODE);
		}

		// Check Address and port
		if ((dataPort != serverPort || !dataAddress.equals(serverAddress)) && dataNumber > 1) {
			sendUnknownIDError(dataAddress, dataPort);
			return true;
		}

		// check the packet number matches what server is expecting
		if (dataNumber < currentBlock) {
			System.out.println("received duplicate data packet");
			isNewData = false;
			currentBlock--;
			// ignore and send next data
		} else if (dataNumber > currentBlock) {
			System.out.println("received data from the future");
			throw new ErrorException("received data from the future", ILLEGAL_OPER_ERR_CODE);
		}

		if (dataPacket.getLength() < 512) {
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

		DatagramPacket dataPacket;
		DatagramPacket ackPacket = buildAckPacket(0);

		File newFile = new File(client_directory + fname);
		
		File directory = new File(client_directory);
		long freeSpace = directory.getFreeSpace();
		
	/*	if( freeSpace <512){
			System.out.println("Space is full on the disk");
			throw new ErrorException("No more room for file - Disk is full", DISK_FULL_ERROR_CODE);
			
		}*/
		

		if (!newFile.exists()) {
			try{
			newFile.createNewFile();
			}
			catch(IOException noA){
				System.out.println("Access to this directory is denied: Access violation");
				throw new ErrorException("Access to client directory is denied", 2);
			}
		}

		@SuppressWarnings("resource")
		BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(newFile));

		// get data1 to save address and port

		dataPacket = receiveData();

		validateData(dataPacket);

		System.out.println("got packet: ");
		for (byte b : dataPacket.getData()) {
			System.out.print(b);
		}
		System.out.println();

		// save port and address of client
		serverAddress = dataPacket.getAddress();
		serverPort = dataPacket.getPort();

		// write data
		if (resending == false) {
			writer.write(dataPacket.getData(), 4, dataPacket.getLength() - 4);
		}

		// build and send ack
		ackPacket = buildAckPacket(currentBlock);
		sendReceiveSocket.send(ackPacket);
		System.out.println("sending packet: ");
		for (byte b : ackPacket.getData()) {
			System.out.print(b);
		}
		System.out.println();

		currentBlock++;

		do {
			// receive and validate data
			do {
				try {
					// Receive data packet
					dataPacket = receiveData();
					System.out.println("received: ");
					for (byte b : dataPacket.getData()) {
						System.out.print(b);
					}
					System.out.println();
				} catch (SocketTimeoutException e) {
					System.out.println(

					"Timeout receiving data " + currentBlock + " resending previous ack ");
					timeout++;
					resending = true;
					if (timeout == timeoutLim) {
						throw new ErrorException("Timeout limit reached", 0);
					}
					resending = true;
					break;
				}

			} while (validateData(dataPacket));

			int receivedblockNum = ((dataPacket.getData()[2] & 0xff) << 8) | (dataPacket.getData()[3] & 0xff);

			
			if( freeSpace < dataPacket.getLength()-4){
				throw new ErrorException("No more room for file - Disk is full", DISK_FULL_ERROR_CODE);
			}

			freeSpace-= dataPacket.getLength() -4;
			
			
			
			
			
			if (resending == false) {
				writer.write(dataPacket.getData(), 4, dataPacket.getLength() - 4);
			}
			// Build the Ack
			ackPacket = buildAckPacket(receivedblockNum);

			System.out.println("sending packet: ");
			for (byte b : ackPacket.getData()) {
				System.out.print(b);
			}
			System.out.println();
			sendReceiveSocket.send(ackPacket);
			if (resending == false) {
				currentBlock++;
			}
			resending = false;
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
		validateAck(ackPacket);

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
		byte[] dataForPacket = null;

		// DatagramPacket dataPacket = new DatagramPacket(dataForPacket,
		// dataForPacket.length, serverAddress, serverPort);

		in = new BufferedInputStream(new FileInputStream(client_directory + fname));

		byte[] dataToSend = new byte[dataSize];

		// Data 1 is read
		int sizeOfDataRead = 0;

		while (transfering) {
			// iterate the file in 512 byte chunks
			// Each iteration send the packet and receive the ack to match block
			// number i
			
			if(resending == false){
			dataSize = in.available();
			if (dataSize >= DATA_SIZE) {
				dataToSend = new byte[DATA_SIZE];
			} else if (dataSize > 0) {
				dataToSend = new byte[dataSize];
			}

			sizeOfDataRead = in.read(dataToSend);

			dataForPacket = new byte[4 + dataToSend.length];
			dataForPacket[0] = 0;
			dataForPacket[1] = 3;
			// Add block number to packet data
			dataForPacket[2] = (byte) ((currentBlock >> 8) & 0xFF);
			dataForPacket[3] = (byte) (currentBlock & 0xFF);

			// Copy the data from the file into the packet data
			if (dataForPacket.length > 4) {
				System.arraycopy(dataToSend, 0, dataForPacket, 4, dataToSend.length);
			}
			}
			// dataPacket.setData(dataForPacket);
			//if(duplicateAck == false){
			System.out.println("sending data " + currentBlock + " of size: " + dataForPacket.length);
			DatagramPacket dataPacket = new DatagramPacket(dataForPacket, dataForPacket.length, serverAddress,
					serverPort);
			sendReceiveSocket.send(dataPacket);
			resending = false;
			System.out.println("sent: ");

			for (byte b : dataPacket.getData()) {
				System.out.print(b);
			}

			System.out.println();
			//}else{
			//	duplicateAck = false;
			//}
			
			
			// Receive ack packet

			do {
				try {
					ackPacket = receiveAck();
					int ackNum = ((ackPacket.getData()[2] & 0xff) << 8) | (ackPacket.getData()[3] & 0xff);
					System.out.println("received ack " + ackNum);

					System.out.println("received: ");

					for (byte b : ackPacket.getData()) {
						System.out.print(b);
					}

					System.out.println();

				} catch (SocketTimeoutException e) {
					System.out.println("Timeout receiving ack " + currentBlock + " resending data " + (currentBlock));
					
					
					timeout++;
					if (timeout == timeoutLim) {
						throw new ErrorException("Timeout limit reached", 0);
					}
					resending = true;
					break;
				}
				

			} while (validateAck(ackPacket));
			
			if(resending == false){
			currentBlock++;
			}

			if (sizeOfDataRead < 512) {
				// Transferring should end
				transfering = false;
			}

		}

		System.out.println("File transfer has finished");
		in.close();
	}

	/**
	 * Receives ack packet
	 * 
	 * @return
	 * @throws IOException
	 */
	private DatagramPacket receiveAck() throws IOException {
		byte[] data = new byte[PACKET_SIZE];
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
	protected boolean validateAck(DatagramPacket ackPacket) throws ErrorException {

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
		if ((ackPort != serverPort || !ackAddress.equals(serverAddress)) && ackNumber > 0) {
			sendUnknownIDError(ackAddress, ackPort);
			return true;
		}

		// check the packet number matches what server is expecting
		if (ackNumber < currentBlock) {
			System.out.println(ackNumber + "  " + currentBlock);
			System.out.println("received duplicate ack packet");
			//duplicateAck = true;
			return true;
			// ignore and send next data
		} else if (ackNumber > currentBlock) {
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