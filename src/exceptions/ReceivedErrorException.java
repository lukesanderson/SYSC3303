package exceptions;

import java.net.DatagramPacket;

public class ReceivedErrorException extends ErrorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String message;

	public ReceivedErrorException(String message, int errCode) {
		super(message, errCode);
	}

	public ReceivedErrorException(DatagramPacket errPacket) {
		super(null, -1);

		byte[] data = errPacket.getData();

		for (byte b : data) {
			System.out.print(b);
		}

		this.message = new String(data);
	}

	public String getMessage() {
		return this.message;
	}

}
