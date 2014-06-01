package custommas.agents.actions;

import custommas.common.SharedUtil;
import eis.iilang.Action;
import eis.iilang.Identifier;

@SuppressWarnings("serial")
public class InspectAction extends Action {
	private String _nodeId;
	private String _agentToInspect;
	private boolean _ranged;
	
	public InspectAction(String nodeId) {
		super(SharedUtil.Actions.Inspect);
		_nodeId = nodeId;
		_ranged = false;
		_agentToInspect = "";
	}
	
	public InspectAction(String nodeId, String agentToInspect) {
		super(SharedUtil.Actions.Inspect, new Identifier(agentToInspect));
		_nodeId = nodeId;
		_ranged = true;
		_agentToInspect = agentToInspect;
	}
	
	public String getNodeId(){
		return _nodeId;
	}
	
	public String getAgentToInspect(){
		return _agentToInspect;
	}
	
	public boolean isRanged(){
		return _ranged;
	}
	
	public String getTarget(){
		return _agentToInspect.length() > 0 ? _agentToInspect : _nodeId;
	}
}
