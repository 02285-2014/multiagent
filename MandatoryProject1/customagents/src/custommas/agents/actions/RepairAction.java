package custommas.agents.actions;

//Peter (s113998)

import custommas.common.SharedUtil;
import custommas.agents.CustomAgent;
import eis.iilang.Action;
import eis.iilang.Identifier;

@SuppressWarnings("serial")
public class RepairAction extends Action {
	private CustomAgent _agentToRepair;

	public RepairAction(CustomAgent agentToRepair) {
		super(SharedUtil.Actions.Repair, new Identifier(agentToRepair.getName()));
		_agentToRepair = agentToRepair;
	}
	
	public CustomAgent getAgentToRepair(){
		return _agentToRepair;
	}
}
