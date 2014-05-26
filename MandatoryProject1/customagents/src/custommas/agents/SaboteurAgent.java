package custommas.agents;

import java.util.Set;

import custommas.common.DistressCenter;
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
		
		if(!isDisabled()){
			act = planRecharge(3);
			if (act != null){
				_actionNow = act;
				return;
			}
		}else{
			DistressCenter.requestHelp(this);
		}
		
		Node currentNode = _graph.getNode(_position);
		
		if(_position == null || currentNode == null){
			println("I DO NOT KNOW WHERE I AM!!!");
			_actionNow = !isDisabled() ? MarsUtil.parryAction() : MarsUtil.skipAction();
			return;
		}
		
		act = planGoToGoal(currentNode);
		if(act != null){
			_actionNow = act;
			return;
		}
		
		if(!isDisabled()){
			Set<OpponentAgent> nearbyOpponents = super.nearbyOpponents(currentNode, 1);
			if(nearbyOpponents.size() > 0){
				_actionNow = MarsUtil.attackAction(SharedUtil.any(nearbyOpponents).getName());
				return;
			}
		}
		
		if(!isDisabled()){
			act = planSurvey(currentNode, _visibilityRange * 3);
			if (act != null){
				_actionNow = act;
				return;
			}
		}
		
		if(!SharedKnowledge.zoneControlMode()){
			act = planRandomWalk(currentNode);
			if(act != null){
				_actionNow = act;
				return;
			}
		}
		
		_actionNow = !isDisabled() ? MarsUtil.parryAction() : MarsUtil.skipAction();
	}
}
