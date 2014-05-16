package custommas.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import custommas.agents.actions.GotoAction;
import custommas.agents.actions.SurveyAction;
import custommas.common.MessageCenter;
import custommas.common.PlanningCenter;
import custommas.common.SharedKnowledge;
import custommas.common.SharedUtil;
import custommas.common.TeamIntel;
import custommas.lib.Edge;
import custommas.lib.EdgeWeightedGraph;
import custommas.lib.Node;
import custommas.lib.Queue;
import custommas.lib.algo.Dijkstra;

import apltk.interpreter.data.LogicGoal;
import eis.exceptions.NoEnvironmentException;
import eis.exceptions.PerceiveException;
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
	protected String _role;
	protected int _visibilityRange;
	
	protected Action _actionNow;
	protected Action _lastAction;
	protected int _actionRound;
	protected int _stepRound;
	
	protected Set<String> _innerGoals;
	private Queue<String> _destinationGoalInsertQueue;
	private Queue<String> _destinationGoals;
	private List<Node> _pathToDestinationGoal;
	
	protected static final HashSet<String> validMessages;
	protected static final HashSet<String> validPercepts;
	
	static {
		// No longer in use, might have to use it again for part 3
		validMessages = SharedUtil.newHashSetFromArray(new String[] { });
		validPercepts = SharedUtil.newHashSetFromArray(new String[] {
			"visibleVertex", "visibleEdge", "visibleEntity", "probedVertex", "surveyedEdge",
			"health", "maxHealth", "position", "energy", "maxEnergy", "money", "achievement",
			"inspectedEntity", "score", "zonesScore"
		});
	}
	
	protected EdgeWeightedGraph _graph;
	
	public CustomAgent(String name, String team) {
		super(name, team);
		if(name.startsWith("agent")){
			_name = name.substring(5).toLowerCase();
		}else{
			_name = name;
		}
		_team = team;
		_visibilityRange = 1;
		_graph = SharedKnowledge.getGraph();
		_innerGoals = new HashSet<String>();
		_destinationGoals = new Queue<String>();
		_destinationGoalInsertQueue = new Queue<String>();
		_pathToDestinationGoal = null;
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
		
		return null;
	}
	
	public void planNewAction(){
		if(_actionRound < 1){
			while(_destinationGoalInsertQueue.size() > 0){
				_destinationGoals.enqueue(_destinationGoalInsertQueue.dequeue());
			}
		}
		
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
			}else if(intel.getName().equals("visibleEdge")){
				if(params.length < 2) continue;
				String node1 = params[0];
				String node2 = params[1];
				
				//println("Got [visibleEdge] " + node1 + " -> " + node2);
				
				Edge edge = _graph.getEdgeFromNodeIds(node1, node2);
				if(edge == null){
					_graph.addEdgeFromNodeIds(node1, node2);
				}
			}else if(intel.getName().equals("visibleEntity")){
				if(params.length < 4) continue;
				String agent = params[0];
				String nodeId = params[1];
				String team = params[2];
				//String status = params[3];
				
				//println("Got [visibleEntity] " + agent + ", " + team + ", " + nodeId + " -> " + node2);
				
				Node node = _graph.getNode(nodeId);
				if(node == null){
					node = _graph.addNode(nodeId);
				}
				_graph.setAgentLocation(agent,  team,  nodeId);
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
					_graph.setAgentLocation(_name, _team, _position);
				}
			}else if(intel.getName().equals("inspectedEntity")){
				// [b19, B, Sentinel, v206, 29, 30, 1, 1, 0, 3]
				String opponentName = params[0];
				String role = params[2].toLowerCase();
				int energy = Integer.parseInt(params[4]);
				int maxEnergy = Integer.parseInt(params[5]);
				int health = Integer.parseInt(params[6]);
				int maxHealth = Integer.parseInt(params[7]);
				
				println("[INSPECT] maxHealth: " + maxHealth);
				
				//<inspectedEntity energy="16" health="6" maxEnergy="25" maxHealth="6" name="b25" node="v97" role="Inspector" strength="0" team="B" visRange="1"/>
				OpponentAgent opponent = SharedKnowledge.getOpponentAgent(opponentName);
				opponent.setEnergy(energy);
				opponent.setHealth(health);
				opponent.setMaxEnergy(maxEnergy);
				opponent.setMaxHealth(maxHealth);
				opponent.setRole(role);	
			/*else if(intel.getName().equals("money")){
				if(params.length < 1) continue;
				_money = Integer.parseInt(params[0]);
			}else if(intel.getName().equals("achievement")){
				String achievement = params[0];
				println("Got achievement: " + achievement);
			}*/
			}else if(intel.getName().equals("score")){
				if(params.length < 1) continue;
				int score = Integer.parseInt(params[0]);
				SharedKnowledge.setTeamScore(score);
			}else if(intel.getName().equals("zonesScore")){
				if(params.length < 1) continue;
				int zoneScore = Integer.parseInt(params[0]);
				SharedKnowledge.setZoneScore(zoneScore);
			}
			
			if(newKnowledge && intel.isPercept() && validMessages.contains(intel.getName())){
				MessageCenter.broadcastMessage(intel.asMessage(_name));
			}
		}
	}

	protected abstract void planAction();
	
	protected Action planRecharge(){
		return planRecharge(_maxEnergy);
	}
	
	protected Action planRecharge(double threshold){
		return planRecharge((int)(threshold*_energy));
	}
	
	protected Action planRecharge(int minEnergy){
		int energy = _energy;
		
		if (_innerGoals.contains("beAtFullCharge")) {
			if (_maxEnergy == _energy) {
				_innerGoals.remove("beAtFullCharge");
			} else {
				return MarsUtil.rechargeAction();
			}
		}else if(energy < minEnergy){
			_innerGoals.add("beAtFullCharge");
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
	
	protected Action planSurvey(Node originNode) {
		return planSurvey(originNode, 1);
	}
	
	protected Action planSurvey(Node originNode, int minUnsurveyedCount) {
		if(!PlanningCenter.proceed(SharedUtil.Actions.Survey, originNode.getId())) return null;
		
		int range = 0;
		int unsurveyedCount = 0;
		HashSet<String> checkedNodes = new HashSet<String>();
		Queue<Node> nextCheck = new Queue<Node>();
		nextCheck.enqueue(originNode);
		checkedNodes.add(originNode.getId());
		
		while(nextCheck.size() > 0 && range < _visibilityRange){
			Queue<Node> toCheck = nextCheck;
			nextCheck = new Queue<Node>();
			range++;
			
			while(toCheck.size() > 0){
				Node node = toCheck.dequeue();
				for(Node n : _graph.getAdjacentTo(node)){
					if(checkedNodes.contains(n.getId())) continue;
					nextCheck.enqueue(n);
					checkedNodes.add(n.getId());
					Edge e = _graph.getEdgeFromNodes(node, n);
					if(!e.isSurveyed()){
						if(++unsurveyedCount >= minUnsurveyedCount){
							return new SurveyAction(node.getId());
						}
					}
				}
			}
		}
		
		return null;
	}
	
	protected Action planGoToGoal(Node currentNode){
		Node moveToNode = null;
		if(_destinationGoals.size() > 0){
			println("Trying to reach goal: " + _destinationGoals.peek());
			if(_destinationGoals.peek().equals(currentNode.getId())){
				// Goal reached
				_pathToDestinationGoal = null;
				_destinationGoals.dequeue();
			}

			if(_pathToDestinationGoal == null){
				Node goalNode = _graph.getNode(_destinationGoals.peek());
				if(goalNode != null){
					// New goal
					_pathToDestinationGoal = Dijkstra.getPath(_graph, currentNode, goalNode);
				}
			}
			
			if(_pathToDestinationGoal != null){
				while(_pathToDestinationGoal.size() > 0 && _pathToDestinationGoal.get(0).equals(currentNode)){
					_pathToDestinationGoal.remove(0);
				}
				if(_pathToDestinationGoal.size() > 0){
					moveToNode = _pathToDestinationGoal.get(0);
				}
			}
		}
		
		if(moveToNode != null && !_graph.getAdjacentTo(currentNode).contains(moveToNode)){
			println("Trying to move to node i cant move to!");
			moveToNode = null;
		}
		
		if(moveToNode != null){
			println("On my way to my goal I will move from " + currentNode.getId() + " to " + moveToNode.getId());
			return new GotoAction(moveToNode.getId(), _pathToDestinationGoal.size());
		}
		
		return null;
	}
	
	public void gotoNode(String nodeId){
		boolean validId = SharedUtil.isValidNodeId(nodeId);
		println("Received destination goal: " + nodeId + " [" + (validId ? "VALID" : "INVALID") + "]");
		if(validId){
			_destinationGoalInsertQueue.enqueue(nodeId);
		}
	}
	
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
		return _role;
	}
	
	@Override
	protected Collection<Percept> getAllPercepts() {
		
		try {
			Map<String, Collection<Percept>> percepts = getEnvironmentInterface().getAllPercepts(getName());
			Collection<Percept> ret = new LinkedList<Percept>();
			for ( Collection<Percept> ps : percepts.values() ) {
				ret.addAll(ps);
			}
			
			// sweep mental attitudes if there has been a restart 
//			int step = -1;
//			for ( Percept p : ret ) {
//				if ( p.getName().equals("step")) {
//					step = new Integer(p.getParameters().get(0).toProlog()).intValue();
//					break;
//				}
//			}
//			if ( step != -1 && step < oldStep) {
//				println("sweeping mental attitudes");
//				beliefs.clear();
//				goals.clear();
//			}
//			if ( step != -1 )
//				oldStep = step;
			
			return ret;
		} catch (PerceiveException e) {
			//e.printStackTrace();
			println("error perceiving \"" + e.getMessage() + "\"");	
			return new LinkedList<Percept>();
		} catch (NoEnvironmentException e) {
			//e.printStackTrace();
			println("error perceiving \"" + e.getMessage() + "\"");	
			return new LinkedList<Percept>();
		}
		
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
