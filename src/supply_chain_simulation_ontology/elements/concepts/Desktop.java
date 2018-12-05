package supply_chain_simulation_ontology.elements.concepts;

import java.util.ArrayList;

import jade.content.onto.annotations.AggregateSlot;
import supply_chain_simulation_ontology.elements.concepts.comps.Desktop_CPU;
import supply_chain_simulation_ontology.elements.concepts.comps.Desktop_Motherboard;

public class Desktop extends PC {
	
	public ArrayList<Comp> pc_components = new ArrayList<Comp>();
	
	// Constructor
	public Desktop() {
		this.appendComp(new Desktop_CPU());
		this.appendComp(new Desktop_Motherboard());
	}

	// Add a single component
	public void appendComp(Comp c) {
		this.pc_components.add(c);
	}
	
	// toString
	@Override
	public String toString() {
		return "Desktop [pc_components=" + pc_components + "]";
	}
	
	// Getters and Setters
	@AggregateSlot ( cardMin = 5)
	public ArrayList<Comp> getPc_components() {
		return pc_components;
	}

	public void setPc_components(ArrayList<Comp> pc_components) {
		this.pc_components = pc_components;
	}
	
}
