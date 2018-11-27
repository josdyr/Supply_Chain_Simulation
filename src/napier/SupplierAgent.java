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
import supply_chain_simulation_ontology.elements.Comp;
import supply_chain_simulation_ontology.elements.Order;
import supply_chain_simulation_ontology.elements.PC;
import supply_chain_simulation_ontology.elements.SupOrder;

public class SupplierAgent extends Agent {
	
	ArrayList<Comp> components_in_demand = new ArrayList<>();
	
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
					System.out.println(
							"    " + "Agent: " + myAgent.getLocalName() + "\n" +
							"\t" + "Message received from " + msg.getSender() + "\n" +
							"\t" + "Content: " + msg.getContent() + "\n");

					// Let JADE convert from String to Java objects
					// Output will be a ContentElement
					ce = getContentManager().extractContent(msg);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof SupOrder) {
							SupOrder sup_order = (SupOrder)action;
							components_in_demand = sup_order.getComponents_in_demand();
							
							System.out.println(sup_order.getComponents_in_demand().size());
							System.out.println(sup_order.getComponents_in_demand().get(0));
							System.out.println(components_in_demand.size());
							
							// Extract the content and demonstrate the use of the ontology
							System.out.println(
									"    " + "Agent: " + myAgent.getLocalName() + "\n"
									+ "    " + "    Extracting Content...");
							
							// Print Components in Demand
							for (int i = 0; i < components_in_demand.size(); i++) {
								System.out.println("comp_" + i + " being ordered: " + components_in_demand.get(i).toString());
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
		}
		
	}
}
