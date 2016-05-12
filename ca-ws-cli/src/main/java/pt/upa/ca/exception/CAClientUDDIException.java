package pt.upa.ca.exception;

public class CAClientUDDIException extends Exception {

	/**
	 * Exception used by CAClient to relay UDDI lookup errors
	 */	

	private static final long serialVersionUID = 6602575715900025922L;
	
	public CAClientUDDIException() {
	}

	public CAClientUDDIException(String message) {
		super(message);
	}

	public CAClientUDDIException(Throwable cause) {
		super(cause);
	}

	public CAClientUDDIException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
