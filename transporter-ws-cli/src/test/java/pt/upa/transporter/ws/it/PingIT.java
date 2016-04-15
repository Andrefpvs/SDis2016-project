package pt.upa.transporter.ws.it;

import org.junit.*;
import static org.junit.Assert.*;

public class PingIT extends BaseTransporterIT {
	
 	private static final String MESSAGE_TO_UNKNOWNS = "Who is this?";

	/**
	 * Test that ping returns a message that acknowledges
	 * the specific client
	 */
	@Test
	public void testReturnMessage() throws Exception {
		String response = client.ping("Integration Test");
		assertNotEquals(MESSAGE_TO_UNKNOWNS, response);
	}
	
	/**
	 * Test that ping returns a specific message if the pinger
	 * doesn't identify themselves
	 */
	@Test
	public void testReturnMessageToUnknowns() throws Exception {
		String response = client.ping(null);
		assertEquals(MESSAGE_TO_UNKNOWNS, response);
	}
	
}
