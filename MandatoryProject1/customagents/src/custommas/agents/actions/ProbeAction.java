package custommas.agents.actions;

import custommas.common.SharedUtil;
import eis.iilang.Action;

public class ProbeAction  extends Action {
	private String _nodeId;
	
	public ProbeAction(String nodeId) {
		super(SharedUtil.Actions.Probe);
		_nodeId = nodeId;
	}
	
	public String getNodeId(){
		return _nodeId;
	}
}
