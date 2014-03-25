package custommas.agents;

import java.util.List;

import custommas.common.TeamIntel;
import eis.iilang.Action;
import massim.javaagents.agents.MarsUtil;

public class SkippyAgent extends CustomAgent {
	public SkippyAgent(String name, String team) {
		super(name, team);
	}
	
	@Override
	public Action step(){
		return MarsUtil.skipAction();
	}
	
	@Override
	protected void planActions(){
		_actionNow = MarsUtil.skipAction();
		_actionNext = null;
	}

	@Override
	protected void handleIntel(List<TeamIntel> intelList) {
		return;
	}
}
