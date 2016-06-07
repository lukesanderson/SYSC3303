package server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import exceptions.UnknownIDException;

public class RequestHandler {

	protected DatagramPacket request;
	protected Server parentServer;
	protected DatagramSocket inOutSocket;

	protected int clientPort;
	protected InetAddress clientAddress;

	protected boolean transfering = true;
	protected boolean waitingForAck = true;

	public static final String SERVER_DIRECTORY = System.getProperty("user.dir") + File.separator + "src" + File.separator + "server" + File.separator;
	//to check for error 2 
	//protected static final String SERVER_DIRECTORY = "F:\\JunkDataToFill\\";
	protected static final int PACKET_SIZE = 516;
	protected static final int DATA_SIZE = 512;

	// Error codes
	protected static final int FILE_NOT_FOUND_CODE = 1;
	protected static final int ACCESS_DENIED_CODE = 2;
	protected static final int DISK_FULL_ERROR_CODE = 3;
	protected static final int ILLEGAL_OPER_ERR_CODE = 4;
	protected static final int UNKNOWN_TRANSFER_ID_ERR_CODE = 5;
	protected static final int FILE_EXISTS_CODE = 6;

	protected static int timeoutLim = 2;
	protected int timeout = 0;
	protected int currentBlock = 1;
	protected boolean resending = false;

	public RequestHandler(DatagramPacket request, Server parent) {
		this.request = request;
		parentServer = parent;

		clientPort = request.getPort();
		clientAddress = request.getAddress();

		try {
			inOutSocket = new DatagramSocket();
			inOutSocket.setSoTimeout(2000);
		} catch (SocketException e) {
			System.out.println("Request Handler: " + "Unable to create a socket to handle request");
			e.printStackTrace();
		}

	}

	/**
	 * Extracts the filename from the data in a request packet
	 * 
	 * @param data
	 * @return filename in request packet
	 */
	protected String getFileName(byte[] data) {

		String filename = "";

		for (int i = 2; i < data.length; i++) {
			if (data[i] == (byte) 0) {
				break;
			}

			char g = (char) data[i];
			filename += g;
		}
		return filename;
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
			inOutSocket.send(err);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}