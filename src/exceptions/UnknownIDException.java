package exceptions;

import java.net.InetAddress;

public class UnknownIDException extends ErrorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private InetAddress invAddress;
	private int invPort;
	private static final int ERR_CODE = 5;

	public UnknownIDException(InetAddress invAddress, int invPort) {
		super("Received unknown id with address: " + invAddress + " and port " + invPort, ERR_CODE);

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
