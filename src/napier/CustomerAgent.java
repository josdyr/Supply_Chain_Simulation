package napier;

import java.util.ArrayList;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class CustomerAgent extends Agent {
	
	static int TIME_OF_ONE_DAY = 24 * 60 * 60 * 1000;
	static int SPEED_UP_SIMULATION = 100000; //equates to one second
	
	private ArrayList<AID> receiverAgents = new ArrayList<>();
	
	String memory = "";
	String hardDrive = "";
	String os = "";
	
	Order myOrder;
	
	//This method is called when the agent is launched
	protected void setup() {
		// Print out a welcome message
		System.out.println("Enrolled: " + getAID().getName() + ", standing by...");
		
		//generate 90 random orders
		for (int i = 0; i < 90; i++) {
			
			//create random PCs
			PC myPC = new PC();
			
			//create random order & print it
			myOrder = new Order(myPC);
			System.out.println("myOrder: " + myOrder.printOrder());
			
		}
		
		//add this agent to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		
		sd.setType("customer-agent");
		sd.setName(getLocalName() + "-customer-agent");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		addBehaviour(new SearchYellowPages(this, (1000)));
		addBehaviour(new SenderBehaviour(this, (TIME_OF_ONE_DAY / SPEED_UP_SIMULATION)));
		
	}
	
	protected void takeDown() {
		// try to deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
	
	//create the SearchYellowPages behaviour
	public class SearchYellowPages extends TickerBehaviour {
		
		public SearchYellowPages(Agent agent, long period) {
			super(agent, period);
		}
		
		@Override
		protected void onTick() {
			//create a template
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			
			sd.setType("manufacturer-agent");;
			dfd.addServices(sd);
			
			//query the dfAgent
			try {
				DFAgentDescription[] result = DFService.search(myAgent, dfd);
				receiverAgents.clear();
				for (int i = 0; i < result.length; i++) {
					receiverAgents.add(result[i].getName()); //.getName() = AID
				}
			} catch (FIPAException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	//create the SenderBehaviour behaviour
	public class SenderBehaviour extends TickerBehaviour {
		
		public SenderBehaviour(Agent agent, long period) {
			super(agent, period);
		}
		
			@Override
			protected void onTick() {
				//send a message to all receiver agents
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setContent("Hello from agent: " + myAgent.getLocalName());
				
				//add receivers
				for (AID receiver : receiverAgents) {
					msg.addReceiver(receiver);
				}
				myAgent.send(msg);
				//System.out.println("sending msg: " + msg);
			}
		
	}
	
}