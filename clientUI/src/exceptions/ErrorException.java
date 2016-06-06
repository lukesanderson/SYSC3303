package exceptions;

public class ErrorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected String message;
	protected int errCode;
	
	
	public ErrorException(String message, int errCode){
		//System.out.println(message);

		//System.out.println(errCode);
		
		this.message = message;
		this.errCode = errCode;
	}

	public String getMessage() {
		return message;
	}
	
	public int getErrorCode() {
		return errCode;
	}

}
