package pt.upa.broker.ws.it;

import org.junit.*;
import static org.junit.Assert.*;

import pt.upa.broker.ws.TransportStateView;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnknownTransportFault_Exception;



public class ReplicationIT extends BaseBrokerIT {
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
	 * Test normal replication. The Secondary Broker will print a message
	 * every time it receives a sign of life or a state update from
	 * the Primary Broker.
	 */
	@Test
	public void testStateReplication() throws Exception {	
		System.out.println("(TEST) Adding a transport to Primary Broker");
		String job1 = client.requestTransport(CENTRO_CITY2, CENTRO_CITY1, 20);
		Thread.sleep(500);
		System.out.println("(TEST) Adding another transport to Primary Broker");
		String job2 = client.requestTransport(CENTRO_CITY2, CENTRO_CITY1, 50);
		Thread.sleep(500);
		System.out.println("(TEST) Adding yet another transport to Primary Broker");
		String job3 = client.requestTransport(CENTRO_CITY2, CENTRO_CITY1, 29);
		Thread.sleep(500);
		System.out.println("(TEST) Added three transports: ("
				+ job1 + ", " + job2 + ", " + job3 + ")" +	" to Primary Broker. ");
		Thread.sleep(1000);
		System.out.println("(TEST) Clearing all transports from Primary Broker");
		client.clearTransports();
		System.out.println("(TEST) Adding a transport to Primary Broker");
		job1 = client.requestTransport(CENTRO_CITY2, CENTRO_CITY1, 20);
		assertNotNull(job1);
		System.out.println("(TEST) Giving this last transport some time to change state... (6s)");
		Thread.sleep(6000);  //after this time the state will have almost assuredly changed
		TransportView transport = client.viewTransport(job1);
		System.out.println("(TEST) Checking if the transport's state changed");
		assertNotEquals(TransportStateView.BOOKED, transport.getState());
	}
    
	/**
	 * Test that after the Primary Broker goes down, the Client's FrontEnd
	 * obtains the Secondary's endpoint when querying UDDI for the
	 * Primary's name.
	 */
	@Test 
	public void testUDDISubstitution() throws Exception {
		String endpoint = "";
		String newEndpoint = "";
		endpoint = client.getWsURL();
		System.out.println("(TEST) Client is connected to " + endpoint);
		client.ping("Test");
		Thread.sleep(1000);
		System.out.println("(TEST) You now have 20 SECONDS to terminate Primary Broker. "
				+ "The test will fail if the Primary isn't terminated now.");
		Thread.sleep(20000);
		client.ping("Test"); //this should trigger the UDDI substitution on the Client
		newEndpoint = client.getWsURL();
		System.out.println("(TEST) Client is now connected to " + newEndpoint);
		assertNotEquals(endpoint, newEndpoint);
	}

	/**
	 * Test that after requesting some transports to the Primary Broker,
	 * the Secondary Broker will still have those transports after the
	 * Primary goes down.
	 */
	@Test
	public void testStateReplicationWithFault() throws Exception {	
		System.out.println("(TEST) Adding a transport to Primary Broker");
		String job1 = client.requestTransport(CENTRO_CITY2, CENTRO_CITY1, 20);
		Thread.sleep(500);
		System.out.println("(TEST) Adding another transport to Primary Broker");
		String job2 = client.requestTransport(CENTRO_CITY2, CENTRO_CITY1, 50);
		Thread.sleep(500);
		System.out.println("(TEST) Adding yet another transport to Primary Broker");
		String job3 = client.requestTransport(CENTRO_CITY2, CENTRO_CITY1, 29);
		Thread.sleep(500);
		System.out.println("(TEST) Added three transports: ("
				+ job1 + ", " + job2 + ", " + job3 + ")" +	" to Primary Broker. "
				+ "You now have 15 SECONDS to terminate Primary Broker.");
		Thread.sleep(15000);
		System.out.println("(TEST) After the new endpoint is obtained, we'll check"
				+ " if the Secondary Broker (new Primary) has the transports...");
		TransportView tv1 = client.viewTransport(job1);
		assertNotNull(tv1);
		if(tv1 != null) System.out.println("(TEST) The new Broker has " + tv1.getId() + "!");
		TransportView tv2 = client.viewTransport(job2);
		assertNotNull(tv2);
		if(tv2 != null) System.out.println("(TEST) The new Broker has " + tv2.getId() + "!");
		TransportView tv3 = client.viewTransport(job3);
		assertNotNull(tv3);		
		if(tv3 != null) System.out.println("(TEST) The new Broker has " + tv3.getId() + "!");
	}
}
