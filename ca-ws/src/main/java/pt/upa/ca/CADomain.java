package pt.upa.ca;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import javax.xml.registry.JAXRException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.ws.*;

/*
 * 
 * Implementation of Broker logic and structures
 *
 */
public class CADomain {
	
	private static final String MESSAGE_TO_UNKNOWNS = "Who is this?";
		
	private String wsname; //Broker Name
	private UDDINaming uddiNaming = null;
	
	public CADomain(String wsname, String uddiURL) throws JAXRException {
		this.wsname = wsname;
		this.uddiNaming = new UDDINaming(uddiURL);		
	}
	
	public String ping(String name) {        
		String response = "";
		
		if (name == null || name.length() == 0) {
        	return MESSAGE_TO_UNKNOWNS;
        } 
		response += "Hello, " + name + ". " + wsname + " is ready! ";
				
		return response;
	}
}