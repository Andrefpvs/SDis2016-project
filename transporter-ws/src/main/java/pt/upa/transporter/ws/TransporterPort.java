package pt.upa.transporter.ws;

import java.util.List;
import javax.jws.WebService;
import javax.jws.HandlerChain;

import pt.upa.transporter.*;

@WebService(
	    endpointInterface="pt.upa.transporter.ws.TransporterPortType",
	    wsdlLocation="transporter.1_0.wsdl",
	    name="UpaTransporter",
	    portName="TransporterPort",
	    targetNamespace="http://ws.transporter.upa.pt/",
	    serviceName="TransporterService"
)
@HandlerChain(file="/handler-chain.xml")

public class TransporterPort implements TransporterPortType {
	
	TransporterDomain domain;
	
	// endpoint
	private TransporterEndpointManager endpoint;

	public TransporterPort(TransporterEndpointManager endpoint) {
		this.endpoint = endpoint;
		this.domain = new TransporterDomain(this.endpoint.getWsName());
	}

	// constructor used for Unit Testing
	TransporterPort(String serviceName) {
		this.domain = new TransporterDomain(serviceName);
	}
	
	/*
	 *  TransporterPortType implementation
	 */

	@Override
	public String ping(String name) {
		return domain.ping(name);
	}

	@Override
	public JobView requestJob(String origin, String destination, int price)
			throws BadLocationFault_Exception, BadPriceFault_Exception {
		return domain.requestJob(origin, destination, price);
	}

	@Override
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		return domain.decideJob(id, accept);
	}

	@Override
	public JobView jobStatus(String id) {
		return domain.jobStatus(id);
	}

	@Override
	public List<JobView> listJobs() {
		return domain.listJobs();
	}

	@Override
	public void clearJobs() {
		domain.clearJobs();	
	}

}
