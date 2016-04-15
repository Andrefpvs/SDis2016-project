package pt.upa.broker.ws;

import org.junit.*;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.cli.TransporterClient;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.registry.JAXRException;

public class BrokerPortTest {

    // static members
	private static final String MESSAGE_TO_UNKNOWNS = "Who is this?";
	private static final String WS_NAME = "UpaBroker";
	private static final String UDDIURL = "http://localhost:9090";
	private static final String TRANSPORTER_WS_URL_1 = "http://localhost:8081/transporter-ws/endpoint";
	private static final String TRANSPORTER_WS_NAME_1 = "UpaTransporter1";
	private static final String TRANSPORTER_WS_URL_2 = "http://localhost:8082/transporter-ws/endpoint";
	private static final String TRANSPORTER_WS_NAME_2 = "UpaTransporter2";
	private static final String TRANSPORTER_WS_URL_3 = "http://localhost:8083/transporter-ws/endpoint";
	private static final String TRANSPORTER_WS_NAME_3 = "UpaTransporter3";

    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() {

    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members

    private BrokerPort localPort;
    Collection<String> endpoints;
    TransporterClient transporter1;
    JobView job1;
    
    // initialization and clean-up for each test

    @Before
    public void setUp() {
    	try {
			localPort = new BrokerPort(WS_NAME, UDDIURL);
		} catch (JAXRException e) {
			// Ignore. We will be mocking the UDDINaming object
		}
    	endpoints = new ArrayList<String>();
    	endpoints.add(TRANSPORTER_WS_URL_1);
    	transporter1 = new TransporterClient(TRANSPORTER_WS_URL_1);
    	job1 = new JobView();
    }

    @After
    public void tearDown() {
    	localPort = null;
    	endpoints = null;
    	transporter1 = null;
    	job1 = null;
    }


    // tests
    
	/**
	 * Test that Ping method returns a message
	 */
    @Test
	public void testPing() throws Exception {		
		assertNotNull(localPort.ping("WS_NAME"));
	}

	/**
	 * Test that failing to provide a name for the Ping method yields a
	 * different message
	 */
	@Test
	public void testPingWithNoName() throws Exception {
		assertEquals(MESSAGE_TO_UNKNOWNS, localPort.ping(null));
	}

	/**
	 * Test that providing an empty name to the Ping method yields a different
	 * message
	 */
	@Test
	public void testPingWithEmptyName() throws Exception {
		assertEquals(MESSAGE_TO_UNKNOWNS, localPort.ping(""));
	}
	
	/**
	 * Test that requesting a transport with valid parameters
	 * returns a valid ID.
	 */
	@Test
	public void testRequestTransport(
			@Mocked final UDDINaming uddiNaming,
			@Mocked final TransporterClient client) 
			throws Exception {
		
		job1.setCompanyName(TRANSPORTER_WS_NAME_1);
		job1.setJobDestination("Leiria");
		job1.setJobIdentifier("T1I1");
		job1.setJobOrigin("Lisboa");
		job1.setJobPrice(5);
		job1.setJobState(JobStateView.PROPOSED);
		JobView job2 = job1;
		job2.setJobState(JobStateView.ACCEPTED);
		
		
        new Expectations() {{
        	uddiNaming.list("UpaTransporter%"); result = endpoints;
        	new TransporterClient(TRANSPORTER_WS_URL_1); result = transporter1;
        	client.requestJob("Lisboa", "Leiria", 9); result = job1;
        	client.jobStatus("T1I1"); result = job1;
        	client.decideJob("T1I1", true); result = job2;
        }};
		
		String job = localPort.requestTransport("Lisboa", "Leiria", 9);
		
        new Verifications() {{
            // Verify that the following functions were called
        	// the specified amount of times
        	uddiNaming.list("UpaTransporter%"); times = 1;
        	new TransporterClient(TRANSPORTER_WS_URL_1); minTimes = 1;
        	client.requestJob("Lisboa", "Leiria", 9); minTimes = 1;
        	client.jobStatus("T1I1"); minTimes = 1;
        	client.decideJob("T1I1", true); minTimes = 1;
        }};
		
		assertEquals("T1I1", job);
	}
	
	/**
	 * Test that when every Transporter returns null (i.e., we
	 * receive no job offers for the requested locations and/or price)
	 * the UnavailableTransportFault Exception is thrown
	 */
	@Test(expected = UnavailableTransportFault_Exception.class)
	public void testRequestTransportNoOffers(
			@Mocked final UDDINaming uddiNaming,
			@Mocked final TransporterClient client) 
			throws Exception {
		
    	endpoints.add(TRANSPORTER_WS_URL_3);
    	TransporterClient transporter3 = new TransporterClient(TRANSPORTER_WS_URL_3);
		
        new Expectations() {{
        	uddiNaming.list("UpaTransporter%"); result = endpoints;
        	new TransporterClient(TRANSPORTER_WS_URL_1); result = transporter1;
        	new TransporterClient(TRANSPORTER_WS_URL_3); result = transporter3;
        	client.requestJob("Porto", "Leiria", 9); result = null;
        }};
		
		String job = localPort.requestTransport("Porto", "Leiria", 9);
		
        new Verifications() {{
            // Verify that the following functions were called
        	// the specified amount of times
        	uddiNaming.list("UpaTransporter%"); times = 1;
        	new TransporterClient(TRANSPORTER_WS_URL_1); minTimes = 1;
        	new TransporterClient(TRANSPORTER_WS_URL_3); minTimes = 1;
        	client.requestJob("Porto", "Leiria", 9); minTimes = 1;
        }};		
	}
	
	/**
	 * Test that when we receive job offers, but none of them is 
	 * under the price asked by the client, the
	 * UnavailableTransportPriceFault Exception is thrown
	 */
	@Test(expected = UnavailableTransportPriceFault_Exception.class)
	public void testRequestTransportNoGoodPrices(
			@Mocked final UDDINaming uddiNaming,
			@Mocked final TransporterClient client) 
			throws Exception {
		
		job1.setCompanyName(TRANSPORTER_WS_NAME_1);
		job1.setJobDestination("Leiria");
		job1.setJobIdentifier("T1I1");
		job1.setJobOrigin("Lisboa");
		job1.setJobPrice(13);
		job1.setJobState(JobStateView.PROPOSED);
		JobView job3 = new JobView();
		job3.setCompanyName(TRANSPORTER_WS_NAME_3);
		job3.setJobDestination("Leiria");
		job3.setJobIdentifier("T3I1");
		job3.setJobOrigin("Lisboa");
		job3.setJobPrice(94);
		job3.setJobState(JobStateView.PROPOSED);
		
    	endpoints.add(TRANSPORTER_WS_URL_3);
    	TransporterClient transporter3 = new TransporterClient(TRANSPORTER_WS_URL_3);
		
        new Expectations() {{
        	uddiNaming.list("UpaTransporter%"); result = endpoints;
        	new TransporterClient(TRANSPORTER_WS_URL_1); result = transporter1;
        	new TransporterClient(TRANSPORTER_WS_URL_3); result = transporter3;
        	client.requestJob("Lisboa", "Leiria", 12); returns(job1, job3);
        }};
		
		String job = localPort.requestTransport("Lisboa", "Leiria", 12);
		
        new Verifications() {{
            // Verify that the following functions were called
        	// the specified amount of times
        	uddiNaming.list("UpaTransporter%"); times = 1;
        	new TransporterClient(TRANSPORTER_WS_URL_1); minTimes = 1;
        	new TransporterClient(TRANSPORTER_WS_URL_3); minTimes = 1;
        	client.requestJob("Lisboa", "Leiria", 9); times = 2;
        }};		
	}
	
	/**
	 * Test requesting a transport from an unknown source
	 */
	@Test(expected = UnknownLocationFault_Exception.class)
	public void testRequestTransportWithBadOriginLocation() throws Exception {
		localPort.requestTransport("Planeta Plutão", "Leiria", 9);
	}
	
	/**
	 * Test requesting a transport to an unknown destination
	 */
	@Test(expected = UnknownLocationFault_Exception.class)
	public void testRequestTransferWithBadDestination() throws Exception {
		localPort.requestTransport("Viseu", "RNL", 9);
	}
	
	/**
	 * Test requesting a transport with a null source
	 */
	@Test(expected = UnknownLocationFault_Exception.class)
	public void testRequestTransportWithNullOriginLocation() throws Exception {
		localPort.requestTransport(null, "Leiria", 9);
	}
	
	/**
	 * Test requesting a transport with a null destination
	 */
	@Test(expected = UnknownLocationFault_Exception.class)
	public void testRequestTransportWithNullDestinationLocation() throws Exception {
		localPort.requestTransport("Viseu", null, 9);
	}
	
		
	/**
	 * Test requesting a transport with a negative reference price
	 */
	@Test (expected = InvalidPriceFault_Exception.class)
	public void testRequestTransportWithInvalidPrice() throws Exception {
		localPort.requestTransport("Lisboa", "Leiria", -1);
	}
	
	/* Come back to these later
	@Test 
	public void testViewTransport() throws Exception { //TODO FAILING
		String TransportId = "random";
		localPort.viewTransport(TransportId);
	}
	@Test (expected = UnknownTransportFault_Exception.class)
	public void testViewTransportWithNullId() throws Exception { //TODO FAILING
		localPort.viewTransport(null);
	}
	*/

}
