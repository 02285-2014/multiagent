package custommas.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import custommas.agents.CustomAgent;
import custommas.agents.actions.GotoAndProbeAction;
import custommas.agents.actions.ProbeAction;
import custommas.agents.actions.SurveyAction;
import custommas.lib.Queue;
import eis.iilang.Action;

public class PlanningCenter {
	private static Map<String, AgentAction> _probePlan = new HashMap<String, AgentAction>();
	private static Map<String, AgentAction> _surveyPlan = new HashMap<String, AgentAction>();
	private static Map<String, AgentAction> _goToAndProbePlan = new HashMap<String, AgentAction>();
	
	private static final HashMap<String, Map<String, AgentAction>> _actionToPlan;
	static{
		_actionToPlan = new HashMap<String, Map<String, AgentAction>>();
		_actionToPlan.put(SharedUtil.Actions.Probe, _probePlan);
		_actionToPlan.put(SharedUtil.Actions.Survey, _surveyPlan);
		_actionToPlan.put(SharedUtil.Actions.Custom.GoToAndProbe, _goToAndProbePlan);
	}
	
	private static final HashSet<String> bestOnlyActions = SharedUtil.newHashSetFromArray(new String[] {
		ProbeAction.class.getSimpleName(), SurveyAction.class.getSimpleName(),
		GotoAndProbeAction.class.getSimpleName()
	});
	
	public static Queue<CustomAgent> _replanRequired = new Queue<CustomAgent>();
	
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
			_probePlan.clear();
			_surveyPlan.clear();
			_goToAndProbePlan.clear();
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
				_surveyPlan.put(surveyAction.getNodeId(), aa);
			}
			
		}else if(action instanceof GotoAndProbeAction){
			GotoAndProbeAction gapAction = (GotoAndProbeAction)action;
			if(_probePlan.containsKey(gapAction.getNodeId())){
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
				aa.target = gapAction.getNodeId();
				aa.weight = gapAction.getSteps();
				_goToAndProbePlan.put(gapAction.getGoalNodeId(), aa);
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
