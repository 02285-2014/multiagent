package custommas.agents.actions;

import custommas.common.SharedUtil;
import custommas.agents.CustomAgent;
import eis.iilang.Action;

@SuppressWarnings("serial")
public class RepairAction extends Action {
	private CustomAgent _agent;

	public RepairAction(CustomAgent agent) {
		super(SharedUtil.Actions.Repair);
		_agent = agent;
	}
	
	public CustomAgent getAgent(){
		return _agent;
	}
}
