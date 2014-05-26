package custommas.agents;

import custommas.agents.actions.GotoAndProbeAction;
import custommas.agents.actions.ProbeAction;
import custommas.common.DistressCenter;
import custommas.common.PlanningCenter;
import custommas.common.SharedKnowledge;
import custommas.common.SharedUtil;
import custommas.lib.Node;
import custommas.lib.Stack;
import custommas.lib.algo.BreadthFirstSearch;
import custommas.lib.interfaces.INodePredicate;

import eis.iilang.Action;
import massim.javaagents.agents.MarsUtil;

// Andreas (s092638)

public class ExplorerAgent extends CustomAgent{	
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
			
			if(!isDisabled()){
				act = planRecharge(1.0/3.0);
				if (act != null){
					_actionNow = act;
					return;
				}
			}else{
				DistressCenter.requestHelp(this);
			}
		}
		
		Node currentNode = _graph.getNode(_position);
		//println("Current node: " + currentNode);
		
		if(_position == null || currentNode == null){
			println("I DO NOT KNOW WHERE I AM!!!");
			_actionNow = !isDisabled() ? MarsUtil.rechargeAction() : MarsUtil.skipAction();
			return;
		}
		
		if(_actionRound == 1){
			_actionRound = 2;
			if(!isDisabled()){
				act = planProbe(currentNode);
				if (act != null){
					_actionNow = act;
					return;
				}
			}
		}
		
		act = planGoToGoal(currentNode);
		if(act != null){
			_actionNow = act;
			return;
		}
		
		if(_actionRound == 2){
			_actionRound = 3;
			if(!isDisabled()){
				act = planSurvey(currentNode, SharedKnowledge.zoneControlMode() ? 1 : _visibilityRange * 3);
				if (act != null){
					_actionNow = act;
					return;
				}
			}
		}
		
		if(!SharedKnowledge.zoneControlMode()){
			if(_actionRound == 3){
				BreadthFirstSearch unprobedSearch = new BreadthFirstSearch(_graph);
				Node moveToNode = unprobedSearch.findClosestNodeSatisfyingPredicate(currentNode, unprobedPredicate);
			
				if(moveToNode != null){
					act = planNextUnprobed(unprobedSearch, _position, moveToNode);
					if(act != null){
						_actionNow = act;
						return;
					}
				}
				_actionRound = 4;
			}
		} else {
			if(_actionRound == 3){
				if(SharedKnowledge.getMaxSumComponent() != null && SharedKnowledge.getMaxSumComponent().getNodes().contains(currentNode)){
					BreadthFirstSearch unprobedSearch = new BreadthFirstSearch(SharedKnowledge.getMaxSumComponent().getGraph());
					Node moveToNode = unprobedSearch.findClosestNodeSatisfyingPredicate(currentNode, unprobedPredicate);
				
					if(moveToNode != null){
						act = planNextUnprobed(unprobedSearch, _position, moveToNode);
						if(act != null){
							_actionNow = act;
							return;
						}
					}
				}
				_actionRound = 4;
			}
		}
		
		if(!isDisabled()){
			act = planRecharge();
		}
		
		_actionNow = act != null ? act : MarsUtil.skipAction();
	}
	
	private Action planProbe(Node node) {
		return !node.isProbed() && PlanningCenter.proceed(SharedUtil.Actions.Probe,  node.getId()) 
				? new ProbeAction(node.getId()) : null;
	}
	
	private Action planNextUnprobed(BreadthFirstSearch unprobedSearch, String position, Node firstUnprobed) {
		if(firstUnprobed != null){
			Stack<Node> pathToUnprobed = unprobedSearch.pathTo(firstUnprobed);
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
