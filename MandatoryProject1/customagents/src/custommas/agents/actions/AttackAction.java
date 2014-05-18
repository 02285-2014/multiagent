package custommas.agents.actions;

import custommas.common.SharedUtil;
import custommas.agents.CustomAgent;
import custommas.agents.OpponentAgent;
import eis.iilang.Action;

@SuppressWarnings("serial")
public class AttackAction extends Action {
	private OpponentAgent _agent;

	public AttackAction(OpponentAgent agent) {
		super(SharedUtil.Actions.Attack);
		_agent = agent;
	}
	
	public OpponentAgent getAgent(){
		return _agent;
	}
}
