package supply_chain_simulation_ontology.elements;

import java.util.HashMap;
import java.util.Random;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;

public class PC implements Concept {
	
	Random rand = new Random();
	
	// Default is Desktop
	String type = "Desktop";
	Boolean screen = false;
	String cpu = "desktopCPU";
	String motherboard = "desktopMotherboard";
	
	// Default spesifications
	String memory = "8GB";
	String harddrive = "1Tb";
	String os = "Windows";

	public PC() {
		
		// Switch to Laptop
		if (rand.nextFloat() > 0.5f) {
			this.setType("Laptop");
			this.setScreen(true);
			this.setCpu("laptopCPU");
			this.setMotherboard("laptopMotherboard");
		}
		
		//generate random components
		if (rand.nextFloat() > 0.5f) {
			this.setMemory("16GB");
		}
		if (rand.nextFloat() > 0.5f) {
			this.setHarddrive("2Tb");
		}
		if (rand.nextFloat() > 0.5f) {
			this.setOs("Linux");
		}
		
	}
	
	// Print PC
	public String printPC() {
		return this.type + " : (" + "memory: " + this.memory + ", harddrive: " + this.harddrive + ", os: " + this.os + ")";
	}
	
	// Getters and Setters
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getScreen() {
		return screen;
	}

	public void setScreen(Boolean screen) {
		this.screen = screen;
	}

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getMotherboard() {
		return motherboard;
	}

	public void setMotherboard(String motherboard) {
		this.motherboard = motherboard;
	}

	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getHarddrive() {
		return harddrive;
	}

	public void setHarddrive(String harddrive) {
		this.harddrive = harddrive;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}
	
}