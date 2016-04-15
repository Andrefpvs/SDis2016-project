package pt.upa.broker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import javax.xml.registry.JAXRException;

import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.*;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;
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
	private int failedNumber = 1; // Number attributed to transporter in FAILED state
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
				+ "Here's what the Transporters I know about tell me:\n";
		
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

		if (!cities.contains(origin)) {
			throw new UnknownLocationFault_Exception("Origin location unknown", new UnknownLocationFault());
		}
		if (!cities.contains(destination)) {
			throw new UnknownLocationFault_Exception("Destination location unknown", new UnknownLocationFault());
		}
			
		if (price < 0) {
			throw new InvalidPriceFault_Exception("Your price is negative...", new InvalidPriceFault());
		}
		
		updateTransporters();
		
		/*
		 * Build TransportView
		 */
		
		TransportView transport = new TransportView();
		transport.setOrigin(origin);
		transport.setDestination(destination);
		transport.setState(TransportStateView.REQUESTED);
		
		ArrayList<JobView> jobOffers = new ArrayList<JobView>();
		TreeMap<Integer, JobView> sortedJobOffers = new TreeMap<Integer, JobView>();

		for(TransporterClient client : transporters) {
			try {
				JobView jobOffer = client.requestJob(origin, destination, price);
				if (jobOffer != null) {
					jobOffers.add(jobOffer);
					sortedJobOffers.put(jobOffer.getJobPrice(), jobOffer);
				}
			} catch (BadLocationFault_Exception | BadPriceFault_Exception e) {
				e.printStackTrace();
			}
		}
		
		if (jobOffers.isEmpty()) { //if we had no offers
			throw new UnavailableTransportFault_Exception("No transports exist for "
					+ "those locations" , new UnavailableTransportFault());
		} else {
			transport.setState(TransportStateView.BUDGETED);
			JobView bestJob = sortedJobOffers.get(sortedJobOffers.firstKey());
			if (!(bestJob.getJobPrice() < price)) {
				transport.setState(TransportStateView.FAILED);
				throw new UnavailableTransportPriceFault_Exception("No transports exist for "
						+ "that price", new UnavailableTransportPriceFault());
			} else {
				transport.setId(bestJob.getJobIdentifier());
				transport.setPrice(bestJob.getJobPrice());
				transport.setTransporterCompany(bestJob.getCompanyName());
				
				JobView decidedJob = null;
				
				for(JobView jobOffer : jobOffers) {
					for(TransporterClient client : transporters) {
						if (client.jobStatus(jobOffer.getJobIdentifier()) != null) {
							if (jobOffer.equals(bestJob)) {
								try {
									decidedJob = client.decideJob(jobOffer.getJobIdentifier(), true);
								} catch (BadJobFault_Exception e) {
									e.printStackTrace();
								}
							} else {
								try {
									client.decideJob(jobOffer.getJobIdentifier(), false);
								} catch (BadJobFault_Exception e) {
									e.printStackTrace();
								}
							}
								
						}
					}
				}
				
				if (decidedJob.getJobState() == JobStateView.ACCEPTED) {
					transport.setState(TransportStateView.BOOKED);
				} else if (decidedJob.getJobState() == JobStateView.REJECTED) {
					transport.setState(TransportStateView.FAILED);
				}
			}
			
			
		}		
		
		/*
		 * Special Identifier format for FAILED Transports:
		 * TF + failedNumber
		 * Ex: TF3 (for the third failed transport)
		 */
		if (transport.getState() == TransportStateView.FAILED) {
			transport.setId("TF" + this.failedNumber);
			this.failedNumber = this.failedNumber + 1;
		}
		
		transports.put(transport.getId(), transport);
		
		
		return transport.getId();
	}
	
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		
		TransportView transport = transports.get(id);
		
		if (transport == null) {
			throw new UnknownTransportFault_Exception("Unexisting transport ID", new UnknownTransportFault());
		}
		
		updateTransporters();
		for(TransporterClient client : transporters) {
			if (client.jobStatus(id) != null) {
				if (client.jobStatus(id).getJobState() == JobStateView.HEADING) {
					transport.setState(TransportStateView.HEADING);
				} else if (client.jobStatus(id).getJobState() == JobStateView.ONGOING) {
					transport.setState(TransportStateView.ONGOING);
				} else if (client.jobStatus(id).getJobState() == JobStateView.COMPLETED) {
					transport.setState(TransportStateView.COMPLETED);
				}
				
			}
		}

		transports.put(id, transport);
		
		
		return transport;
	}
	
	public List<TransportView> listTransports() {
		List<TransportView> transportList = new ArrayList<TransportView>();
		for(Map.Entry<String, TransportView> job : transports.entrySet()){
			transportList.add(job.getValue());
		}
		return transportList;
	}
	
	public void clearTransports() {
		transports.clear();
		this.failedNumber = 1;
		updateTransporters();
		for(TransporterClient client : transporters) {
			client.clearJobs();
		}		
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