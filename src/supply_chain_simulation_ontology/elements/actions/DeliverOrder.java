package supply_chain_simulation_ontology.elements.actions;

import jade.content.AgentAction;
import supply_chain_simulation_ontology.elements.concepts.PC;

public class DeliverOrder implements AgentAction {
	
	private PC myPC;

	// Getters and Setters
	public PC getMyPC() {
		return myPC;
	}

	public void setMyPC(PC myPC) {
		this.myPC = myPC;
	}
	
}
