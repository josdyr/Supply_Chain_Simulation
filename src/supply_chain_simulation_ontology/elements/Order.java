package supply_chain_simulation_ontology.elements;

import java.lang.*;
import java.util.Random;

import jade.content.Concept;

public class Order implements Concept {
	
	Random rand = new Random();
	
	PC myPC;
	
	int quantity = (int)Math.floor(1 + 50 * rand.nextFloat());
	int price = (int)Math.floor(600 + 200 * rand.nextFloat());
	int total_price = (int)quantity * price;
	int due_in_days = (int)Math.floor(1 + 10 * rand.nextFloat());
	
	public Order(PC myPC) {
		this.myPC = myPC;
	}
	
	public String printOrder() {
		return "\n" +
				"\t" + "PC: " + this.myPC.printPC() + "\n" +
				"\t" + "quantity: " + this.quantity + "\n" +
				"\t" + "price: " + this.price + "\n" +
				"\t" + "total_price: " + this.total_price + "\n" +
				"\t" + "due_in_days: " + this.due_in_days + "\n";
	}
	
}
