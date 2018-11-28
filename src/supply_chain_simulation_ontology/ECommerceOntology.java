package supply_chain_simulation_ontology;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class ECommerceOntology extends BeanOntology {
	
	private static Ontology theInstance = new ECommerceOntology("my_ontology");
	
	public static Ontology getInstance(){
		return theInstance;
	}
	
	//singleton pattern
	private ECommerceOntology(String name) {
		super(name);
		try {
			add("supply_chain_simulation_ontology.elements.concepts");
			add("supply_chain_simulation_ontology.elements.concepts.comps");
			add("supply_chain_simulation_ontology.elements.actions");
			
			System.out.println("added the ontologies.");
		}
			catch (BeanOntologyException e) {
			e.printStackTrace();
		}
	}
	
}