package custommas.agents.actions;

import custommas.common.SharedUtil;
import eis.iilang.Action;
import eis.iilang.Identifier;

//Andreas (s092638)

@SuppressWarnings("serial")
public class GotoAction  extends Action {
	private String _nodeId;
	private int _weight;
	
	public GotoAction(String nodeId, int weight) {
		super(SharedUtil.Actions.GoTo, new Identifier(nodeId));
		_nodeId = nodeId;
		_weight = weight;
	}
	
	public String getNodeId(){
		return _nodeId;
	}
	
	public int getWeight(){
		return _weight;
	}
}
