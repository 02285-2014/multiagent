package custommas.agents;

import massim.javaagents.agents.MarsUtil;

public class InspectorAgent extends CustomAgent {
	public InspectorAgent(String name, String team) {
		super(name, team);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void planAction() {
		_actionNow = MarsUtil.skipAction();
	}

	@Override
	public void gotoNode(String nodeId) {
		// TODO Auto-generated method stub
		
	}
}
