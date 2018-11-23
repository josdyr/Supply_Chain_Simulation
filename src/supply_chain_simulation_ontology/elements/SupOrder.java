package supply_chain_simulation_ontology.elements;

import java.util.HashMap;

import jade.content.AgentAction;

public class SupOrder implements AgentAction {
	
	public HashMap<String, Integer> comps_in_demand = new HashMap<>();

	public HashMap<String, Integer> getComps_in_demand() {
		return comps_in_demand;
	}

	public void setComps_in_demand(HashMap<String, Integer> comps_in_demand) {
		this.comps_in_demand = comps_in_demand;
	}
	
}
