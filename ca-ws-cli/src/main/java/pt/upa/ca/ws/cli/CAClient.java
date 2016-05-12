package pt.upa.ca.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.exception.CAClientUDDIException;
import pt.upa.ca.ws.CAPortType;
import pt.upa.ca.ws.CAService;

public class CAClient implements CAPortType {
	
	/** WS service */
	CAService service = null;

	/** WS port (port type is the interface, port is the implementation) */
	CAPortType port = null;

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
	public CAClient(String wsURL) {
		this.wsURL = wsURL;
		createStub();
	}

	/** constructor with provided UDDI location and name 
	 * @throws CAClientUDDIException */
	public CAClient(String uddiURL, String wsName) throws CAClientUDDIException {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		uddiLookup();
		createStub();
	}
	
	/** UDDI lookup 
	 * @throws CAClientUDDIException */
	private void uddiLookup() throws CAClientUDDIException {
		try {
			if (verbose)
				System.out.printf("Contacting UDDI at %s%n", uddiURL);
			UDDINaming uddiNaming = new UDDINaming(uddiURL);

			if (verbose)
				System.out.printf("Looking for '%s'%n", wsName);
			wsURL = uddiNaming.lookup(wsName);

		} catch (Exception e) {
			String msg = String.format("Client failed lookup on UDDI at %s!", uddiURL);
			throw new CAClientUDDIException(msg, e);
		}

		if (wsURL == null) {
			String msg = String.format("Service with name %s not found on UDDI at %s", 
					wsName, uddiURL);
			throw new CAClientUDDIException(msg);
		}
	}

	/** Stub creation and configuration */
	private void createStub() {
		if (verbose)
			System.out.println("Creating stub...");
		service = new CAService();
		port = service.getCAPort(); 
		
		if (wsURL != null) {
			if (verbose)
				System.out.println("Setting endpoint address...");
			BindingProvider bindingProvider = (BindingProvider) port;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
		}
	}
	
	
	/*
	 *  CAPortType methods
	 */
	
	@Override
	public String ping(String name) {
		return port.ping(name);
	}
}
