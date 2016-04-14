package pt.upa.broker.ws;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *  Unit Test example
 *  
 *  Invoked by Maven in the "test" life-cycle phase
 *  If necessary, should invoke "mock" remote servers 
 */
public class BrokerPortTest {

    // static members
	private static final String MESSAGE_TO_UNKNOWNS = "Who is this?";
	private static final String ODD_TRANSPORTER = "UpaTransporter1";
	private static final String EVEN_TRANSPORTER = "UpaTransporter2";


    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() {

    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members

    private BrokerPort localPort;
    // initialization and clean-up for each test

    @Before
    public void setUp() {
    	localPort = new BrokerPort("UpaTransporter1");
    	
    }

    @After
    public void tearDown() {
    }


    // tests

    @Test
	public void testPing() {
		
		assertNotNull(localPort.ping("Unit Test"));
	}

	/**
	 * Test that failing to provide a name for the Ping method yields a
	 * different message
	 */
	@Test
	public void testPingWithNoName() throws Exception {
		//localPort = new BrokerPort();
		assertEquals(MESSAGE_TO_UNKNOWNS, localPort.ping(null));
	}

	/**
	 * Test that providing an empty name to the Ping method yields a different
	 * message
	 */
	@Test
	public void testPingWithEmptyName() throws Exception {
		//localPort = new BrokerPort();
		assertEquals(MESSAGE_TO_UNKNOWNS, localPort.ping(""));
	}
	
	//Test requesting a valid transport. 
	@Test
	public void testRequestTransport() throws Exception {
		//localPort = new BrokerPort("UpaTransporter1");
		String job = localPort.requestTransport("Lisboa", "Leiria", 9);
		assertNotNull(job);
	}
	
	//Test requesting a transport from an unknown source
	@Test(expected = UnknownLocationFault_Exception.class)
	public void testRequestTransportWithBadOriginLocation() throws Exception {
		localPort = new BrokerPort("UpaTransporter1");
		localPort.requestTransport("Planeta Plutão", "Leiria", 9);
	}
	
	//Test requesting a transport to an unknown destination
	@Test(expected = UnknownLocationFault_Exception.class)
	public void testRequestTransferWithBadDestination() throws Exception {
		localPort = new BrokerPort("UpaTransporter1");
		localPort.requestTransport("Viseu", "RNL", 9);
	}
	
	//Test requesting a transport with a null source
	@Test(expected = UnknownLocationFault_Exception.class)
	public void testRequestTransportWithNullOriginLocation() throws Exception {
		localPort = new BrokerPort("UpaTransporter1");
		localPort.requestTransport(null, "Leiria", 9);
	}
	
	//Test requesting a transport with a null destination
	@Test(expected = UnknownLocationFault_Exception.class)
	public void testRequestTransportWithNullDestinationLocation() throws Exception {
		localPort = new BrokerPort("UpaTransporter1");
		localPort.requestTransport("Viseu", null, 9);
	}
	
		
	//Test requesting a transport with an invalid price. 
	@Test (expected = InvalidPriceFault_Exception.class)
	public void testRequestTransportWithInvalidPrice() throws Exception {
		//localPort = new BrokerPort("UpaTransporter1");
		localPort.requestTransport("Lisboa", "Leiria", -1);
	}
	@Test 
	public void testViewTransport() throws Exception {
		//localPort = new BrokerPort("UpaTransporter1");
		String TransportId = "random";
		localPort.viewTransport(TransportId);
	}
	@Test (expected = UnknownTransportFault_Exception.class)
	public void testViewTransportWithNullId() throws Exception {
		//localPort = new BrokerPort("UpaTransporter1");
		localPort.viewTransport(null);
	}


        // assertEquals(expected, actual);
        // if the assert fails, the test fails
    }
