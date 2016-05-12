package pt.upa.broker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import javax.xml.registry.JAXRException;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.exception.BrokerSecondaryServerNotFoundException;
import pt.upa.broker.ws.*;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.cli.TransporterClient;
import pt.upa.broker.ws.cli.BrokerClient;

/*
 * 
 * Implementation of Broker logic and structures
 *
 */
public class BrokerDomain {
	
	private static final String MESSAGE_TO_UNKNOWNS = "Who is this?";
	private static final String PRIMARY_SERVER_NAME = "UpaBroker";
	private static final String SECONDARY_SERVER_NAME = "UpaBrokerSub";
	private static final long PING_INTERVAL_TIME = 3000;
	
	
	private TreeMap<String, TransportView> transports; //String = Transport ID
	private ArrayList<TransporterClient> transporters;	
	private String wsname; //Broker Name
	private int failedNumber = 1; // Number attributed to transporter in FAILED state
	private ArrayList<String> cities = new ArrayList<String>(); //All regions cities
	private UDDINaming uddiNaming = null;
	
	private boolean processedLifeSign = false;
	private boolean takingOverPrimary = false;
	private boolean isPrimaryServer = true;
	private boolean replicationMode = true; /*if false, Broker will not sync its status
	 											with a Secondary server. Before calling
	 											keepStateUpdated(), we must always check
	 											if this is true. */
	
	private Timer lifeSignSender = new Timer();
	private Timer statusDecider = new Timer();

	private BrokerClient otherBroker = null;

	
	public BrokerDomain(String wsname, String uddiURL) throws JAXRException {
		this.transports = new TreeMap<String, TransportView>();
		this.transporters = new ArrayList<TransporterClient>();
		this.wsname = wsname;
		this.uddiNaming = new UDDINaming(uddiURL);
		initialiseCities();
		
		if(!wsname.equals(PRIMARY_SERVER_NAME)) {
			//this.uddiNaming.unbind(PRIMARY_SERVER_NAME);
			isPrimaryServer = false;
			//findPrimaryBroker();
			//new BrokerPingReminder(PING_INTERVAL_TIME, this);
		} else {
			try {
				findSecondaryBroker();
				lifeSignSender.scheduleAtFixedRate(new TimerTask(){
  					public void run() {otherBroker.sendLifeSign();}
  						}, PING_INTERVAL_TIME, PING_INTERVAL_TIME);
			} catch (BrokerSecondaryServerNotFoundException e) {
				System.out.println("Secondary Broker not found. Starting in \"No Replication\" mode");
				this.replicationMode = false;
			}
		}
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
			transport.setState(TransportStateView.FAILED);
			throw new UnavailableTransportFault_Exception("No transports exist for "
					+ "those locations" , new UnavailableTransportFault());
		} else {
			transport.setState(TransportStateView.BUDGETED);
			JobView bestJob = sortedJobOffers.get(sortedJobOffers.firstKey());
			if (!(bestJob.getJobPrice() < price)) { //if we had no offers below our price
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
		
		if(this.isPrimaryServer && this.replicationMode){
			try {
				findSecondaryBroker();
				keepStateUpdated(transport, this.failedNumber);
			} catch (BrokerSecondaryServerNotFoundException e) {
				System.out.println("Secondary Server NOT FOUND!");
				this.replicationMode = false;
			}
		}
		
		
		return transport.getId();
	}
	
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		
		if (id == null) {
			throw new UnknownTransportFault_Exception("Null transport ID", new UnknownTransportFault());
		}
		
		TransportView transport = transports.get(id);
		
		if (transport == null) {
			throw new UnknownTransportFault_Exception("Non-existing transport ID", new UnknownTransportFault());
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
		
		if(this.isPrimaryServer && this.replicationMode){
			try {
				findSecondaryBroker();
				keepStateUpdated(transport, this.failedNumber);
			} catch (BrokerSecondaryServerNotFoundException e) {
				System.out.println("Secondary Server NOT FOUND!");
				this.replicationMode = false;
			}
		}
		
		
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
		
		if(this.isPrimaryServer && this.replicationMode){
			try {
				findSecondaryBroker();
				otherBroker.clearTransports();
			} catch (BrokerSecondaryServerNotFoundException e) {
				System.out.println("Secondary Server NOT FOUND!");
				this.replicationMode = false;
			}
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
	
	
	/*
	 * 
	 * REPLICATION HELPER FUNCTIONS BELOW 
	 *
	 */
	
	
	public void findPrimaryBroker() throws JAXRException {
		if (this.otherBroker != null) return;

		String endpoint = null;
		BrokerClient foundBroker = null;
		//System.out.println("Please start the Primary Broker server now.");
		try {
			endpoint = uddiNaming.lookup(PRIMARY_SERVER_NAME);
		} catch (JAXRException e) {
			this.otherBroker = null;
		}		
		
		if(endpoint == null) {
			this.otherBroker = null;
			System.out.println("Primary Broker not found!");
		}

		foundBroker = new BrokerClient(endpoint);						
					
		this.otherBroker = foundBroker;
	} 
	 
	
	public void findSecondaryBroker() throws BrokerSecondaryServerNotFoundException {
		if (this.otherBroker != null) return;
		
		String endpoint = null;
		BrokerClient foundBroker = null;
		System.out.println("Please start the Secondary Broker server now.");
		while (endpoint == null) {
			try {
				endpoint = uddiNaming.lookup(SECONDARY_SERVER_NAME);
			} catch (JAXRException e) {
				this.otherBroker = null;
				throw new BrokerSecondaryServerNotFoundException("JAXRException " + "caught during lookup");
			}
		}				
		
		foundBroker = new BrokerClient(endpoint);				
					
		this.otherBroker = foundBroker;
	}
	
	/**
	 * Must be called if in Replication Mode whenever a "transport" is added
	 * or modified on the Primary server.
	 */
	public void keepStateUpdated(TransportView transport, int failedNumber) { //TODO add message ID behaviour
		if(this.isPrimaryServer) {
			otherBroker.keepStateUpdated(transport, this.failedNumber);
		} else {
			System.out.println("Received state update from Primary. Updating...");
			transports.put(transport.getId(), transport);
			this.failedNumber = failedNumber;
			System.out.println("Updated.");
		}
	}
	
	public void sendLifeSign() {
		if(this.isPrimaryServer) return;

		if(!processedLifeSign) {
			statusDecider.scheduleAtFixedRate(new TimerTask(){
			      					public void run() {secondaryStatusUpdate();}
			      					}, PING_INTERVAL_TIME, PING_INTERVAL_TIME);
		}
		processedLifeSign = true;
		takingOverPrimary = false;
		System.out.println("Primary Broker sent a sign of life!");
	}
	
	public void secondaryStatusUpdate() {
		if(takingOverPrimary) {
			statusDecider.cancel();
			System.out.println("Primary Broker is down!");
			System.out.println(this.wsname + " taking over as primary Broker...");
			String endpoint;
			try {
				endpoint = uddiNaming.lookup(this.wsname);
				//print endpoint?
				this.isPrimaryServer = true;
				replicationMode = false;
				uddiNaming.unbind(this.wsname);
				this.wsname = PRIMARY_SERVER_NAME;
				uddiNaming.rebind(wsname, endpoint);
				System.out.println("Took over.");
			} catch (JAXRException e) {
				System.out.println("Error in Secondary takeover rebind.");
				e.printStackTrace();
			}
			
		} else takingOverPrimary = true; //if we don't get a new life signal, this will remain true
	}
	
	/**
	 * @deprecated Use sendLifeSign() instead
	 */
	
	public void pingPrimary() {
		if(this.isPrimaryServer) return;
		
		String response = otherBroker.ping(wsname);
		if(response != null) {
			System.out.println("Primary is alive and said: " + response);
			//new BrokerPingReminder(PING_INTERVAL_TIME, this);
		} else {
			//Takes over as primary
			System.out.println("Primary Broker is down!");
			System.out.println(wsname + " taking over as primary Broker...");
			String endpoint;
			try {
				endpoint = uddiNaming.lookup(wsname);
				//print endpoint?
				this.isPrimaryServer = true;
				replicationMode = false;
				uddiNaming.unbind(wsname);
				wsname = PRIMARY_SERVER_NAME;
				uddiNaming.rebind(wsname, endpoint);
				System.out.println("Took over.");
			} catch (JAXRException e) {
				System.out.println("Error in Secondary takeover rebind.");
				e.printStackTrace();
			}
			
		}
	}
	
}