package napier;

import jade.core.Agent;

public class SupplierAgent extends Agent {
	//This method is called when the agent is launched
	protected void setup() {
		// Print out a welcome message
		System.out.println("Enrolled: " + getAID().getName() + ", standing by...");
	}
}
