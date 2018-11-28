package supply_chain_simulation_ontology.elements.actions;

import jade.content.AgentAction;
import jade.core.AID;
import supply_chain_simulation_ontology.elements.concepts.Order;

public class Buy implements AgentAction {
	
	private Order order;

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
	
}
