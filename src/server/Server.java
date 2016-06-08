package server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Scanner;

public class Server {

	private Scanner input = new Scanner(System.in);

	private Listener requestListener;

	private int threadCount;
	private String serverDir = System.getProperty("user.dir") + File.separator + "src"
			+ File.separator + "server" + File.separator;;
	private boolean newDir;

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
		new Thread(new ReadRequestHandler(request, this, serverDir)).start();
		threadCreated();
	}

	public void newWriteRequest(DatagramPacket request) {
		new Thread(new WriteRequestHandler(request, this, serverDir)).start();
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

		String choices;
		boolean gdanswear = false;
		do {
			System.out.println("Your current directory is: " + serverDir);
			System.out.println("Would you like to change your directory: [y/N]");
			choices = input.nextLine(); // reads the input String

			if (!(choices.equalsIgnoreCase("y")) && !(choices.equalsIgnoreCase("N"))) {
				System.out.println("invalid choice.  Please try again...");

			} else {
				gdanswear = true;
			}

		} while (gdanswear == false);
		if (choices.equalsIgnoreCase("y")) {
			while (newDir == false) {
				System.out.println("Please enter the name of the directory you would like to switch to.");
				String directory = input.nextLine(); // reads the input String
				if (!directory.isEmpty()) {
					File dir = new File(directory);
					if (dir.isDirectory() && dir.exists()) {
						if (dir.canWrite() && !directory.equals("\\")) {
							serverDir = directory + File.separator;
							newDir = true;
							System.out.println("Your new directory is: " + serverDir);
							try {
								File.createTempFile("test", null, dir).deleteOnExit();
								newDir = true;
							} catch (IOException noA) {
								System.out.println("Access to this directory is denied: Access violation");
								newDir = false;
							}
						} else {
							System.out.println("You can't write to this directory");
							newDir = false;
						}
					} else {
						System.out.println("Directory does not exist, please try another.");
						newDir = false;
					}
				} else {
					break;
				}
			}
			// gets a file directory from the user
		} // end of the if statement if the want to change a file
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
