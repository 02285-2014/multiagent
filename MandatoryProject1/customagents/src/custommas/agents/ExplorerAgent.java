package custommas.agents;

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
import custommas.lib.Node;
import custommas.lib.Queue;
import custommas.lib.Stack;
import custommas.lib.algo.BreadthFirstExplorerSearch;
import custommas.lib.algo.Dijkstra;
import custommas.lib.interfaces.INodePredicate;

import eis.iilang.Action;
import massim.javaagents.agents.MarsUtil;

// Andreas (s092638)

public class ExplorerAgent extends CustomAgent{
	private BreadthFirstExplorerSearch _unprobedSearch;
	private Queue<String> _destinationGoals;
	private Queue<String> _goalInsertQueue;
	private List<Node> _pathToDestinationGoal;
	
	private INodePredicate unprobedPredicate = new INodePredicate(){
		public boolean evaluate(Node node, int comparableValue) {
			return !node.isProbed() && !node.isOccupied() 
					&& PlanningCenter.proceed(SharedUtil.Actions.Custom.GoToAndProbe, node.getId(), comparableValue)
					&& PlanningCenter.proceed(SharedUtil.Actions.Probe, node.getId());
		}
	};
	
	public ExplorerAgent(String name, String team) {
		super(name, team);
		_role = SharedUtil.Agents.Explorer;
		_visibilityRange = 2;
	}
	
	@Override
	protected void planAction() {
		Action act = null;
		
		if(_actionRound < 1){
			_actionRound = 1;
			
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
			act = planSurvey(currentNode, 4);
			if (act != null){
				_actionNow = act;
				return;
			}
		}
		
		act = planGoToGoal(currentNode);
		if(act != null){
			_actionNow = act;
			return;
		}
		
		if(!SharedKnowledge.zoneControlMode()){
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
				Node moveToNode = _unprobedSearch.findClosestUnexploredNodeUsingPredicate(currentNode, unprobedPredicate);
			
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
		}
		
		act = planRecharge();
		_actionNow = act != null ? act : MarsUtil.skipAction();
	}
	
	private Action planProbe(Node node) {
		return !node.isProbed() && PlanningCenter.proceed(SharedUtil.Actions.Probe,  node.getId()) 
				? new ProbeAction(node.getId()) : null;
	}
	
	private Action planNextUnprobed(String position, Node firstUnprobed) {
		//println("Trying to find unprobed node from position: " + position);
		if(firstUnprobed != null){
			Stack<Node> pathToUnprobed = _unprobedSearch.pathTo(firstUnprobed);
			if(pathToUnprobed.size() > 1){
				if(pathToUnprobed.peek().getId().equals(position)){
					pathToUnprobed.pop();
				}
				//println("Found unprobed node (" + pathToUnprobed.size() + " steps), can reach it by movin from " + position + " to " + pathToUnprobed.peek().getId());
				return new GotoAndProbeAction(pathToUnprobed.peek().getId(), firstUnprobed.getId(), pathToUnprobed.size());
			}
		}
		
		//println("Couldn't find an unprobed node");
		return null;
	}
}
