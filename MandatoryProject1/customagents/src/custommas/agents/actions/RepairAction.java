package custommas.agents.actions;

import custommas.common.SharedUtil;
import custommas.agents.CustomAgent;
import eis.iilang.Action;
import eis.iilang.Identifier;

@SuppressWarnings("serial")
public class RepairAction extends Action {
	private CustomAgent _agent;

	public RepairAction(CustomAgent agent) {
		super(SharedUtil.Actions.Repair, new Identifier(agent.getName()));
		_agent = agent;
	}
	
	public CustomAgent getAgent(){
		return _agent;
	}
}
