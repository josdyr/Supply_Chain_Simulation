package napier;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
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

public class SupplierAgent extends Agent {
	
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
