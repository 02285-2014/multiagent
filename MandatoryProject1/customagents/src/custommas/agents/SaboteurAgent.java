package custommas.agents;

import custommas.common.SharedUtil;
import massim.javaagents.agents.MarsUtil;

public class SaboteurAgent extends CustomAgent {
	public SaboteurAgent(String name, String team) {
		super(name, team);
		_role = SharedUtil.Agents.Saboteur;
	}

	@Override
	protected void planAction() {
		_actionNow = MarsUtil.parryAction();
	}
}
