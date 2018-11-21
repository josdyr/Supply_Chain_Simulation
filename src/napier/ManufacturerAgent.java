package napier;

import java.util.ArrayList;
import java.util.HashMap;

import jade.content.Concept;
import jade.content.ContentElement;
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
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import supply_chain_simulation_ontology.ECommerceOntology;
import supply_chain_simulation_ontology.elements.CD;
import supply_chain_simulation_ontology.elements.Item;
import supply_chain_simulation_ontology.elements.Order;
import supply_chain_simulation_ontology.elements.Owns;
import supply_chain_simulation_ontology.elements.PC;
import supply_chain_simulation_ontology.elements.Sell;
import supply_chain_simulation_ontology.elements.Track;

public class ManufacturerAgent extends Agent {
	
	private HashMap<Integer,Item> itemsForSale = new HashMap<>(); 
	
	private int day = 0;
	private AID tickerAgent;
	
	private Codec codec = new SLCodec();
	private Ontology ontology = ECommerceOntology.getInstance();
	
	//This method is called when the agent is launched
	protected void setup() {
		
		// Populate the itemsForSale
		CD cd = new CD();
		cd.setName("Synchronicity");
		cd.setSerialNumber(123);
		ArrayList<Track> tracks = new ArrayList<Track>();
		Track t = new Track();
		t.setName("Every breath you take");
		t.setDuration(230);
		tracks.add(t);
		t = new Track();
		t.setName("King of pain");
		t.setDuration(500);
		tracks.add(t);
		cd.setTracks(tracks);
		itemsForSale.put(cd.getSerialNumber(), cd);
		
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		// Print out a welcome message
		System.out.println("Enrolled: " + getAID().getName() + ", standing by...");
		
		//add this agent to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		
		sd.setType("manufacturer-agent");
		sd.setName(getLocalName() + "-manufacturer-agent");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
//		addBehaviour(new ReceiverBehaviour(this));
		addBehaviour(new TickerWaiter(this));
		
	}
	
	//takeDown
	protected void takeDown() {
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
					dailyActivity.addSubBehaviour(new ReceiverBehaviour(myAgent));
					dailyActivity.addSubBehaviour(new EndDay(myAgent));
					
					//enroll the subBehaviours of the SequentialBehaviour: "dailyActivity-Behaviour". ("list")
					myAgent.addBehaviour(dailyActivity);
					
					//normal behaviours will execute normally
					// ...
					
					// Wait a bit only the first day in order to receive the first message
					doWait(3000);
					
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
	
	//create a cyclic behaviour
	public class ReceiverBehaviour extends OneShotBehaviour {
		
		public ReceiverBehaviour(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			//try to receive a message
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				
				// = Process the message =
				
				try {
					ContentElement ce = null;
					
					// Print out the message content in SL
					System.out.println(
							"    " + "Agent: " + myAgent.getLocalName() + "\n" +
							"\t" + "Message received from " + msg.getSender() + "\n" +
							"\t" + "Content: " + msg.getContent() + "\n");

					// Let JADE convert from String to Java objects
					// Output will be a ContentElement
					ce = getContentManager().extractContent(msg);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof Order) {
							Order order = (Order)action;
							PC _currentPC = order.getMyPC();
							// Extract the CD name and print it to demonstrate use of the ontology
							if(_currentPC instanceof PC){
								PC currentPC = (PC)_currentPC;
								//check if seller has it in stock
//								if(itemsForSale.containsKey(cd.getSerialNumber())) {
//									System.out.println("Selling CD " + cd.getName());
//								}
//								else {
//									System.out.println("You tried to order something out of stock!!!! Check first!");
//								}
							}
						}
					}
				}
				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				}
				
			}
			else {
				//put the behaviour to sleep until a message arrives
				System.out.println("    " + myAgent.getLocalName() + " waiting for message...");
				block();
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
