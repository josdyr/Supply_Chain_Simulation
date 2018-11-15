package napier;

import java.util.ArrayList;
import java.util.Random;

import com.sun.org.apache.xpath.internal.axes.WalkerFactory;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import supply_chain_simulation_ontology.ECommerceOntology;

public class CustomerAgent extends Agent {
	
	private int day = 0;
	private AID tickerAgent;
	
	static int TIME_OF_ONE_DAY = 24 * 60 * 60 * 1000;
	static int SPEED_UP_SIMULATION = 100000; //equates to one second
	
	private ArrayList<AID> receiverAgents = new ArrayList<>();
	
	String memory = "";
	String hardDrive = "";
	String os = "";
	
	Order myOrder;
	
	private Codec codec = new SLCodec();
	private Ontology ontology = ECommerceOntology.getInstance();
	
	//This method is called when the agent is launched
	protected void setup() {
		
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
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
		
		addBehaviour(new TickerWaiter(this));
		
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
		
	}
	
	protected void takeDown() {
		// try to deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
	
	public class TickerWaiter extends CyclicBehaviour {
		
		//behaviour to wait for a new day
		public TickerWaiter(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			// Wait for new daily message
			MessageTemplate mt = MessageTemplate.MatchContent("new day");
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (tickerAgent == null) {
					tickerAgent = msg.getSender();
				}
				if(msg.getContent().equals("new day")) {
					
					day++;
					System.out.println("    " + getLocalName() + " day: " + day);
					
					//spawn new sequential behaviour for day's activities
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					
					//sub-behaviours will execute in the order they are added
					// ...
					
					//normal behaviours will execute normally
					myAgent.addBehaviour(new SearchYellowPages(myAgent, (1000)));
					myAgent.addBehaviour(new SenderBehaviour(myAgent, (2000)));
					myAgent.addBehaviour(new EndDay(myAgent));
					
					//enroll the subBehaviours of the SequentialBehaviour: "dailyActivity-Behaviour". ("list")
					myAgent.addBehaviour(dailyActivity);
				}
				else {
					//termination message to end simulation
					myAgent.doDelete();
				}
			} else {
				block();
			}
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
				System.out.println("    " + "Searching the Yellow Pages...");
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
			
			//use this instead of setContent()
			//getContentManager.fillContent();
			
			//add receivers
			for (AID receiver : receiverAgents) {
				msg.addReceiver(receiver);
			}
			myAgent.send(msg);
			System.out.println("    " + "sending msg: " + msg);
		}
		
	}
	
	public class EndDay extends OneShotBehaviour {
		
		public EndDay(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(tickerAgent);
			msg.setContent("done");
			myAgent.send(msg);
			
//			//send a message to each seller that we have finished
//			ACLMessage buyerDone = new ACLMessage(ACLMessage.INFORM);
//			buyerDone.setContent("done");
//			for(AID seller : sellers) {
//				buyerDone.addReceiver(seller);
//			}
//			myAgent.send(buyerDone);
		}
		
	}
	
}