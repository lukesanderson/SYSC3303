package exceptions;

import java.net.InetAddress;

public class UnknownIDException extends ErrorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private InetAddress invAddress;
	private int invPort;

	public UnknownIDException(InetAddress invAddress, int invPort) {
		super("Received unkown idd with address: " + invAddress + " and port " + invPort, 0);

		this.invAddress = invAddress;
		this.invPort = invPort;
	}

	public InetAddress getAddress() {
		return invAddress;
	}

	public int getPort() {
		return invPort;
	}
}
