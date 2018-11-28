package supply_chain_simulation_ontology.elements.concepts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;

public abstract class PC implements Concept {
	
	public abstract void appendComp(Comp c);
	
}