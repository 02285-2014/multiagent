package custommas.agents;

import java.util.Collection;

import custommas.agents.actions.GotoAndProbeAction;
import custommas.agents.actions.GotoAndRepairAction;
import custommas.agents.actions.RepairAction;
import custommas.common.DistressCenter;
import custommas.common.PlanningCenter;
import custommas.common.SharedKnowledge;
import custommas.common.SharedUtil;
import custommas.lib.Node;
import custommas.lib.Node.OccupyInfo;
import custommas.lib.Stack;
import custommas.lib.algo.BreadthFirstSearch;
import custommas.lib.interfaces.INodePredicate;
import eis.iilang.Action;
import massim.javaagents.agents.MarsUtil;

public class RepairerAgent extends CustomAgent {
	private INodePredicate distressedPredicate = new INodePredicate(){
		public boolean evaluate(Node node, int comparableValue) {
			if(!DistressCenter.getDistressedAgentPositions().contains(node.getId())) return false;
			
			Collection<OccupyInfo> agentsOnNode = node.getOccupantsForTeam(SharedKnowledge.OurTeam);
			if(agentsOnNode.size() < 1) return false;
			
			for(OccupyInfo agentOnNode : agentsOnNode){
				CustomAgent agent = SharedKnowledge.getCustomAgent(agentOnNode.getAgentName());
				if(agent == null || !DistressCenter.getDistressedAgents().contains(agent)) continue;
				
				if(PlanningCenter.proceed(SharedUtil.Actions.Custom.GoToAndRepair, agentOnNode.getAgentName(), comparableValue)
						&& PlanningCenter.proceed(SharedUtil.Actions.Repair, agentOnNode.getAgentName(), 1)){
					return true;
				}
			}
			
			return false;
		}
	};
	
	public RepairerAgent(String name, String team) {
		super(name, team);
		_role = SharedUtil.Agents.Repairer;
	}

	@Override
	protected void planAction() {
		Action act = null;
		
		act = planRecharge(3);
		if (act != null){
			_actionNow = act;
			return;
		}
		
		Node currentNode = _graph.getNode(_position);
		
		if(_position == null || currentNode == null){
			println("I DO NOT KNOW WHERE I AM!!!");
			_actionNow = MarsUtil.parryAction();
			return;
		}
		
		// Should have precedence as it might be necessary to replan
		// Run only if planning must be done a second time
		if(_actionRound > 0 && DistressCenter.getDistressedAgents().size() > 0){
			BreadthFirstSearch distressedSearch = new BreadthFirstSearch(_graph);
			Node moveToNode = distressedSearch.findClosestNodeSatisfyingPredicate(currentNode, distressedPredicate);
		
			if(moveToNode != null){
				CustomAgent agentToRepair = null;
				Collection<OccupyInfo> agentsOnNode = moveToNode.getOccupantsForTeam(SharedKnowledge.OurTeam);
				
				for(OccupyInfo agentOnNode : agentsOnNode){
					CustomAgent agent = SharedKnowledge.getCustomAgent(agentOnNode.getAgentName());
					if(agent == null || !DistressCenter.getDistressedAgents().contains(agent)) continue;
					if(!PlanningCenter.proceed(SharedUtil.Actions.Repair, agent.getName(), 1)) continue;
					
					agentToRepair = agent;
					break;
				}
				
				if(agentToRepair != null){
					if(moveToNode.getId().equals(_position)){
						act = new RepairAction(agentToRepair);
					}else{
						act = planDistressHelp(distressedSearch, _position, moveToNode, agentToRepair);
					}
					
					if(act != null){
						DistressCenter.respondToHelp(((RepairAction)act).getAgent());
						_actionNow = act;
						return;
					}
				}
			}
		}
		
		_actionRound = 1;
		
		act = planGoToGoal(currentNode);
		if(act != null){
			_actionNow = act;
			return;
		}
		
		act = planSurvey(currentNode, _visibilityRange * 3);
		if (act != null){
			_actionNow = act;
			return;
		}
		
		if(!SharedKnowledge.zoneControlMode()){
			act = planRandomWalk(currentNode);
			if(act != null){
				_actionNow = act;
				return;
			}
		}
		
		_actionNow = MarsUtil.parryAction();
	}
	
	private Action planDistressHelp(BreadthFirstSearch distressedSearch, String position, Node distressedAgentNode, CustomAgent agentToRepair) {
		if(distressedAgentNode != null){
			Stack<Node> pathDistressed = distressedSearch.pathTo(distressedAgentNode);
			if(pathDistressed.size() > 1){
				if(pathDistressed.peek().getId().equals(position)){
					pathDistressed.pop();
				}
				return new GotoAndRepairAction(
						pathDistressed.peek().getId(), 
						agentToRepair, 
						_graph.getEdgeFromNodeIds(position, pathDistressed.peek().getId()).getWeight(), 
						pathDistressed.size());
			}
		}
		
		return null;
	}
}
