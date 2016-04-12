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
		// TODO Implement
		return null;
	}
	
	public JobView jobStatus(String id) {
		// TODO Implement
		return null;
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
