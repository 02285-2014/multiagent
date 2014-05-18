package custommas.agents.actions;

import custommas.agents.CustomAgent;

@SuppressWarnings("serial")
public class GotoAndRepairAction extends GotoAction {
	private int _steps;
	private CustomAgent _agent;
	
	public GotoAndRepairAction(String nextNodeId, CustomAgent agent, int weight, int steps) {
		super(nextNodeId, weight);
		_steps = steps;
		_agent = agent;
	}		

	public int getSteps(){
		return _steps;
	}
	
	public CustomAgent getAgent(){
		return _agent;
	}
}
