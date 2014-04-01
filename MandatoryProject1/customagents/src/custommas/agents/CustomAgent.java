package custommas.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import custommas.common.MessageCenter;
import custommas.common.PlanningCenter;
import custommas.common.SharedKnowledge;
import custommas.common.SharedUtil;
import custommas.common.TeamIntel;
import custommas.lib.Edge;
import custommas.lib.EdgeWeightedGraph;
import custommas.lib.Node;
import custommas.ui.AgentMonitor;

import apltk.interpreter.data.LogicGoal;
import eis.iilang.Action;
import eis.iilang.Percept;
import massim.javaagents.Agent;
import massim.javaagents.agents.MarsUtil;

public abstract class CustomAgent extends Agent {
	protected String _name;
	protected String _team;
	protected String _position;
	protected int _health;
	protected int _maxHealth;
	protected int _energy;
	protected int _maxEnergy;
	protected int _money;
	protected String _role;
	
	protected Action _actionNow;
	protected Action _lastAction;
	protected int _actionRound;
	protected int _stepRound;
	
	protected static final HashSet<String> validMessages;
	protected static final HashSet<String> validPercepts;
	
	static {
		// No longer in use, might have to use it again for part 3
		validMessages = SharedUtil.newHashSetFromArray(new String[] { });
		validPercepts = SharedUtil.newHashSetFromArray(new String[] {
			"visibleVertex", "visibleEdge", "visibleEntity", "probedVertex", "surveyedEdge",
			"health", "maxHealth", "position", "energy", "maxEnergy", "money", "achievement"
		});
	}
	
	protected EdgeWeightedGraph _graph;
	
	public CustomAgent(String name, String team) {
		super(name, team);
		_name = name;
		_team = team;
		_graph = SharedKnowledge.getGraph();
		AgentMonitor.getInstance().registerAgent(this);
	}

	@Override
	public void handlePercept(Percept percept) {
		println("[Percept " + _name + "/" + _team + "]: " + percept);
	}

	@Override
	public Action step() {
		_lastAction = _actionNow;
		_actionNow = null;
		_actionRound = 0;
		
		List<TeamIntel> intel = new LinkedList<TeamIntel>();
		for(TeamIntel message : MessageCenter.getMessages(this)){
			intel.add(message);
		}
		
		for(Percept percept : getAllPercepts()){
			intel.add(new TeamIntel(percept));
		}
		
		handleIntel(intel);
		planNewAction();
		
		return _actionNow;
	}
	
	public void planNewAction(){
		planAction();
		//println("Planning my action: " + _actionNow);
		PlanningCenter.planAction(this, _actionNow);
	}
	
	protected void handleIntel(List<TeamIntel> intelList){
		_position = null;
		for(TeamIntel intel : intelList){
			// For now skip belief-only-intel
			if(intel.isBelief()) continue;
			
			// Skip messages that are irrelevant
			if(intel.isMessage()){
				if(!validMessages.contains(intel.getName())) continue;
			}else{
				if(!validPercepts.contains(intel.getName())) continue;
			}
			
			boolean newKnowledge = false;
			String[] params = intel.getParameters();
			
			if(intel.getName().equals("visibleVertex")){
				if(params.length < 2) continue;
				String nodeId = params[0];
				String ownerTeam = params[1];
				
				//println("Got [visibleVertex] " + nodeId + " owned by " + ownerTeam);
				
				Node node = _graph.getNode(nodeId);
				if(node == null){
					node = _graph.addNode(nodeId);
				}
				node.setOwner(ownerTeam);
			}else if(intel.getName().equals("visibleEdge")){
				if(params.length < 2) continue;
				String node1 = params[0];
				String node2 = params[1];
				
				//println("Got [visibleEdge] " + node1 + " -> " + node2);
				
				Edge edge = _graph.getEdgeFromNodeIds(node1, node2);
				if(edge == null){
					_graph.addEdgeFromNodeIds(node1, node2);
				}
			/*}else if(intel.getName().equals("visibleEntity")){
				if(params.length < 4) continue;
				String agent = params[0];
				String nodeId = params[1];
				String status = params[2];
				String team = params[3];
				
				//println("Got [visibleEntity] " + agent + ", " + team + ", " + nodeId + " -> " + node2);
				
				Node node = _graph.getNode(nodeId);
				if(node == null){
					node = _graph.addNode(nodeId);
				}
				
				// Do more here at some point!*/
			}else if(intel.getName().equals("probedVertex")){
				// Explorer only
				if(params.length < 2) continue;
				String nodeId = params[0];
				int value = Integer.parseInt(params[1]);
				
				//println("Got [probedVertex] " + nodeId + ": " + value);
				
				Node node = _graph.getNode(nodeId);
				
				if(node.getValue() != value){
					_graph.setNodeProbedValue(node,  value);
				}
			}else if(intel.getName().equals("surveyedEdge")){
				if(params.length < 3) continue;
				String node1 = params[0];
				String node2 = params[1];
				int weight = Integer.parseInt(params[2]);
				
				//println("Got [surveyedEdge] " + node1 + " -> " + node2 + ": " + weight);
				
				Edge edge = _graph.getEdgeFromNodeIds(node1, node2);
						
				if(edge.getWeight() != weight){
					edge.setWeight(weight);
				}
			}else if(intel.getName().equals("health")){
				if(params.length < 1) continue;
				_health = Integer.parseInt(params[0]);
			}else if(intel.getName().equals("maxHealth")){
				if(params.length < 1) continue;
				_maxHealth = Integer.parseInt(params[0]);
			}else if(intel.getName().equals("energy")){
				if(params.length < 1) continue;
				_energy = Integer.parseInt(params[0]);
			}else if(intel.getName().equals("maxEnergy")){
				if(params.length < 1) continue;
				_maxEnergy = Integer.parseInt(params[0]);
			}else if(intel.getName().equals("position")){
				if(params.length < 1) continue;
				if(_position == null || !_position.equals(params[0])){
					_position = params[0];
					_graph.setAgentLocation(_name, _position);
				}
			}else if(intel.getName().equals("money")){
				if(params.length < 1) continue;
				_money = Integer.parseInt(params[0]);
			}else if(intel.getName().equals("achievement")){
				String achievement = params[0];
				println("Got achievement: " + achievement);
			}
			
			if(newKnowledge && intel.isPercept() && validMessages.contains(intel.getName())){
				MessageCenter.broadcastMessage(intel.asMessage(_name));
			}
		}
	}

	protected abstract void planAction();
	
	protected Action planRecharge(double threshold){
		int energy = _energy;
		int maxEnergy = _maxEnergy;
		
		// if agent has the goal of being recharged...
		if (goals.contains(new LogicGoal("beAtFullCharge"))) {
			if (maxEnergy == energy) {
				//println("I can stop recharging. I am at full charge");
				removeGoals("beAtFullCharge");
			} else {
				//println("recharging...");
				return MarsUtil.rechargeAction();
			}
		}else if(energy < maxEnergy * threshold){
			//println("I need to recharge");
			goals.add(new LogicGoal("beAtFullCharge"));
			return MarsUtil.rechargeAction();
		}
		
		return null;
	}
	
	protected Action planRandomWalk(Node currentNode) {
		//println("Trying to find random node to walk to");
		List<Node> neighbours = new ArrayList<Node>(_graph.getAdjacentTo(currentNode));
		if(neighbours == null || neighbours.size() < 1) return null;
		//println("Found neighbours, will shuffle");
		Collections.shuffle(neighbours);
		//println("I will go to " + neighbours.get(0).getId());
		return MarsUtil.gotoAction(neighbours.get(0).getId());
	}
	
	public abstract void gotoNode(String nodeId);
	
	public Action getPlannedAction(){
		return _actionNow;
	}
	
	public Action getLastAction(){
		return _lastAction;
	}
	
	public String getPosition(){
		return _position;
	}
	
	public int getHealth(){
		return _health;
	}
	
	public int getMaxHealth(){
		return _maxHealth;
	}
	
	public boolean hasMaxHealth(){
		return _health == _maxHealth;
	}
	
	public int getEnergy(){
		return _energy;
	}
	
	public int getMaxEnergy(){
		return _maxEnergy;
	}
	
	public boolean hasMaxEnergy(){
		return _energy == _maxEnergy;
	}
	
	public String getRole(){
		if(_role == null){
			if(this instanceof ExplorerAgent){
				_role = "Explorer";
			}else if(this instanceof InspectorAgent){
				_role = "Inspector";
			}else if(this instanceof RepairerAgent){
				_role = "Repairer";
			}else if(this instanceof SaboteurAgent){
				_role = "Saboteur";
			}else if(this instanceof SentinelAgent){
				_role = "Sentinel";
			}else if(this instanceof SkippyAgent){
				_role = "Skippy";
			}else{
				_role = "Unknown";
			}
		}
		return _role;
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
