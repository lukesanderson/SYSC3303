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
import TFTP.VerboseQuiet;

import exceptions.ErrorException;
import exceptions.ReceivedErrorException;
import exceptions.UnknownIDException;
//import server.Server;

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
	private static final String CLIENT_DIRECTORY = System.getProperty("user.dir") + File.separator + "src" + File.separator + "TFTP" + File.separator;
	private static final int DATA_SIZE = 512;
	private static final int PACKET_SIZE = 516;
	private static int timeoutLim = 2;
	private int timeout = 0;
	private boolean resending = false;
	private int currentBlock = 1;

	private static final int ILLEGAL_OPER_ERR_CODE = 4;
	private static final int UNKNOWN_TRANSFER_ID_ERR_CODE = 5;

	private static final int SERVER_LISTENER = 69;
	private static final int INTERMEDIARY_LISTENER = 69;
	private int dataSize = 512;
	private boolean transfering = true;
	private boolean isNewData = true;
	private InetAddress serverAddress;
	private int serverPort;
	private boolean invalidFile = true;
	private boolean verboseMode = true;
	VerboseQuiet vq = new VerboseQuiet(verboseMode);

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
		System.out.println("open Client Program!\n");
		c.inter();
	}
	
	

	public void inter() throws IOException {
		String mode = "netascii"; // The used mode
		Decision request = Decision.RRQ; // default decision which is Read
		input = new Scanner(System.in); // run a new scanner to scan the input
										// from the user
		
		
		//activate quiet or verbose mode
		System.out.println("Start in (q)uiet or (v)erbose");
		String isVerbose = input.nextLine();
		if (isVerbose.equalsIgnoreCase("v")) {
			verboseMode = true;
		} else if (isVerbose.equalsIgnoreCase("q")) {
			verboseMode = false;
		}
		
		
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
		
		
		//Checks for file size, and if file exists in directory
		if (request == Decision.WRQ) {
			while (invalidFile) {
				fname = input.nextLine();
				try {
					FileInputStream localFile = new FileInputStream(CLIENT_DIRECTORY + fname);
					BufferedInputStream in = new BufferedInputStream(localFile);
					if(in.available() >= 1000000){
						System.out.println("Your selected file is too big.");
						System.out.println("Please select a file less than 1 mB");
						invalidFile = true;
					}else{
						invalidFile = false;
					}					

				} catch (FileNotFoundException nF) {
					System.out.println("The file " + fname + " does not exist in your directory:" + CLIENT_DIRECTORY);
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
		requestPacket.setPort(INTERMEDIARY_LISTENER);

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

		File newFile = new File(CLIENT_DIRECTORY + fname);

		if (!newFile.exists()) {
			newFile.createNewFile();
		}

		@SuppressWarnings("resource")
		BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(newFile));

		// get data1 to save address and port

		dataPacket = receiveData();

		validateData(dataPacket);

		
		
		/*for (byte b : dataPacket.getData()) {
			System.out.print(b);
		}
		System.out.println();*/
		
		vq.printThis(false, "got packet: \n");
		//vq.printThis(false, "\npacket: ");
		vq.printThis2(verboseMode, dataPacket);

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
		
		/*for (byte b : ackPacket.getData()) {
			System.out.print(b);
		}
		System.out.println();*/
		
		vq.printThis(false, "sending packet: \n");
		//vq.printThis(false, "\npacket: ");
		vq.printThis2(verboseMode, ackPacket);
		
		currentBlock++;

		do {
			// receive and validate data
			do {
				try {
					// Receive data packet
					dataPacket = receiveData();
					/*System.out.println("received: ");
					for (byte b : dataPacket.getData()) {
						System.out.print(b);
						
						
					}
					System.out.println();*/
					
					vq.printThis(false, "received: \n");
					//vq.printThis(false, "\npacket: ");
					vq.printThis2(verboseMode, dataPacket);
					
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

			if (resending == false) {
				writer.write(dataPacket.getData(), 4, dataPacket.getLength() - 4);
			}
			// Build the Ack
			ackPacket = buildAckPacket(receivedblockNum);

			/*System.out.println("sending packet: ");
			for (byte b : ackPacket.getData()) {
				System.out.print(b);
			}
			System.out.println();*/
			
			vq.printThis(false, "sending packet: \n");
			//vq.printThis(false, "\npacket: ");
			vq.printThis2(verboseMode, ackPacket);
			
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
		
	/*	System.out.println("received: ");
		for (byte b : ackPacket.getData()) {
			System.out.print(b);
		}
		System.out.println();*/
		
		vq.printThis(false, "received: \n");
		//vq.printThis(false, "\npacket: ");
		vq.printThis2(verboseMode, ackPacket);

		// Save server address and port
		serverPort = ackPacket.getPort();
		serverAddress = ackPacket.getAddress();

		// Set up data packet and stream to create files.
		byte[] dataForPacket;

		// DatagramPacket dataPacket = new DatagramPacket(dataForPacket,
		// dataForPacket.length, serverAddress, serverPort);

		in = new BufferedInputStream(new FileInputStream(CLIENT_DIRECTORY + fname));

		byte[] dataToSend = new byte[dataSize];

		// Data 1 is read
		int sizeOfDataRead;

		while (transfering) {
			// iterate the file in 512 byte chunks
			// Each iteration send the packet and receive the ack to match block
			// number i
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

			// dataPacket.setData(dataForPacket);
			System.out.println("sending data " + currentBlock + " of size: " + dataForPacket.length);
			DatagramPacket dataPacket = new DatagramPacket(dataForPacket, dataForPacket.length, serverAddress,
					serverPort);
			sendReceiveSocket.send(dataPacket);

			/*System.out.println("sent: ");

			for (byte b : dataPacket.getData()) {
				System.out.print(b);
			}

			System.out.println();*/
			
			vq.printThis(false, "sent:\n");
			//vq.printThis(false, "\npacket: ");
			vq.printThis2(verboseMode, dataPacket);

			// Receive ack packet

			do {
				try {
					ackPacket = receiveAck();
					int ackNum = ((ackPacket.getData()[2] & 0xff) << 8) | (ackPacket.getData()[3] & 0xff);
				/*	System.out.println("received ack " + ackNum);

					System.out.println("received: ");

					for (byte b : ackPacket.getData()) {
						System.out.print(b);
					}

					System.out.println();*/
					
					vq.printThis(false, "received ack "+ ackNum);
					vq.printThis(false, "received: \n");
					//vq.printThis(false, "\npacket: ");
					vq.printThis2(verboseMode, ackPacket);

				} catch (SocketTimeoutException e) {
					System.out.println("Timeout receiving ack " + currentBlock + " resending data " + (currentBlock));
					currentBlock--;
					timeout++;
					if (timeout == timeoutLim) {
						throw new ErrorException("Timeout limit reached", 0);
					}
					resending = true;
					break;
				}

			} while (validateAck(ackPacket));

			currentBlock++;

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