package pt.upa.transporter.ws;

import org.junit.*;
import static org.junit.Assert.*;


public class TransporterPortTest {
    // static members
	
 	private static final String MESSAGE_TO_UNKNOWNS = "Who is this?";

	
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
    public void testPingWithNoName() {
    	localPort = new TransporterPort("UpaTransporter1");
    	assertEquals(MESSAGE_TO_UNKNOWNS, localPort.ping(null));
    }
    
    /**
     * Test that providing an empty name to
     * the Ping method yields a different message
     */
    @Test
    public void testPingWithEmptyName() {
    	localPort = new TransporterPort("UpaTransporter1");
    	assertEquals(MESSAGE_TO_UNKNOWNS, localPort.ping(""));
    }
    
}
