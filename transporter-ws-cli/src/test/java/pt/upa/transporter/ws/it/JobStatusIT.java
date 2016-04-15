package pt.upa.transporter.ws.it;

import org.junit.*;
import static org.junit.Assert.*;


import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;

public class JobStatusIT extends BaseTransporterIT {
	private static final String ODD_TRANSPORTER = "UpaTransporter1";
	private static final String EVEN_TRANSPORTER = "UpaTransporter2";
	private static final String CENTRO_CITY1 = "Lisboa";
	private static final String CENTRO_CITY2 = "Leiria";
	
    @Before
    public void setUp() {
    	client.clearJobs();
    }

    @After
    public void tearDown() {
    	client.clearJobs();
    }
    
    /**
	 * Test if jobStatus method correctly returns an existing job
	 */
	@Test
	public void testJobStatusExistingJob() throws Exception {
		if (client.ping("--getTransporterName").equals(ODD_TRANSPORTER)) {
			JobView job1 = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
			JobView job2 = client.jobStatus("T1I1");
			assertEquals("UpaTransporter1", job2.getCompanyName());
			assertEquals("T1I1", job2.getJobIdentifier());
			assertEquals(CENTRO_CITY2, job2.getJobOrigin());
			assertEquals(CENTRO_CITY1, job2.getJobDestination());
			assertEquals(JobStateView.PROPOSED, job2.getJobState());
		} else {
			JobView job1 = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
			JobView job2 = client.jobStatus("T2I1");
			assertEquals("UpaTransporter2", job2.getCompanyName());
			assertEquals("T2I1", job2.getJobIdentifier());
			assertEquals(CENTRO_CITY2, job2.getJobOrigin());
			assertEquals(CENTRO_CITY1, job2.getJobDestination());
			assertEquals(JobStateView.PROPOSED, job2.getJobState());
		}
		
	}

	/**
	 * Test if jobStatus method correctly returns null for an unexisting job
	 */
	@Test
	public void testJobStatusUnexistingJob() throws Exception {
		JobView job1 = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
		JobView job2 = client.jobStatus("T1I3");
		assertNull(job2);
	}
}
