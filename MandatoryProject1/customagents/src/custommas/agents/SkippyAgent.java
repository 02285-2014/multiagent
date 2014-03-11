package custommas.agents;

import eis.iilang.Action;
import massim.javaagents.agents.MarsUtil;

public class SkippyAgent extends CustomAgent {
	public SkippyAgent(String name, String team) {
		super(name, team);
	}

	@Override
	protected Action nextAction() {
		return MarsUtil.skipAction();
	}

	@Override
	protected void handlePercepts() {
		return;
	}
}
