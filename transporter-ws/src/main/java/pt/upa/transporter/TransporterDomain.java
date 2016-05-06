package pt.upa.transporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import pt.upa.transporter.ws.*;

/*
 * 
 * Implementation of Transporter logic and structures
 *
 */
public class TransporterDomain {
	
 	private static final String MESSAGE_TO_UNKNOWNS = "Who is this?";

	
	private TreeMap<String, JobView> jobs; //String = Job ID
	private String wsname; //Transporter Name
	private int transporterId; // Transporter ID
	private int jobNumber = 1; // Number attributed to next created job
	private ArrayList<String> norte = new ArrayList<String>(); //"norte" region cities
	private ArrayList<String> centro = new ArrayList<String>(); //"centro" region cities
	private ArrayList<String> sul = new ArrayList<String>(); //"sul" region cities

	
	
	public TransporterDomain(String wsname) {
		this.jobs = new TreeMap<String, JobView>();
		this.wsname = wsname;
		this.transporterId = transporterId(this.wsname);
		initialiseCities(this.transporterId);
	}
	
	
	public String ping(String name) {
        if (name == null || name.length() == 0) {
        	return MESSAGE_TO_UNKNOWNS;
        } else if (name.equals("--getTransporterName")){
        	return this.wsname;
        } else return "Hello, " + name + ". " + wsname + " is ready!";
	}
	
	public JobView requestJob(String origin, String destination, int price)
			throws BadLocationFault_Exception, BadPriceFault_Exception {
		if (!norte.contains(origin) && !centro.contains(origin) && !sul.contains(origin)) {
			throw new BadLocationFault_Exception("Origin location unknown", new BadLocationFault());
		}
		if (!norte.contains(destination) && !centro.contains(destination) && !sul.contains(destination)) {
			throw new BadLocationFault_Exception("Destination location unknown",
					new BadLocationFault());
		}
		if (price < 0)
			throw new BadPriceFault_Exception("Your price is negative...", new BadPriceFault());

		// EVEN: Centro + Norte -- ODD: Centro + Sul

		if ((this.transporterId % 2) == 0) { // If even...
			if ((!norte.contains(origin) && !centro.contains(origin))
					|| (!norte.contains(destination) && !centro.contains(destination))) {
				return null;
			}
		} else //If odd...
			if ((!sul.contains(origin) && !centro.contains(origin)) 
					|| (!sul.contains(destination) && !centro.contains(destination))) {
				return null;
		}

		if (price > 100)
			return null;
		
		/*
		 * Decide price based on parameters
		 */
		
		int offerPrice = 0; //if reference price == 0 or 1, this is maintained
		
		if (price > 1 && price <= 10) {
			offerPrice = ThreadLocalRandom.current().nextInt(0, price);
		}
		
		if (price > 10) {
			if (((this.transporterId % 2) == 0 && (price % 2) == 0)
					|| ((this.transporterId % 2) == 1 && (price % 2) == 1)) {
				offerPrice = ThreadLocalRandom.current().nextInt(0, price);
			} else offerPrice = ThreadLocalRandom.current().nextInt(price + 1, 100 + 2);
		}
		
		/*
		 * JobView jobIdentifier Format:
		 * T + transporterId + I + jobNumber
		 * Ex: T1I4
		 */
		
		String jobIdentifier = "T" + this.transporterId + "I" + this.jobNumber;
		
		/*
		 * Build jobView
		 */
		
		JobView job = new JobView();
		job.setCompanyName(this.wsname);
		job.setJobIdentifier(jobIdentifier);
		job.setJobOrigin(origin);
		job.setJobDestination(destination);
		job.setJobPrice(offerPrice);
		job.setJobState(JobStateView.PROPOSED);
		
		jobs.put(jobIdentifier, job);
		
		this.jobNumber = this.jobNumber + 1;
		
		return job; //return jobview
	}
	
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		if(id == null) {
			throw new BadJobFault_Exception("Null job index", new BadJobFault());
		}
		JobView job = jobs.get(id);
		boolean accepted = false;
		if (job == null) throw new BadJobFault_Exception("Invalid job index", new BadJobFault());
		
		if(accept && (job.getJobState() == JobStateView.PROPOSED)) {
			job.setJobState(JobStateView.ACCEPTED);
			accepted = true;
			
		} else if(!accept && (job.getJobState() == JobStateView.PROPOSED)) {
			job.setJobState(JobStateView.REJECTED);
			
		} else throw new BadJobFault_Exception("Job not in PROPOSED state", new BadJobFault());
		
		if(accepted) {
			int timeToNextState = ThreadLocalRandom.current().nextInt(1000, 5000 + 1);
			TransporterUpdateReminder reminder = 
					new TransporterUpdateReminder(timeToNextState, job.getJobIdentifier(), this);
		}
		
		jobs.put(id, job);
		return job;
	}
	
	public JobView jobStatus(String id) {
		if (id == null) return null;
		JobView job = jobs.get(id);
		if (job == null) return null;
		
		return job;
	}

	public List<JobView> listJobs() {
		List<JobView> jobList = new ArrayList<JobView>();
		for(Map.Entry<String, JobView> job : jobs.entrySet()){
			jobList.add(job.getValue());
		}
		return jobList;
	}
	
	public void clearJobs() {
		jobs.clear();
		this.jobNumber = 1;
	}
	
	/*
	 * 
	 * Implementation of Domain auxiliary functions
	 *
	 */
	
	public int transporterId(String wsname){
		int i = wsname.length();
		while (i > 0 && Character.isDigit(wsname.charAt(i -1))) {
			i--;			
		}
		if (wsname.substring(i).equals("")) {
			System.err.println("Starting " + wsname + "with no given "
					+ "Transporter ID number!");
		}		
		return new Integer(wsname.substring(i));
	}
	
	public void initialiseCities(int transporterId) {
		ArrayList<String> norteList = new ArrayList<String>();
		ArrayList<String> centroList = new ArrayList<String>();
		ArrayList<String> sulList = new ArrayList<String>();
		
		// Norte
		norteList.add("Porto");
		norteList.add("Braga");
		norteList.add("Viana do Castelo");
		norteList.add("Vila Real");
		norteList.add("Bragança");
		// Sul
		sulList.add("Setúbal");
		sulList.add("Évora");
		sulList.add("Portalegre");
		sulList.add("Beja");
		sulList.add("Faro");
		// Centro
		centroList.add("Lisboa");
		centroList.add("Leiria");
		centroList.add("Santarém");
		centroList.add("Castelo Branco");
		centroList.add("Coimbra");
		centroList.add("Aveiro");
		centroList.add("Viseu");
		centroList.add("Guarda");

		this.norte = norteList;
		this.centro = centroList;
		this.sul = sulList;
	}
	
	public int getNextJobNumber() {
		return this.jobNumber;
	}
	
	public void updateJobState(String jobIdentifier) {
		JobView job = jobs.get(jobIdentifier);
		if (job.getJobState() == JobStateView.ACCEPTED) {
			job.setJobState(JobStateView.HEADING);
		} else if (job.getJobState() == JobStateView.HEADING) {
			job.setJobState(JobStateView.ONGOING);
		} else if (job.getJobState() == JobStateView.ONGOING) {
			job.setJobState(JobStateView.COMPLETED);
		}
		
		if (job.getJobState() != JobStateView.COMPLETED) {
			int timeToNextState = ThreadLocalRandom.current().nextInt(1000, 5000 + 1);
			TransporterUpdateReminder reminder = 
					new TransporterUpdateReminder(timeToNextState, job.getJobIdentifier(), this);
		}
		jobs.put(jobIdentifier, job);
	}
	
	public String jobStateToString(JobStateView state) {
		if (state == JobStateView.PROPOSED) return "PROPOSED";
		if (state == JobStateView.REJECTED) return "REJECTED";
		if (state == JobStateView.ACCEPTED) return "ACCEPTED";
		if (state == JobStateView.HEADING) return "HEADING";
		if (state == JobStateView.ONGOING) return "ONGOING";
		if (state == JobStateView.COMPLETED) return "COMPLETED";
		
		return "UNKNOWN STATE";
	}
}
