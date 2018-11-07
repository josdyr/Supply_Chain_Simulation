package napier;

public class Laptop {
	
	Boolean screen = true;
	String cpu = "laptopCPU";
	String motherboard = "laptopMotherboard";
	String memory, harddrive, os;

	public Laptop(String memory, String harddrive, String os) {
		this.memory = memory;
		this.harddrive = harddrive;
		this.os = os;
	}
	
	public String printPC() {
		return "Laptop: (" + "memory: " + this.memory + ", harddrive: " + this.harddrive + ", os: " + this.os + ")";
	}
}
