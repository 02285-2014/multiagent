package custommas.agents;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import custommas.agents.actions.GotoAction;
import custommas.common.DistressCenter;
import custommas.common.SharedKnowledge;
import custommas.common.SharedUtil;
import custommas.lib.Node;
import eis.iilang.Action;
import massim.javaagents.agents.MarsUtil;

public class SentinelAgent extends CustomAgent {
	private int _parryCount;
	
	public SentinelAgent(String name, String team) {
		super(name, team);
		_role = SharedUtil.Agents.Sentinel;
		_visibilityRange = 3;
		_parryCount = 0;
	}

	@Override
	protected void planAction() {
		Action act = null;
		
		if(!isDisabled()){
			act = planRecharge(1.0/4.0);
			if (act != null){
				_actionNow = act;
				return;
			}
		}else{
			DistressCenter.requestHelp(this);
		}
		
		Node currentNode = _graph.getNode(_position);
		if(_position == null || currentNode == null) {
			// I don't know my position
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
			
			// Remember if zone-controlling - just parry. Don't be a coward!
			// Otherwise run
			if(SharedKnowledge.zoneControlMode()) {
				if(nearbyOpponents.size() > 0){
					_actionNow = MarsUtil.parryAction();
					_parryCount++;
					return;
				}
				
				_actionNow = MarsUtil.rechargeAction();
				return;
			}
			
			if(_parryCount < 5) {
				if(currentNode.getOccupantsForTeam(SharedKnowledge.OpponentTeam).size() > 0) {
					// An opponent is on my node parry!
					_actionNow = MarsUtil.parryAction();
					_parryCount++;
					return;
				}
			}
			// Reset parrycount
			_parryCount = 0;
			
			if(nearbyOpponents.size() > 0){
				act = planRun(currentNode);
				if(act != null) {
					_actionNow = act;
				}
				_actionNow = MarsUtil.parryAction();
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
		
		if(!isDisabled()){
			act = planRecharge();
			if(act != null) {
				_actionNow = act;
				return;
			}
		}
		
		_actionNow = !isDisabled() ? MarsUtil.parryAction() : MarsUtil.skipAction();
	}
	
	private Action planRun(Node currentNode) {
		Collection<Node> possibleNodes = new HashSet<Node>(_graph.getAdjacentTo(currentNode));
		
		Collection<Node> adjacent = _graph.getAdjacentTo(currentNode);
		
		for(Node n : adjacent) {
			if(n.getOccupantsForTeam(SharedKnowledge.OpponentTeam).size() > 0) {
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
