package supply_chain_simulation_ontology.elements.actions;

import jade.content.AgentAction;
import supply_chain_simulation_ontology.elements.concepts.Order;

public class ConfirmOrder implements AgentAction {
	
	private Order order;
	private Integer cost;
	
	// Getters and Setters
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
	public Integer getCost() {
		return cost;
	}
	public void setCost(Integer cost) {
		this.cost = cost;
	}
	
}
