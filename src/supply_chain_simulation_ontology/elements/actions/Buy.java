package supply_chain_simulation_ontology.elements.actions;

import jade.content.AgentAction;
import jade.content.onto.annotations.Slot;
import jade.core.AID;
import supply_chain_simulation_ontology.elements.concepts.Order;

public class Buy implements AgentAction {
	
	private Order order;
	private AID customer_buyer;

	// Getters and Setters
	@Slot ( mandatory = true )
	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public AID getCustomer_buyer() {
		return customer_buyer;
	}

	public void setCustomer_buyer(AID customer_buyer) {
		this.customer_buyer = customer_buyer;
	}
	
}
