package pt.upa.broker;

import pt.upa.broker.ws.cli.*;

public class BrokerClientApplication {

	public static void main(String[] args) throws Exception {
		System.out.println(BrokerClientApplication.class.getSimpleName() + " starting...");
		
		if (args.length == 0) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + BrokerClientApplication.class.getName() + " wsURL OR uddiURL wsName");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		if (args.length == 1) {
			wsURL = args[0];
		} else if (args.length >= 2) {
			uddiURL = args[0];
			wsName = args[1];
		}

		/** 
		 *  Create client
		 *  This object will then talk with
		 *  Transporter clients
		 */
		BrokerClient client = null;

		if (wsURL != null) {
			System.out.printf("Creating client for server at %s%n", wsURL);
			client = new BrokerClient(wsURL);
		} else if (uddiURL != null) {
			System.out.printf("Creating client using UDDI at %s for server with name %s%n", uddiURL, wsName);
			client = new BrokerClient(uddiURL, wsName);
		}

		// Test remote invocation with ping method (does not replace Unit testing)
		System.out.print("Pinging...\n");
		String pingResult = client.ping("BrokerClientApplication");
		System.out.println(pingResult);
		System.out.println(client.requestTransport("Lisboa", "Leiria", 49)); //TODO Remove line
		
		

	}

}
