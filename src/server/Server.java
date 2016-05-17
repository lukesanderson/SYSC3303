package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Scanner;

public class Server {

	private Scanner input = new Scanner(System.in);

	private Listener requestListener;

	private int threadCount;

	public Server() {
		threadCount = 0;
		requestListener = new Listener(this);

	}

	public void threadCreated() {
		threadCount++;
	}

	public void threadClosed() {
		threadCount--;
	}

	public void listThreads() {
		System.out.println("Server: "+threadCount);
	}

	public void newRequest(DatagramPacket request) {
		new Thread(new RequestHandler(request, this)).start();
		threadCreated();
	}

	private void begin() {

		Thread listenerThread = new Thread(requestListener);
		listenerThread.start();

		String command = input.nextLine();
		if (command.equalsIgnoreCase("q")) {
			requestListener.exit();
			System.out.println("Server: "+"threads to close: " + threadCount);
			int placeholder = threadCount;
			while (threadCount != 0) {
				// System.out.println("Server: "+threadCount);
				if (threadCount != placeholder) {
					System.out.println("Server: "+"Transfers waiting to finish: " + threadCount);
					placeholder = threadCount;
				}
			}
			System.out.println("Server: "+"Server has shut down.");

		}

	}

	public static void main(String[] args) {

		// Server that receives requests at port 69
		Server requestServer = new Server();

		requestServer.begin();

	}

}
