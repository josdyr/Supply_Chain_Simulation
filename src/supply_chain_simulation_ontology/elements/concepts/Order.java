package supply_chain_simulation_ontology.elements.concepts;

import java.lang.*;
import java.util.Random;

import jade.content.AgentAction;
import jade.content.Concept;
import jade.content.onto.annotations.Slot;
import jade.core.AID;

public class Order implements Concept {
	
	private AID buyer;

	private PC myPC;
	
	int quantity;
	int price;
	int total_price;
	int due_in_days;

	@Override
	public String toString() {
		return "Order [buyer=" + buyer + ", myPC=" + myPC.toString() + ", quantity=" + quantity + ", price=" + price
				+ ", total_price=" + total_price + ", due_in_days=" + due_in_days + "]";
	}
	
	// Getters and Setters
	@Slot ( mandatory = true )
	public AID getBuyer() {
		return buyer;
	}

	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}

	@Slot ( mandatory = true )
	public PC getMyPC() {
		return myPC;
	}

	public void setMyPC(PC myPC) {
		this.myPC = myPC;
	}

	@Slot ( mandatory = true )
	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Slot ( mandatory = true )
	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	@Slot ( mandatory = true )
	public int getTotal_price() {
		return total_price;
	}

	public void setTotal_price(int total_price) {
		this.total_price = total_price;
	}

	@Slot ( mandatory = true )
	public int getDue_in_days() {
		return due_in_days;
	}

	public void setDue_in_days(int due_in_days) {
		this.due_in_days = due_in_days;
	}
	
}
