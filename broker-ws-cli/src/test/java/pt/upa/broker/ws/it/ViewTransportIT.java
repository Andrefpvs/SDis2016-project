package pt.upa.broker.ws.it;

import org.junit.*;
import static org.junit.Assert.*;

import pt.upa.broker.ws.TransportStateView;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnknownTransportFault_Exception;



public class ViewTransportIT extends BaseBrokerIT {
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
	 * Test that viewTransport correctly returns an updated view
	 * of the transport's state, after contacting the Transporter
	 */
	@Test 
	public void testViewTransport() throws Exception {		
		String jobID = client.requestTransport(CENTRO_CITY1, CENTRO_CITY2, 9);
		assertNotNull(jobID);
		System.out.println("(TEST) Giving a state some time to change... (6s max.)");
		Thread.sleep(6000);  //after this time the state will have almost assuredly changed
		TransportView transport = client.viewTransport(jobID);
		assertNotEquals(TransportStateView.BOOKED, transport.getState());
	}
	
	/**
	 * Test that when viewTransport received an invalid transport ID,
	 * the UnknownTransportFault Exception is correctly thrown
	 */
	@Test(expected = UnknownTransportFault_Exception.class)
	public void testViewTransportWithInvalidID() throws Exception {		
		String jobID = client.requestTransport(CENTRO_CITY1, CENTRO_CITY2, 9);		
		TransportView transport = client.viewTransport(jobID + "!#$%&");
	}
    
	/**
	 * Testing a general use case of the Broker application
	 * 
	 * Cenário por João Afonso Martins:
	 * "Eu, a minha mãe e o meu irmão queremos 
	 *  passar um fim-de-semana a uma cidade no centro do país: 
	 *	Peço a minha viagem e vejo-a; a minha mãe pede a viagem 
	 *	dela e quer ver todas as que pedimos até agora;
	 *	o meu irmão marca depois, vê a dele, e na véspera da viagem
	 *	são revistas todas."
	 */
	@Test
	public void testViewTransportEverydayUse() throws Exception {		
		String job = client.requestTransport(CENTRO_CITY1, CENTRO_CITY2, 20);		
		assertNotNull(client.viewTransport(job));
		
		String job2 = client.requestTransport(CENTRO_CITY1, CENTRO_CITY2, 50);
		assertNotNull(client.listTransports());
		
		String job3 = client.requestTransport(CENTRO_CITY1, CENTRO_CITY2, 29);
		assertNotNull(client.listTransports());

		String job4 = client.requestTransport(CENTRO_CITY2, CENTRO_CITY1, 20);
		String job5 = client.requestTransport(CENTRO_CITY2, CENTRO_CITY1, 50);
		String job6 = client.requestTransport(CENTRO_CITY2, CENTRO_CITY1, 29);
		assertNotNull(client.viewTransport(job4));
		assertNotNull(client.viewTransport(job5));
		assertNotNull(client.viewTransport(job6));		
	}
}
