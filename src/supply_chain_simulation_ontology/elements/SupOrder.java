package supply_chain_simulation_ontology.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jade.content.AgentAction;

public class SupOrder implements AgentAction {
	
	public List<Map.Entry<String, Integer>> comps_in_demand = new ArrayList<Map.Entry<String, Integer>>();

	public List<Map.Entry<String, Integer>> getComps_in_demand() {
		return comps_in_demand;
	}

	public void setComps_in_demand(List<Map.Entry<String, Integer>> comps_in_demand) {
		this.comps_in_demand = comps_in_demand;
	}
	
}
