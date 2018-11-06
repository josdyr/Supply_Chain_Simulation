package napier;

import jade.core.Agent;

public class ManufacturerAgent extends Agent {
	//This method is called when the agent is launched
	protected void setup() {
		// Print out a welcome message
		System.out.println("Hello! Agent "+getAID().getName()+" is ready.");
	}
}
