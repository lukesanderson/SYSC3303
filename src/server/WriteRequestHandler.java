package server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class WriteRequestHandler extends RequestHandler implements Runnable {

	public WriteRequestHandler(DatagramPacket request, Server parent) {
		super(request, parent);
		// TODO Auto-generated constructor stub
	}

	private void writeRequest() throws IOException {
		String filename = getFileName(request.getData());

		byte[] incomingData = new byte[PACKET_SIZE];

		DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
		DatagramPacket ackPacket = buildAckPacket(0);

		inOutSocket.send(ackPacket);

		System.out.println(filename);

		File newFile = new File(SERVER_DIRECTORY + filename);

		if (!newFile.exists()) {
			newFile.createNewFile();
		}

		BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(newFile));
		do {

			System.out.println("Request Handler: " + "receiving data packet");
			inOutSocket.receive(incomingPacket);
			System.out.println("Request Handler: " + "got packet");

			int blockNumber = ((incomingData[2] & 0xff) << 8) | (incomingData[3] & 0xff);

			for (byte b : incomingPacket.getData()) {
				System.out.print(b);
			}

			System.out.println();
			if (!validateDataPacket(incomingPacket)) {
				System.out.println("Request Handler: " + "unexpected packet");
				break;
			}
			System.out.println("Request Handler: " + "validated packet");

			incomingData = incomingPacket.getData();

			System.out.println("Request Handler: " + "packet #" + blockNumber);

			// write block
			writer.write(incomingData, 4, DATA_SIZE);

			// Build ack
			DatagramPacket ackPack = buildAckPacket(blockNumber);
			inOutSocket.send(ackPack);
			System.out.println("Request Handler: " + "sent ack");

		} while (transfering);
		// transfer complete

		System.out.println("Finished");
		writer.close();

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			writeRequest();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		inOutSocket.close();
		parentServer.threadClosed();

	}

}
