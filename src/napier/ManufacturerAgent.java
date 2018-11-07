package napier;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import supply_chain_simulation_ontology.ECommerceOntology;

public class ManufacturerAgent extends Agent {
	
	private Codec codec = new SLCodec();
	private Ontology ontology = ECommerceOntology.getInstance();
	
	//This method is called when the agent is launched
	protected void setup() {
		
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		// Print out a welcome message
		System.out.println("Enrolled: " + getAID().getName() + ", standing by...");
		
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
	}
	
	//takeDown
	protected void takeDown() {
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
	
	//create a cyclic behaviour
	public class ReceiverBehaviour extends CyclicBehaviour {
		
		public ReceiverBehaviour(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			//try to receive a message
			ACLMessage msg = myAgent.receive();
			if (msg != null) {
				// process the message
				System.out.println(
						"Agent: " + myAgent.getLocalName() + "\n" +
						"\t" + "Message received from " + msg.getSender() + "\n" +
						"\t" + "msg: " + msg.getContent() + "\n"
						);
			}
			else {
				//put the behaviour to sleep until a message arrives
				block();
			}
		}
		
	}
	
}
