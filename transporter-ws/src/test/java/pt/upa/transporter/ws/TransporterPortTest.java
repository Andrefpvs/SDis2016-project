package pt.upa.transporter.ws;

import org.junit.*;
import static org.junit.Assert.*;


public class TransporterPortTest {
    // static members
	
	private static final String MESSAGE_TO_UNKNOWNS = "Who is this?";
	private static final String ODD_TRANSPORTER =  "UpaTransporter1";
	private static final String EVEN_TRANSPORTER = "UpaTransporter2";

	
	// one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() {

    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members
    private TransporterPort localPort;

    // initialization and clean-up for each test

    @Before
    public void setUp() {
    	/* localPort not defined here on purpose,
    	 * due to we wanting to test different constructor
    	 * parameters
    	 */    	
    }

    @After
    public void tearDown() {
    	localPort = null;
    }


    // tests

    /**
     * Test that Ping method returns a message
     */
    @Test
    public void testPing() {
    	localPort = new TransporterPort("UpaTransporter1");
    	assertNotNull(localPort.ping("Unit Test"));
    }
    
    /**
     * Test that failing to provide a name for
     * the Ping method yields a different message
     */
    @Test
    public void testPingWithNoName() throws Exception {
    	localPort = new TransporterPort("UpaTransporter1");
    	assertEquals(MESSAGE_TO_UNKNOWNS, localPort.ping(null));
    }
    
    /**
     * Test that providing an empty name to
     * the Ping method yields a different message
     */
    @Test
    public void testPingWithEmptyName() throws Exception {
    	localPort = new TransporterPort("UpaTransporter1");
    	assertEquals(MESSAGE_TO_UNKNOWNS, localPort.ping(""));
    }
    
    /**
     * Test that requesting a job with valid parameters
     * returns a valid job
     */
    @Test
    public void testRequestJob() throws Exception {
    	localPort = new TransporterPort("UpaTransporter1");
    	JobView job = localPort.requestJob("Lisboa", "Leiria", 9);
    	assertNotNull(job);
    }
    
    /**
     * Test that requesting a job for an unexisting origin location
     * triggers an exception
     */
    @Test(expected = BadLocationFault_Exception.class)
    public void testRequestJobWithBadOriginLocation() throws Exception {
    	localPort = new TransporterPort("UpaTransporter1");
    	JobView job = localPort.requestJob("Planeta Plutão", "Leiria", 9);
    }
    
    /**
     * Test that requesting a job for an unexisting destination location
     * triggers an exception
     */
    @Test(expected = BadLocationFault_Exception.class)
    public void testRequestJobWithBadDestinationLocation() throws Exception {
    	localPort = new TransporterPort("UpaTransporter1");
    	JobView job = localPort.requestJob("Viseu", "RNL", 9);
    }
    
    /**
     * Test that requesting a job for a negative reference price
     * triggers an exception
     */
    @Test(expected = BadPriceFault_Exception.class)
    public void testRequestJobWithNegativePrice() throws Exception {
    	localPort = new TransporterPort("UpaTransporter1");
    	JobView job = localPort.requestJob("Lisboa", "Leiria", -1);
    }
    
    /**
     * Test that an ODD numbered transporter does not accept
     * a job for the Norte region
     */
    @Test
    public void testRequestJobWithTransporterNotServingNorteRegion() throws Exception {
    	localPort = new TransporterPort(ODD_TRANSPORTER);
    	JobView job = localPort.requestJob("Porto", "Braga", 50);
    	assertNull(job);
    }
    
    /**
     * Test that an EVEN numbered transporter does not accept
     * a job for the Sul region
     */
    @Test
    public void testRequestJobWithTransporterNotServingSulRegion() throws Exception {
    	localPort = new TransporterPort(EVEN_TRANSPORTER);
    	JobView job = localPort.requestJob("Faro", "Lisboa", 50);
    	assertNull(job);
    }
    
    /**
     * Test that a transporter does not propose a job 
	 * when the reference price is over 100
     */
    @Test
    public void testRequestJobWithReferencePriceOver100() throws Exception {
    	localPort = new TransporterPort("UpaTransporter1");
    	JobView job = localPort.requestJob("Leiria", "Lisboa", 9001);
    	assertNull(job);
    }
    
    /**
     * Test that when a transporter receives a reference price
     * equal to or under 10, the transporter always offers a price
     * under the reference price (or 0 if the reference price is 0 or 1)
     */
    @Test
    public void testRequestJobWithReferencePriceUnder10() throws Exception {
    	localPort = new TransporterPort("UpaTransporter1");
    	JobView job = localPort.requestJob("Leiria", "Lisboa", 3);
    	//System.out.println(job.getJobPrice()); //uncomment for debug
    	assertTrue(job.getJobPrice() >= 0 && job.getJobPrice() < 3);    	
    }
    
    /**
     * Test similar to testRequestJobWithReferencePriceUnder10(), 
     * but with the reference price always 0
     */
    @Test
    public void testRequestJobWithReferencePriceEqualTo0() throws Exception {
    	localPort = new TransporterPort("UpaTransporter1");
    	JobView job = localPort.requestJob("Leiria", "Lisboa", 0);
    	//System.out.println(job.getJobPrice()); //uncomment for debug
    	assertEquals(0, job.getJobPrice());    	
    }
    
    /**
     * Test that when a transporter receives a reference price
     * with a parity equal to the parity of that transporter 
     * (e.g. UpaTransporter4 and Price = 20 are (4, 20) -> (EVEN, EVEN))
     * then the transporter always offers a price under the reference 
     * price (or 0 if the reference price is 0 or 1) [Prices over 10]
     */
    @Test
    public void testRequestJobWithSameParity() throws Exception {
    	localPort = new TransporterPort(ODD_TRANSPORTER);
    	JobView job = localPort.requestJob("Leiria", "Lisboa", 33);
    	assertTrue(job.getJobPrice() >= 0 && job.getJobPrice() < 33); 
    	
    	localPort = new TransporterPort(EVEN_TRANSPORTER);
    	job = localPort.requestJob("Leiria", "Lisboa", 44);
    	assertTrue(job.getJobPrice() >= 0 && job.getJobPrice() < 44); 
    }
    
    /**
     * Test that when a transporter receives a reference price
     * with a parity different to the parity of that transporter 
     * (e.g. UpaTransporter6 and Price = 15 are (6, 15) -> (EVEN, ODD))
     * then the transporter always offers a price above the reference 
     * price [Prices over 10]
     */
    @Test
    public void testRequestJobWithDifferentParity() throws Exception {
    	localPort = new TransporterPort(ODD_TRANSPORTER);
    	JobView job = localPort.requestJob("Leiria", "Lisboa", 34);
    	assertTrue(job.getJobPrice() > 34 && job.getJobPrice() <= 100); 
    	
    	localPort = new TransporterPort(EVEN_TRANSPORTER);
    	job = localPort.requestJob("Leiria", "Lisboa", 99);
    	assertTrue(job.getJobPrice() > 99 && job.getJobPrice() <= 100); 
    }
    
    /**
     * Test that the Job Identifiers are being correctly formed
     * according to the following rules:
     * 
     * 	 	JobView jobIdentifier Format:
     * 		T + transporterId + I + jobNumber
	 * 		Ex.: T1I4 = UpaTransporter1 + JobNumber = 4
     */
    @Test
    public void testRequestJobIdentifiers() throws Exception {
    	localPort = new TransporterPort("UpaTransporter1");
    	JobView job = localPort.requestJob("Leiria", "Lisboa", 50);
    	assertEquals("T1I1", job.getJobIdentifier());    
    	
    	job = localPort.requestJob("Leiria", "Lisboa", 50);
    	assertEquals("T1I2", job.getJobIdentifier());   
    	
    	job = localPort.requestJob("Leiria", "Lisboa", 50);
    	assertEquals("T1I3", job.getJobIdentifier());   
    	
    	localPort = new TransporterPort("UpaTransporter2");
    	job = localPort.requestJob("Leiria", "Lisboa", 50);
    	assertEquals("T2I1", job.getJobIdentifier());  
    	
    	localPort = new TransporterPort("UpaTransporter60");
    	job = localPort.requestJob("Leiria", "Lisboa", 50);
    	assertEquals("T60I1", job.getJobIdentifier());   
    }
    
    /**
     * Test that the JobView object is correctly formed by the method
     * and added to the Transporter's registry
     */
    @Test
    public void testRequestJobObject() throws Exception {
    	localPort = new TransporterPort("UpaTransporter1");
    	JobView job = localPort.requestJob("Leiria", "Lisboa", 50); //dummy job to pad the JobNumber counter
    	
    	JobView originalJob = localPort.requestJob("Lisboa", "Leiria", 80);
    	JobView storedJob = localPort.listJobs().get(1);
    	assertEquals(originalJob, storedJob);
    	assertEquals(3, localPort.domain.getNextJobNumber());
    	
    	assertEquals("UpaTransporter1", storedJob.getCompanyName());
    	assertEquals("T1I2", storedJob.getJobIdentifier());
    	assertEquals("Lisboa", storedJob.getJobOrigin());
    	assertEquals("Leiria", storedJob.getJobDestination());
    	assertEquals(JobStateView.PROPOSED, storedJob.getJobState());    	
    }
    
    // TODO Tests for "decideJob", "jobStatus", "listJobs", "clearJobs"
}
