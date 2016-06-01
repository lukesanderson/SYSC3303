package server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import exceptions.ErrorException;
import exceptions.ReceivedErrorException;

public class ReadRequestHandler extends RequestHandler implements Runnable {

	private int dataSize = 512;
	private BufferedInputStream in;

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

		File readFile = new File(SERVER_DIRECTORY + filename);

		if (!readFile.exists()) {
			System.out.println("Unable to find file " + filename);
			throw new ErrorException(filename + " not found", FILE_NOT_FOUND_CODE);
		}

		// printVerbose(request, false);

		byte[] dataForPacket = null;

		DatagramPacket ackPacket = null;

		in = new BufferedInputStream(new FileInputStream(SERVER_DIRECTORY + filename));

		byte[] dataToSend = new byte[dataSize];

		// Data 1 is read
		int sizeOfDataRead = 0;

		DatagramPacket dataPacket;

		while (transfering) {

			if (resending == false) {
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

				// Set packet data

				// PRINT DATA HERE

				// Send data Packet
			}
			dataPacket = new DatagramPacket(dataForPacket, dataForPacket.length, clientAddress, clientPort);

			System.out.println("sending data " + currentBlock + " of size: " + dataForPacket.length);

			inOutSocket.send(dataPacket);
			if (resending) {
				resending = false;
			}

			System.out.println("sent: ");

			for (byte b : dataPacket.getData()) {
				System.out.print(b);
			}

			System.out.println();

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
					System.out.println("Timeout receiving ack " + currentBlock + " resending data ");
					timeout++;
					if (timeout == timeoutLim) {
						throw new ErrorException("Timeout limit reached", 0);
					}
					resending = true;
					break;
				}
			} while (validateAck(ackPacket));

			if (resending == false) {
				currentBlock++;
			}
			if (sizeOfDataRead < 512) {
				// Transferring should end
				transfering = false;
			}

		}
		in.close();
		System.out.println("Read finished");

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

		inOutSocket.receive(receivePacket);

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
		if (ackPort != clientPort || !ackAddress.equals(clientAddress)) {
			sendUnknownIDError(ackAddress, ackPort);
			return true;
		}

		// check the packet number matches what server is expecting
		if (ackNumber < currentBlock) {
			System.out.println("received duplicate ack packet");
			return true;
			// ignore and send next data
		} else if (ackNumber > currentBlock) {
			System.out.println("received ack from the future");
			throw new ErrorException("received ack from the future", ILLEGAL_OPER_ERR_CODE);
		}

		return false;

	}

	public void run() {

		try {
			readRequest();
		} catch (ReceivedErrorException e) {
			System.out.println("Received error packet. message: \n");
			System.out.println(e.getErrorCode());
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
