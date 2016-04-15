package pt.upa.transporter.ws.it;

import org.junit.*;
import static org.junit.Assert.*;


import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;

public class DecideJobIT extends BaseTransporterIT {
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
	 * Test that the transporter doesn't decide on jobs that have an invalid Id
	 */
	@Test(expected = BadJobFault_Exception.class)
	public void testDecideJobWithInvalidId() throws Exception {
		JobView job1 = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 40);
		JobView job2 = client.decideJob("T10I4", true);
	}

	/**
	 * Test that the transporter correctly accepts jobs
	 */
	@Test
	public void testDecideJobAccept() throws Exception {
		if (client.ping("--getTransporterName").equals(ODD_TRANSPORTER)) {
			JobView job1 = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
			JobView job2 = client.decideJob("T1I1", true);
			assertEquals(JobStateView.ACCEPTED, job2.getJobState());
		} else {
			JobView job1 = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
			JobView job2 = client.decideJob("T2I1", true);
			assertEquals(JobStateView.ACCEPTED, job2.getJobState());
		}
	}

	/**
	 * Test that the transporter correctly rejects jobs
	 */
	@Test
	public void testDecideJobReject() throws Exception {
		if (client.ping("--getTransporterName").equals(ODD_TRANSPORTER)) {
			JobView job1 = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
			JobView job2 = client.decideJob("T1I1", false);
			assertEquals(JobStateView.REJECTED, job2.getJobState());
		} else {
			JobView job1 = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
			JobView job2 = client.decideJob("T2I1", false);
			assertEquals(JobStateView.REJECTED, job2.getJobState());
		}
	}

	/**
	 * Test that the transporter doesn't decide on jobs not in PROPOSED state
	 */
	@Test(expected = BadJobFault_Exception.class)
	public void testDecideJobIncorrectState() throws Exception {
		if (client.ping("--getTransporterName").equals(ODD_TRANSPORTER)) {
			JobView job1 = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
			JobView job2 = client.decideJob("T1I1", true);
			assertEquals(JobStateView.ACCEPTED, job2.getJobState());
			JobView job3 = client.decideJob("T1I1", true);
		} else {
			JobView job1 = client.requestJob(CENTRO_CITY2, CENTRO_CITY1, 50);
			JobView job2 = client.decideJob("T2I1", true);
			assertEquals(JobStateView.ACCEPTED, job2.getJobState());
			JobView job3 = client.decideJob("T2I1", true);
		}
	}
}
