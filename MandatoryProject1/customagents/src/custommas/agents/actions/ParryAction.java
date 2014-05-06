package custommas.agents.actions;

import custommas.common.SharedUtil;
import eis.iilang.Action;

@SuppressWarnings("serial")
public class ParryAction extends Action {
	private String _nodeId;

	public ParryAction(String nodeId) {
		super(SharedUtil.Actions.Parry);
		_nodeId = nodeId;
	}
	
	public String getNodeId(){
		return _nodeId;
	}
}
