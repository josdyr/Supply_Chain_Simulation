package napier;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.HashMap;
//import jade.util.leap.HashMap;

import jade.content.AgentAction;
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
import supply_chain_simulation_ontology.elements.actions.Buy;
import supply_chain_simulation_ontology.elements.concepts.Comp;
import supply_chain_simulation_ontology.elements.concepts.Order;
import supply_chain_simulation_ontology.elements.concepts.PC;
import supply_chain_simulation_ontology.elements.concepts.SupOrder;

public class ManufacturerAgent extends Agent {
	
	SupOrder mySupOrder;
	Buy buy;
	
	private int totalValueOfOrdersShipped;
	private int penaltyForLateOrders;
	private int warehouseStorage;
	private int suppliesPurchased;
	
	ArrayList<Comp> components_in_demand = new ArrayList<>();
	
	private AID mySupplierAgentAID;
	
	private int day = 0;
	private AID tickerAgent;
	
	private Codec codec = new SLCodec();
	private Ontology ontology = ECommerceOntology.getInstance();
	
	//This method is called when the agent is launched
	protected void setup() {
		
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		// Print out a welcome message
		System.out.println("Enrolled: " + getAID().getName() + ", standing by...");
		
		mySupplierAgentAID = new AID("mySupplierAgent", AID.ISLOCALNAME);
		
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
		
		addBehaviour(new ReceiverBehaviour(this));
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
					
					//spawn new sequential behaviour for day's activities
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					
					//sub-behaviours will execute in the order they are added
					dailyActivity.addSubBehaviour(new ReceiverBehaviour(myAgent));
					dailyActivity.addSubBehaviour(new SendOrder(myAgent));
					dailyActivity.addSubBehaviour(new EndDay(myAgent));
					
					//enroll the subBehaviours of the SequentialBehaviour: "dailyActivity-Behaviour". ("list")
					myAgent.addBehaviour(dailyActivity);
					
					//normal behaviours will execute normally
					// ...
					
					// Wait a bit only the first day in order to receive the first message
					doWait(1000);
					
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
						Concept _action = ((Action)ce).getAction();
						if (_action instanceof Buy) {
							Buy _buy = (Buy)_action;
							System.out.println();
							if (_buy instanceof Buy) {
								Order _order = (Order)_buy;
								
								
								
								
								
								PC _currentPC = order.getMyPC();
								// Extract the CD name and print it to demonstrate use of the ontology
								if(_currentPC instanceof PC) {
									PC currentPC = (PC)_currentPC;
									
									System.out.println("MAN: " + currentPC.toString());
									System.out.println("MAN: " + order.toString());
									
//									buy = new Buy(new Order(currentPC));
//									
//									buy.toString();
									
								}
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
	
	//create the SenderBehaviour behaviour
		public class SendOrder extends OneShotBehaviour {
			
			public SendOrder(Agent agent) {
				super(agent);
			}
			
			@Override
			public void action() {
				
				// Prepare receiving template
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(mySupplierAgentAID);
				msg.setLanguage(codec.getName());
				msg.setOntology(ontology.getName());
				
				// Action Wrapper
				Action request = new Action();
				request.setAction(mySupOrder);
				request.setActor(mySupplierAgentAID);
				
				try {
					getContentManager().fillContent(msg, request);
					send(msg);
					
					System.out.println(
							"    " + "Agent: " + myAgent.getLocalName() + "\n"
							+ "    " + "    Message sent to " + mySupplierAgentAID.getLocalName() + "\n");
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
	
//	private void addComp(List comps_in_demand, String Comp) {
//		
//		Integer value;
//		
//		// If Comp already in list, then just increment the value by 1 - Otherwise, add the comp and amount=1
//		if (comps_in_demand.containsKey(Comp)) {
//			value = (Integer) comps_in_demand.get(Comp);
//			comps_in_demand.put(Comp, value + 1);
//		} else { // Else add the Comp and the value 1
//			comps_in_demand.put(Comp, 1);
//		}
//		
//	}
	
}
