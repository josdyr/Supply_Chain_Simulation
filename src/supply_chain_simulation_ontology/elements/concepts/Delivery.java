package supply_chain_simulation_ontology.elements.concepts;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;
import jade.core.AID;

public class Delivery implements Concept {
	
	// Mandatory
	PC myPC;
	
	// Not Mandatory
	private Integer deliver_in_days;
	private Integer total_cost;
	private AID receiver;

	@Override
	public String toString() {
		return "Delivery [deliver_in_days=" + deliver_in_days + ", total_cost=" + total_cost + ", myPC=" + myPC + "]";
	}

	// Getters and Setters
	@Slot ( mandatory = true )
	public PC getMyPC() {
		return myPC;
	}

	public void setMyPC(PC myPC) {
		this.myPC = myPC;
	}
	
	public Integer getDeliver_in_days() {
		return deliver_in_days;
	}

	public void setDeliver_in_days(Integer deliver_in_days) {
		this.deliver_in_days = deliver_in_days;
	}

	public Integer getTotal_cost() {
		return total_cost;
	}

	public void setTotal_cost(Integer total_cost) {
		this.total_cost = total_cost;
	}

	@Slot ( mandatory = true )
	public AID getReceiver() {
		return receiver;
	}

	public void setReceiver(AID receiver) {
		this.receiver = receiver;
	}
	
}
