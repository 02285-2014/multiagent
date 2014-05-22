package custommas.agents;

//Andreas (s092638)
//Peter (s113998)

import java.util.Collection;

import custommas.agents.actions.RepairAction;
import custommas.agents.actions.GotoAndRepairAction;
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
		
		if (!SharedKnowledge.zoneControlMode() &&
				this.getHealth()/this.getMaxHealth() < DistressCenter.DistressThreshold){
			DistressCenter.requestHelp(this);
			
			if(DistressCenter.AgentGettingHelp(this)){
				String gettingHelpAt = PlanningCenter.helpAt(this);
				if(gettingHelpAt != null){
					gotoNode(gettingHelpAt);
				}
			} else {
				if (!SharedKnowledge.zoneControlMode() &&
						this.getHealth()/this.getMaxHealth() < DistressCenter.DistressThresholdLow){
					gotoNode(DistressCenter.findNearestNextHelp());
				}
			}
		}

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
						if(act != null){
							DistressCenter.respondToHelp(((RepairAction)act).getAgentToRepair());
							_actionNow = act;
							return;
						}
					}else{
						act = planDistressHelp(distressedSearch, _position, moveToNode, agentToRepair);
						if(act != null){
							_actionNow = act;
							return;
						}
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
		Node nodeHelpAt = distressedAgentNode;
		if(distressedAgentNode != null){
			Stack<Node> pathDistressed = distressedSearch.pathTo(distressedAgentNode);
			if(pathDistressed.size() > 1){
				if(pathDistressed.peek().getId().equals(position)){
					pathDistressed.pop();
				}
				Node goal = pathDistressed.peek();
				if (!SharedKnowledge.zoneControlMode()){
					int stepsToHalfWay;
					nodeHelpAt = pathDistressed.peek();
					if(pathDistressed.size() % 2 == 0){
						stepsToHalfWay = pathDistressed.size()/2;
					}else{
						stepsToHalfWay = (pathDistressed.size()+1)/2;					
					}
					for(int step=1; step<stepsToHalfWay; step++){
						nodeHelpAt = pathDistressed.pop();
					}
				} else {
					nodeHelpAt = distressedAgentNode;
				}
				Action act = new GotoAndRepairAction(
						goal.getId(), 
						agentToRepair, 
						nodeHelpAt.getId(),
						_graph.getEdgeFromNodeIds(position, goal.getId()).getWeight(),  // weight for whole path
						pathDistressed.size());
				if(act != null){
					DistressCenter.respondToHelp(((RepairAction)act).getAgentToRepair());
					return act;
				}
			}
		}
		
		return null;
	}
}
