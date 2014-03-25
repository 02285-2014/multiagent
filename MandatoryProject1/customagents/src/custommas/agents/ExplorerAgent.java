package custommas.agents;

import java.util.HashSet;
import java.util.List;

import custommas.common.SharedUtil;
import custommas.common.TeamIntel;
import custommas.lib.Edge;
import custommas.lib.Node;
import custommas.lib.Queue;
import custommas.lib.Stack;
import custommas.lib.algo.BreadthFirstExplorerSearch;
import custommas.lib.algo.Dijkstra;
import custommas.lib.interfaces.INodePredicate;
import custommas.ui.ExplorerInput;
import custommas.ui.IInputCallback;

import eis.iilang.Action;
import massim.javaagents.agents.MarsUtil;

public class ExplorerAgent extends CustomAgent{
	private BreadthFirstExplorerSearch _unprobedSearch;
	private ExplorerInput _input;
	private Queue<String> _destinationGoals;
	private Queue<String> _goalInsertQueue;
	private List<Node> _pathToDestinationGoal;
	private boolean allProbed;
	
	private INodePredicate unprobedPredicate = new INodePredicate(){
		public boolean evaluate(Node node, int comparableValue) {
			return !node.isProbed() && !_graph.isNodeOccupied(node.getId()) 
					&& _actionSchedule.Proceed(SharedUtil.Actions.Custom.GoToAndProbe, node.getId(), comparableValue)
					&& _actionSchedule.Proceed(SharedUtil.Actions.Probe, node.getId(), _internalUniqueId);
		}
	};
	
	public ExplorerAgent(String name, String team) {
		super(name, team);
		_destinationGoals = new Queue<String>();
		_goalInsertQueue = new Queue<String>();
		_pathToDestinationGoal = null;
		_input = new ExplorerInput(name, new IInputCallback() {
			@Override
			public void inputReceived(String input){
				boolean validId = SharedUtil.isValidNodeId(input);
				println("Received destination goal: " + input + " [" + (validId ? "VALID" : "INVALID") + "]");
				if(validId){
					_goalInsertQueue.enqueue(input);
				}
			};
		});
	}

	@Override
	protected void planActions() {
		_actionNow = null;
		_actionNext = null;
		Action act = null;
		
		// Add the goals here to take care of race conditions.
		if(_goalInsertQueue.size() > 0){
			_destinationGoals.enqueue(_goalInsertQueue.dequeue());
		}
		
		//1. recharging
		act = planRecharge(1.0/3.0);
		if (act != null){
			_actionNow = act;
		}
		
//		// 2. buying battery with a certain probability
//		act = planBuyBattery();
//		if ( act != null ) return act;
		
		Node currentNode = _graph.getNode(_position);
		//println("Current node: " + currentNode);
		
		if(_position == null || currentNode == null){
			println("I do not know my position, I'll recharge");
			if(_actionNow == null){
				_actionNow = MarsUtil.rechargeAction();
			}else{
				_actionNext = MarsUtil.rechargeAction();
				return;
			}
		}
		
		// 3. probing if necessary (neighbour, probed, position)
		act = planProbe(currentNode);
		if (act != null){
			if(_actionNow == null){
				_actionNow = act;
			}else{
				_actionNext = act;
				return;
			}
		}
		
		// 4. surveying if necessary (visibleEdge,surveyEdge,position)
		act = planSurvey(currentNode);
		if (act != null){
			if(_actionNow == null){
				_actionNow = act;
			}else{
				_actionNext = act;
				return;
			}
		}
		
		Node moveToNode = null;
		
		if(_destinationGoals.size() > 0){
			println("Trying to reach goal: " + _destinationGoals.peek());
			if(_destinationGoals.peek().equals(currentNode.getId())){
				// Goal reached
				_pathToDestinationGoal = null;
				_destinationGoals.dequeue();
				_input.showGoalFound(currentNode.getId());
			}else{
				if(_pathToDestinationGoal == null){
					Node goalNode = _graph.getNode(_destinationGoals.peek());
					if(goalNode != null){
						// New goal
						// TODO: Make some heuristics that take into account that number of steps to reach the goal
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
		}
		
		if(moveToNode != null && !_graph.getAdjacentTo(currentNode).contains(moveToNode)){
			println("Trying to move to node i cant move to!");
			moveToNode = null;
		}
		
		if(moveToNode != null){
			if(_actionNow == null){
				println("On my way to my goal I will move from " + currentNode.getId() + " to " + moveToNode.getId());
				_actionNow = new GotoAction(moveToNode.getId(), _pathToDestinationGoal.size());

				Action posNextAct = planProbe(moveToNode);
				if(posNextAct != null){
					_actionNext = posNextAct;
					return;
				}else{
					posNextAct = planSurvey(moveToNode);
					if(posNextAct != null){
						_actionNext = posNextAct;
						return;
					}
				}
			}else{
				_actionNext = new GotoAction(moveToNode.getId(), _pathToDestinationGoal.size());
				return;
			}
		}
		
		if(allProbed){
			if(_destinationGoals.size() > 0 && _graph.getNode(_destinationGoals.peek()) == null){
				println("Invalid goal input, removing it!");
				_destinationGoals.dequeue();
			}
			println("All nodes probed, recharging while waiting for commands!");
			if(_actionNow == null){
				_actionNow = MarsUtil.rechargeAction();
			}else{
				_actionNext = MarsUtil.rechargeAction();
			}
			return;
		}
		
		_unprobedSearch = new BreadthFirstExplorerSearch(_graph);
		moveToNode = _unprobedSearch.findClosestUnexploredNodeUsingPredicate(currentNode, unprobedPredicate);
		
		if(moveToNode == null && _actionNow == null){
			// Only do this if it's for the current turn as we might discover new things next turn.
			println("No unprobed node found!");
			allProbed = true;
		}
		
		// 5. Find closest unprobed node
		act = planNextUnprobed(_position, moveToNode);
		if(act != null){
			if(_actionNow == null){
				_actionNow = act;
			}else{
				_actionNext = act;
				return;
			}
		}
		
		if(hasMaxEnergy()){
			if(_actionNow == null){
				_actionNow = MarsUtil.skipAction();
			}
		}else{
			if(_actionNow == null){
				_actionNow = MarsUtil.rechargeAction();
			}else{
				_actionNext = MarsUtil.rechargeAction();
				return;
			}
		}
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
					newKnowledge = true;
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
					newKnowledge = true;
				}
			}else if(intel.getName().equals("probedVertex")){
				if(params.length < 2) continue;
				String nodeId = params[0];
				int value = Integer.parseInt(params[1]);
				
				Node node = intel.isMessage() 
						? _graph.addNode(nodeId)
						: _graph.getNode(nodeId);
				
				if(node.getValue() != value){
					node.setValue(value);
					newKnowledge = true;
				}
			}else if(intel.getName().equals("surveyedEdge")){
				if(params.length < 3) continue;
				String node1 = params[0];
				String node2 = params[1];
				int weight = Integer.parseInt(params[2]);
				
				Edge edge = intel.isMessage() 
						? _graph.addEdgeFromNodeIds(node1, node2)
						: _graph.getEdgeFromNodeIds(node1, node2);
						
				if(edge.getWeight() != weight){
					edge.setWeight(weight);
					newKnowledge = true;
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
				if(intel.isPercept()){
					if(_position == null || !_position.equals(params[0])){
						_position = params[0];
						newKnowledge = true;
					}
					_graph.setAgentLocation(_name, _position);
				}else if(intel.isMessage()){
					_graph.setAgentLocation(intel.getSender(), params[0]);
				}
			}else if(intel.getName().equals("money")){
				if(params.length < 1) continue;
				_money = Integer.parseInt(params[0]);
			}else if(intel.getName().equals("achievement")){
				// TODO: Something here?
				String achievement = params[0];
				println("Got achievement: " + achievement);
			}
			
			if(newKnowledge && intel.isPercept() && validMessages.contains(intel.getName())){
				broadcastBelief(intel.toBelief());
			}
		}
	}
	
	private Action planProbe(Node node) {
		return !node.isProbed() && _actionSchedule.Proceed(SharedUtil.Actions.Probe, node.getId(), _internalUniqueId) 
				? new ProbeAction(node.getId()) : null;
	}
	
	private Action planSurvey(Node node) {
		if(!_actionSchedule.Proceed(SharedUtil.Actions.Survey, node.getId(), _internalUniqueId)) return null;
		
		HashSet<String> checkedNodes = new HashSet<String>();
		Queue<Node> toCheck = new Queue<Node>();
		toCheck.enqueue(node);
		
		// TODO: Maybe save a list of currently visible vertices and edges!
		
		for(Node n : _graph.getAdjacentTo(node)){
			if(checkedNodes.contains(n.getId())) continue;
			checkedNodes.add(n.getId());
			Edge e = _graph.getEdgeFromNodes(node, n);
			if(!e.isSurveyed()) return new SurveyAction(node.getId());
			
			for(Node n2 : _graph.getAdjacentTo(n)){
				Edge e2 = _graph.getEdgeFromNodes(n, n2);
				if(!e2.isSurveyed()) return new SurveyAction(node.getId());
			}
		}
		
		return null;
	}
	
	private Action planNextUnprobed(String position, Node firstUnprobed) {
		println("Trying to find unprobed node from position: " + position);
		if(firstUnprobed != null){
			Stack<Node> pathToUnprobed = _unprobedSearch.pathTo(firstUnprobed);
			if(pathToUnprobed.size() > 1){
				if(pathToUnprobed.peek().getId().equals(position)){
					pathToUnprobed.pop();
				}
				println("Found unprobed node (" + pathToUnprobed.size() + " steps), can reach it by movin from " + position + " to " + pathToUnprobed.peek().getId());
				return new GotoAndProbeAction(pathToUnprobed.peek().getId(), pathToUnprobed.size());
			}
		}
		
		println("Couldn't find an unprobed node");
		return null;
	}
}
