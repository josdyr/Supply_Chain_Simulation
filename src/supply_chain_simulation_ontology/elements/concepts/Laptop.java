package supply_chain_simulation_ontology.elements.concepts;

import java.util.ArrayList;

import supply_chain_simulation_ontology.elements.concepts.comps.Laptop_CPU;
import supply_chain_simulation_ontology.elements.concepts.comps.Laptop_Motherboard;
import supply_chain_simulation_ontology.elements.concepts.comps.Screen;

public class Laptop extends PC {
	
	private ArrayList<Comp> pc_components = new ArrayList<Comp>();
	
	// Constructor
	public Laptop() {
		this.appendComp(new Laptop_CPU());
		this.appendComp(new Laptop_Motherboard());
		this.appendComp(new Screen());
	}

	// Add a single component
	public void appendComp(Comp c) {
		this.pc_components.add(c);
	}
	
	// toString
	@Override
	public String toString() {
		return "Laptop [pc_components=" + pc_components + "]";
	}

	public ArrayList<Comp> getPc_components() {
		return pc_components;
	}

	public void setPc_components(ArrayList<Comp> pc_components) {
		this.pc_components = pc_components;
	}
	
}
