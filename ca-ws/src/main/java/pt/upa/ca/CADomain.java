package pt.upa.ca;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

import javax.xml.registry.JAXRException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.ws.*;

/*
 * 
 * Implementation of CA logic and structures
 *
 */
public class CADomain {
	
	private static final String MESSAGE_TO_UNKNOWNS = "Who is this?";
	private static final String BROKER_CERTIFICATE = "keys/UpaBroker/UpaBroker.cer"; // ./ (?)
	private static final String TRANSPORTER1_CERTIFICATE = "keys/UpaTransporter1/UpaTransporter1.cer";
	private static final String TRANSPORTER2_CERTIFICATE = "keys/UpaTransporter2/UpaTransporter2.cer";
	
	private Certificate upaBrokerCertificate;
	private Certificate upaTransporter1Certificate;
	private Certificate upaTransporter2Certificate;
		
	private String wsname;
	private UDDINaming uddiNaming = null;
	
	public CADomain(String wsname, String uddiURL) throws JAXRException {
		this.wsname = wsname;
		this.uddiNaming = new UDDINaming(uddiURL);
		
		FileInputStream fis;		
		try {
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			fis = new FileInputStream(BROKER_CERTIFICATE);
			upaBrokerCertificate = factory.generateCertificate(fis);
			fis = new FileInputStream(TRANSPORTER1_CERTIFICATE);
			upaTransporter1Certificate = factory.generateCertificate(fis);
			fis = new FileInputStream(TRANSPORTER2_CERTIFICATE);
			upaTransporter2Certificate = factory.generateCertificate(fis);
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public String ping(String name) {        
		String response = "";
		
		if (name == null || name.length() == 0) {
        	return MESSAGE_TO_UNKNOWNS;
        } 
		response += "Hello, " + name + ". " + wsname + " is ready! ";
				
		return response;
	}
	
	public byte[] requestBrokerCertificate() {
		byte[] certificateResponse = null;
		
		try {
			certificateResponse = upaBrokerCertificate.getEncoded();
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
				
		if(certificateResponse == null) {
			System.out.println("Error creating Broker Certificate");
		}
		return certificateResponse;
	}
	
	public byte[] requestTransporter1Certificate() {
		byte[] certificateResponse = null;
		
		try {
			certificateResponse = upaTransporter1Certificate.getEncoded();
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
				
		if(certificateResponse == null) {
			System.out.println("Error creating Transporter1 Certificate");
		}
		return certificateResponse;
	}
	
	public byte[] requestTransporter2Certificate() {
		byte[] certificateResponse = null;
		
		try {
			certificateResponse = upaTransporter2Certificate.getEncoded();
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
				
		if(certificateResponse == null) {
			System.out.println("Error creating Transporter2 Certificate");
		}
		return certificateResponse;
	}
}