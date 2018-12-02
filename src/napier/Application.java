package napier;

import java.util.ArrayList;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Runtime;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Application {
	public static void main(String[] args) {
		
		// Prepare Information about the 3 different Suppliers
		// ...

		//Setup the JADE environment
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		ContainerController myContainer = myRuntime.createMainContainer(myProfile);

		try {
			//Start the agent controller, which is itself an agent (rma)
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();

			// SynchTickerAgent
			AgentController SynchTickerAgent = myContainer.createNewAgent("mySynchTickerAgent", SynchTickerAgent.class.getCanonicalName(), null);
			SynchTickerAgent.start();

			// Spin off SupplierAgents
			Object[] sup_num = new Object[1];
			int numSupplierAgent = 3;
			for(int i = 0; i < numSupplierAgent; i++) {
				AgentController SupplierAgent;
				sup_num[0] = i+1;
				SupplierAgent = myContainer.createNewAgent("mySupplierAgent" + (i+1), SupplierAgent.class.getCanonicalName(), sup_num);
				SupplierAgent.start();
			}

			
			
			Object[] s1 = new Object[11];
			Object[] s2 = new Object[11];
			Object[] s3 = new Object[11];
			
			
			// Read CSV File
			String csvFile = "src/sup_data/sup_data.csv";
	        BufferedReader br = null;
	        String line = "";
	        String cvsSplitBy = ",";

	        try {

	            br = new BufferedReader(new FileReader(csvFile));
	            Integer index = 0;
	            while ((line = br.readLine()) != null) {
	            	
	            	String[] col = line.split(cvsSplitBy);
	            	
	                s1[index] = col[0];
	                s2[index] = col[1];
	                s3[index] = col[2];
	            	
	            	index++;
	            }

	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (br != null) {
	                try {
	                    br.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
			
	        // Spin off one ManufacturerAgent
			Object[] suppliers_info = new Object[3];
			suppliers_info[0] = s1;
			suppliers_info[1] = s2;
			suppliers_info[2] = s3;
			AgentController ManufacturerAgent = myContainer.createNewAgent("myManufacturerAgent", ManufacturerAgent.class.getCanonicalName(), suppliers_info);
			ManufacturerAgent.start();

			// Spin off CustumerAgents
			Object[] cus_num = new Object[1];
			int numCustumerAgents = 1;
			for(int i = 0; i < numCustumerAgents; i++) {
				AgentController CustomerAgent;
				cus_num[0] = i+1;
				CustomerAgent = myContainer.createNewAgent("myCustomerAgent" + (i + 1), CustomerAgent.class.getCanonicalName(), cus_num);
				CustomerAgent.start();
			}

		} catch (Exception e) {
			System.out.println("Exception starting agent: " + e.toString());
		}


	}
}
