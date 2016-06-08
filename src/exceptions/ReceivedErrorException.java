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

		
		errCode = ((errPacket.getData()[2] & 0xff) << 8) | (errPacket.getData()[3] & 0xff);
		if(!validateErr()){
			System.out.println("Received Unexpected error code");
		}
		System.arraycopy(errPacket.getData(), 4, messageBytes, 0, errPacket.getLength()-4);
		
		

		this.message = new String(messageBytes);
	}
	
	
	private boolean validateErr(){
		for(int i = 0; i <=7;i++){
			if(i == errCode){
				return true;
			}
		}
		return false;
	}
}
