package pt.upa.broker.ws.it;

import org.junit.*;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;

import static org.junit.Assert.*;

public class RequestTransportIT extends BaseBrokerIT {	
	private static final String SUL_CITY1 = "Beja";
	private static final String SUL_CITY2 = "Faro";
	private static final String CENTRO_CITY1 = "Lisboa";
	private static final String CENTRO_CITY2 = "Leiria";
	private static final String NORTE_CITY1 = "Porto";
	private static final String NORTE_CITY2 = "Braga";
	
    @Before
    public void setUp() {
    	client.clearTransports();
    }

    @After
    public void tearDown() {
    	client.clearTransports();
    }
    
	/**
	 * Test that requesting a transport with valid parameters
	 * returns a valid ID.
	 */
	@Test
	public void testRequestValidTransport() throws Exception {		
		String job = client.requestTransport(CENTRO_CITY1, CENTRO_CITY2, 9);		
		assertNotNull(job);
	}   
    
	/**
	 * Test that when every Transporter returns null (i.e., we
	 * receive no job offers for the requested locations and/or price)
	 * the UnavailableTransportFault Exception is thrown
	 */
	@Test(expected = UnavailableTransportFault_Exception.class)
	public void testRequestTransportNoOffers() throws Exception {
		client.requestTransport("Porto", CENTRO_CITY2, 120);	
	}
	
	/**
	 * Test that when we receive job offers, but none of them is 
	 * under the price asked by the client, the
	 * UnavailableTransportPriceFault Exception is thrown (#1)
	 */
	@Test(expected = UnavailableTransportPriceFault_Exception.class)
	public void testRequestTransportNoGoodPrices1() throws Exception {		
		client.requestTransport(NORTE_CITY1, NORTE_CITY2, 77);	
	}
	
	/**
	 * Test that when we receive job offers, but none of them is 
	 * under the price asked by the client, the
	 * UnavailableTransportPriceFault Exception is thrown (#2)
	 */
	@Test(expected = UnavailableTransportPriceFault_Exception.class)
	public void testRequestTransportNoGoodPrices2() throws Exception {		
		client.requestTransport(SUL_CITY1, SUL_CITY2, 88);	
	}
	
	/**
	 * Test requesting a transport from an unknown source
	 */
	@Test(expected = UnknownLocationFault_Exception.class)
	public void testRequestTransportWithBadOriginLocation() throws Exception {
		client.requestTransport("Planeta Plutão", CENTRO_CITY2, 9);
	}
	
	/**
	 * Test requesting a transport to an unknown destination
	 */
	@Test(expected = UnknownLocationFault_Exception.class)
	public void testRequestTransferWithBadDestination() throws Exception {
		client.requestTransport("Viseu", "RNL", 9);
	}
	
	/**
	 * Test requesting a transport with a null source
	 */
	@Test(expected = UnknownLocationFault_Exception.class)
	public void testRequestTransportWithNullOriginLocation() throws Exception {
		client.requestTransport(null, CENTRO_CITY2, 9);
	}
	
	/**
	 * Test requesting a transport with a null destination
	 */
	@Test(expected = UnknownLocationFault_Exception.class)
	public void testRequestTransportWithNullDestinationLocation() throws Exception {
		client.requestTransport("Viseu", null, 9);
	}
	
		
	/**
	 * Test requesting a transport with a negative reference price
	 */
	@Test (expected = InvalidPriceFault_Exception.class)
	public void testRequestTransportWithInvalidPrice() throws Exception {
		client.requestTransport(CENTRO_CITY1, CENTRO_CITY2, -1);
	}
    
	/**
	 * Test requesting a transport with all empty strings
	 */
	@Test (expected = UnknownLocationFault_Exception.class)
	public void testRequestTransportWithEmptyStrings() throws Exception {
		client.requestTransport("", "", 0);
	}
}
