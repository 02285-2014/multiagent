package custommas.agents;

import java.util.Set;

import custommas.common.DistressCenter;
import custommas.common.SharedKnowledge;
import custommas.common.SharedUtil;
import custommas.lib.Node;
import eis.iilang.Action;
import massim.javaagents.agents.MarsUtil;

public class SaboteurAgent extends CustomAgent {
	private String _lastAgentAttacked = null;
	private int _attackRepeatCurrent = 0;
	private static int _attackRepeatMax = 4;
	
	public SaboteurAgent(String name, String team) {
		super(name, team);
		_role = SharedUtil.Agents.Saboteur;
	}

	@Override
	protected void planAction() {
		Action act = null;
		
		if(isDisabled() || getHealthRatio() <= DistressCenter.DistressThreshold){
			DistressCenter.requestHelp(this);
		}
		
		act = planRecharge(3);
		if (act != null){
			_actionNow = act;
			return;
		}
		
		Node currentNode = _graph.getNode(_position);
		
		if(_position == null || currentNode == null){
			println("I DO NOT KNOW WHERE I AM!!!");
			_actionNow = !isDisabled() ? MarsUtil.parryAction() : MarsUtil.rechargeAction();
			return;
		}
		
		act = planGoToGoal(currentNode);
		if(act != null){
			_actionNow = act;
			return;
		}
		
		if(!isDisabled()){
			Set<OpponentAgent> nearbyOpponents = super.nearbyOpponents(currentNode, 1);
			while(nearbyOpponents.size() > 0){
				OpponentAgent toAttack = SharedUtil.any(nearbyOpponents);
				if(_lastAgentAttacked == null || (_lastAgentAttacked.equals(toAttack.getName()) && _attackRepeatCurrent <= _attackRepeatMax)){
					_lastAgentAttacked = toAttack.getName();
					_attackRepeatCurrent++;
					_actionNow = MarsUtil.attackAction(_lastAgentAttacked);
					return;
				}
				nearbyOpponents.remove(toAttack);
			}
			_lastAgentAttacked = null;
			_attackRepeatCurrent = 0;
		}
		
		if(!isDisabled()){
			act = planSurvey(currentNode, SharedKnowledge.zoneControlMode() ? 1 : _visibilityRange * 3);
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
