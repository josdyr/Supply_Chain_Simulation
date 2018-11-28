package supply_chain_simulation_ontology.elements.concepts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jade.content.AgentAction;
import jade.content.Concept;

public class SupOrder implements Concept {
	
	private ArrayList<Comp> components_in_demand = new ArrayList<>();

	public ArrayList<Comp> getComponents_in_demand() {
		return components_in_demand;
	}

	public void setComponents_in_demand(ArrayList<Comp> components_in_demand) {
		this.components_in_demand = components_in_demand;
	}
	
}
