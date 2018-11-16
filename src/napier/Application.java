package napier;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Runtime;

public class Application {
	public static void main(String[] args) {

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

			// Spin off SupplierAgent
			int numSupplierAgent = 1;
			AgentController SupplierAgent;
			for(int i = 0; i < numSupplierAgent; i++) {
				SupplierAgent = myContainer.createNewAgent("mySupplierAgent", SupplierAgent.class.getCanonicalName(), null);
				SupplierAgent.start();
			}

			AgentController ManufacturerAgent = myContainer.createNewAgent("myManufacturerAgent", ManufacturerAgent.class.getCanonicalName(), null);
			ManufacturerAgent.start();

			// Spin off CustumerAgents
			int numCustumerAgents = 1;
			AgentController CustomerAgent;
			for(int i = 0; i < numCustumerAgents; i++) {
				CustomerAgent = myContainer.createNewAgent("myCustomerAgent", CustomerAgent.class.getCanonicalName(), null);
				CustomerAgent.start();
			}

		} catch (Exception e) {
			System.out.println("Exception starting agent: " + e.toString());
		}


	}
}
