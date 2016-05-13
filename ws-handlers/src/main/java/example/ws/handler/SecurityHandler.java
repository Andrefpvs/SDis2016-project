package example.ws.handler;

import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.xml.registry.JAXRException;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.ws.cli.*;

/**
 *  This SOAPHandler shows how to set/get values from headers in
 *  inbound/outbound SOAP messages.
 *
 *  A header is created in an outbound message and is read on an
 *  inbound message.
 *
 *  The value that is read from the header
 *  is placed in a SOAP message context property
 *  that can be accessed by other handlers or by the application.
 */
public class SecurityHandler implements SOAPHandler<SOAPMessageContext> {

    CAClient ca = null;
    String endpoint = null;

    private static final String CA_NAME = "CA";

    public static final String CONTEXT_PROPERTY = "my.property";
    
    public static final String RESPONSE_PROPERTY = "my.response.property";
    
    final static String BROKER_KEYSTORE_FILE = "../broker-ws/keys/UpaBroker/UpaBroker.jks";
    final static String TRANSPORTER1_KEYSTORE_FILE = "../transporter-ws/keys/UpaTransporter1/UpaTransporter1.jks";
    final static String TRANSPORTER2_KEYSTORE_FILE = "../transporter-ws/keys/UpaTransporter2/UpaTransporter2.jks";
    
    private final String STORE_PASS = "ins3cur3";
    private final String KEY_PASS = "1nsecure";

    //
    // Handler interface methods
    //
    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
    
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        System.out.println("SecurityHandler: Handling message.");

        Boolean outboundElement = (Boolean) smc
                .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {
            if (outboundElement.booleanValue()) {
            
            //IS IT BROKER, TRANSPORTER1 OR TRANSPORTER2?
            String propertyValue = (String) smc.get(RESPONSE_PROPERTY);
            
            
            
            //CREATING CA CLIENT
            UDDINaming uddiNaming = new UDDINaming("http://localhost:9090");
        
		try {
			endpoint = uddiNaming.lookup(CA_NAME);
		} catch (JAXRException e) {
			System.out.println("Failed to lookup CA");
		}
        
        ca = new CAClient(endpoint);
        
            if(propertyValue == "UpaTransporter1"){
            
            
                System.out.println("Message departing from Transporter1");
                System.out.println("Writing header in outbound SOAP message...");

                FileInputStream fIn = new FileInputStream(TRANSPORTER1_KEYSTORE_FILE);
                KeyStore keystore = KeyStore.getInstance("JKS");
                keystore.load(fIn, STORE_PASS.toCharArray());
                
                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();

                // add header
                SOAPHeader sh = se.getHeader();
                if (sh == null)
                    sh = se.addHeader();
                
                
                //ASSINATURA DIGITAL
                // add header element (name, namespace prefix, namespace)
                Name signame = se.createName("DigitalSignature", "s", "security");
                SOAPHeaderElement signature = sh.addHeaderElement(signame);

                //get broker public key from ca (encoded)
                byte[] transporter1publickey = ca.requestTransporter1Certificate();
                
                // add header element value
                
                String transporter1keystring = printBase64Binary(transporter1publickey);
                signature.addTextNode(transporter1keystring);
                
                
                //DIGEST
                msg.writeTo(baos);
                String msgcontent = baos.toString();
                byte[] convertedmsg = parseBase64Binary(msgcontent);
                
                PrivateKey pk = (PrivateKey) keystore.getKey("UpaTransporter1", KEY_PASS.toCharArray());
                
                byte[] digitalsigncypher = null;
                try{
                   // digitalsigncypher = makeDigitalSignature(convertedmsg, pk);
                }catch(Exception e){
                    System.out.println("Couldn't cypher message content");
                }
                
                Name digname = se.createName("Cyphered and digested message", "s", "security");
                SOAPHeaderElement digest = sh.addHeaderElement(digname);
                
                String digstring = printBase64Binary(digitalsigncypher);
                signature.addTextNode(digstring);
                
                
                //NOUNCE
                //Generate Nounce (byte array)
                //SecureRandomGen generator = new SecureRandomGen();
	//	byte[] random = generator.getRandomNumber();
                
                
                byte[] nouncecypher = null;
                
                try{
                   // nouncecypher = makeDigitalSignature(random, pk);
                }catch(Exception e){
                    System.out.println("Couldn't cypher nounce");
                }
                
                Name nouncename = se.createName("Nounce", "s", "security");
                SOAPHeaderElement nounce = sh.addHeaderElement(nouncename);
                
                // add header element value
                
                String nouncestring = printBase64Binary(nouncecypher);
                signature.addTextNode(nouncestring);
                
                
                } else if(propertyValue == "UpaTransporter2"){
            
            
                System.out.println("Message departing from Transporter2");
                System.out.println("Writing header in outbound SOAP message...");
                
                FileInputStream fIn = new FileInputStream(TRANSPORTER2_KEYSTORE_FILE);
                KeyStore keystore = KeyStore.getInstance("JKS");
                keystore.load(fIn, STORE_PASS.toCharArray());

                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();

                // add header
                SOAPHeader sh = se.getHeader();
                if (sh == null)
                    sh = se.addHeader();
                
                
                //ASSINATURA DIGITAL
                // add header element (name, namespace prefix, namespace)
                Name signame = se.createName("DigitalSignature", "s", "security");
                SOAPHeaderElement signature = sh.addHeaderElement(signame);

                //get broker public key from ca (encoded)
                byte[] transporter2publickey = ca.requestTransporter2Certificate();
                
                // add header element value
                
                String transporter2keystring = printBase64Binary(transporter2publickey);
                signature.addTextNode(transporter2keystring);
                
                
                //DIGEST
                msg.writeTo(baos);
                String msgcontent = baos.toString();
                byte[] convertedmsg = parseBase64Binary(msgcontent);
                
                PrivateKey pk = (PrivateKey) keystore.getKey("UpaTransporter2", KEY_PASS.toCharArray());
                
                byte[] digitalsigncypher = null;
                try{
                   // digitalsigncypher = makeDigitalSignature(convertedmsg, pk);
                }catch(Exception e){
                    System.out.println("Couldn't cypher message content");
                }
                
                Name digname = se.createName("Cyphered and digested message", "s", "security");
                SOAPHeaderElement digest = sh.addHeaderElement(digname);
                
                String digstring = printBase64Binary(digitalsigncypher);
                signature.addTextNode(digstring);
                
                
                //NOUNCE
                //Generate Nounce (byte array)
               // SecureRandomGen generator = new SecureRandomGen();
	//	byte[] random = generator.getRandomNumber();
                
                
                byte[] nouncecypher = null;
                
                try{
                  //  nouncecypher = makeDigitalSignature(random, pk);
                }catch(Exception e){
                    System.out.println("Couldn't cypher nounce");
                }
                
                Name nouncename = se.createName("Nounce", "s", "security");
                SOAPHeaderElement nounce = sh.addHeaderElement(nouncename);
                
                // add header element value
                
                String nouncestring = printBase64Binary(nouncecypher);
                signature.addTextNode(nouncestring);
                
                } else {
            
            
                System.out.println("Message departing from Broker");
                System.out.println("Writing header in outbound SOAP message...");
                
                FileInputStream fIn = new FileInputStream(BROKER_KEYSTORE_FILE);
                KeyStore keystore = KeyStore.getInstance("JKS");
                keystore.load(fIn, STORE_PASS.toCharArray());

                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();

                // add header
                SOAPHeader sh = se.getHeader();
                if (sh == null)
                    sh = se.addHeader();
                
                
                //ASSINATURA DIGITAL
                // add header element (name, namespace prefix, namespace)
                Name signame = se.createName("DigitalSignature", "s", "security");
                SOAPHeaderElement signature = sh.addHeaderElement(signame);

                //get broker public key from ca (encoded)
                byte[] brokerpublickey = ca.requestBrokerCertificate();
                
                // add header element value
                
                String brokerkeystring = printBase64Binary(brokerpublickey);
                signature.addTextNode(brokerkeystring);
                
                
                //DIGEST
                msg.writeTo(baos);
                String msgcontent = baos.toString();
                byte[] convertedmsg = parseBase64Binary(msgcontent);
                
                PrivateKey pk = (PrivateKey) keystore.getKey("UpaBroker", KEY_PASS.toCharArray());
                
                byte[] digitalsigncypher = null;
                try{
                    //digitalsigncypher = makeDigitalSignature(convertedmsg, pk);
                }catch(Exception e){
                    System.out.println("Couldn't cypher message content");
                }
                
                Name digname = se.createName("Cyphered and digested message", "s", "security");
                SOAPHeaderElement digest = sh.addHeaderElement(digname);
                
                String digstring = printBase64Binary(digitalsigncypher);
                signature.addTextNode(digstring);
                
                
                //NOUNCE
                //Generate Nounce (byte array)
              //  SecureRandomGen generator = new SecureRandomGen();
		//byte[] random = generator.getRandomNumber();
                
                
                byte[] nouncecypher = null;
                
                try{
                //    nouncecypher = makeDigitalSignature(random, pk);
                }catch(Exception e){
                    System.out.println("Couldn't cypher nounce");
                }
                
                Name nouncename = se.createName("Nounce", "s", "security");
                SOAPHeaderElement nounce = sh.addHeaderElement(nouncename);
                
                // add header element value
                
                String nouncestring = printBase64Binary(nouncecypher);
                signature.addTextNode(nouncestring);
    
                }

            } else {
                System.out.println("Reading header in inbound SOAP message...");

                // get SOAP envelope header
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = se.getHeader();

                // check header
                if (sh == null) {
                    System.out.println("Header not found.");
                    return true;
                }

                // get first header element
                Name name = se.createName("myHeader", "d", "http://demo");
                Iterator it = sh.getChildElements(name);
                // check header element
                if (!it.hasNext()) {
                    System.out.println("Header element not found.");
                    return true;
                }
                SOAPElement element = (SOAPElement) it.next();

                // get header element value
                String valueString = element.getValue();
                int value = Integer.parseInt(valueString);

                // print received header
                System.out.println("Header value is " + value);

                // put header in a property context
                //smc.put(CONTEXT_PROPERTY, value);
                // set property scope to application client/server class can access it
                //smc.setScope(CONTEXT_PROPERTY, Scope.APPLICATION);

            }
        } catch (Exception e) {
            System.out.print("Caught exception in handleMessage: ");
            System.out.println(e);
            System.out.println("Continue normal processing...");
        }

        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        System.out.println("Ignoring fault message...");
        return true;
    }

    public void close(MessageContext messageContext) {
    }

}