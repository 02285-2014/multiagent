package custommas.agents;

import java.util.HashSet;
import java.util.List;

import custommas.agents.actions.GotoAction;
import custommas.agents.actions.GotoAndProbeAction;
import custommas.agents.actions.ProbeAction;
import custommas.agents.actions.SurveyAction;
import custommas.common.MessageCenter;
import custommas.common.PlanningCenter;
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
	
	private INodePredicate unprobedPredicate = new INodePredicate(){
		public boolean evaluate(Node node, int comparableValue) {
			return !node.isProbed() && !_graph.isNodeOccupied(node.getId()) 
					&& PlanningCenter.proceed(SharedUtil.Actions.Custom.GoToAndProbe, node.getId(), comparableValue)
					&& PlanningCenter.proceed(SharedUtil.Actions.Probe, node.getId());
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
				gotoNode(input);
			};
		});
	}
	
	public void gotoNode(String nodeId){
		boolean validId = SharedUtil.isValidNodeId(nodeId);
		println("Received destination goal: " + nodeId + " [" + (validId ? "VALID" : "INVALID") + "]");
		if(validId){
			_goalInsertQueue.enqueue(nodeId);
		}
	}

	@Override
	protected void planAction() {
		Action act = null;
		
		if(_actionRound < 1){
			_actionRound = 1;
			
			if(_goalInsertQueue.size() > 0){
				_destinationGoals.enqueue(_goalInsertQueue.dequeue());
			}
			
			act = planRecharge(1.0/3.0);
			if (act != null){
				_actionNow = act;
				return;
			}
		}
		
		Node currentNode = _graph.getNode(_position);
		//println("Current node: " + currentNode);
		
		if(_position == null || currentNode == null){
			println("I do not know my position, I'll recharge");
			_actionNow = MarsUtil.rechargeAction();
			return;
		}
		
		if(_actionRound == 1){
			_actionRound = 2;
			act = planProbe(currentNode);
			if (act != null){
				_actionNow = act;
				return;
			}
		}
		
		if(_actionRound == 2){
			_actionRound = 3;
			act = planSurvey(currentNode);
			if (act != null){
				_actionNow = act;
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
			println("On my way to my goal I will move from " + currentNode.getId() + " to " + moveToNode.getId());
			_actionNow = new GotoAction(moveToNode.getId(), _pathToDestinationGoal.size());
			return;
		}
		
		if(_graph.allNodesProbed()){
			if(_destinationGoals.size() > 0 && _graph.getNode(_destinationGoals.peek()) == null){
				println("Invalid goal input, removing it!");
				_destinationGoals.dequeue();
			}
			println("All nodes probed, recharging while waiting for commands!");
			_actionNow = MarsUtil.rechargeAction();
			return;
		}
		
		if(_actionRound == 3){
			_unprobedSearch = new BreadthFirstExplorerSearch(_graph);
			moveToNode = _unprobedSearch.findClosestUnexploredNodeUsingPredicate(currentNode, unprobedPredicate);
		
			if(moveToNode == null){
				println("No unprobed node found!");
				_actionRound = 4;
			}else{
				act = planNextUnprobed(_position, moveToNode);
				if(act != null){
					_actionNow = act;
					return;
				}else{
					println("Couldn't find path to unprobed node!");
					_actionRound = 4;
				}
			}
		}
		
		if(_actionRound == 4){
			_actionRound = 5;
			if(!hasMaxEnergy()){
				_actionNow = MarsUtil.rechargeAction();
				return;
			}
		}
		
		_actionNow = MarsUtil.skipAction();
	}
	
	private Action planProbe(Node node) {
		return !node.isProbed() && PlanningCenter.proceed(SharedUtil.Actions.Probe,  node.getId()) 
				? new ProbeAction(node.getId()) : null;
	}
	
	private Action planSurvey(Node node) {
		if(!PlanningCenter.proceed(SharedUtil.Actions.Survey, node.getId())) return null;
		
		HashSet<String> checkedNodes = new HashSet<String>();
		Queue<Node> toCheck = new Queue<Node>();
		toCheck.enqueue(node);
		
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
				return new GotoAndProbeAction(pathToUnprobed.peek().getId(), firstUnprobed.getId(), pathToUnprobed.size());
			}
		}
		
		println("Couldn't find an unprobed node");
		return null;
	}
}
