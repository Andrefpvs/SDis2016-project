package pt.upa.transporter.ws;

import org.junit.*;
import static org.junit.Assert.*;
import mockit.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.TransporterEndpointManager;

public class TransporterEndpointManagerTest {
	
	/**
     *  In this test the UDDINaming object is mocked
     *  to allow testing the Endpoint creation class.
     */
    @Test
    public void testEndpointManager(
        @Mocked final UDDINaming uddiNaming) 
        throws Exception {

        // Preparation code not specific to JMockit, if any.
        final String WS_NAME = "UpaTransporter1";
        final String WS_URL = "http://localhost:8081/transporter-ws/endpoint";
        final String UDDI_URL = "http://localhost:9090";

        // The "expectations block"
        // One or more invocations to the mocked type
        new Expectations() {{
            uddiNaming.rebind(WS_NAME, WS_URL);
        }};

        // Unit under test is exercised.
        TransporterEndpointManager endpoint = new TransporterEndpointManager(UDDI_URL, WS_NAME, WS_URL);
        endpoint.setVerbose(false); //it's a test, we don't need UDDI messages
        endpoint.start();

        // A "verification block"
        // One or more invocations to mocked types, causing expectations to be verified.
        new Verifications() {{
            // Verifies that zero or one invocations occurred, with the specified argument value:
            uddiNaming.rebind(WS_NAME, WS_URL); maxTimes = 1;
        }};

        // Additional verification code
        assertNotNull(endpoint);
    }
}
