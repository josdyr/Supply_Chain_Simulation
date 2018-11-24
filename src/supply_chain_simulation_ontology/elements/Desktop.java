package supply_chain_simulation_ontology.elements;

public class Desktop extends PC {
	
	private String cpu = "desktopCPU";
	private String motherboard = "desktopMotherboard";
	private Boolean screen = false;
	
	// Display the PC
	void display() {
		System.out.println(
				"Memory: " + super.memory + 
				", Harddrive: " + super.harddrive + 
				", OS: " + super.os + 
				", CPU: " + this.cpu + 
				", Motherboard: " + this.motherboard + 
				", Screen: " + this.screen
				);
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

	public Boolean getScreen() {
		return screen;
	}

	public void setScreen(Boolean screen) {
		this.screen = screen;
	}
	
	public String getMemory() {
		return super.memory;
	}
	
	public void setMemory(String memory) {
		super.memory = memory;
	}
	
	public String getHarddrive() {
		return super.harddrive;
	}
	
	public void setHarddrive(String harddrive) {
		super.harddrive = harddrive;
	}
	
	public String getOs() {
		return super.os;
	}
	
	public void setOs(String os) {
		super.os = os;
	}
	
}
