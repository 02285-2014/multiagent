package custommas.agents.actions;

// Peter (s113998)
import custommas.common.SharedUtil;
import custommas.agents.OpponentAgent;
import eis.iilang.Action;
import eis.iilang.Identifier;

@SuppressWarnings("serial")
public class AttackAction extends Action {
	private OpponentAgent _agentToAttack;

	public AttackAction(OpponentAgent agentToAttack) {
		super(SharedUtil.Actions.Attack, new Identifier(agentToAttack.getName()));
		_agentToAttack = agentToAttack;
	}
	
	public OpponentAgent getAgentToAttack(){
		return _agentToAttack;
	}
}
