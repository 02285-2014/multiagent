package custommas.agents.actions;

import custommas.common.SharedUtil;
import custommas.agents.OpponentAgent;
import eis.iilang.Action;
import eis.iilang.Identifier;

@SuppressWarnings("serial")
public class AttackAction extends Action {
	private OpponentAgent _agent;

	public AttackAction(OpponentAgent agent) {
		super(SharedUtil.Actions.Attack, new Identifier(agent.getName()));
		_agent = agent;
	}
	
	public OpponentAgent getAgent(){
		return _agent;
	}
}
