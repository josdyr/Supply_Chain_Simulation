package supply_chain_simulation_ontology.elements;

import java.lang.*;
import java.util.Random;

import jade.content.AgentAction;
import jade.content.Concept;
import jade.core.AID;

public class Order implements AgentAction {
	
	Random rand = new Random();
	
	private AID buyer;
	private PC myPC;
	
	int quantity = (int)Math.floor(1 + 50 * rand.nextFloat());
	int price = (int)Math.floor(600 + 200 * rand.nextFloat());
	int total_price = (int)quantity * price;
	int due_in_days = (int)Math.floor(1 + 10 * rand.nextFloat());
	
	public Order(PC myPC) {
		setMyPC(myPC);
	}
	
	public String printOrder() {
		return "\n" +
				"\t" + "PC: " + this.myPC.printPC() + "\n" +
				"\t" + "quantity: " + this.quantity + "\n" +
				"\t" + "price: " + this.price + "\n" +
				"\t" + "total_price: " + this.total_price + "\n" +
				"\t" + "due_in_days: " + this.due_in_days + "\n";
	}
	
	// Getters and Setters
	public AID getBuyer() {
		return buyer;
	}

	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}

	public PC getMyPC() {
		return myPC;
	}

	public void setMyPC(PC myPC) {
		this.myPC = myPC;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getTotal_price() {
		return total_price;
	}

	public void setTotal_price(int total_price) {
		this.total_price = total_price;
	}

	public int getDue_in_days() {
		return due_in_days;
	}

	public void setDue_in_days(int due_in_days) {
		this.due_in_days = due_in_days;
	}
	
}
