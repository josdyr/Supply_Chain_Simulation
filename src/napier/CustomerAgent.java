package napier;

import java.util.ArrayList;
import java.util.Random;

import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import supply_chain_simulation_ontology.ECommerceOntology;
import supply_chain_simulation_ontology.elements.Desktop;
import supply_chain_simulation_ontology.elements.Laptop;
import supply_chain_simulation_ontology.elements.Order;
import supply_chain_simulation_ontology.elements.PC;

public class CustomerAgent extends Agent {
	
	private AID myManufacturerAgentAID;
	
	PC myPC;
	Order myOrder;
	
	private int day = 0;
	private AID tickerAgent;
	
	static int TIME_OF_ONE_DAY = 24 * 60 * 60 * 1000;
	static int SPEED_UP_SIMULATION = 100000; //equates to one second
	
	private ArrayList<AID> manufacturerAgents = new ArrayList<>();
	
	String memory = "";
	String hardDrive = "";
	String os = "";
	
	private Codec codec = new SLCodec();
	private Ontology ontology = ECommerceOntology.getInstance();
	
	//This method is called when the agent is launched
	protected void setup() {
		
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		// Print out a welcome message
		System.out.println("Enrolled: " + getAID().getName() + ", standing by...");
		
		myManufacturerAgentAID = new AID("myManufacturerAgent", AID.ISLOCALNAME);
		
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
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("new day"), MessageTemplate.MatchContent("terminate"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (tickerAgent == null) {
					tickerAgent = msg.getSender();
				}
				if(msg.getContent().equals("new day")) {
					
					day++;
					
					//spawn new sequential behaviour for day's activities
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					
					//sub-behaviours will execute in the order they are added
					dailyActivity.addSubBehaviour(new FindManufacturer(myAgent));
					dailyActivity.addSubBehaviour(new GenerateOrder(myAgent));
					dailyActivity.addSubBehaviour(new SendOrder(myAgent));
					dailyActivity.addSubBehaviour(new EndDay(myAgent));
					
					//enroll the subBehaviours of the SequentialBehaviour: "dailyActivity-Behaviour". ("list")
					myAgent.addBehaviour(dailyActivity);
					
					//normal behaviours will execute normally
					// ...
					
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
	public class FindManufacturer extends OneShotBehaviour {
		
		public FindManufacturer(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			//create a template
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			
			sd.setType("manufacturer-agent");;
			dfd.addServices(sd);
			
			//query the dfAgent
			try {
				manufacturerAgents.clear();
				DFAgentDescription[] result = DFService.search(myAgent, dfd);
				
				for (int i = 0; i < result.length; i++) {
					manufacturerAgents.add(result[i].getName()); //.getName() = AID
				}
			} catch (FIPAException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public class GenerateOrder extends OneShotBehaviour {
		
		public GenerateOrder(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			//create random PCs
			myPC = new PC();
			
			//create random order & print it
			myOrder = new Order();
			myOrder.setMyPC(myPC);
			
			System.out.println(
					"    " + "Agent: myCustomerAgent:" +
					"\n" +"    " + "  " + "myOrder: " + myOrder.printOrder()
					);
			
		}
		
	}
	
	//create the SenderBehaviour behaviour
	public class SendOrder extends OneShotBehaviour {
		
		public SendOrder(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			// Prepare receiving template
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(myManufacturerAgentAID);
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName());
			
			// Action Wrapper
			Action request = new Action();
			request.setAction(myOrder);
			request.setActor(myManufacturerAgentAID);
			
			try {
				getContentManager().fillContent(msg, request);
				send(msg);
			}
			catch (CodecException ce) {
				ce.printStackTrace();
			}
			catch (OntologyException oe) {
				oe.printStackTrace();
			}
			
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
			doWait(3000);
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