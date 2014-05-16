package custommas.agents.actions;

//Andreas (s092638)

@SuppressWarnings("serial")
public class GotoAndProbeAction extends GotoAction {
	private String _goalNodeId;
	
	public GotoAndProbeAction(String nextNodeId, String goalNodeId, int steps) {
		super(nextNodeId, steps);
		_goalNodeId = goalNodeId;
	}		
	
	public String getGoalNodeId(){
		return _goalNodeId;
	}
}
