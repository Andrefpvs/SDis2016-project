package pt.upa.broker;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;

import pt.upa.broker.ws.*;

/*
 * 
 * Implementation of Transporter logic and structures
 *
 */
public class BrokerDomain {
	
	private static final String MESSAGE_TO_UNKNOWNS = "Who is this?";
		
	
	private TreeMap<String, TransportView> transports; //String = Transport ID
	private String wsname; //Broker Name
	private ArrayList<String> cities = new ArrayList<String>(); //All regions cities

	
	public BrokerDomain(String wsname) {
		this.transports = new TreeMap<String, TransportView>();
		this.wsname = wsname;
		initialiseCities();
	}
	
	public String ping(String name) {
        //TODO call ping on all transporters
		if (name == null || name.length() == 0) {
        	return MESSAGE_TO_UNKNOWNS;
        } else return "Hello, " + name + ". " + wsname + " is ready!";
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

}
