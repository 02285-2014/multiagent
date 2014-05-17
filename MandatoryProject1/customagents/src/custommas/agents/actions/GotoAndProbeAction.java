package custommas.agents.actions;

//Andreas (s092638)

@SuppressWarnings("serial")
public class GotoAndProbeAction extends GotoAction {
	private String _goalNodeId;
	private int _steps;
	
	public GotoAndProbeAction(String nextNodeId, String goalNodeId, int weight, int steps) {
		super(nextNodeId, weight);
		_goalNodeId = goalNodeId;
	}		
	
	public String getGoalNodeId(){
		return _goalNodeId;
	}
	
	public int getSteps(){
		return _steps;
	}
}
