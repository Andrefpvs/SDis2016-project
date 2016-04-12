package pt.upa.transporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import pt.upa.transporter.ws.*;

/**
 * 
 * Implementation of Transporter logic and structures
 *
 */
public class TransporterDomain {
	
	private TreeMap<Integer, JobView> jobs; //Integer = job id
	
	public TransporterDomain() {
		this.jobs = new TreeMap<Integer, JobView>();
	}
	
	
	public String ping(String name, String WsName) {
		return "Hello, " + name + ". " + WsName + " is ready.";
	}
	
	public JobView requestJob(String origin, String destination, int price)
			throws BadLocationFault_Exception, BadPriceFault_Exception {
		// TODO Implement
		return null;
	}
	
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		JobView job = jobs.get(Integer.parseInt(id));
		if (job == null) throw new BadJobFault_Exception("Invalid job index", new BadJobFault());
		
		if(accept && (job.getJobState() == JobStateView.PROPOSED)) {
			job.setJobState(JobStateView.ACCEPTED);
			
		} else if(!accept && (job.getJobState() == JobStateView.PROPOSED)) {
			job.setJobState(JobStateView.REJECTED);
			
		} else throw new BadJobFault_Exception("Job not in PROPOSED state", new BadJobFault());
		
		jobs.put(Integer.parseInt(id), job);		
		return job;
	}
	
	public JobView jobStatus(String id) {
		JobView job = jobs.get(Integer.parseInt(id));
		if (job == null) return null;
		
		return job;
	}

	public List<JobView> listJobs() {
		List<JobView> jobList = new ArrayList<JobView>();
		for(Map.Entry<Integer, JobView> job : jobs.entrySet()){
			jobList.add(job.getValue());
		}
		return jobList;
	}
	
	public void clearJobs() {
		jobs.clear();	
	}

}
