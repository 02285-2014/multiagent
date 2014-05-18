package custommas.agents.actions;

@SuppressWarnings("serial")
public class GotoAndAttackAction extends GotoAction {
	private String _goalNodeId;
	private int _steps;
	
	public GotoAndAttackAction(String nextNodeId, String goalNodeId, int steps) {
		super(nextNodeId, steps);
		_goalNodeId = goalNodeId;
	}		
	
	public String getGoalNodeId(){
		return _goalNodeId;
	}

	public int getSteps(){
		return _steps;
	}
}
