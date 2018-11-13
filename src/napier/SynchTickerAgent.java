package napier;

import java.util.ArrayList; import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription; import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage; import jade.lang.acl.MessageTemplate;

public class SynchTickerAgent extends Agent {
	
	@Override
	protected void setup() {
		//add this agent to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("ticker-agent");
		sd.setName(getLocalName() + "-ticker-agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException e) {
			e.printStackTrace();
		}
		
		// Print out a welcome message
		System.out.println("Enrolled: " + getAID().getName() + ", standing by...");
		
		// Make sure all agents have started
		System.out.println("Waiting to make sure all agents get enrolled...");
		doWait(5000);
		System.out.println("=== Starting simulation ===");
		addBehaviour(new SynchAgentsBehaviour(this));
		
	}
	
	@Override
	protected void takeDown() {
		// Deregister from yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
	
	public class SynchAgentsBehaviour extends Behaviour {
		
		private int step = 0; //where we are in the behaviour
		private int numFinReceived = 0; //number of finished messages from other agents
		private ArrayList<AID> simulationAgents = new ArrayList<>();
		
		public SynchAgentsBehaviour(Agent a) {
			super(a);
		}
		
		@Override
		public void action () {
			switch(step) {
			case 0:
				//find all agents using directory service
				//here we have two types of agent
				//"simulation-agent" and "simulation-agent2"
				DFAgentDescription template1 = new DFAgentDescription();
				ServiceDescription sd1 = new ServiceDescription();
				sd1.setType("customer-agent");
				template1.addServices(sd1);
				DFAgentDescription template2 = new DFAgentDescription();
				ServiceDescription sd2 = new ServiceDescription();
				sd2.setType("manufacturer-agent");
				template2.addServices(sd2);
				DFAgentDescription template3 = new DFAgentDescription();
				ServiceDescription sd3 = new ServiceDescription();
				sd3.setType("supplier-agent");
				template3.addServices(sd3);
				try {
					simulationAgents.clear();
					
					//search for agents of type "simulation-agent"
					DFAgentDescription[] agentsType1 = DFService.search(myAgent, template1);
					for (int i = 0; i < agentsType1.length; i++) {
						simulationAgents.add(agentsType1[i].getName());
						System.out.println(agentsType1[i].getName());
					}
					//search for agents of type "manufacturer-agent"
					DFAgentDescription[] agentsType2 = DFService.search(myAgent, template2);
					for (int i = 0; i < agentsType2.length; i++) {
						simulationAgents.add(agentsType2[i].getName());
						System.out.println(agentsType2[i].getName());
					}
					//search for agents of type "supplyer-agent"
					DFAgentDescription[] agentsType3 = DFService.search(myAgent, template3);
					for (int i = 0; i < agentsType3.length; i++) {
						simulationAgents.add(agentsType3[i].getName());
						System.out.println(agentsType3[i].getName());
					}
				}
				catch (FIPAException e) {
					e.printStackTrace();
				}
				
				// Send a new day message to each agent
				ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
				tick.setContent("new day");
				for (AID id : simulationAgents) {
					tick.addReceiver(id);
				}
				myAgent.send(tick);
				step++;
				break;
				
			case 1:
				// Wait to receive a "done" message from all agents
				MessageTemplate mt = MessageTemplate.MatchContent("done");
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					numFinReceived++;
					if (numFinReceived >= simulationAgents.size()) {
						step++;
					}
				} else {
					block();
				}
				
			}
		}
		
		@Override
		public boolean done() {
			return step == 2;
		}
		
		@Override
		public void reset () {
			step = 0; numFinReceived = 0;
		}
		
		@Override
		public int onEnd () {
			System.out.println("= End of day =");
			reset();
			myAgent.addBehaviour(this);
			return 0;
		}
		
	}
	
}
