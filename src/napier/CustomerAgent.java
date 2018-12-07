package napier;

import java.util.ArrayList;
import java.util.Random;

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
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import supply_chain_simulation_ontology.ECommerceOntology;
import supply_chain_simulation_ontology.elements.actions.Buy;
import supply_chain_simulation_ontology.elements.actions.ConfirmOrder;
import supply_chain_simulation_ontology.elements.actions.DeliverOrder;
import supply_chain_simulation_ontology.elements.actions.RefuseOrder;
import supply_chain_simulation_ontology.elements.actions.Supply;
import supply_chain_simulation_ontology.elements.concepts.Delivery;
import supply_chain_simulation_ontology.elements.concepts.Desktop;
import supply_chain_simulation_ontology.elements.concepts.Laptop;
import supply_chain_simulation_ontology.elements.concepts.Order;
import supply_chain_simulation_ontology.elements.concepts.PC;
import supply_chain_simulation_ontology.elements.concepts.comps.HDD_1Tb;
import supply_chain_simulation_ontology.elements.concepts.comps.HDD_2Tb;
import supply_chain_simulation_ontology.elements.concepts.comps.OS_Linux;
import supply_chain_simulation_ontology.elements.concepts.comps.OS_Windows;
import supply_chain_simulation_ontology.elements.concepts.comps.RAM_16Gb;
import supply_chain_simulation_ontology.elements.concepts.comps.RAM_8Gb;

public class CustomerAgent extends Agent {
	
	private AID myManufacturerAgentAID;
	private PC myPC;
	private Order myOrder;
	private Buy buy;
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
		
		addBehaviour(new ReceiveReceipt(this));
//		addBehaviour(new ReceiveOrder(this));
		addBehaviour(new StartBehaviours(this));
		
	}
	
	protected void takeDown() {
		// try to deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
	
	public class StartBehaviours extends CyclicBehaviour {
		
		//behaviour to wait for a new day
		public StartBehaviours(Agent a) {
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
	
	public class ReceiveReceipt extends CyclicBehaviour {

		public ReceiveReceipt(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			//try to receive a message
			MessageTemplate mt =
					MessageTemplate.or(
							MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
							MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
							);
			ACLMessage msg = myAgent.receive(mt);
			
			if (msg != null) {
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(msg);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof RefuseOrder) {
							RefuseOrder refuse_order = (RefuseOrder)action;
							System.out.println("-> " + myAgent.getLocalName() + ": I will try another order again tomorrow...");
						} else {
							ConfirmOrder confirm_order = (ConfirmOrder)action;
							System.out.println("-> " + myAgent.getLocalName() + ": Thank you for confirming...");
						}
					}
				}
				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				}
				
			} else {
				//put the behaviour to sleep until a message arrives
				System.out.println(
						"\n" + "-> " + myAgent.getLocalName() + ": " + "\n" +
						"   * " + "Waiting for message..."
							);
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
		
		Random rand = new Random();
		
		public GenerateOrder(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			// Generate either Laptop or Desktop
			if (rand.nextFloat() > 0.5f) {
				myPC = new Laptop();
			} else {
				myPC = new Desktop();
			}
			
			// Set Custom Components
			if (rand.nextFloat() > 0.5f) {
				myPC.appendComp(new RAM_8Gb());
			} else {
				myPC.appendComp(new RAM_16Gb());
			}
			if (rand.nextFloat() > 0.5f) {
				myPC.appendComp(new HDD_1Tb());
			} else {
				myPC.appendComp(new HDD_2Tb());
			}
			if (rand.nextFloat() > 0.5f) {
				myPC.appendComp(new OS_Windows());
			} else {
				myPC.appendComp(new OS_Linux());
			}
			
			//create random order & print it
			myOrder = new Order();
			myOrder.setMyPC(myPC);
			myOrder.setBuyer(myAgent.getAID());
			myOrder.setQuantity((int)Math.floor(1 + 50 * rand.nextFloat()));
			myOrder.setPrice((int)Math.floor(600 + 200 * rand.nextFloat()));
			myOrder.setTotal_price(myOrder.getQuantity() * (int)Math.floor(600 + 200 * rand.nextFloat()));
			myOrder.setDue_in_days((int)Math.floor(1 + 10 * rand.nextFloat()));
			
			System.out.println(
					"\n" + "-> " + myAgent.getLocalName() + ": " + "\n" +
					"   * " + "Generating order..." + "\n" +
					"   * " + myOrder.toString()
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
			buy = new Buy();
			buy.setOrder(myOrder);
			request.setAction(buy);
			request.setActor(myManufacturerAgentAID);
			
			try {
				getContentManager().fillContent(msg, request);
				send(msg);
				System.out.println(
						"\n" + "-> " + myAgent.getLocalName() + ": " + "\n" +
						"   * " + "Order sent." + "\n"
								);
			}
			catch (CodecException ce) {
				ce.printStackTrace();
			}
			catch (OntologyException oe) {
				oe.printStackTrace();
			}
			
		}
		
	}
	
	public class ReceiveOrder extends CyclicBehaviour {
		
		public ReceiveOrder(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			//try to receive a message
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			
			if (msg != null) {
				
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(msg);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof DeliverOrder) {
							DeliverOrder order = (DeliverOrder)action;
							PC pc = order.getMyPC();
							if (pc instanceof PC) {
								// Print out received PC
								System.out.println("-> " + myAgent.getLocalName() + ": " + pc.toString());
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
				
			} else {
				//put the behaviour to sleep until a message arrives
				System.out.println(
						"\n" + "-> " + myAgent.getLocalName() + ": " + "\n" +
						"   * " + "Waiting for message..."
							);
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
			doWait(400);
			myAgent.send(msg);
		}
		
	}
	
}