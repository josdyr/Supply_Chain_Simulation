package napier;

public class Desktop {
	
	Boolean screen = false;
	String cpu = "desktopCPU";
	String motherboard = "desktopMotherboard";
	String memory, harddrive, os;

	public Desktop(String memory, String harddrive, String os) {
		this.memory = memory;
		this.harddrive = harddrive;
		this.os = os;
	}
	
	public String printPC() {
		return "Desktop: (" + "memory: " + this.memory + ", harddrive: " + this.harddrive + ", os: " + this.os + ")";
	}
}
