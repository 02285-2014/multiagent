package custommas.agents;

import custommas.common.SharedUtil;
import massim.javaagents.agents.MarsUtil;

public class RepairerAgent extends CustomAgent {
	public RepairerAgent(String name, String team) {
		super(name, team);
		_role = SharedUtil.Agents.Repairer;
	}

	@Override
	protected void planAction() {
		_actionNow = MarsUtil.parryAction();
	}
}
