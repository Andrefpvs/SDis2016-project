package pt.upa.ca.ws;

import java.util.List;
import javax.jws.WebService;
import javax.xml.registry.JAXRException;

import pt.upa.ca.*;

@WebService(
	    endpointInterface="pt.upa.ca.ws.CAPortType",
	    wsdlLocation="ca.1_0.wsdl",
	    name="CA",
	    portName="CAPort",
	    targetNamespace="http://ws.ca.upa.pt/",
	    serviceName="CAService"
)

public class CAPort implements CAPortType {
	
	CADomain domain;
	
	//endpoint
	private CAEndpointManager endpoint;	

	public CAPort(CAEndpointManager endpoint) throws JAXRException {
		this.endpoint = endpoint;
		this.domain = new CADomain(this.endpoint.getWsName(), this.endpoint.getUddiURL());
	}
	
	//constructor used for unit testing
	public CAPort(String serviceName, String uddiURL) throws JAXRException {
		this.domain = new CADomain(serviceName, uddiURL);
	}
	
	/*
	 *  CAPortType implementation
	 */

	@Override
	public String ping(String name) {
		return domain.ping(name);
	}	
}
