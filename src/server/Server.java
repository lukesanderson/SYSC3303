package server;

import java.net.DatagramPacket;
import java.util.Scanner;

public class Server {

	private Scanner input = new Scanner(System.in);

	private Listener requestListener;

	private int threadCount;

	private boolean verboseMode = false;

	public Server() {
		threadCount = 0;
		requestListener = new Listener(this);

	}

	public void threadCreated() {
		System.out.println("request opened");
		threadCount++;
	}

	public void threadClosed() {
		System.out.println("request closed");
		threadCount--;
	}

	public void listThreads() {
		System.out.println("Server: " + threadCount);
	}

	public boolean isVerbose() {
		return verboseMode;
	}

	public void newReadRequest(DatagramPacket request) {
		new Thread(new ReadRequestHandler(request, this)).start();
		threadCreated();
	}

	public void newWriteRequest(DatagramPacket request) {
		new Thread(new WriteRequestHandler(request, this)).start();
		threadCreated();
	}

	private void begin() {

		System.out.println("Start in (q)uiet or (v)erbose");
		String isVerbose = input.nextLine();
		if (isVerbose.equalsIgnoreCase("v")) {
			verboseMode = true;
		} else if (isVerbose.equalsIgnoreCase("q")) {
			verboseMode = false;
		}

		Thread listenerThread = new Thread(requestListener);
		listenerThread.start();

		String command = input.nextLine();
		if (command.equalsIgnoreCase("q")) {
			requestListener.exit();
			System.out.println("Server: " + "threads to close: " + threadCount);
			int placeholder = threadCount;
			while (threadCount != 0) {
				// System.out.println("Server: "+threadCount);
				if (threadCount != placeholder) {
					System.out.println("Server: " + "Transfers waiting to finish: " + threadCount);
					placeholder = threadCount;
				}
			}
			System.out.println("Server: " + "Server has shut down.");
		}
	}

	public static void main(String[] args) {

		// Server that receives requests at port 69

		Server requestServer = new Server();

		requestServer.begin();

	}

}
