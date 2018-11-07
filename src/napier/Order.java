package napier;

import java.lang.*;
import java.util.Random;

public class Order {
	
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
