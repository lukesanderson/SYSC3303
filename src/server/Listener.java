package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Listener implements Runnable {

	private DatagramSocket socket;
	private boolean running;
	private Server parentServer;

	private static final int DEFAULT_PACKET_SIZE = 512;

	public Listener(Server parent) {
		parentServer = parent;
		running = true;
		try {
			socket = new DatagramSocket(69);
		} catch (SocketException e) {
			System.out.println("Listener: " + "Unable to create listener socket");
			e.printStackTrace();
		}

	}

	public void exit() {
		running = false;
		socket.close();
	}

	@Override
	public void run() {

		while (running) {
			byte[] requestData = new byte[DEFAULT_PACKET_SIZE];
			DatagramPacket request = new DatagramPacket(requestData, requestData.length);
			try {
				System.out.println("Listener: " + "Listener waiting for request");
				System.out.println("Press (Q) to shutdown server");
				socket.receive(request);

				byte[] data = request.getData();

				if (data[0] == (byte) 0 && data[1] == (byte) 1) {
					// Read
					parentServer.newReadRequest(request);
				}

				else if (data[0] == (byte) 0 && data[1] == (byte) 2) {
					// Write
					parentServer.newWriteRequest(request);
				}

			} catch (IOException e) {
				break;
			}
		}

		System.out.println("Listener: " + "Listener has closed");
	}

}
