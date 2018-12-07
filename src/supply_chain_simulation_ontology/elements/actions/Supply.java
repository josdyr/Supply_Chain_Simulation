package supply_chain_simulation_ontology.elements.actions;

import jade.content.AgentAction;
import jade.content.onto.annotations.Slot;
import jade.core.AID;
import supply_chain_simulation_ontology.elements.concepts.Delivery;
import supply_chain_simulation_ontology.elements.concepts.Order;

public class Supply implements AgentAction {
	
	Delivery delivery;

	@Override
	public String toString() {
		return "Supply [delivery=" + delivery + "]";
	}

	// Getters and Setters
	
	@Slot ( mandatory = true )
	public Delivery getDelivery() {
		return delivery;
	}

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
	}
	
}
