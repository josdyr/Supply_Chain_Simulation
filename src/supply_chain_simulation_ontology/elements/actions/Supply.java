package supply_chain_simulation_ontology.elements.actions;

import jade.content.AgentAction;
import jade.core.AID;
import supply_chain_simulation_ontology.elements.concepts.Delivery;
import supply_chain_simulation_ontology.elements.concepts.Order;

public class Supply implements AgentAction {
	
	Delivery delivery;

	@Override
	public String toString() {
		return "Supply [delivery=" + delivery + "]";
	}

	public Delivery getDelivery() {
		return delivery;
	}

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
	}
	
}
