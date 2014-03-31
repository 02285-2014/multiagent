package custommas.agents.actions;

import custommas.common.SharedUtil;
import eis.iilang.Action;
import eis.iilang.Identifier;

@SuppressWarnings("serial")
public class GotoAction  extends Action {
	private String _nodeId;
	private int _steps;
	
	public GotoAction(String nodeId, int steps) {
		super(SharedUtil.Actions.GoTo, new Identifier(nodeId));
		_nodeId = nodeId;
	}
	
	public String getNodeId(){
		return _nodeId;
	}
	
	public int getSteps(){
		return _steps;
	}
}
