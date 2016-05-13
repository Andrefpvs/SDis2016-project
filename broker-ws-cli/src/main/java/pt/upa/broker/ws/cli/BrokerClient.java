package pt.upa.broker.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

/**
 * 
 * FrontEnd implementation is in this class as well
 * 
 */

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.exception.BrokerClientUDDIException;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.BrokerService;
import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

public class BrokerClient implements BrokerPortType{
	
    BindingProvider bindingProvider;
    Map<String, Object> requestContext;
	
	/** WS service */
	BrokerService service = null;

	/** WS port (port type is the interface, port is the implementation) */
	BrokerPortType port = null;

	/** UDDI server URL */
	private String uddiURL = null;

	/** WS name */
	private String wsName = null;

	/** WS endpoint address */
	private String wsURL = null; // default value is defined inside WSDL

	public String getWsURL() {
		return wsURL;
	}

	/** output option **/
	private boolean verbose = false;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided web service URL */
	public BrokerClient(String wsURL) {
		this.wsURL = wsURL;
		createStub();
		bindingProvider = (BindingProvider) port;
		requestContext = bindingProvider.getRequestContext();
		
		int connectionTimeout = 1000;
		// The connection timeout property has different names in different
		// versions of JAX-WS
		// Set them all to avoid compatibility issues
		final List<String> CONN_TIME_PROPS = new ArrayList<String>();
		CONN_TIME_PROPS.add("com.sun.xml.ws.connect.timeout");
		CONN_TIME_PROPS.add("com.sun.xml.internal.ws.connect.timeout");
		CONN_TIME_PROPS.add("javax.xml.ws.client.connectionTimeout");
		// Set timeout until a connection is established (unit is milliseconds;
		// 0 means infinite)
		for (String propName : CONN_TIME_PROPS)
			requestContext.put(propName, connectionTimeout);
		System.out.printf("Set connection timeout to %d milliseconds%n", connectionTimeout);

		int receiveTimeout = 6000;
		// The receive timeout property has alternative names
		// Again, set them all to avoid compability issues
		final List<String> RECV_TIME_PROPS = new ArrayList<String>();
		RECV_TIME_PROPS.add("com.sun.xml.ws.request.timeout");
		RECV_TIME_PROPS.add("com.sun.xml.internal.ws.request.timeout");
		RECV_TIME_PROPS.add("javax.xml.ws.client.receiveTimeout");
		// Set timeout until the response is received (unit is milliseconds; 0
		// means infinite)
		for (String propName : RECV_TIME_PROPS)
			requestContext.put(propName, receiveTimeout);
		System.out.printf("Set receive timeout to %d milliseconds%n", receiveTimeout);
	}

	/** constructor with provided UDDI location and name 
	 * @throws BrokerClientUDDIException */
	public BrokerClient(String uddiURL, String wsName) throws BrokerClientUDDIException {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		uddiLookup();
		createStub();
		bindingProvider = (BindingProvider) port;
		requestContext = bindingProvider.getRequestContext();
		
		int connectionTimeout = 1000;
		// The connection timeout property has different names in different
		// versions of JAX-WS
		// Set them all to avoid compatibility issues
		final List<String> CONN_TIME_PROPS = new ArrayList<String>();
		CONN_TIME_PROPS.add("com.sun.xml.ws.connect.timeout");
		CONN_TIME_PROPS.add("com.sun.xml.internal.ws.connect.timeout");
		CONN_TIME_PROPS.add("javax.xml.ws.client.connectionTimeout");
		// Set timeout until a connection is established (unit is milliseconds;
		// 0 means infinite)
		for (String propName : CONN_TIME_PROPS)
			requestContext.put(propName, connectionTimeout);
		System.out.printf("Set connection timeout to %d milliseconds%n", connectionTimeout);

		int receiveTimeout = 6000;
		// The receive timeout property has alternative names
		// Again, set them all to avoid compability issues
		final List<String> RECV_TIME_PROPS = new ArrayList<String>();
		RECV_TIME_PROPS.add("com.sun.xml.ws.request.timeout");
		RECV_TIME_PROPS.add("com.sun.xml.internal.ws.request.timeout");
		RECV_TIME_PROPS.add("javax.xml.ws.client.receiveTimeout");
		// Set timeout until the response is received (unit is milliseconds; 0
		// means infinite)
		for (String propName : RECV_TIME_PROPS)
			requestContext.put(propName, receiveTimeout);
		System.out.printf("Set receive timeout to %d milliseconds%n", receiveTimeout);
	}
	
	/** UDDI lookup 
	 * @throws BrokerClientUDDIException */
	private void uddiLookup() throws BrokerClientUDDIException {
		try {
			if (verbose)
				System.out.printf("Contacting UDDI at %s%n", uddiURL);
			UDDINaming uddiNaming = new UDDINaming(uddiURL);

			if (verbose)
				System.out.printf("Looking for '%s'%n", wsName);
			wsURL = uddiNaming.lookup(wsName);

		} catch (Exception e) {
			String msg = String.format("Client failed lookup on UDDI at %s!", uddiURL);
			throw new BrokerClientUDDIException(msg, e);
		}

		if (wsURL == null) {
			String msg = String.format("Service with name %s not found on UDDI at %s", 
					wsName, uddiURL);
			throw new BrokerClientUDDIException(msg);
		}
	}

	/** Stub creation and configuration */
	private void createStub() {
		if (verbose)
			System.out.println("Creating stub...");
		service = new BrokerService();
		port = service.getBrokerPort();

		if (wsURL != null) {
			if (verbose)
				System.out.println("Setting endpoint address...: " + wsURL);
			BindingProvider bindingProvider = (BindingProvider) port;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
		}
	}
	
	/*
	 *  BrokerPortType methods
	 */
	
	@Override
	public String ping(String name) {
		// call using set endpoint address
		String result = null;
        try {
        	
            result = port.ping(name);
            
        } catch (WebServiceException wse) {
			// System.out.println("Caught: " + wse);
			Throwable cause = wse.getCause();
			if (cause != null && cause instanceof SocketTimeoutException) {
				// System.out.println("The cause was a timeout exception: " +
				// cause);
				System.out.println("(CLIENT) Main Broker Server was down. Cause: "  + cause);
				this.setVerbose(true);
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					uddiLookup();
				} catch (BrokerClientUDDIException e) {
					System.err.println("But there was no Secondary server...");
				}
				createStub();
				
				System.out.println("(CLIENT) Resending request...");
	            result = port.ping(name);
			}
		}
		return result;
	}

	@Override
	public String requestTransport(String origin, String destination, int price)
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		
		String result = null;
		try {

			result = port.requestTransport(origin, destination, price);

		} catch (WebServiceException wse) {
			// System.out.println("Caught: " + wse);
			Throwable cause = wse.getCause();
			if (cause != null && cause instanceof SocketTimeoutException) {
				// System.out.println("The cause was a timeout exception: " +
				// cause);
				System.out.println("(CLIENT) Main Broker Server was down. Cause: "  + cause);
				this.setVerbose(true);
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					uddiLookup();
				} catch (BrokerClientUDDIException e) {
					System.err.println("But there was no Secondary server...");
				}
				createStub();
				
				System.out.println("(CLIENT) Resending request...");
				result = port.requestTransport(origin, destination, price);
			}
		}
		return result;
	}

	@Override
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		
		TransportView result = null;
		try {

			result = port.viewTransport(id);

		} catch (WebServiceException wse) {
			// System.out.println("Caught: " + wse);
			Throwable cause = wse.getCause();
			if (cause != null && cause instanceof SocketTimeoutException) {
				// System.out.println("The cause was a timeout exception: " +
				// cause);
				System.out.println("(CLIENT) Main Broker Server was down. Cause: "  + cause);
				this.setVerbose(true);
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					uddiLookup();
				} catch (BrokerClientUDDIException e) {
					System.err.println("But there was no Secondary server...");
				}
				createStub();
				
				System.out.println("(CLIENT) Resending request...");
				result = port.viewTransport(id);
			}
		}
		return result;
	}

	@Override
	public List<TransportView> listTransports() {

		List<TransportView> result = null;
		try {

			result = port.listTransports();

		} catch (WebServiceException wse) {
			// System.out.println("Caught: " + wse);
			Throwable cause = wse.getCause();
			if (cause != null && cause instanceof SocketTimeoutException) {
				// System.out.println("The cause was a timeout exception: " +
				// cause);
				System.out.println("(CLIENT) Main Broker Server was down. Cause: "  + cause);
				this.setVerbose(true);
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					uddiLookup();
				} catch (BrokerClientUDDIException e) {
					System.err.println("But there was no Secondary server...");
				}
				createStub();
				
				System.out.println("(CLIENT) Resending request...");
				result = port.listTransports();
			}
		}
		return result;
	}

	@Override
	public void clearTransports() {
		
		try {

			port.clearTransports();

		} catch (WebServiceException wse) {
			// System.out.println("Caught: " + wse);
			Throwable cause = wse.getCause();
			if (cause != null && cause instanceof SocketTimeoutException) {
				// System.out.println("The cause was a timeout exception: " +
				// cause);
				System.out.println("(CLIENT) Main Broker Server was down. Cause: "  + cause);
				this.setVerbose(true);
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					uddiLookup();
				} catch (BrokerClientUDDIException e) {
					System.err.println("But there was no Secondary server...");
				}
				createStub();
				
				System.out.println("(CLIENT) Resending request...");
				port.clearTransports();
			}
		}
	}
	
	@Override
	public void keepStateUpdated(TransportView transport, int failedNumber) {
		port.keepStateUpdated(transport, failedNumber);
	}

	@Override
	public void sendLifeSign() {
		port.sendLifeSign();
	}

}
