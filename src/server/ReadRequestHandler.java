package server;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import exceptions.ErrorException;
import exceptions.ReceivedErrorException;

public class ReadRequestHandler extends RequestHandler implements Runnable {

	public ReadRequestHandler(DatagramPacket request, Server parent) {
		super(request, parent);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Handles a read request
	 * 
	 * @throws IOException
	 * @throws ErrorException
	 */
	private void readRequest() throws IOException, ErrorException {

		String filename = getFileName(request.getData());
		// printVerbose(request, false);

		byte[] dataForPacket = new byte[PACKET_SIZE];
		dataForPacket[0] = 0;
		dataForPacket[1] = 3;

		DatagramPacket dataPacket = new DatagramPacket(dataForPacket, dataForPacket.length, clientAddress, clientPort);
		DatagramPacket ackPacket = null;

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(SERVER_DIRECTORY + filename));

		byte[] dataToSend = new byte[512];
		int sizeOfDataRead = in.read(dataToSend);
		int currentPacketNumber = 1;

		while (transfering) {

			// Add block number to packet data
			dataForPacket[2] = (byte) ((currentPacketNumber >> 8) & 0xFF);
			dataForPacket[3] = (byte) (currentPacketNumber & 0xFF);

			// Copy the data from the file into the packet data
			System.arraycopy(dataToSend, 0, dataForPacket, 4, dataToSend.length);

			// Set packet data
			dataPacket.setData(dataForPacket);
			System.out.println("sending data " + currentPacketNumber + " of size: " + sizeOfDataRead);

			// PRINT DATA HERE

			// Send data Packet
			try {
				inOutSocket.send(dataPacket);
			} catch (SocketTimeoutException e) {
				System.out.println("Timeout sending packet data " + currentPacketNumber);
			}

			// Receive ack packet

			do {
				try {
					ackPacket = receiveAck();
				} catch (SocketTimeoutException e) {
					System.out.println(
							"Timeout receiving ack " + currentPacketNumber + " resending data " + currentPacketNumber);
				}

				// validate ack
				// validateAck(ackPacket, currentPacketNumber);
			} while (validateAck(ackPacket, currentPacketNumber));

			// System.out.println("received ack " + request.getData()[3]);

			dataToSend = new byte[512];

			sizeOfDataRead = in.read(dataToSend);
			if (sizeOfDataRead == -1) {
				// Trasnfering should end
				transfering = false;
			}

			currentPacketNumber++;

		}

		in.close();
	}

	/**
	 * Receives ack packet
	 * 
	 * @return
	 * @throws IOException
	 */
	private DatagramPacket receiveAck() throws IOException {
		byte[] data = new byte[4];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);

		inOutSocket.receive(receivePacket);

		return receivePacket;
	}

	/**
	 * 
	 * @param ackPacket
	 * @param currentPacketNumber
	 * @return
	 */
	protected boolean validateAck(DatagramPacket ackPacket, int currentPacketNumber) throws ErrorException {

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
		if (ackPort != clientPort || !ackAddress.equals(clientAddress)) {
			sendUnknownIDError(ackAddress, ackPort);
			return true;
		}

		// check the packet number matches what server is expecting
		if (ackNumber < currentPacketNumber) {
			System.out.println("received duplicate ack packet");
			return true;
			// ignore and send next data
		} else if (ackNumber > currentPacketNumber) {
			System.out.println("received ack from the future");
			throw new ErrorException("received ack from the future", ILLEGAL_OPER_ERR_CODE);
		}

		return false;

	}

	public void run() {

		try {
			readRequest();
		} catch (ReceivedErrorException e) {
			System.out.println("Received error packet. message: \n" + e.getMessage());
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		inOutSocket.close();
		parentServer.threadClosed();

	}

}
