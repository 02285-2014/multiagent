package custommas.agents;

import massim.javaagents.agents.MarsUtil;

public class RepairerAgent extends CustomAgent {
	public RepairerAgent(String name, String team) {
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
