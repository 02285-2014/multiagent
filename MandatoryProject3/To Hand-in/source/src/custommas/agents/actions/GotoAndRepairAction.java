package custommas.agents.actions;

// Peter (s113998)

import custommas.agents.CustomAgent;

@SuppressWarnings("serial")
public class GotoAndRepairAction extends GotoAction {
	private int _steps;
	private CustomAgent _agentToRepair;
	private String _goalNodeId;
	
	public GotoAndRepairAction(String nextNodeId, CustomAgent agentToRepair, String goalNodeId, int weight, int steps) {
		super(nextNodeId, weight);
		_steps = steps;
		_agentToRepair = agentToRepair;
		_goalNodeId = goalNodeId;
	}		

	public int getSteps(){
		return _steps;
	}
	
	public CustomAgent getAgentToRepair(){
		return _agentToRepair;
	}
	
	public String getGoalNodeId(){
		return _goalNodeId;
	}
	
}
