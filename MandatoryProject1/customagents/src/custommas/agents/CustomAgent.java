package custommas.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import custommas.common.ActionSchedule;
import custommas.common.MessageCenter;
import custommas.common.PlanningCenter;
import custommas.common.SharedUtil;
import custommas.common.TeamIntel;
import custommas.lib.EdgeWeightedGraph;
import custommas.lib.Node;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import apltk.interpreter.data.Message;
import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import massim.javaagents.Agent;
import massim.javaagents.agents.MarsUtil;

public abstract class CustomAgent extends Agent {
	protected int _internalUniqueId;
	protected String _name;
	protected String _team;
	protected String _position;
	protected int _health;
	protected int _maxHealth;
	protected int _energy;
	protected int _maxEnergy;
	protected int _money;
	
	protected Action _actionNow;
	protected int _actionRound;
	protected int _stepRound;
	
	protected static final HashSet<String> validMessages;
	protected static final HashSet<String> validPercepts;
	protected static final HashSet<String> validActionShouts;
	
	static {
		validMessages = SharedUtil.newHashSetFromArray(new String[] {
			"visibleVertex", "visibleEdge", "probedVertex", "surveyedEdge", "position"
		});
		validPercepts = SharedUtil.newHashSetFromArray(new String[] {
			"visibleVertex", "visibleEdge", "probedVertex", "surveyedEdge",
			"health", "maxHealth", "position", "energy", "maxEnergy", "money", "achievement"
		});
		validActionShouts = SharedUtil.newHashSetFromArray(new String[] {
			SharedUtil.Actions.GoTo, SharedUtil.Actions.Custom.GoToAndProbe, 
			SharedUtil.Actions.Probe, SharedUtil.Actions.Survey
		});
	}
	
	protected EdgeWeightedGraph _graph;
	
	public CustomAgent(String name, String team) {
		super(name, team);
		_internalUniqueId = SharedUtil.getUnassignedAgentId();
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
		_actionNow = null;
		_actionRound = 0;
		//_stepRound = PlanningCenter.getStep();
		
		List<TeamIntel> intel = new LinkedList<TeamIntel>();
		TeamIntel[] messages = MessageCenter.getMessages(this);
		for(TeamIntel message : messages){
			intel.add(message);
		}
		println("Read all my messages: " + messages.length);
		
		for(Percept percept : getAllPercepts()){
			intel.add(new TeamIntel(percept));
		}
		
		//println("Noted all my percepts");
		
		handleIntel(intel);
		
		//println("Handled all my intel");
		
		pingForNewAction();
		
		/*println("Started ping for action");
		
		while(!PlanningCenter.donePlanning()){
			try {
				if(_actionNow != null){
					println("Waiting for PC! My action: " + _actionNow);
				}else{
					println("No action planned for me!");
				}
				Thread.sleep(100);
			} catch (InterruptedException e) {
				println("Something went wrong waiting for planning, I'll just do as I planned to");
				break;
			}
		}*/
		
		return _actionNow;
	}
	
	public void pingForNewAction(/*int syncStep*/){
		/*if(syncStep < _stepRound){
			println("[OUT_OF_SYNC]");
			return;
		}*/
		planAction();
		if(_actionNow != null){
			println("Planning my action: " + _actionNow);
		}else{
			println("Couldn't find any action!");
		}
		PlanningCenter.planAction(this, _actionNow/*, syncStep*/);
	}
	
	protected abstract void handleIntel(List<TeamIntel> intelList);
	protected abstract void planAction();
	
	protected Action planRecharge(double threshold){
		int energy = _energy;
		int maxEnergy = _maxEnergy;
		
		// if agent has the goal of being recharged...
		if (goals.contains(new LogicGoal("beAtFullCharge"))) {
			if (maxEnergy == energy) {
				println("I can stop recharging. I am at full charge");
				removeGoals("beAtFullCharge");
			} else {
				println("recharging...");
				return MarsUtil.rechargeAction();
			}
		}else if(energy < maxEnergy * threshold){
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
	
	public Action getPlannedAction(){
		return _actionNow;
	}
	
	public boolean hasMaxEnergy(){
		return _energy == _maxEnergy;
	}
	
	@Override
	public boolean equals(Object o){
		if(o == null || !(o instanceof CustomAgent)) return false;
		return _name.equals(((CustomAgent)o).getName());
	}
	
	@Override
	public int hashCode(){
		return _name.hashCode();
	}
}
