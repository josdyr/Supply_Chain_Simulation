package napier;

import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class CustomerAgent extends Agent {
	
//	float rand = 0.0f;
	
	Random rand = new Random();
	
	String memory = "";
	String hardDrive = "";
	String os = "";
	
	Desktop desktop_pc;
	Laptop laptop_pc;
	
	//This method is called when the agent is launched
	protected void setup() {
		// Print out a welcome message
		System.out.println("Enrolled: " + getAID().getName() + ", standing by...");
		
		//generate random components of order
		if (rand.nextFloat() < 0.5f) {
			memory = "8GB";
		} else {
			memory = "16GB";
		}
		if (rand.nextFloat() < 0.5f) {
			hardDrive = "1Tb";
		} else {
			hardDrive = "2Tb";
		}
		if (rand.nextFloat() < 0.5f) {
			os = "Windows";
		} else {
			os = "Linux";
		}
		
		//generate the random computer order from components
		if (rand.nextFloat() < 0.5f) {
			//desktop
			desktop_pc = new Desktop(memory, hardDrive, os);
			System.out.println("desktop_pc: " + desktop_pc.printPC());
		} else {
			//laptop
			laptop_pc = new Laptop(memory, hardDrive, os);
			System.out.println("laptop_pc: " + laptop_pc.printPC());
		}
		
		System.out.println("My order is: " + "order...");
		
	}
}