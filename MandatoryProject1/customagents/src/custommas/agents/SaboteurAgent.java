package custommas.agents;

import java.util.Set;

import custommas.common.SharedKnowledge;
import custommas.common.SharedUtil;
import custommas.lib.Node;
import eis.iilang.Action;
import massim.javaagents.agents.MarsUtil;

public class SaboteurAgent extends CustomAgent {
	public SaboteurAgent(String name, String team) {
		super(name, team);
		_role = SharedUtil.Agents.Saboteur;
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
		
		act = planGoToGoal(currentNode);
		if(act != null){
			_actionNow = act;
			return;
		}
		
		Set<OpponentAgent> nearbyOpponents = super.nearbyOpponents(currentNode, 1);
		if(nearbyOpponents.size() > 0){
			_actionNow = MarsUtil.attackAction(SharedUtil.any(nearbyOpponents).getName());
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
}
