package server;

import java.net.DatagramPacket;

public class RequestHandler implements Runnable {

	private DatagramPacket request;
	private Server parentServer;

	public RequestHandler(DatagramPacket request, Server parent) {
		this.request = request;
		parentServer = parent;
	}

	@Override
	public void run() {

		byte[] data = request.getData();

		if (data[0] == (byte) 0 && data[1] == (byte) 1) {
			System.out.println("read");
		} else if (data[0] == (byte) 0 && data[1] == (byte) 2) {
			System.out.println("write");
		} else {
			System.out.println("unexpected packet");
		}

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		parentServer.threadClosed();
	}

}
