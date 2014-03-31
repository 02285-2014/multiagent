package custommas.agents;

import java.util.List;

import custommas.common.PlanningCenter;
import custommas.common.TeamIntel;
import eis.iilang.Action;
import massim.javaagents.agents.MarsUtil;

public class SkippyAgent extends CustomAgent {
	public SkippyAgent(String name, String team) {
		super(name, team);
	}
	
	@Override
	public Action step(){
		_actionNow = MarsUtil.skipAction();
		PlanningCenter.planAction(this, _actionNow/*, PlanningCenter.getStep()*/);
		return _actionNow;
	}
	
	@Override
	protected void planAction(){
		_actionNow = MarsUtil.skipAction();
	}

	@Override
	protected void handleIntel(List<TeamIntel> intelList) {
		return;
	}
}
