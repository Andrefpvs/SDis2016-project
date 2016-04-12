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
	
	private TreeMap<String, JobView> jobs; //String = Job ID
	private String wsname;
	private int transporterId; // Transporter ID
	private int jobNumber = 1;
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
		return "Hello, " + name + ". " + wsname + " is ready.";
	}
	
	public JobView requestJob(String origin, String destination, int price)
			throws BadLocationFault_Exception, BadPriceFault_Exception {
		if (!norte.contains(origin) && !centro.contains(origin) && !sul.contains(origin)) {
			throw new BadLocationFault_Exception("Origin location unknown", new BadLocationFault());
		}
		if (!norte.contains(destination) && !centro.contains(destination) && !sul.contains(destination)) {
			throw new BadLocationFault_Exception("This transporter does " + "not serve that destination region",
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
		} else if ((!sul.contains(origin) && !centro.contains(origin))
				|| (!sul.contains(destination) && !centro.contains(destination))) {
			return null;
		}

		if (price > 100)
			return null;
		
		/*
		 * Decide price based on parameters
		 */
		
		int offerPrice = 0;
		if (price <= 10) {
			offerPrice = ThreadLocalRandom.current().nextInt(0, price + 1);
		}
		
		if (price > 10) {
			if( ((this.transporterId % 2) == 0 && (price % 2) == 0)
					|| ((this.transporterId % 2) == 1 && (price % 2) == 1)) {
				offerPrice = ThreadLocalRandom.current().nextInt(0, price + 1);
			} else offerPrice = ThreadLocalRandom.current().nextInt(price, 100 + 1);
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
		JobView job = jobs.get(Integer.parseInt(id));
		if (job == null) throw new BadJobFault_Exception("Invalid job index", new BadJobFault());
		
		if(accept && (job.getJobState() == JobStateView.PROPOSED)) {
			job.setJobState(JobStateView.ACCEPTED);
			
		} else if(!accept && (job.getJobState() == JobStateView.PROPOSED)) {
			job.setJobState(JobStateView.REJECTED);
			
		} else throw new BadJobFault_Exception("Job not in PROPOSED state", new BadJobFault());
		
		jobs.put(id, job);		
		return job;
	}
	
	public JobView jobStatus(String id) {
		JobView job = jobs.get(Integer.parseInt(id));
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
	}
	
	/*
	 * 
	 * Implementation of Domain auxiliary functions
	 *
	 */
	
	public int transporterId(String wsname){ //TODO Test invalid ID situation
		int i = wsname.length();
		while (i > 0 && Character.isDigit(wsname.charAt(i -1))) {
			i--;			
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
}
