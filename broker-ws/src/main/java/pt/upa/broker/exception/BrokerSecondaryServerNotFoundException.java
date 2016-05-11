package pt.upa.broker.exception;

public class BrokerSecondaryServerNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2735747135506698652L;

	/**
	 * Exception used by BrokerClient to relay UDDI lookup errors
	 */
	
	public BrokerSecondaryServerNotFoundException() {
	}

	public BrokerSecondaryServerNotFoundException(String message) {
		super(message);
	}

	public BrokerSecondaryServerNotFoundException(Throwable cause) {
		super(cause);
	}

	public BrokerSecondaryServerNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
