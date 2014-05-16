package custommas.agents;

import java.util.Collection;
import java.util.List;

import custommas.agents.actions.GotoAction;
import custommas.agents.actions.InspectAction;
import custommas.agents.actions.ProbeAction;
import custommas.common.PlanningCenter;
import custommas.common.SharedKnowledge;
import custommas.common.SharedUtil;
import custommas.lib.Node;
import custommas.lib.Node.OccupyInfo;
import eis.iilang.Action;
import massim.javaagents.agents.MarsUtil;

public class InspectorAgent extends CustomAgent {
	
	public InspectorAgent(String name, String team) {
		super(name, team);
		_role = SharedUtil.Agents.Inspector;
	}

	@Override
	protected void planAction() {
		Action act = null;
		
		if(_actionRound < 1){
			_actionRound = 1;
			
			act = planRecharge(3);
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
		
		act = planGoToGoal(currentNode);
		if(act != null){
			println("Must go to goal node!");
			_actionNow = act;
			return;
		}
		
		if(_actionRound == 1){
			act = planInspect(currentNode);
			if(act != null){
				_actionNow = act;
				return;
			}
			_actionRound = 2;
		}
		
		if(_actionRound == 2){
			_actionRound = 3;
			act = planSurvey(currentNode, 2);
			if (act != null){
				_actionNow = act;
				return;
			}
		}
		
		if(!SharedKnowledge.zoneControlMode()){
			act = planRandomWalk(currentNode);
			if(act != null){
				println("Must random walk!");
				_actionNow = act;
				return;
			}
		}
		
		act = planRecharge();
		_actionNow = act != null ? act : MarsUtil.skipAction();
	}
	
	private Action planInspect(Node node){
		Collection<OccupyInfo> opponents = null;
		
		if(PlanningCenter.proceed(SharedUtil.Actions.Inspect, node.getId(), 0)){
			opponents = node.getOccupantsForTeam(SharedKnowledge.OpponentTeam);
			for(OccupyInfo info : opponents){
				if(!SharedKnowledge.getOpponentAgent(info.getAgentName()).inspected()){
					println("Inspecting node: " + node.getId() + ", " + info.getAgentName());
					return new InspectAction(node.getId());
				}
			}
		}
		
		for(Node n : _graph.getAdjacentTo(node)){
			if(!PlanningCenter.proceed(SharedUtil.Actions.Inspect, n.getId(), 1)) continue;
			
			opponents = n.getOccupantsForTeam(SharedKnowledge.OpponentTeam);
			for(OccupyInfo info : opponents){
				if(!SharedKnowledge.getOpponentAgent(info.getAgentName()).inspected()){
					println("Inspecting agent: " + info.getAgentName());
					return new InspectAction(n.getId(), info.getAgentName());
				}
			}
		}
		
		return null;
	}
}
