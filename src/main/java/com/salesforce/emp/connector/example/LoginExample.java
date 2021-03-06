/*
 * Copyright (c) 2016, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.TXT file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.emp.connector.example;

import static com.salesforce.emp.connector.LoginHelper.login;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.eclipse.jetty.util.ajax.JSON;

import com.salesforce.emp.connector.BayeuxParameters;
import com.salesforce.emp.connector.EmpConnector;
import com.salesforce.emp.connector.LoginHelper;
import com.salesforce.emp.connector.TopicSubscription;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

/**
 * An example of using the EMP connector using login credentials
 *
 * @author hal.hildebrand
 * @since API v37.0
 */
public class LoginExample {
    public static void main(String[] argv) throws Exception {
    	System.out.println("Inside LoginExample..............####################..............");
        if (argv.length < 3) { // || argv.length > 4) {
            System.err.println("Usage: LoginExample username password list_of_topics each separated by a space...");
            System.exit(1);
        }
        
        long replayFrom = EmpConnector.REPLAY_FROM_EARLIEST;
        /*if (argv.length == 4) {
            replayFrom = Long.parseLong(argv[3]);
        }*/

        BearerTokenProvider tokenProvider = new BearerTokenProvider(() -> {
            try {
                return login(argv[0], argv[1]);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.exit(1);
                throw new RuntimeException(e);
            }
        });

        BayeuxParameters params = tokenProvider.login();

        /*try {	
        	System.out.println("Calling writeToFile");
	        Consumer<Map<String, Object>> consumer2 = event -> writeToFile(JSON.toString(JSON.toString(event)));
        	System.out.println("After calling writeToFile");
        } catch(Exception e) {
        	e.printStackTrace();
        }*/

        Consumer<Map<String, Object>> employeeConsumer = employeeEvent -> System.out.println(String.format("Received Employee update:\n%s", JSON.toString(employeeEvent)) + "\n\n\n");   //writeToFile(JSON.toString(event));
	        
        Consumer<Map<String, Object>> playerConsumer = playerEvent -> System.out.println(String.format("Received Player update:\n%s", JSON.toString(playerEvent)) + "\n\n\n");   //writeToFile(JSON.toString(event));
        
        Consumer<Map<String, Object>> opportunityConsumer = opportunityEvent -> System.out.println(String.format("Received Opportunity update:\n%s", JSON.toString(opportunityEvent)) + "\n\n\n");   //writeToFile(JSON.toString(event));
        
        EmpConnector connector = new EmpConnector(params);

        connector.setBearerTokenProvider(tokenProvider);

        connector.start().get(5, TimeUnit.SECONDS);

        TopicSubscription employeeSubscription = connector.subscribe(argv[2], replayFrom, employeeConsumer).get(5, TimeUnit.SECONDS);

        TopicSubscription playerSubscription = connector.subscribe(argv[3], replayFrom, playerConsumer).get(5, TimeUnit.SECONDS);

        TopicSubscription opportunitySubscription = connector.subscribe(argv[4], replayFrom, opportunityConsumer).get(5, TimeUnit.SECONDS);

        System.out.println(String.format("Subscribed: %s", employeeSubscription));

        System.out.println(String.format("Subscribed: %s", playerSubscription));

        System.out.println(String.format("Subscribed: %s", opportunitySubscription));
    }

	private static void writeToFile(String eventData) {
		FileOutputStream fos = null;
		try {
			/*fos = new FileOutputStream(new File("/Users/ant/per/Salesforce_StreamingAPI/EMP-Connector/events.log"));
			System.out.println("Writing data to log file");
			fos.write(eventData.getBytes(), 0, eventData.length());
			System.out.println("Finished writing data to log file");
			
			FileOutputStream outputStream = new FileOutputStream("/Users/ant/per/Salesforce_StreamingAPI/EMP-Connector/events.log", true);
		    byte[] strToBytes = eventData.getBytes();
		    outputStream;
		    outputStream.write(strToBytes);
		  
		    outputStream.close();*/
		    
		    BufferedWriter writer = new BufferedWriter(
                    new FileWriter("/Users/ant/per/Salesforce_StreamingAPI/EMP-Connector/events.log", true)  //Set true for append mode
			                );  
			writer.newLine();   //Add new line
			writer.newLine();   //Add new line
			writer.write(eventData);
			writer.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();;
		}
		
		
		
	}
}
