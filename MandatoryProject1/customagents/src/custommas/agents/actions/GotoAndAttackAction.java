package custommas.agents.actions;

@SuppressWarnings("serial")
public class GotoAndAttackAction extends GotoAction {
	private String _goalNodeId;
	private int _steps;
	
	public GotoAndAttackAction(String nextNodeId, String goalNodeId, int weight, int steps) {
		super(nextNodeId, weight);
		_goalNodeId = goalNodeId;
		_steps = steps;
	}		
	
	public String getGoalNodeId(){
		return _goalNodeId;
	}

	public int getSteps(){
		return _steps;
	}
}
