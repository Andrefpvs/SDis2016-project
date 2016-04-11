package pt.upa.transporter.exception;

public class TransporterClientUDDIException extends Exception {

	/**
	 * Exception used by TransporterClient to relay UDDI lookup errors
	 */
	private static final long serialVersionUID = -8047474069557518687L;
	
	public TransporterClientUDDIException() {
	}

	public TransporterClientUDDIException(String message) {
		super(message);
	}

	public TransporterClientUDDIException(Throwable cause) {
		super(cause);
	}

	public TransporterClientUDDIException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
