package napier;

import java.util.Random;

public class PC {
	
	Random rand = new Random();
	
	String type = "";
	Boolean screen;
	String cpu;
	String motherboard;
	String memory, harddrive, os;

	public PC() {
		
		//generate random PC
		if (rand.nextFloat() < 0.5f) {
			//Desktop
			this.type = "Desktop";
			this.screen = false;
			this.cpu = "desktopCPU";
			this.motherboard = "desktopMotherboard";
		} else {
			//Laptop
			this.type = "Laptop";
			this.screen = true;
			this.cpu = "laptopCPU";
			this.motherboard = "laptopMotherboard";
		}
		
		//generate random components
		if (rand.nextFloat() < 0.5f) {
			this.memory = "8GB";
		} else {
			this.memory = "16GB";
		}
		if (rand.nextFloat() < 0.5f) {
			this.harddrive = "1Tb";
		} else {
			this.harddrive = "2Tb";
		}
		if (rand.nextFloat() < 0.5f) {
			this.os = "Windows";
		} else {
			this.os = "Linux";
		}
	}
	
	public String printPC() {
		return this.type + " : (" + "memory: " + this.memory + ", harddrive: " + this.harddrive + ", os: " + this.os + ")";
	}
}
