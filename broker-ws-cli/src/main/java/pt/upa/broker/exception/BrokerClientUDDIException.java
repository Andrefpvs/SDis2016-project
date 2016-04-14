package pt.upa.broker.exception;

public class BrokerClientUDDIException extends Exception {

	/**
	 * Exception used by BrokerClient to relay UDDI lookup errors
	 */
	private static final long serialVersionUID = -4425121312496748989L;
	
	public BrokerClientUDDIException() {
	}

	public BrokerClientUDDIException(String message) {
		super(message);
	}

	public BrokerClientUDDIException(Throwable cause) {
		super(cause);
	}

	public BrokerClientUDDIException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
