package pt.upa.transporter.ws;

import java.util.List;
import javax.jws.WebService;

@WebService(
	    endpointInterface="pt.upa.transporter.ws.TransporterPortType",
	    wsdlLocation="transporter.1_0.wsdl",
	    name="UpaTransporter",
	    portName="TransporterPort",
	    targetNamespace="http://ws.transporter.upa.pt/",
	    serviceName="TransporterService"
)

public class TransporterPort implements TransporterPortType {

	@Override
	public String ping(String name) {
		return "Hello, " + name + ". Transporter is ready.";
	}

	@Override
	public JobView requestJob(String origin, String destination, int price)
			throws BadLocationFault_Exception, BadPriceFault_Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobView jobStatus(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<JobView> listJobs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearJobs() {
		// TODO Auto-generated method stub
		
	}

}
