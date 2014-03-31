package custommas.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import custommas.agents.CustomAgent;
import custommas.agents.actions.GotoAndProbeAction;
import custommas.agents.actions.ProbeAction;
import custommas.agents.actions.SurveyAction;
import custommas.lib.Queue;
import eis.iilang.Action;

import massim.javaagents.Agent;

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
	
	private static int _planningAgents = 0;
	private static AtomicInteger _actionsConfirmed = new AtomicInteger(0);
	
	private static Object _stepSyncLock = new Object();
	private static int _stepCounter = 0;
	private static long _stepTime = System.currentTimeMillis();
	
	public static int getStep(){
		return _stepCounter;
	}
	
	/* 
	 * Try to to make it sequential instead!
	 * Have a queue of agents conflicting/to run again. 
	 */
	
	public static int newStep(int step, int amountOfAgents){
		synchronized(_stepSyncLock){
			_planningAgents = amountOfAgents;
			if(_stepCounter < step){
				System.out.println("Planning Center: New step, clearing old plans!");
				_stepCounter = step;
				_stepTime = System.currentTimeMillis();
				_actionsConfirmed.set(0);
				_probePlan.clear();
				_surveyPlan.clear();
				_goToAndProbePlan.clear();
			}
			return _stepCounter;
		}
	}
	
	public static void planAction(CustomAgent agent, Action action/*, int syncStep*/){
		if(agent == null || action == null){
			System.out.println("[NULL_ACTION]");
			return;
		}
		
		if(!bestOnlyActions.contains(action.getClass().getSimpleName())){
			_actionsConfirmed.incrementAndGet();
			return;
		}
		
		CustomAgent agentToPing = null;
		if(action instanceof ProbeAction){
			ProbeAction probeAction = (ProbeAction)action;
			synchronized(_probePlan){
				if(_probePlan.containsKey(probeAction.getNodeId())){
					agentToPing = agent;
				}else{
					AgentAction aa = new AgentAction();
					aa.agent = agent;
					aa.action = probeAction.getName();
					aa.target = probeAction.getNodeId();
					_probePlan.put(probeAction.getNodeId(), aa);
					_actionsConfirmed.incrementAndGet();
				}
			}

		}else if(action instanceof SurveyAction){
			SurveyAction surveyAction = (SurveyAction)action;
			synchronized(_surveyPlan){
				if(_surveyPlan.containsKey(surveyAction.getNodeId())){
					agentToPing = agent;
				}else{
					AgentAction aa = new AgentAction();
					aa.agent = agent;
					aa.action = surveyAction.getName();
					aa.target = surveyAction.getNodeId();
					_surveyPlan.put(surveyAction.getNodeId(), aa);
					_actionsConfirmed.incrementAndGet();
				}
			}
			
		}else if(action instanceof GotoAndProbeAction){
			GotoAndProbeAction gapAction = (GotoAndProbeAction)action;
			synchronized(_goToAndProbePlan){
				if(_goToAndProbePlan.containsKey(gapAction.getGoalNodeId())){
					AgentAction aa = _goToAndProbePlan.get(gapAction.getGoalNodeId());
					if(gapAction.getSteps() < aa.weight){
						agentToPing = aa.agent;
						_actionsConfirmed.decrementAndGet();
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
					_actionsConfirmed.incrementAndGet();
				}
			}
		}
		
		if(agentToPing != null){
			_replanRequired.enqueue(agentToPing);
			//agentToPing.pingForNewAction(syncStep);
			//pingAgentForNewAction(agentToPing, syncStep);
		}
	}
	
	public static boolean donePlanning(){
		return _actionsConfirmed.get() >= _planningAgents
				|| (System.currentTimeMillis() - _stepTime) > 1500;
	}
	
	public static int getActionsConfirmed(){
		return _actionsConfirmed.get();
	}
	
	public static boolean proceed(String action, String target){
		return proceed(action, target, 0);
	}
	
	public static boolean proceed(String action, String target, int weight){
		Map<String, AgentAction> plan = _actionToPlan.get(action);
		if(plan == null) return true;
		
		synchronized(plan){
			AgentAction plannedAlready = plan.get(target);
			if(plannedAlready == null) return true;
			
			return weight < plannedAlready.weight;
		}
	}
	
	public static int getReplanningAgentsCount(){
		return _replanRequired.size();
	}
	
	public static Queue<CustomAgent> getReplanningAgents(){
		Queue<CustomAgent> current = _replanRequired;
		_replanRequired = new Queue<CustomAgent>();
		return current;
	}
	
	private static void pingAgentForNewAction(CustomAgent agent, int syncStep){
		Thread t = new Thread(new AgentReplan(agent, syncStep));
		t.start();
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
	
	private static class AgentReplan implements Runnable {
		private CustomAgent _agent;
		private int _syncStep;
		
		public AgentReplan(CustomAgent agent, int syncStep){
			_agent = agent;
			_syncStep = syncStep;
		}
		
		@Override
		public void run() {
			_agent.pingForNewAction(/*_syncStep*/);
		}
	}
}
