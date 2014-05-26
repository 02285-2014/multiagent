package custommas.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import custommas.agents.CustomAgent;
import custommas.agents.actions.*;
import custommas.lib.Queue;
import eis.iilang.Action;

//Andreas (s092638)
//Morten (s133304)
//Peter (s113998)

public class PlanningCenter {
	private static Map<String, AgentAction> _probePlan = new HashMap<String, AgentAction>();
	private static Map<String, AgentAction> _surveyPlan = new HashMap<String, AgentAction>();
	private static Map<String, AgentAction> _goToAndProbePlan = new HashMap<String, AgentAction>();
	private static Map<String, AgentAction> _inspectPlan = new HashMap<String, AgentAction>();
	private static Map<String, AgentAction> _repairPlan = new HashMap<String, AgentAction>();
	private static Map<String, AgentAction> _goToAndRepairPlan = new HashMap<String, AgentAction>();
	private static Map<String, AgentAction> _attackPlan = new HashMap<String, AgentAction>();
	private static Map<String, AgentAction> _goToAndAttackPlan = new HashMap<String, AgentAction>();
	
	private static final HashMap<String, Map<String, AgentAction>> _actionToPlan;
	static{
		_actionToPlan = new HashMap<String, Map<String, AgentAction>>();
		_actionToPlan.put(SharedUtil.Actions.Probe, _probePlan);
		_actionToPlan.put(SharedUtil.Actions.Survey, _surveyPlan);
		_actionToPlan.put(SharedUtil.Actions.Custom.GoToAndProbe, _goToAndProbePlan);
		_actionToPlan.put(SharedUtil.Actions.Inspect, _inspectPlan);
		_actionToPlan.put(SharedUtil.Actions.Repair, _repairPlan);
		_actionToPlan.put(SharedUtil.Actions.Custom.GoToAndRepair, _goToAndRepairPlan);
		_actionToPlan.put(SharedUtil.Actions.Attack, _attackPlan);
		_actionToPlan.put(SharedUtil.Actions.Custom.GoToAndAttack, _goToAndAttackPlan);
	}
	
	private static final HashSet<String> bestOnlyActions = SharedUtil.newHashSetFromArray(new String[] {
		ProbeAction.class.getSimpleName(), SurveyAction.class.getSimpleName(),
		GotoAndProbeAction.class.getSimpleName(), InspectAction.class.getSimpleName(),
		RepairAction.class.getSimpleName(), GotoAndRepairAction.class.getSimpleName(),
		AttackAction.class.getSimpleName(), GotoAndAttackAction.class.getSimpleName()
	});
	
	private static Queue<CustomAgent> _replanRequired = new Queue<CustomAgent>();
	
	private static int _stepCounter = 0;
	private static long _stepTime = System.currentTimeMillis();
	
	public static int getStep(){
		return _stepCounter;
	}
	
	public static int newStep(int step){
		if(_stepCounter < step){
			System.out.println("Planning Center: New step, clearing old plans!");
			_stepCounter = step;
			_stepTime = System.currentTimeMillis();
			_replanRequired = new Queue<CustomAgent>();
			for(Map<String, AgentAction> plan : _actionToPlan.values()){
				plan.clear();
			}
		}
		return _stepCounter;
	}
	
	public static void planAction(CustomAgent agent, Action action){
		if(agent == null || action == null){
			System.out.println("[NULL_ACTION]");
			return;
		}
		
		if(!bestOnlyActions.contains(action.getClass().getSimpleName())){
			return;
		}
		
		CustomAgent agentToPing = null;
		if(action instanceof ProbeAction){
			ProbeAction probeAction = (ProbeAction)action;
			if(_probePlan.containsKey(probeAction.getNodeId())){
				agentToPing = agent;
			}else{
				AgentAction aa = new AgentAction();
				aa.agent = agent;
				aa.action = probeAction.getName();
				aa.target = probeAction.getNodeId();
				aa.weight = 0;
				_probePlan.put(probeAction.getNodeId(), aa);
				
				if(_goToAndProbePlan.containsKey(probeAction.getNodeId())){
					AgentAction gapAction = _goToAndProbePlan.remove(probeAction.getNodeId());
					agentToPing = gapAction.agent;
				}
			}

		}else if(action instanceof SurveyAction){
			SurveyAction surveyAction = (SurveyAction)action;
			if(_surveyPlan.containsKey(surveyAction.getNodeId())){
				agentToPing = agent;
			}else{
				AgentAction aa = new AgentAction();
				aa.agent = agent;
				aa.action = surveyAction.getName();
				aa.target = surveyAction.getNodeId();
				aa.weight = 0;
				_surveyPlan.put(surveyAction.getNodeId(), aa);
			}
			
		}else if(action instanceof GotoAndProbeAction){
			GotoAndProbeAction gapAction = (GotoAndProbeAction)action;
			if(_probePlan.containsKey(gapAction.getGoalNodeId())){
				agentToPing = agent;
			}else if(_goToAndProbePlan.containsKey(gapAction.getGoalNodeId())){
				AgentAction aa = _goToAndProbePlan.get(gapAction.getGoalNodeId());
				if(gapAction.getSteps() < aa.weight){
					agentToPing = aa.agent;
				}else{
					agentToPing = agent;
				}
			}
			if(agentToPing != agent){
				AgentAction aa = new AgentAction();
				aa.agent = agent;
				aa.action = gapAction.getName();
				aa.target = gapAction.getGoalNodeId();
				aa.weight = gapAction.getSteps();
				_goToAndProbePlan.put(gapAction.getGoalNodeId(), aa);
			}
			
		}else if(action instanceof InspectAction){
			InspectAction inspectAction = (InspectAction)action;
			if(_inspectPlan.containsKey(inspectAction.getTarget())){
				AgentAction aa = _inspectPlan.get(inspectAction.getTarget());
				if(!inspectAction.isRanged() && aa.weight > 0){
					agentToPing = aa.agent;
				}else{
					agentToPing = agent;
				}
			}
			
			if(agentToPing != agent){
				AgentAction aa = new AgentAction();
				aa.agent = agent;
				aa.action = inspectAction.getName();
				aa.target = inspectAction.getTarget();
				aa.weight = inspectAction.isRanged() ? 1 : 0;
				_inspectPlan.put(inspectAction.getTarget(), aa);
			}
		
		}else if(action instanceof RepairAction){
			RepairAction repairAction = (RepairAction)action;
			if(_repairPlan.containsKey(repairAction.getAgentToRepair().getName())){
				agentToPing = agent;
			}else{
				AgentAction aa = new AgentAction();
				aa.agent = agent;
				aa.action = repairAction.getName();
				aa.target = repairAction.getAgentToRepair().getName();
				aa.weight = 0;
				_repairPlan.put(repairAction.getAgentToRepair().getName(), aa);
			}

		}else if(action instanceof GotoAndRepairAction){
			GotoAndRepairAction garAction = (GotoAndRepairAction)action;
			if(_repairPlan.containsKey(garAction.getAgentToRepair().getName())){
				agentToPing = agent;
			}else if(_goToAndRepairPlan.containsKey(garAction.getAgentToRepair().getName())){
				AgentAction aa = _goToAndRepairPlan.get(garAction.getAgentToRepair().getName());
				if(garAction.getSteps() < aa.weight){
					agentToPing = aa.agent;
				}else{
					agentToPing = agent;
				}
			}
			if(agentToPing != agent){
				AgentAction aa = new AgentAction();
				aa.agent = agent;
				aa.action = garAction.getName();
				aa.target = garAction.getAgentToRepair().getName();
				aa.weight = garAction.getSteps();
				_goToAndRepairPlan.put(garAction.getAgentToRepair().getName(), aa);
			}

		}else if(action instanceof AttackAction){
			AttackAction attackAction = (AttackAction)action;
			if(_attackPlan.containsKey(attackAction.getAgentToAttack())){
				agentToPing = agent;
			}else{
				AgentAction aa = new AgentAction();
				aa.agent = agent;
				aa.action = attackAction.getName();
				aa.target = attackAction.getAgentToAttack().getName();
				aa.weight = 0;
				_attackPlan.put(attackAction.getAgentToAttack().getName(), aa);
			}

		}else if(action instanceof GotoAndAttackAction){
			GotoAndAttackAction gaaAction = (GotoAndAttackAction)action;
			if(_attackPlan.containsKey(gaaAction.getNodeId())){
				agentToPing = agent;
			}else if(_goToAndAttackPlan.containsKey(gaaAction.getGoalNodeId())){
				AgentAction aa = _goToAndAttackPlan.get(gaaAction.getGoalNodeId());
				if(gaaAction.getSteps() < aa.weight){
					agentToPing = aa.agent;
				}else{
					agentToPing = agent;
				}	
			}

			if(agentToPing != agent){
				AgentAction aa = new AgentAction();
				aa.agent = agent;
				aa.action = gaaAction.getName();
				aa.target = gaaAction.getNodeId();
				aa.weight = gaaAction.getSteps();
				_goToAndAttackPlan.put(gaaAction.getGoalNodeId(), aa);
			}

		}

	if(agentToPing != null){
			_replanRequired.enqueue(agentToPing);
		}
	}
	
	public static boolean proceed(String action, String target){
		return proceed(action, target, 0);
	}
	
	public static boolean proceed(String action, String target, int weight){
		Map<String, AgentAction> plan = _actionToPlan.get(action);
		if(plan == null) return true;
		
		AgentAction plannedAlready = plan.get(target);
		if(plannedAlready == null) return true;
		
		return weight < plannedAlready.weight;
	}
	
	public static int getReplanningAgentsCount(){
		return _replanRequired.size();
	}
	
	public static Queue<CustomAgent> getReplanningAgents(){
		Queue<CustomAgent> current = _replanRequired;
		_replanRequired = new Queue<CustomAgent>();
		return current;
	}
	
	public static long getPlanningTimeSpendInMillis(){
		return System.currentTimeMillis() - _stepTime;
	}
	
	private static class AgentAction {
		public CustomAgent agent;
		public String action;
		public String target;
		public int weight;
		
		public AgentAction(){}
		
		@Override
		public boolean equals(Object o){
			if(o == null || !(o instanceof AgentAction)) return false;
			AgentAction a = (AgentAction)o;
			return action.equals(a.action) && target.equals(a.target) && weight == a.weight;
		}
	}
}
