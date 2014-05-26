package custommas.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import custommas.agents.actions.*;
import custommas.common.PlanningCenter;
import custommas.common.SharedKnowledge;
import custommas.common.SharedUtil;
import custommas.common.TeamIntel;
import custommas.lib.Edge;
import custommas.lib.EdgeWeightedGraph;
import custommas.lib.Node;
import custommas.lib.Node.OccupyInfo;
import custommas.lib.Queue;
import custommas.lib.algo.Dijkstra;

import eis.exceptions.NoEnvironmentException;
import eis.exceptions.PerceiveException;
import eis.iilang.Action;
import eis.iilang.Parameter;
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
	protected Action _actionLast;
	protected String _actionLastResult;
	protected int _actionRound;
	protected int _stepRound;
	
	protected Set<String> _innerGoals;
	private String _destinationGoal;
	private List<Node> _pathToDestinationGoal;
	
	protected static final HashSet<String> validPercepts;
	private static final boolean _debug = true;
	
	static {
		validPercepts = SharedUtil.newHashSetFromArray(new String[] {
			"visibleVertex", "visibleEdge", "visibleEntity", "probedVertex", "surveyedEdge",
			"health", "maxHealth", "position", "energy", "maxEnergy", "money", "achievement",
			"inspectedEntity", "score", "zonesScore", "lastActionResult", "step"
		});
	}
	
	protected EdgeWeightedGraph _graph;
	
	public CustomAgent(String name, String team) {
		super(name, team);
		if(name.startsWith("agent") && name.length() > 5){
			_name = name.substring(5).toLowerCase();
		}else{
			_name = name;
		}
		_team = team;
		_visibilityRange = 1;
		_graph = SharedKnowledge.getGraph();
		_innerGoals = new HashSet<String>();
		_destinationGoal = null;
		_pathToDestinationGoal = null;
		
		SharedKnowledge.addCustomAgent(this);
	}

	@Override
	public void handlePercept(Percept percept) {
		println("[Percept " + _name + "/" + _team + "]: " + percept);
	}

	@Override
	public Action step() {
		_graph = SharedKnowledge.getGraph();
		_actionLast = _actionNow;
		_actionLastResult = "failed";
		_actionNow = null;
		_actionRound = 0;
		
		List<TeamIntel> intel = new LinkedList<TeamIntel>();
		for(Percept percept : getAllPercepts()){
			intel.add(new TeamIntel(percept));
		}
		
		handleIntel(intel);
		
		return null;
	}
	
	public void planNewAction(){
		planAction();
		//println("Planning my action: " + _actionNow);
		
		if(_debug){
			if(_actionLast != null){
				println("Last action[" + _position + ", " + _health + ", " + _energy + "]: " + _actionLast.getName() + ", result: " + _actionLastResult);
			}
			if(_actionNow != null){
				StringBuilder pars = new StringBuilder();
				for(Parameter par : _actionNow.getParameters()){
					pars.append(par.toProlog() + ", ");
				}
				println("Next action[" + _position + ", " + _health + ", " + _energy + "]: " + _actionNow.getName() + ", pars: " + pars.toString());
			}
		}
		
		boolean forceRecharge = false;
		if(_actionNow != null && _actionLast != null && _actionNow.getName().equals(_actionLast.getName())){
			if(_actionLastResult.equals("failed_resources")){
				forceRecharge = true;
			}
		}
		
		if(!forceRecharge){
			if(_actionNow instanceof GotoAction){
				GotoAction goAct = ((GotoAction)_actionNow);
				forceRecharge = goAct.getWeight() != Edge.NonSurveyed && _energy < goAct.getWeight();
			}else if(_actionNow instanceof SurveyAction){
				forceRecharge = _energy < 1;
			}else if(_actionNow instanceof ProbeAction){
				forceRecharge = _energy < 1;
			} else if(_actionNow.getName().equals("parry")){
				forceRecharge = _energy < 2;
			}
		}
		
		if(forceRecharge){
			println("FORCE RECHARGE!");
			Action act = planRecharge();
			if(act != null){
				_actionNow = act;
			}
		}
		PlanningCenter.planAction(this, _actionNow);
	}
	
	protected void handleIntel(List<TeamIntel> intelList){
		_position = null;
		int knownStep = PlanningCenter.getStep();
		
		for(TeamIntel intel : intelList){
			// For now skip belief-only-intel
			if(intel.isBelief()) continue;
			
			// Skip messages that are irrelevant
			if(!validPercepts.contains(intel.getName())) continue;
			
			String[] params = intel.getParameters();
			
			if(intel.getName().equals("step")){
				if(params.length < 1) continue;
				int realStep = Integer.parseInt(params[0]);
				if(realStep == 0 && knownStep > 1){
					PlanningCenter.newGame();
					// Should run again to ensure that intel is added to the new graph
					handleIntel(intelList);
					return;
				}
			}else if(intel.getName().equals("visibleVertex")){
				if(params.length < 2) continue;
				String nodeId = params[0];
				
				Node node = _graph.getNode(nodeId);
				if(node == null){
					node = _graph.addNode(nodeId);
				}
			}else if(intel.getName().equals("visibleEdge")){
				if(params.length < 2) continue;
				String node1 = params[0];
				String node2 = params[1];
				
				Edge edge = _graph.getEdgeFromNodeIds(node1, node2);
				if(edge == null){
					_graph.addEdgeFromNodeIds(node1, node2);
				}
			}else if(intel.getName().equals("visibleEntity")){
				if(params.length < 4) continue;
				String agent = params[0];
				String nodeId = params[1];
				String team = params[2];
				
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
				
				Node node = _graph.getNode(nodeId);
				
				if(node.getValue() != value){
					_graph.setNodeProbedValue(node,  value);
				}
			}else if(intel.getName().equals("surveyedEdge")){
				if(params.length < 3) continue;
				String node1 = params[0];
				String node2 = params[1];
				int weight = Integer.parseInt(params[2]);
				
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
				// Inspector only
				String opponentName = params[0];
				String role = params[2].toLowerCase();
				int energy = Integer.parseInt(params[4]);
				int maxEnergy = Integer.parseInt(params[5]);
				int health = Integer.parseInt(params[6]);
				int maxHealth = Integer.parseInt(params[7]);
				
				OpponentAgent opponent = SharedKnowledge.getOpponentAgent(opponentName);
				opponent.setEnergy(energy);
				opponent.setHealth(health);
				opponent.setMaxEnergy(maxEnergy);
				opponent.setMaxHealth(maxHealth);
				opponent.setRole(role);	
			}else if(intel.getName().equals("score")){
				if(params.length < 1) continue;
				int score = Integer.parseInt(params[0]);
				SharedKnowledge.setTeamScore(score);
			}else if(intel.getName().equals("zonesScore")){
				if(params.length < 1) continue;
				int zoneScore = Integer.parseInt(params[0]);
				SharedKnowledge.setZoneScore(zoneScore);
			}else if(intel.getName().equals("lastActionResult")){
				_actionLastResult = params[0];
			}
		}
	}

	protected abstract void planAction();
	
	protected Action planRecharge(){
		return planRecharge(_maxEnergy);
	}
	
	protected Action planRecharge(double threshold){
		return planRecharge((int)(threshold*_maxEnergy));
	}
	
	protected Action planRecharge(int minEnergy){		
		if (_innerGoals.contains("beAtFullCharge")) {
			if (_maxEnergy == _energy) {
				_innerGoals.remove("beAtFullCharge");
			} else {
				return MarsUtil.rechargeAction();
			}
		}else if(_energy < minEnergy){
			_innerGoals.add("beAtFullCharge");
			return MarsUtil.rechargeAction();
		}
		
		return null;
	}
	
	protected Action planRandomWalk(Node currentNode) {
		println("PLANNING RANDOM WALK!");
		List<Node> neighbours = new ArrayList<Node>(_graph.getAdjacentTo(currentNode));
		if(neighbours == null || neighbours.size() < 1) return null;
		Collections.shuffle(neighbours);
		return new GotoAction(neighbours.get(0).getId(), 1);
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
							return new SurveyAction(originNode.getId());
						}
					}
				}
			}
		}
		
		return null;
	}
	
	protected Action planGoToGoal(Node currentNode){
		Node moveToNode = null;
		if(_destinationGoal != null){	
			println("Trying to reach goal: " + _destinationGoal);
			if(_destinationGoal.equals(currentNode.getId())){
				// Goal reached
				_pathToDestinationGoal = null;
				_destinationGoal = null;
			}

			if(_pathToDestinationGoal == null){
				Node goalNode = _graph.getNode(_destinationGoal);
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
			return new GotoAction(moveToNode.getId(), _graph.getEdgeFromNodes(currentNode, moveToNode).getWeight());
		}
		
		return null;
	}
	
	public void gotoNode(String nodeId){
		boolean validId = SharedUtil.isValidNodeId(nodeId);
		println("Received destination goal: " + nodeId + " [" + (validId ? "VALID" : "INVALID") + "]");
		if(validId){
			_destinationGoal = nodeId;
			_pathToDestinationGoal = null;
		}
	}
	
	protected Set<OpponentAgent> nearbyOpponents(Node originNode) {
		return nearbyOpponents(originNode, _visibilityRange);
	}
	
	protected Set<OpponentAgent> nearbyOpponents(Node originNode, int maxRange) {
		return nearbyOpponents(originNode, maxRange, 0);
	}	
	
	protected Set<OpponentAgent> nearbyOpponents(Node originNode, int maxRange, int timeCutOff) {
		int range = 0;
		Set<OpponentAgent> opponents = new HashSet<OpponentAgent>();
		HashSet<String> checkedNodes = new HashSet<String>();
		Queue<Node> nextCheck = new Queue<Node>();
		nextCheck.enqueue(originNode);
		checkedNodes.add(originNode.getId());
		if(maxRange < 0) maxRange = _visibilityRange;
		if(timeCutOff < 0) timeCutOff = 0;
		
		for(OccupyInfo info : originNode.getOccupantsForTeam(SharedKnowledge.OpponentTeam)){
			if(info.getStepsAgo() <= timeCutOff){
				opponents.add(SharedKnowledge.getOpponentAgent(info.getAgentName()));
			}
		}
		
		while(nextCheck.size() > 0 && range < maxRange){
			Queue<Node> toCheck = nextCheck;
			nextCheck = new Queue<Node>();
			range++;
			
			while(toCheck.size() > 0){
				Node node = toCheck.dequeue();
				for(Node n : _graph.getAdjacentTo(node)){
					if(checkedNodes.contains(n.getId())) continue;
					nextCheck.enqueue(n);
					checkedNodes.add(n.getId());
					
					for(OccupyInfo info : n.getOccupantsForTeam(SharedKnowledge.OpponentTeam)){
						if(info.getStepsAgo() <= timeCutOff){
							opponents.add(SharedKnowledge.getOpponentAgent(info.getAgentName()));
						}
					}
				}
			}
		}
		
		return opponents;
	}
	
	public Action getPlannedAction(){
		return _actionNow;
	}
	
	public String getPosition(){
		return _position;
	}
	
	public Node getNode(){
		return _graph.getNode(_position);
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
	
	public boolean isDisabled(){
		return _health < 1 && _maxHealth > 0;
	}
	
	public void reset(){
		_health = 0;
		_maxHealth = 0;
		_energy = 0;
		_maxEnergy = 0;
		_position = null;
		_graph = SharedKnowledge.getGraph();
		_innerGoals = new HashSet<String>();
		_destinationGoal = null;
		_pathToDestinationGoal = null;
	}
	
	@Override
	protected Collection<Percept> getAllPercepts() {
		try {
			Map<String, Collection<Percept>> percepts = getEnvironmentInterface().getAllPercepts(getName());
			Collection<Percept> ret = new LinkedList<Percept>();
			for ( Collection<Percept> ps : percepts.values() ) {
				ret.addAll(ps);
			}			
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
