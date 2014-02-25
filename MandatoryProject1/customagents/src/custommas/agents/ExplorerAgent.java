package custommas.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.Message;

import eis.iilang.Action;
import eis.iilang.Percept;
import massim.javaagents.Agent;
import massim.javaagents.agents.MarsUtil;

public class ExplorerAgent extends Agent{
	
	private String _name;
	private String _team;
	
	public ExplorerAgent(String name, String team) {
		super(name, team);
		_name = name;
		_team = team;
	}

	@Override
	public void handlePercept(Percept percept) {
		println("[Percept " + _name + "/" + _team + "]: " + percept);
	}

	@Override
	public Action step() {

		handleMessages();
		handlePercepts();

		Action act = null;
//
//		// 1. recharging
//		act = planRecharge();
//		if ( act != null ) return act;
//		
//		// 2. buying battery with a certain probability
//		act = planBuyBattery();
//		if ( act != null ) return act;
//
		
		LinkedList<LogicBelief> positionBeliefs = getAllBeliefs("position");
		if (positionBeliefs.size() == 0) {
			println("strangely I do not know my position");
			return MarsUtil.skipAction();
		}
		String position = positionBeliefs.getFirst().getParameters().firstElement();
		
		LinkedList<LogicBelief> neighbourBeliefs = getAllBeliefs("neighbor");
		
		// 3. probing if necessary (neighbour, probed, position)
		act = planProbe(position, neighbourBeliefs);
		if (act != null) return act;
		
		// 4. surveying if necessary (visibleEdge,surveyEdge,position)
		act = planSurvey(position);
		if (act != null) return act;
				
		// 5. (almost) random walking
		act = planRandomWalk(neighbourBeliefs);
		if ( act != null ) return act;

		return MarsUtil.skipAction();
		
	}

	/* Taken from example source */
	private void handleMessages() {
		// handle messages... believe everything the others say
		Collection<Message> messages = getMessages();
		for ( Message msg : messages ) {
			println(msg.sender + " told me " + msg.value);
			String predicate = ((LogicBelief)msg.value).getPredicate();
			if ( containsBelief((LogicBelief)msg.value) ) {
				println("I already knew that");
			}
			else {
				println("that was new to me");
				if( predicate.equals("probedVertex") || predicate.equals("surveyedEdge") ) {
					addBelief((LogicBelief)msg.value);
					println("I will keep that in mind");
					continue;
				}
				println("but I am not interested in that gibberish");
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void handlePercepts(){
		String position = null;

		// check percepts
		Collection<Percept> percepts = getAllPercepts();
		List<Percept> neighbourPercepts = new ArrayList<Percept>();
		
		// Remove beliefs for last action
		removeBeliefs("visibleEntity");
		removeBeliefs("visibleEdge");
		
		for (Percept p : percepts) {
			println("Handling percept: " + p.getName());
			if (p.getName().equals("step")) {
				println(p);
				continue;
			}
			
			if (p.getName().equals("visibleEntity")) {
				LogicBelief b = MarsUtil.perceptToBelief(p);
				if (!containsBelief(b)) {
					addBelief(b);
				}
				continue;
			}
			
			if (p.getName().equals("visibleEdge")) {
				LogicBelief b = MarsUtil.perceptToBelief(p);
				if (!containsBelief(b)) {
					addBelief(b);
				}
				neighbourPercepts.add(p);
				continue;
			}
			
			if (p.getName().equals("probedVertex")) {
				LogicBelief b = MarsUtil.perceptToBelief(p);
				if (!containsBelief(b)) {
					println("I perceive the value of a vertex that I have not known before");
					addBelief(b);
					broadcastBelief(b);
				}
				continue;
			}
			
			if (p.getName().equals("surveyedEdge")) {
				LogicBelief b = MarsUtil.perceptToBelief(p);
				if (!containsBelief(b)) {
					println("I perceive the weight of an edge that I have not known before");
					addBelief(b);
					broadcastBelief(b);
				}
				continue;
			}
			
			if (p.getName().equals("health")) {
				int health = Integer.parseInt(p.getParameters().get(0).toString());
				
				removeBeliefs("health");
				addBelief(new LogicBelief("health", Integer.toString(health)));
				
				println("my health is " +health );
				if (health == 0 ) {
					println("my health is zero. asking for help");
					broadcastBelief(new LogicBelief("iAmDisabled"));
				}
				continue;
			}
			
			if (p.getName().equals("maxHealth")) {
				int maxHealth = Integer.parseInt(p.getParameters().get(0).toString());
				
				removeBeliefs("maxHealth");
				addBelief(new LogicBelief("maxHealth", Integer.toString(maxHealth)));
				continue;
			}
			
			if (p.getName().equals("position")) {
				position = p.getParameters().get(0).toString();
				removeBeliefs("position");
				addBelief(new LogicBelief("position", position));
				continue;
			}
			
			if (p.getName().equals("energy")) {
				int energy = Integer.parseInt(p.getParameters().get(0).toString());
				removeBeliefs("energy");
				addBelief(new LogicBelief("energy", Integer.toString(energy)));
				continue;
			}
			
			if (p.getName().equals("maxEnergy")) {
				Integer maxEnergy = new Integer(p.getParameters().get(0).toString());
				removeBeliefs("maxEnergy");
				addBelief(new LogicBelief("maxEnergy",maxEnergy.toString()));
				continue;
			}
			
			if (p.getName().equals("money")) {
				int money = Integer.parseInt(p.getParameters().get(0).toString());
				removeBeliefs("money");
				addBelief(new LogicBelief("money", Integer.toString(money)));
				continue;
			}
			
			if (p.getName().equals("achievement")) {
				println("reached achievement " + p);
				continue;
			}
		}
		
		// again for checking neighbors
		this.removeBeliefs("neighbor");
		if(position != null){
			for (Percept p : neighbourPercepts) {
				String vertex1 = p.getParameters().get(0).toString();
				String vertex2 = p.getParameters().get(1).toString();
				if (vertex1.equals(position)){ 
					addBelief(new LogicBelief("neighbor",vertex2));
				}
				if (vertex2.equals(position)){ 
					addBelief(new LogicBelief("neighbor",vertex1));
				}
			}
		}
	}
	
	private Action planProbe(String position, LinkedList<LogicBelief> neighbours) {
		// probe current position if not known
		boolean probed = false;
		LinkedList<LogicBelief> vertices = getAllBeliefs("probedVertex");
		for (LogicBelief v : vertices) {
			if (v.getParameters().get(0).equals(position)) {
				probed = true;
				break;
			}
		}
		if (!probed) {
			println("I do not know the value of my position. I will probe.");
			return MarsUtil.probeAction();
		}
		
		println("I know the value of my position");
		
		// get unprobed neighbors
		List<String> unprobed = new ArrayList<String>();
		for (LogicBelief n : neighbours) {
			probed = false;
			String name = n.getParameters().firstElement();
			for (LogicBelief v : vertices) {
				if (v.getParameters().get(0).equals(name)) {
					probed = true;
					break;
				}		
			}
			if (!probed){
				unprobed.add(name);
			}
		}
		
		if (unprobed.size() > 0) {
			println("some of my neighbors are unprobed.");
			//TODO Implement Fisher-Yates for specific type to possibly increase performance
			Collections.shuffle(unprobed);
			String neighbor = unprobed.get(0);
			println("I will go to " + neighbor);
			return MarsUtil.gotoAction(neighbor);
		}

		
		println("all of my neighbors are probed");	
	
		return null;
	}
	
	private Action planSurvey(String position) {
		// get all neighbors
		LinkedList<LogicBelief> visible = getAllBeliefs("visibleEdge");
		LinkedList<LogicBelief> surveyed = getAllBeliefs("surveyedEdge");
		
		println("I know " + visible.size() + " visible edges");
		println("I know " + surveyed.size() + " surveyed edges");
		
		
		int unsurveyedNum = 0;
		int adjacentNum = 0;
		
		for (LogicBelief v : visible) {
		
			String vVertex0 = v.getParameters().elementAt(0);
			String vVertex1 = v.getParameters().elementAt(1);

			boolean adjacent = false;
			if (vVertex0.equals(position) || vVertex1.equals(position)){
				adjacent = true;
			}
			
			if (!adjacent) continue;
			adjacentNum ++;
			
			boolean isSurveyed = false;
			for (LogicBelief s : surveyed) {
				String sVertex0 = s.getParameters().elementAt(0);
				String sVertex1 = s.getParameters().elementAt(1);
				if (sVertex0.equals(vVertex0) &&  sVertex1.equals(vVertex1)) {
					isSurveyed = true;
					break;
				}
				if (sVertex0.equals(vVertex1) &&  sVertex1.equals(vVertex0)) {
					isSurveyed = true;
					break;
				}
			}
			if (!isSurveyed) unsurveyedNum++;
		}

		println("" + unsurveyedNum + " out of " + adjacentNum + " adjacent edges are unsurveyed");
		
		if (unsurveyedNum > 0) {
			println("I will survey");
			return MarsUtil.surveyAction();
		}
		
		return null;
	}
	
	private Action planRandomWalk(LinkedList<LogicBelief> neighboursBeliefs) {
		Vector<String> neighbors = new Vector<String>();
		for (LogicBelief b : neighboursBeliefs) {
			neighbors.add(b.getParameters().firstElement());
		}
		
		if (neighbors.size() == 0) {
			println("strangely I do not know any neighbors");
			return MarsUtil.skipAction();
		}
		
		// goto neighbors
		Collections.shuffle(neighbors);
		String neighbor = neighbors.firstElement();
		println("I will go to " + neighbor);
		return MarsUtil.gotoAction(neighbor);
	}
	
	public String getVariable(String variable) {
		LinkedList<LogicBelief> beliefs = getAllBeliefs(variable);
		if(beliefs.isEmpty()) return null;
		return beliefs.getFirst().getParameters().get(0);
	}
	
	public String getPosition() {
		return getVariable("position");
	}
	
	public String getRole() {
		return getVariable("role");
	}
	
	public boolean isDisabled() {
		return getVariable("disabled") == null;
	}
	
	public int getHealth() {
		String varHealth = getVariable("health");
		return varHealth != null ? Integer.parseInt(varHealth) : 0;
	}
}
