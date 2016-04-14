package pt.upa.broker;

import java.util.TreeMap;

import pt.upa.broker.ws.*;

public class BrokerDomain {
	
	//private TreeMap<String, JobView> jobs; //String = Job ID
	private String wsname; //Broker Name


	
	public BrokerDomain(String wsname) {
		//this.jobs = new TreeMap<String, JobView>();
		this.wsname = wsname;
		initialiseCities();
	}

	private void initialiseCities() {
		// TODO Auto-generated method stub
		
	}

}
