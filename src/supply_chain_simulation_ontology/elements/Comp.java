package supply_chain_simulation_ontology.elements;

import jade.content.Concept;

public class Comp implements Concept {
	
	private String name;
	
	public Comp(String name, Integer quantity) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "Comp [name=" + name + ", quantity=" + quantity + "]";
	}
	
}
