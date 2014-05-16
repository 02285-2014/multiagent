package custommas.agents;

import java.util.Collection;
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
import custommas.lib.EdgeWeightedGraph;
import custommas.lib.Node;
import custommas.lib.Queue;
import custommas.lib.Stack;
import custommas.lib.algo.BreadthFirstExplorerSearch;
import custommas.lib.algo.Dijkstra;
import custommas.lib.interfaces.INodePredicate;

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
		
		Node currentNode = _graph.getNode(_position);
		if(_position == null || currentNode == null) {
			// I don't know my position
			act = planRecharge(1.0/4.0);
			if(act != null) {
				_actionNow = MarsUtil.rechargeAction();
				return;
			}
			_actionNow = MarsUtil.parryAction();
			return;
		}
		
		String otherTeam = _team == "A" ? "B" : "A";
		
		// Remember if zone-controlling - just parry. Don't be a coward!
		// Otherwise run
		if(SharedKnowledge.zoneControlMode()) {
			if(currentNode.getOccupantsForTeam(otherTeam).size() > 0) {
				// An opponent is on my node parry!
				_actionNow = MarsUtil.parryAction();
				_parryCount++;
				return;
			}
			
			for(Node n : _graph.getAdjacentTo(currentNode)) {
				if(n.getOccupantsForTeam(otherTeam).size() > 0) {
					// An opponent is close to me - parry
					_actionNow = MarsUtil.parryAction();
					return;
				}
			}
			
			_actionNow = MarsUtil.rechargeAction();
			return;
		}
		
		
		if(_parryCount < 5) {
			if(currentNode.getOccupantsForTeam(otherTeam).size() > 0) {
				// An opponent is on my node parry!
				_actionNow = MarsUtil.parryAction();
				_parryCount++;
				return;
			}
		}
		// Reset parrycount
		_parryCount = 0;
		
		act = planRecharge(1.0/4.0);
		if(act != null){
			_actionNow = act;
			return;
		}
		
		act = planGoToGoal(currentNode);
		if(act != null){
			_actionNow = act;
			return;
		}
		
		for(Node n : _graph.getAdjacentTo(currentNode)) {
			if(n.getOccupantsForTeam(otherTeam).size() > 0) {
				// An opponent is close to me - run away
				act = planRun(currentNode);
				if(act != null) {
					_actionNow = act;
				}
				_actionNow = MarsUtil.parryAction();
				return;
			}
		}	
		
		act = planRecharge(1.0/4.0);
		if(act != null) {
			_actionNow = act;
			return;
		}
		
		// PLAN SURVEY
		
		_actionNow = MarsUtil.parryAction();
	}
	
	private Action planRun(Node currentNode) {
		Collection<Node> possibleNodes = new HashSet<Node>(_graph.getAdjacentTo(currentNode));
		
		String otherTeam = _team == "A" ? "B" : "A";
		
		Collection<Node> adjacent = _graph.getAdjacentTo(currentNode);
		
		for(Node n : adjacent) {
			if(n.getOccupantsForTeam(otherTeam).size() > 0) {
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
