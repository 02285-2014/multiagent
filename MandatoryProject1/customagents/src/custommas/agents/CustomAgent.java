package custommas.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import custommas.common.ActionSchedule;
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
	protected Action _actionNext;
	
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
	
	protected ActionSchedule _actionSchedule;
	protected EdgeWeightedGraph _graph;
	
	public CustomAgent(String name, String team) {
		super(name, team);
		_internalUniqueId = SharedUtil.getUnassignedAgentId();
		_name = name;
		_team = team;
		_graph = new EdgeWeightedGraph();
		_actionSchedule = new ActionSchedule();
	}

	@Override
	public void handlePercept(Percept percept) {
		println("[Percept " + _name + "/" + _team + "]: " + percept);
	}

	@Override
	public Action step() {
		_actionSchedule.clear();
		List<TeamIntel> intel = new LinkedList<TeamIntel>();
		for(Message message : getMessages()){
			TeamIntel mIntel = new TeamIntel(message);
			
			println("Got message intel with name: " + mIntel.getName());
			if(mIntel.getName().equals("nextAgentAction")){
				if(mIntel.getParameters().length > 0 && validActionShouts.contains(mIntel.getParameters()[0])){
					println("Got nextAgentAction: " + Arrays.toString(mIntel.getParameters()));
					_actionSchedule.add(mIntel);
				}
			}else{
				intel.add(mIntel);
			}
		}
		
		for(Percept percept : getAllPercepts()){
			intel.add(new TeamIntel(percept));
		}
		
		println("[ACTION SCHEDULE]");
		println(_actionSchedule.toString());
		
		handleIntel(intel);
		
		planActions();
		if(_actionNext != null && validActionShouts.contains(_actionNext.getName())){
			println("Trying to make special: " + _actionNext.getName());
			LogicBelief b = actionToBelief(_actionNext);
			if(b != null){
				println("[SPECIAL] Broadcasting next action: " + b.getParameters());
				broadcastBelief(b);
			}
		}
		return _actionNow;
	}
	
	protected abstract void handleIntel(List<TeamIntel> intelList);
	protected abstract void planActions();
	
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
	
	/*public int getEnergy(){
		return _energy;
	}
	
	public int getMaxEnergy(){
		return _maxEnergy;
	}*/
	
	public boolean hasMaxEnergy(){
		return _energy == _maxEnergy;
	}
	
	/*public String getPosition() {
		return _position;
	}
	
	public int getHealth() {
		return _health;
	}
	
	public int getMaxHealth(){
		return _maxHealth;
	}*/
	
	private LogicBelief actionToBelief(Action action){
		if(action != null){
			Collection<String> pars = new LinkedList<String>();
			if(action instanceof ProbeAction){
				pars.add(SharedUtil.Actions.Probe);
				pars.add(((ProbeAction)action).getNodeId());
				pars.add("" + _internalUniqueId);
			}else if(action instanceof SurveyAction){
				pars.add(SharedUtil.Actions.Survey);
				pars.add(((SurveyAction)action).getNodeId());
				pars.add("" + _internalUniqueId);
			}else if(action instanceof GotoAndProbeAction){
				pars.add(SharedUtil.Actions.Custom.GoToAndProbe);
				pars.add(((GotoAndProbeAction)action).getNodeId());
				pars.add("" + ((GotoAndProbeAction)action).getSteps());
			}
			
			if(pars.size() > 0){
				return new LogicBelief("nextAgentAction", pars);
			}
		}
		return null;
	}
	
	protected class ProbeAction extends Action {
		private String _nodeId;
		
		public ProbeAction(String nodeId) {
			super(SharedUtil.Actions.Probe);
			_nodeId = nodeId;
		}
		
		public String getNodeId(){
			return _nodeId;
		}
	}
	
	protected class SurveyAction extends Action {
		private String _nodeId;
		
		public SurveyAction(String nodeId) {
			super(SharedUtil.Actions.Survey);
			_nodeId = nodeId;
		}
		
		public String getNodeId(){
			return _nodeId;
		}
	}
	
	protected class GotoAction extends Action {
		private String _nodeId;
		private int _steps;
		
		public GotoAction(String nodeId, int steps) {
			super(SharedUtil.Actions.GoTo, new Identifier(nodeId));
			_nodeId = nodeId;
		}
		
		public String getNodeId(){
			return _nodeId;
		}
		
		public int getSteps(){
			return _steps;
		}
	}
	
	protected class GotoAndProbeAction extends GotoAction {
		public GotoAndProbeAction(String nodeId, int steps) {
			super(nodeId, steps);
		}		
	}
}
