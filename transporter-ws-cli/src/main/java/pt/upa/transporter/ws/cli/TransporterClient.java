package pt.upa.transporter.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.exception.TransporterClientUDDIException;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;

public class TransporterClient implements TransporterPortType {
	
	/* public TransporterClient(String uddiurl, String wsname) {
	
		 ver nos exemplos
		 ir ao uddi, obter o endereço, ter o stub
		 // DELETE COMMENT BEFORE DELIVERY
		
	}  */

	/** WS service */
	TransporterService service = null;

	/** WS port (port type is the interface, port is the implementation) */
	TransporterPortType port = null;

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
	public TransporterClient(String wsURL) {
		this.wsURL = wsURL;
		createStub();
	}

	/** constructor with provided UDDI location and name 
	 * @throws TransporterClientUDDIException */
	public TransporterClient(String uddiURL, String wsName) throws TransporterClientUDDIException {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		uddiLookup();
		createStub();
	}
	
	/** UDDI lookup 
	 * @throws TransporterClientUDDIException */
	private void uddiLookup() throws TransporterClientUDDIException {
		try {
			if (verbose)
				System.out.printf("Contacting UDDI at %s%n", uddiURL);
			UDDINaming uddiNaming = new UDDINaming(uddiURL);

			if (verbose)
				System.out.printf("Looking for '%s'%n", wsName);
			wsURL = uddiNaming.lookup(wsName);

		} catch (Exception e) {
			String msg = String.format("Client failed lookup on UDDI at %s!", uddiURL);
			throw new TransporterClientUDDIException(msg, e);
		}

		if (wsURL == null) {
			String msg = String.format("Service with name %s not found on UDDI at %s", 
					wsName, uddiURL);
			throw new TransporterClientUDDIException(msg);
		}
	}

	/** Stub creation and configuration */
	private void createStub() {
		if (verbose)
			System.out.println("Creating stub...");
		service = new TransporterService();
		port = service.getTransporterPort();

		if (wsURL != null) {
			if (verbose)
				System.out.println("Setting endpoint address...");
			BindingProvider bindingProvider = (BindingProvider) port;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
		}
	}
	
	
	
	// TransporterPortType methods
	
	@Override
	public String ping(String name) {
		return port.ping(name);
	}

	@Override
	public JobView requestJob(String origin, String destination, int price)
			throws BadLocationFault_Exception, BadPriceFault_Exception {
		return port.requestJob(origin, destination, price);
	}

	@Override
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		return port.decideJob(id, accept);
	}

	@Override
	public JobView jobStatus(String id) {
		return port.jobStatus(id);
	}

	@Override
	public List<JobView> listJobs() {
		return port.listJobs();
	}

	@Override
	public void clearJobs() {
		port.clearJobs();				
	}

}
