package napier;

import java.lang.reflect.Method;
import java.time.Period;
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
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import supply_chain_simulation_ontology.ECommerceOntology;
import supply_chain_simulation_ontology.elements.actions.Buy;
import supply_chain_simulation_ontology.elements.actions.Supply;
import supply_chain_simulation_ontology.elements.concepts.Comp;
import supply_chain_simulation_ontology.elements.concepts.Delivery;
import supply_chain_simulation_ontology.elements.concepts.Order;
import supply_chain_simulation_ontology.elements.concepts.PC;

public class ManufacturerAgent extends Agent {
	
	static Integer dailyPenaltyForLateOrders = 50;
	static Integer dailyWarehouseStorage = 5;
	
	private ArrayList<AID> supplierAgents = new ArrayList<>();
	ArrayList<HashMap<String, Integer>> suppliers_info = new ArrayList<HashMap<String, Integer>>();
	ArrayList<Integer> deliver_in_days = new ArrayList<>();
	HashMap<Integer, Integer> expected_messages = new HashMap<>();
	Integer total_profit = 0;
	
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
		
		ArrayList<String> comps = new ArrayList<>();
		comps.add("Laptop_CPU");
		comps.add("Desktop_CPU");
		comps.add("Laptop_Motherboard");
		comps.add("Desktop_Motherboard");
		comps.add("RAM_8Gb");
		comps.add("RAM_16Gb");
		comps.add("HDD_1Tb");
		comps.add("HDD_2Tb");
		comps.add("Screen");
		comps.add("OS_Windows");
		comps.add("OS_Linux");
		
		HashMap<String, Integer> s1 = new HashMap<String, Integer>();
		HashMap<String, Integer> s2 = new HashMap<String, Integer>();
		HashMap<String, Integer> s3 = new HashMap<String, Integer>();
		
		// Process CSV file
		// Populate the suppliers_info
		Object[] sup_info = getArguments();
		
		Object[] _s1 = (Object[])sup_info[0];
		Object[] _s2 = (Object[])sup_info[1];
		Object[] _s3 = (Object[])sup_info[2];
		
		for (int i = 0; i < 11; i++) {
			s1.put(comps.get(i), Integer.parseInt((String) _s1[i]));
			s2.put(comps.get(i), Integer.parseInt((String) _s2[i]));
			s3.put(comps.get(i), Integer.parseInt((String) _s3[i]));
		}
		
		suppliers_info.add(s1);
		suppliers_info.add(s2);
		suppliers_info.add(s3);
		
		deliver_in_days.add(1);
		deliver_in_days.add(3);
		deliver_in_days.add(7);
		
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
		
		addBehaviour(new TickerWaiter(this));
		addBehaviour(new ReceiveSupply(this));
		
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
					dailyActivity.addSubBehaviour(new FindSuppliers(myAgent));
					dailyActivity.addSubBehaviour(new ReceiveAndForwardCustumerOrders(myAgent));
					dailyActivity.addSubBehaviour(new EndDay(myAgent));
					
					//enroll the subBehaviours of the SequentialBehaviour: "dailyActivity-Behaviour". ("list")
					myAgent.addBehaviour(dailyActivity);
					
					//normal behaviours will execute normally
//					addBehaviour(new ReceiveSupply(myAgent));
					
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
	
	//create the SearchYellowPages behaviour
	public class FindSuppliers extends OneShotBehaviour {
		
		public FindSuppliers(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			//create a template
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			
			sd.setType("supplier-agent");;
			dfd.addServices(sd);
			
			//query the dfAgent
			try {
				supplierAgents.clear();
				DFAgentDescription[] result = DFService.search(myAgent, dfd);
				
				for (int i = result.length-1; i >= 0; i--) {
					supplierAgents.add(result[i].getName());
				}
			} catch (FIPAException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	//create a cyclic behaviour
	public class ReceiveAndForwardCustumerOrders extends OneShotBehaviour {
		
		public ReceiveAndForwardCustumerOrders(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			for (int i = 0; i < 3; i++) {
				//try to receive a message
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					
					// = Process the message =
					
					try {
						ContentElement ce = null;

						ce = getContentManager().extractContent(msg);
						if(ce instanceof Action) {
							Concept _action = ((Action)ce).getAction();
							if (_action instanceof Buy) {
								Buy buy = (Buy)_action;
								Order order = buy.getOrder();
								PC currentPC = order.getMyPC();
								if(currentPC instanceof PC) {
									
									// == Calculate if manufacturer will gain profit on current order or not ==
									
									// Set current supplier to potentially order from [s1, s2, s3]
									Integer sup_num = calcCurrentSupplier(order);
									AID current_supplier_AID = supplierAgents.get(sup_num - 1);
									
									// calculate the minimum cost from current set supplier
									Integer current_min_cost = calcMinCostFromSupOrder(sup_num, order);
									Integer days_in_warehouse = order.getDue_in_days() - deliver_in_days.get(sup_num-1);
									Integer profit_on_single_order = order.getPrice() - current_min_cost;
									
									// If Manufacturer already have components in stock then:
									if (profit_on_single_order > 0) { // If profit is positive
										if (false) { // Components are in stock
											System.out.println("TODO: COMPONENTS ALREADY IN STOCK...");
										} else { // Components not in stock
											// forward order to supplier
											// Prepare receiving template
											Integer current_profit = 0;
											current_profit = (profit_on_single_order * order.getQuantity());
											total_profit += current_profit;
											
											ACLMessage sup_msg = new ACLMessage(ACLMessage.REQUEST);
											sup_msg.addReceiver(current_supplier_AID);
											sup_msg.setLanguage(codec.getName());
											sup_msg.setOntology(ontology.getName());
											
											// Action Wrapper
											Action request = new Action();
											buy = new Buy();
											buy.setOrder(order);
											request.setAction(buy);
											request.setActor(current_supplier_AID);
											
											try {
												getContentManager().fillContent(sup_msg, request);
												send(sup_msg);
												
												System.out.println(
														"-> " + myAgent.getLocalName() + ": " + "\n" +
														"   * " + "Message received from " + msg.getSender().getLocalName() + "\n" +
														"   * " + "Content: " + msg.getContent() + "\n" +
														"   * " + "Current supplier: " + sup_num + "\n" +
														"   * " + "Cost: " + current_min_cost + "\n" +
														"   * " + "Days in warehouse: " + days_in_warehouse + "\n" +
														"   * " + "Profit on single order: " + profit_on_single_order + "\n" +
														"   * " + "Profit on current order: " + "sng_ord_" + profit_on_single_order + " * " + "qty_" + order.getQuantity() + " = " + current_profit + "\n" +
														"   * " + "Profit: " + total_profit + "\n" +
														"   * " + "Forwarding order to supplier: " + sup_num + "\n"
														);
											}
											catch (CodecException _ce) {
												_ce.printStackTrace();
											}
											catch (OntologyException oe) {
												oe.printStackTrace();
											}
										}
									} else {
										System.out.println("   * " + "Not an acceptable order. Price offered is too low.");
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
					System.out.println(
							"\n" + "-> " + myAgent.getLocalName() + "\n" +
									"   * " + "Waiting for a message..."
					);
					block();
				}
			}
		}

		public Integer calcCurrentSupplier(Order order) {
			if (order.getDue_in_days() >= 7) { // s3
				return 3;
			} else if (order.getDue_in_days() >= 3) { // s2
				return 2;
			} else { // s1
				return 1;
			}
		}

		private Integer calcMinCostFromSupOrder(Integer current_supplier, Order order) {
			Integer total_cost = 0;
			for (Comp comp_order : order.getMyPC().getPc_components()) {
				total_cost += suppliers_info.get(current_supplier-1).get(comp_order.toString());
			}
			return total_cost;
		}
		
	}
	
	public class ReceiveSupply extends CyclicBehaviour {
		
		public ReceiveSupply(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			//try to receive a message
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			
			if (msg != null) {
				
				// = Process the message =
				
				System.out.println(myAgent.getLocalName() + " reveived supply from: " + msg.getSender());
				
				try {
					doWait(500);
					ContentElement ce = null;

					// Let JADE convert from String to Java objects
					// Output will be a ContentElement
					ce = getContentManager().extractContent(msg);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof Supply) {
							Supply supply = (Supply)action;
							Delivery delivery = supply.getDelivery();
							PC currentPC = delivery.getMyPC();
							if (currentPC instanceof PC) {
								
								// == Process message ==
								// formality: receive physical supply INFORM msg (processing already done in ReceiveAndForwardCustumerOrders behaviour...)
								// TODO: forward PC back to original customer
								// TODO: receive physical payment from original customer
								
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
			myAgent.send(msg);
		}
		
	}
	
}
