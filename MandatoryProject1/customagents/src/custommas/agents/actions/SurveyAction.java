package custommas.agents.actions;

import custommas.common.SharedUtil;
import eis.iilang.Action;

@SuppressWarnings("serial")
public class SurveyAction  extends Action {
	private String _nodeId;
	
	public SurveyAction(String nodeId) {
		super(SharedUtil.Actions.Survey);
		_nodeId = nodeId;
	}
	
	public String getNodeId(){
		return _nodeId;
	}
}
