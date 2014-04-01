package custommas.agents;

import massim.javaagents.agents.MarsUtil;

public class SaboteurAgent extends CustomAgent {
	public SaboteurAgent(String name, String team) {
		super(name, team);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void planAction() {
		_actionNow = MarsUtil.parryAction();
	}

	@Override
	public void gotoNode(String nodeId) {
		// TODO Auto-generated method stub
		
	}
}
