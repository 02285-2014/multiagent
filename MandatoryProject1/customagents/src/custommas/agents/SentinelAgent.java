package custommas.agents;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import custommas.agents.actions.GotoAction;
import custommas.agents.actions.GotoAndProbeAction;
import custommas.agents.actions.ProbeAction;
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
import custommas.lib.Stack;
import custommas.lib.algo.BreadthFirstExplorerSearch;
import custommas.lib.algo.Dijkstra;
import custommas.lib.interfaces.INodePredicate;
import custommas.ui.ExplorerInput;
import custommas.ui.IInputCallback;

import eis.iilang.Action;
import massim.javaagents.agents.MarsUtil;

public class SentinelAgent extends CustomAgent {
	private Queue<String> _goalQueue;
	private int _parryCount;
	private boolean _zoneControlling;
	private List<Node> _pathToDestinationGoal;
	
	public SentinelAgent(String name, String team) {
		super(name, team);
		_goalQueue = new Queue<String>();
		_parryCount = 0;
		_zoneControlling = false;
		_pathToDestinationGoal = null;
	}

	@Override
	protected void planAction() {
		Action act = null;
		
		Node currentNode = _graph.getNode(_position);
		if(_position == null || currentNode == null) {
			// I don't know my position
			act = planRecharge(1.0/4.0);
			if(act != null) {
				_actionNow = MarsUtil.rechargeAction();
				return;
			}
			_actionNow = MarsUtil.parryAction();
			return;
		}
		
		String otherTeam = _team == "A" ? "B" : "A";
		
		// Remember if zone-controlling - just parry. Don't be a coward!
		// Otherwise run
		if(_zoneControlling) {
			act = planRecharge(1.0/4.0);
			if(act != null) {
				_actionNow = act;
				return;
			}
			
			if(currentNode.getOccupantsForTeam(otherTeam).size() > 0) {
				// An opponent is on my node parry!
				_actionNow = MarsUtil.parryAction();
				_parryCount++;
				return;
			}
			
			for(Node n : _graph.getAdjacentTo(currentNode)) {
				if(n.getOccupantsForTeam(otherTeam).size() > 0) {
					// An opponent is close to me - parry
					_actionNow = MarsUtil.parryAction();
					return;
				}
			}
			
			_actionNow = MarsUtil.rechargeAction();
			return;
		}
		
		act = planSurvey(currentNode);
		if(act != null) {
			_actionNow = act;
			return;
		}
		
		if(_parryCount < 5) {
			if(currentNode.getOccupantsForTeam(otherTeam).size() > 0) {
				// An opponent is on my node parry!
				_actionNow = MarsUtil.rechargeAction();
				_parryCount++;
				return;
			}
		}
		// Reset parrycount
		_parryCount = 0;
		
		Node moveToNode = null;
		if(!_goalQueue.isEmpty()) {
			act = planRecharge(1.0/4.0);
			if(act != null) {
				_actionNow = act;
				return;
			}
			
			if(_goalQueue.peek().equals(currentNode.getId())){
				// Goal reached
				_pathToDestinationGoal = null;
				_goalQueue.dequeue();
			}else{
				if(_pathToDestinationGoal == null){
					Node goalNode = _graph.getNode(_goalQueue.peek());
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
		
		for(Node n : _graph.getAdjacentTo(currentNode)) {
			if(n.getOccupantsForTeam(otherTeam).size() > 0) {
				// An opponent is close to me - run away
				act = planRun(currentNode);
				if(act != null) {
					_actionNow = act;
				}
				_actionNow = MarsUtil.parryAction();
				return;
			}
		}	
		
		act = planRecharge(1.0/4.0);
		if(act != null) {
			_actionNow = act;
			return;
		}
		
		act = planSurvey(currentNode);
		if(act != null) {
			_actionNow = act;
			return;
		}
		
		
		
		_actionNow = MarsUtil.rechargeAction();
	}

	@Override
	public void gotoNode(String nodeId) {
		boolean isValid = SharedUtil.isValidNodeId(nodeId);
		if(isValid) {
			_goalQueue.enqueue(nodeId);
		}
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
	
	private Action planRun(Node currentNode) {
		Collection<Node> possibleNodes = new HashSet<Node>(_graph.getAdjacentTo(currentNode));
		
		String otherTeam = _team == "A" ? "B" : "A";
		
		Collection<Node> adjacent = _graph.getAdjacentTo(currentNode);
		
		for(Node n : adjacent) {
			if(n.getOccupantsForTeam(otherTeam).size() > 0) {
				possibleNodes.remove(n);
				
				for(Node adjn : _graph.getAdjacentTo(n)) {
					if(possibleNodes.contains(adjn)) {
						possibleNodes.remove(adjn);
					}
				}
				
			}
		}
		
		if(!possibleNodes.isEmpty()) {
			for(Node posNode : possibleNodes) {
				return new GotoAction(posNode.getId(), 1);
			}
		} else {
			return null;
		}
		return null;
	}
}
