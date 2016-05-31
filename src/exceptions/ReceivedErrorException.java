package exceptions;

import java.net.DatagramPacket;

public class ReceivedErrorException extends ErrorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public ReceivedErrorException(String message, int errCode) {
		super(message, errCode);
	}

	public ReceivedErrorException(DatagramPacket errPacket) {
		super(null, -1);

		byte[] messageBytes = new byte[errPacket.getLength()];

		
		
		
		System.arraycopy(errPacket.getData(), 4, messageBytes, 0, errPacket.getLength());
		
		
		// System.out.println("error message in bytes: ");
		// for(byte b : messageBytes){
		// System.out.print(b);
		// }
		// System.out.println();
		//
		// System.out.println("error message in char: ");
		// for(byte b : messageBytes){
		// System.out.print((char)b);
		// }
		// System.out.println();


		this.message = new String(messageBytes);
	}

	// public String getMessage() {
	// return this.message;
	// }

}
