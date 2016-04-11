package pt.upa.transporter;

import pt.upa.transporter.ws.cli.TransporterClient;

public class TransporterClientApplication {

	public static void main(String[] args) throws Exception {
		System.out.println(TransporterClientApplication.class.getSimpleName() + " starting...");
		
		//TransporterClient client = new TransporterClient(uddiurl, wsname);
		
		//client.ping("123");
		// Check arguments
		if (args.length == 0) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + TransporterClientApplication.class.getName() + " wsURL OR uddiURL wsName");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		if (args.length == 1) {
			wsURL = args[0];
		} else if (args.length >= 2) {
			uddiURL = args[0];
			wsName = args[1]; // ex.: mvn -Dws.i=2 exec:java (look for UpaTransporter2)
		}

		// Create client
		TransporterClient client = null;

		if (wsURL != null) {
			System.out.printf("Creating client for server at %s%n", wsURL);
			client = new TransporterClient(wsURL);
		} else if (uddiURL != null) {
			System.out.printf("Creating client using UDDI at %s for server with name %s%n", uddiURL, wsName);
			client = new TransporterClient(uddiURL, wsName);
		}

		// Test remote invocation with ping method (does not replace Unit testing)
		System.out.print("Pinging...\n");
		String pingResult = client.ping("TransporterClientApplication");
		System.out.println(pingResult);
	}
}
