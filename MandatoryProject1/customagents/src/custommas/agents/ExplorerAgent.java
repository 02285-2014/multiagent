package custommas.agents;

import custommas.agents.actions.GotoAndProbeAction;
import custommas.agents.actions.ProbeAction;
import custommas.common.PlanningCenter;
import custommas.common.SharedKnowledge;
import custommas.common.SharedUtil;
import custommas.lib.Node;
import custommas.lib.Stack;
import custommas.lib.algo.BreadthFirstExplorerSearch;
import custommas.lib.interfaces.INodePredicate;

import eis.iilang.Action;
import massim.javaagents.agents.MarsUtil;

// Andreas (s092638)

public class ExplorerAgent extends CustomAgent{
	private BreadthFirstExplorerSearch _unprobedSearch;
	
	private INodePredicate unprobedPredicate = new INodePredicate(){
		public boolean evaluate(Node node, int comparableValue) {
			return !node.isProbed() 
					&& PlanningCenter.proceed(SharedUtil.Actions.Custom.GoToAndProbe, node.getId(), comparableValue)
					&& PlanningCenter.proceed(SharedUtil.Actions.Probe, node.getId(), 1);
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
			println("I DO NOT KNOW WHERE I AM!!!");
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
			act = planSurvey(currentNode, _visibilityRange * 3);
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
		} else {
			if(_actionRound == 3){
				if(SharedKnowledge.getMaxSumComponent() != null && SharedKnowledge.getMaxSumComponent().getNodes().contains(currentNode)){
					_unprobedSearch = new BreadthFirstExplorerSearch(SharedKnowledge.getMaxSumComponent().getGraph());
					Node moveToNode = _unprobedSearch.findClosestUnexploredNodeUsingPredicate(currentNode, unprobedPredicate);
				
					if(moveToNode == null){
						_actionRound = 4;
					}else{
						act = planNextUnprobed(_position, moveToNode);
						if(act != null){
							_actionNow = act;
							return;
						}else{
							_actionRound = 4;
						}
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
		if(firstUnprobed != null){
			Stack<Node> pathToUnprobed = _unprobedSearch.pathTo(firstUnprobed);
			if(pathToUnprobed.size() > 1){
				if(pathToUnprobed.peek().getId().equals(position)){
					pathToUnprobed.pop();
				}
				return new GotoAndProbeAction(
						pathToUnprobed.peek().getId(), 
						firstUnprobed.getId(), 
						_graph.getEdgeFromNodeIds(position, pathToUnprobed.peek().getId()).getWeight(), 
						pathToUnprobed.size());
			}
		}
		
		return null;
	}
}
