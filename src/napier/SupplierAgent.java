package napier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import napier.ManufacturerAgent.EndDay;
import napier.ManufacturerAgent.ReceiverBehaviour;
import supply_chain_simulation_ontology.ECommerceOntology;
import supply_chain_simulation_ontology.elements.actions.Buy;
import supply_chain_simulation_ontology.elements.actions.Supply;
import supply_chain_simulation_ontology.elements.concepts.Comp;
import supply_chain_simulation_ontology.elements.concepts.Delivery;
import supply_chain_simulation_ontology.elements.concepts.Order;
import supply_chain_simulation_ontology.elements.concepts.PC;

public class SupplierAgent extends Agent {
	
	HashMap<Integer, ArrayList<Delivery>> all_deliveries_queue = new HashMap<Integer, ArrayList<Delivery>>();

	private int day = 0;
	private AID tickerAgent;
	private String _sup_num;
	
	ArrayList<Integer> deliver_in_days = new ArrayList<>();
	
	private Codec codec = new SLCodec();
	private Ontology ontology = ECommerceOntology.getInstance();
	
	//This method is called when the agent is launched
	protected void setup() {
		
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		// Print out a welcome message
		System.out.println("Enrolled: " + getAID().getName() + ", standing by...");
		
		deliver_in_days.add(1);
		deliver_in_days.add(3);
		deliver_in_days.add(7);
		
		//add this agent to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		
		sd.setType("supplier-agent");
		sd.setName(getLocalName() + "-supplier-agent");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
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
					doWait(2000); // Hardcode a timer to wait for the first message
					dailyActivity.addSubBehaviour(new ReceiverBehaviour(myAgent));
					dailyActivity.addSubBehaviour(new SenderBehaviour(myAgent));
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
					System.out.println("\n" + "-> " + myAgent.getLocalName() + ": ");
					System.out.println("   * " + "Message received from " + msg.getSender());
					System.out.println("   * " + "Content: " + msg.getContent());

					// Let JADE convert from String to Java objects
					// Output will be a ContentElement
					ce = getContentManager().extractContent(msg);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof Buy) {
							Buy buy = (Buy)action;
							Order order = buy.getOrder();
							PC currentPC = order.getMyPC();
							if (currentPC instanceof PC) {
								
								// Append Delivery to all_deliveries list
								_sup_num = myAgent.getLocalName().substring(myAgent.getLocalName().length()-1);
								Integer sup_num = Integer.parseInt(_sup_num);
								Integer delivery_day = day + deliver_in_days.get(sup_num-1);
								ArrayList<Delivery> daily_list = new ArrayList<Delivery>();
								
								if (all_deliveries_queue.containsKey(delivery_day)) {
									// Amend Daily List
									daily_list = all_deliveries_queue.get(delivery_day);
									daily_list.add(new Delivery(order.getMyPC()));
									all_deliveries_queue.put(delivery_day, daily_list);
								} else {
									// Make a new Daily List
									daily_list.add(new Delivery(order.getMyPC()));
									all_deliveries_queue.put(delivery_day, daily_list);
								}
								
								System.out.println("   * " + "Delivery added to queue.");
								System.out.println("   * " + "Delivery to " + msg.getSender().getLocalName() + " is on day: " + delivery_day);
								System.out.println("   * " + "Delivery to myCustomerAgent is on day: " + (day+order.getDue_in_days()));
								
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
				System.out.println("\n" + "-> " + myAgent.getLocalName() + ": ");
				System.out.println("   * " + "Waiting for message...");
				block(1000);
			}
		}
		
	}
	
	//create a cyclic behaviour
		public class SenderBehaviour extends OneShotBehaviour {
			
			public SenderBehaviour(Agent agent) {
				super(agent);
			}
			
			@Override
			public void action() {
				
				if (all_deliveries_queue.containsKey(day)) {
					System.out.println("\n" + "-> " + myAgent.getLocalName() + ": ");
					System.out.println("   * " + "Supplying delivery to myManufacturerAgent...");
					for (Delivery current_delivery : all_deliveries_queue.get(day)) {
						System.out.println("   * " + current_delivery.toString());
						
						// Deliver a msg(Supply(Delivery(PC())))
						
						
					}
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
		}
		
	}
}
