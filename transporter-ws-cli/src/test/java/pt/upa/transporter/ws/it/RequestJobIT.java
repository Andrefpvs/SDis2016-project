package pt.upa.transporter.ws.it;

import org.junit.*;
import static org.junit.Assert.*;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;

public class RequestJobIT extends BaseTransporterIT {
	private static final String ODD_TRANSPORTER = "UpaTransporter1";
	private static final String EVEN_TRANSPORTER = "UpaTransporter2";
	private static final String SUL_CITY1 = "Beja";
	private static final String SUL_CITY2 = "Faro";
	private static final String CENTRO_CITY1 = "Lisboa";
	private static final String CENTRO_CITY2 = "Leiria";
	private static final String NORTE_CITY1 = "Porto";
	private static final String NORTE_CITY2 = "Braga";


	
    @Before
    public void setUp() {
    	client.clearJobs();
    }

    @After
    public void tearDown() {
    	client.clearJobs();
    }

	/**
	 * Test that requesting a job with valid parameters returns a valid job
	 */
	@Test
	public void testRequestValidJob() throws Exception {
		JobView job = client.requestJob(CENTRO_CITY1, CENTRO_CITY2, 9);
		assertNotNull(job);
	}
	
	/**
	 * Test that requesting a job for a non-existing origin location triggers an
	 * exception
	 */
	@Test(expected = BadLocationFault_Exception.class)
	public void testRequestJobWithBadOriginLocation() throws Exception {
		JobView job = client.requestJob("Planeta Plutão", CENTRO_CITY2, 9);
	}

	/**
	 * Test that requesting a job for a non-existing destination location
	 * triggers an exception
	 */
	@Test(expected = BadLocationFault_Exception.class)
	public void testRequestJobWithBadDestinationLocation() throws Exception {
		JobView job = client.requestJob(CENTRO_CITY1, "RNL", 9);
	}

	/**
	 * Test that requesting a job for a negative reference price triggers an
	 * exception
	 */
	@Test(expected = BadPriceFault_Exception.class)
	public void testRequestJobWithNegativePrice() throws Exception {
		JobView job = client.requestJob(CENTRO_CITY1, CENTRO_CITY2, -1);
	}

	/**
	 * Test what every transporter does when requested a job for 
	 * the Norte region
	 */
	@Test
	public void testRequestJobWithTransporterNorteRegion() throws Exception {
		JobView job = client.requestJob(NORTE_CITY1, NORTE_CITY2, 50);
		if (client.ping("--getTransporterName").equals(EVEN_TRANSPORTER)) {
			assertNotNull(job);

		} else assertNull(job);
	}
	
	/**
	 * Test what every transporter does when requested a job for 
	 * the Sul region
	 */
	@Test
	public void testRequestJobWithTransporterSulRegion() throws Exception {
		JobView job = client.requestJob(SUL_CITY1, SUL_CITY2, 50);
		if (client.ping("--getTransporterName").equals(ODD_TRANSPORTER)) {
			assertNotNull(job);

		} else assertNull(job);
	}

	/**
	 * Test that a transporter does not propose a job when the reference price
	 * is over 100
	 */
	@Test
	public void testRequestJobWithReferencePriceOver100() throws Exception {
		JobView job = client.requestJob(CENTRO_CITY1, CENTRO_CITY2, 9001);
		assertNull(job);
	}

	/**
	 * Test that when a transporter receives a reference price equal to or under
	 * 10, the transporter always offers a price under the reference price (or 0
	 * if the reference price is 0 or 1)
	 */
	@Test
	public void testRequestJobWithReferencePriceUnder10() throws Exception {
		JobView job = client.requestJob(CENTRO_CITY1, CENTRO_CITY2, 3);
		assertTrue(job.getJobPrice() >= 0 && job.getJobPrice() < 3);
	}

	/**
	 * Test similar to testRequestJobWithReferencePriceUnder10(), but with the
	 * reference price always 0
	 */
	@Test
	public void testRequestJobWithReferencePriceEqualTo0() throws Exception {
		JobView job = client.requestJob(CENTRO_CITY1, CENTRO_CITY2, 0);
		assertEquals(0, job.getJobPrice());
	}

	
	
	/**
	 * Test that when a transporter receives a reference price with a parity
	 * equal to the parity of that transporter (e.g. UpaTransporter4 and Price =
	 * 20 are (4, 20) -> (EVEN, EVEN)) then the transporter always offers a
	 * price under the reference price (or 0 if the reference price is 0 or 1)
	 * [Prices over 10]
	 */
	@Test
	public void testRequestJobWithSameParity() throws Exception {
		JobView job;
		if (client.ping("--getTransporterName").equals(ODD_TRANSPORTER)) {
			job = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 33);
			assertTrue(job.getJobPrice() >= 0 && job.getJobPrice() < 33);
		} else {
			job = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 44);
			assertTrue(job.getJobPrice() >= 0 && job.getJobPrice() < 44);
		}
	}

	/**
	 * Test that when a transporter receives a reference price with a parity
	 * different to the parity of that transporter (e.g. UpaTransporter6 and
	 * Price = 15 are (6, 15) -> (EVEN, ODD)) then the transporter always offers
	 * a price above the reference price [Prices over 10]
	 */
	@Test
	public void testRequestJobWithDifferentParity() throws Exception {
		JobView job;
		if (client.ping("--getTransporterName").equals(ODD_TRANSPORTER)) {
			job = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 34);
			assertTrue(job.getJobPrice() > 34 && job.getJobPrice() <= 100);
		} else {
			job = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 99);
			assertTrue(job.getJobPrice() > 99 && job.getJobPrice() <= 100);
		}
	}

	/**
	 * Test that the Job Identifiers are being correctly formed according to the
	 * following rules:
	 * 
	 * JobView jobIdentifier Format: T + transporterId + I + jobNumber 
	 * Ex.: T1I4 = UpaTransporter1 + JobNumber = 4
	 */
	@Test
	public void testRequestJobIdentifiers() throws Exception {
		if (client.ping("--getTransporterName").equals(ODD_TRANSPORTER)) {
			JobView job = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
			assertEquals("T1I1", job.getJobIdentifier());

			job = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
			assertEquals("T1I2", job.getJobIdentifier());

			job = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
			assertEquals("T1I3", job.getJobIdentifier());
		}


		if (client.ping("--getTransporterName").equals(EVEN_TRANSPORTER)) {
			JobView job = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
			assertEquals("T2I1", job.getJobIdentifier());
			
			job = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
			assertEquals("T2I2", job.getJobIdentifier());			
			
			job = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
			assertEquals("T2I3", job.getJobIdentifier());
		}		
	}

	/**
	 * Test that the JobView object is correctly formed by the method and added
	 * to the Transporter's registry
	 */
	@Test
	public void testRequestJobObject() throws Exception {
		if (client.ping("--getTransporterName").equals(ODD_TRANSPORTER)) {
			JobView job = client.requestJob("Leiria", "Lisboa", 50); //ID-padding job		
			JobView originalJob = client.requestJob(CENTRO_CITY1, CENTRO_CITY2, 80);
			JobView storedJob = client.listJobs().get(1);
			assertEquals("UpaTransporter1", storedJob.getCompanyName());
			assertEquals(CENTRO_CITY1, storedJob.getJobOrigin());
			assertEquals(CENTRO_CITY2, storedJob.getJobDestination());
			assertEquals(JobStateView.PROPOSED, storedJob.getJobState());
		} else {
			JobView job = client.requestJob("Leiria", "Lisboa", 50); //ID-padding job		
			JobView originalJob = client.requestJob(CENTRO_CITY1, CENTRO_CITY2, 80);
			JobView storedJob = client.listJobs().get(1);
			assertEquals("UpaTransporter2", storedJob.getCompanyName());
			assertEquals(CENTRO_CITY1, storedJob.getJobOrigin());
			assertEquals(CENTRO_CITY2, storedJob.getJobDestination());
			assertEquals(JobStateView.PROPOSED, storedJob.getJobState());
		}


	}	

}
