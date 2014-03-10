package custommas.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import custommas.lib.EdgeWeightedGraph;
import custommas.lib.Node;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import apltk.interpreter.data.Message;
import eis.iilang.Action;
import eis.iilang.Percept;
import massim.javaagents.Agent;
import massim.javaagents.agents.MarsUtil;

public abstract class CustomAgent extends Agent {
	protected String _name;
	protected String _team;
	
	protected EdgeWeightedGraph _graph;
	
	public CustomAgent(String name, String team) {
		super(name, team);
		_name = name;
		_team = team;
		_graph = new EdgeWeightedGraph();
	}

	@Override
	public void handlePercept(Percept percept) {
		println("[Percept " + _name + "/" + _team + "]: " + percept);
	}

	@Override
	public Action step() {
		handleMessages();
		handlePercepts();
		return nextAction();
	}
	
	protected abstract Action nextAction();
	
	/* Taken from example source */
	protected void handleMessages() {
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
	
	protected abstract void handlePercepts();
	
	protected Action planRecharge(double threshold){
		int energy = getEnergy();
		int maxEnergy = getMaxEnergy();
		
		// if has the goal of being recharged...
		if (goals.contains(new LogicGoal("beAtFullCharge"))) {
			if (maxEnergy == energy) {
				println("I can stop recharging. I am at full charge");
				removeGoals("beAtFullCharge");
			} else {
				println("recharging...");
				return MarsUtil.rechargeAction();
			}
		}else if(energy < maxEnergy * threshold){
			// TODO: Optimize to beAtHalfCharge and then restore full power
			println("I need to recharge");
			goals.add(new LogicGoal("beAtFullCharge"));
			return MarsUtil.rechargeAction();
		}
		
		return null;
	}
	
	protected Action planRandomWalk(Node currentNode) {
		println("Trying to find random node to walk to");
		List<Node> neighbours = new ArrayList<Node>(_graph.getAdjacentTo(currentNode));
		if(neighbours == null || neighbours.size() < 1) return null;
		println("Found neighbours, will shuffle");
		Collections.shuffle(neighbours);
		println("I will go to " + neighbours.get(0).getId());
		return MarsUtil.gotoAction(neighbours.get(0).getId());
	}
	
	public String getVariable(String variable) {
		LinkedList<LogicBelief> beliefs = getAllBeliefs(variable);
		if(beliefs.isEmpty()) return null;
		return beliefs.getFirst().getParameters().get(0);
	}
	
	public int getVariableAsInt(String variable, int fallbackValue){
		String var = getVariable(variable);
		return var != null ? Integer.parseInt(var) : fallbackValue;
	}
	
	public int getEnergy(){
		return getVariableAsInt("energy", 0);
	}
	
	public int getMaxEnergy(){
		return getVariableAsInt("maxEnergy", 0);
	}
	
	public boolean hasMaxEnergy(){
		return getEnergy() == getMaxEnergy();
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
		return getVariableAsInt("health", 0);
	}
}
