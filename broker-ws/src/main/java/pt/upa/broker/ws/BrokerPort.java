package pt.upa.broker.ws;

import java.util.List;
import javax.jws.WebService;
import javax.xml.registry.JAXRException;

import pt.upa.broker.*;

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

	public BrokerPort(BrokerEndpointManager endpoint) throws JAXRException {
		this.endpoint = endpoint;
		this.domain = new BrokerDomain(this.endpoint.getWsName(), this.endpoint.getUddiURL());
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
	
}
