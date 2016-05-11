package pt.upa.broker.ws;

import java.util.List;
import javax.jws.WebService;
import javax.xml.registry.JAXRException;

import pt.upa.broker.*;
import pt.upa.broker.exception.BrokerSecondaryServerNotFoundException;

@WebService(
	    endpointInterface="pt.upa.broker.ws.BrokerPortType",
	    wsdlLocation="broker.1_0.wsdl",
	    name="UpaBroker",
	    portName="BrokerPort",
	    targetNamespace="http://ws.broker.upa.pt/",
	    serviceName="BrokerService"
)

public class BrokerPort implements BrokerPortType {
	
	BrokerDomain domain;
	
	//endpoint
	private BrokerEndpointManager endpoint;	

	public BrokerPort(BrokerEndpointManager endpoint) throws JAXRException, 
			BrokerSecondaryServerNotFoundException {
		this.endpoint = endpoint;
		this.domain = new BrokerDomain(this.endpoint.getWsName(), this.endpoint.getUddiURL());
	}
	
	//constructor used for unit testing
	public BrokerPort(String serviceName, String uddiURL) throws JAXRException,
			BrokerSecondaryServerNotFoundException {
		this.domain = new BrokerDomain(serviceName, uddiURL);
	}
	
	/*
	 *  BrokerPortType implementation
	 */

	@Override
	public String ping(String name) {
		return domain.ping(name);
	}

	@Override
	public String requestTransport(String origin, String destination, int price)
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		return domain.requestTransport(origin, destination, price);
	}

	@Override
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		return domain.viewTransport(id);
	}

	@Override
	public List<TransportView> listTransports() {
		return domain.listTransports();
	}

	@Override
	public void clearTransports() {
		domain.clearTransports();		
	}

	@Override
	public void keepStateUpdated(TransportView transport, int failedNumber) {
		domain.keepStateUpdated(transport, failedNumber);
	}
	
}
