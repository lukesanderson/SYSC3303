package server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;

import exceptions.ErrorException;
import exceptions.ReceivedErrorException;

public class WriteRequestHandler extends RequestHandler implements Runnable {
	private static int timeoutLim = 2;
	private boolean isNewData = true;
	private int timeout = 0;
	private int currentBlock = 1;
	private boolean resending = false;

	public WriteRequestHandler(DatagramPacket request, Server parent) {
		super(request, parent);
	}

	private void writeRequest() throws IOException, ErrorException {

		String filename = getFileName(request.getData());
		byte[] incomingData = new byte[PACKET_SIZE];

		DatagramPacket dataPacket = null;
				//new DatagramPacket(incomingData, incomingData.length);

		// Build and send ack0
		DatagramPacket ackPacket = buildAckPacket(0);
		inOutSocket.send(ackPacket);

		System.out.println("sent: ");

		for (byte b : ackPacket.getData()) {
			System.out.print(b);
		}

		System.out.println();

		File newFile = new File(SERVER_DIRECTORY + filename);

		if (!newFile.exists()) {
			/**
			 * 
			 * SEND ERROR THAT FILE ALREADY EXISTS
			 * 
			 */
			newFile.createNewFile();
		}



		BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(newFile));
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
					System.out.println("Timeout receiving Data " + currentBlock + " resending ack ");
					timeout++;
					if(timeout == timeoutLim){
						throw new ErrorException("Timeout limit reached", 0);	
					}
					resending = true;
					break;
				}

			} while (validateData(dataPacket));

			incomingData = dataPacket.getData();
			System.out.println(incomingData.length);
			
			int receivedNumber = ((incomingData[2] & 0xff) << 8) | (incomingData[3] & 0xff);
			
			// write block
			if (resending == false) {
				writer.write(dataPacket.getData(), 4, dataPacket.getLength()-1);
			}
			
			// Build ack
			ackPacket = buildAckPacket(receivedNumber);

			System.out.println("sent: ");
			for (byte b : ackPacket.getData()) {
				System.out.print(b);
			}

			System.out.println();

			inOutSocket.send(ackPacket);
			if(resending == false){
			currentBlock++;
			}
			resending = false;

		} while (transfering);
		// transfer complete

		System.out.println("Finished");
		newFile.deleteOnExit();
		writer.close();

	}

	/**
	 * Builds an ack packet for the given block number. Uses
	 * 
	 * @param blockNumber
	 * @return
	 */
	protected DatagramPacket buildAckPacket(int blockNumber) {
		byte[] data = new byte[4];

		data[0] = 0;
		data[1] = 4;
		data[2] = (byte) ((blockNumber >> 8) & 0xFF);
		data[3] = (byte) (blockNumber & 0xFF);

		DatagramPacket ackPack = new DatagramPacket(data, data.length);

		ackPack.setAddress(clientAddress);
		ackPack.setPort(clientPort);

		return ackPack;
	}

	private DatagramPacket receiveData() throws IOException {

		byte[] data = new byte[PACKET_SIZE];

		DatagramPacket dataPacket = new DatagramPacket(data, data.length);

		inOutSocket.receive(dataPacket);
		

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
		if (dataPort != clientPort || !dataAddress.equals(clientAddress)) {
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

		if (data[data.length - 1] == (byte) 0) {
			transfering = false;
		}

		isNewData = true;
		return false;

	}

	@Override
	public void run() {
		try {
			writeRequest();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReceivedErrorException e) {
			System.out.println("\nReceived error packet.");
			File q = new File(SERVER_DIRECTORY+getFileName(request.getData()));
			q.delete();
		} catch (ErrorException e) {

			// Build the error
			DatagramPacket err = buildError(e.getMessage().getBytes(), e.getErrorCode());

			// set port and address
			err.setAddress(clientAddress);
			err.setPort(clientPort);

			// Send error
			try {
				inOutSocket.send(err);
			} catch (IOException e1) {
				// TODO error sending Error packet
				e1.printStackTrace();
			}
			File q = new File(SERVER_DIRECTORY+getFileName(request.getData()));
			q.delete();
		}
		
		
		inOutSocket.close();
		parentServer.threadClosed();

	}

}
