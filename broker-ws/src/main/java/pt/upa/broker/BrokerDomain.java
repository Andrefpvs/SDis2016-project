package pt.upa.broker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import javax.xml.registry.JAXRException;

import java.util.List;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.*;
import pt.upa.transporter.ws.cli.TransporterClient;

/*
 * 
 * Implementation of Transporter logic and structures
 *
 */
public class BrokerDomain {
	
	private static final String MESSAGE_TO_UNKNOWNS = "Who is this?";
		
	
	private TreeMap<String, TransportView> transports; //String = Transport ID
	private ArrayList<TransporterClient> transporters;
	private String wsname; //Broker Name
	private ArrayList<String> cities = new ArrayList<String>(); //All regions cities
	private UDDINaming uddiNaming = null;

	
	public BrokerDomain(String wsname, String uddiURL) throws JAXRException {
		this.transports = new TreeMap<String, TransportView>();
		this.transporters = new ArrayList<TransporterClient>();
		this.wsname = wsname;
		this.uddiNaming = new UDDINaming(uddiURL);
		initialiseCities();
	}
	
	public String ping(String name) {        
		String response = "";
		
		if (name == null || name.length() == 0) {
        	return MESSAGE_TO_UNKNOWNS;
        } 
		response += "Hello, " + name + ". " + wsname + " is ready! "
				+ "Here's what the Transporters I know about have to say:\n";
		
		updateTransporters();
		
		if (transporters.isEmpty()){
			return "Hello, " + name + ". " + wsname + " is ready! I can't "
					+ "find any Transporters right now...";
		}
		
		for (TransporterClient client : transporters) {
			response += ("  \"" + client.ping(this.wsname) + "\"\n");
		}
				
		return response;
	}
	
	public String requestTransport(String origin, String destination, int price)
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		// TODO Actually implement the method
		return "Here's your transport";
	}
	
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<TransportView> listTransports() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void clearTransports() {
		// TODO Auto-generated method stub
		
	}

	/*
	 * 
	 * Implementation of Domain auxiliary functions
	 *
	 */
	
	public void initialiseCities() {
		ArrayList<String> cityList = new ArrayList<String>();
		
		// Norte
		cityList.add("Porto");
		cityList.add("Braga");
		cityList.add("Viana do Castelo");
		cityList.add("Vila Real");
		cityList.add("Bragança");
		// Sul
		cityList.add("Setúbal");
		cityList.add("Évora");
		cityList.add("Portalegre");
		cityList.add("Beja");
		cityList.add("Faro");
		// Centro
		cityList.add("Lisboa");
		cityList.add("Leiria");
		cityList.add("Santarém");
		cityList.add("Castelo Branco");
		cityList.add("Coimbra");
		cityList.add("Aveiro");
		cityList.add("Viseu");
		cityList.add("Guarda");

		this.cities = cityList;
	}
	
	public void updateTransporters() {
		Collection<String> endpoints = null;
		ArrayList<TransporterClient> updatedTransporters = new ArrayList<TransporterClient>();
		try {
			endpoints = uddiNaming.list("UpaTransporter%");
		} catch (JAXRException e) {
			this.transporters.clear();
			endpoints = null;
		}
		
		if(endpoints == null) {
			this.transporters.clear();
			return;
		}
		
		if (!endpoints.isEmpty()) {
			for (String endpoint : endpoints) {			
				TransporterClient tc = new TransporterClient(endpoint);
				updatedTransporters.add(tc);
			}			
			this.transporters = updatedTransporters;
		} else this.transporters.clear();		
	}

}
