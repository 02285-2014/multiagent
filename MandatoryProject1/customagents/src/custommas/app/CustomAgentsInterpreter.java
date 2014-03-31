package custommas.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import massim.javaagents.Agent;
import custommas.common.MessageCenter;
import custommas.common.PlanningCenter;
import custommas.lib.Queue;
import custommas.agents.CustomAgent;
import eis.exceptions.ActException;
import eis.iilang.Action;
import apltk.core.StepResult;

public class CustomAgentsInterpreter extends AgentsInterpreter {
	private int _step = 0;
	private HashMap<String, AgentStepper> _agentSteppers = new HashMap<String, AgentStepper>();
	
	public CustomAgentsInterpreter(){
		super();
	}
	
	public CustomAgentsInterpreter(String arg){
		super(arg);
	}
	
	@Override
	public StepResult step(){
		_step = PlanningCenter.newStep(_step + 1, agents.size());
		System.out.println("Step " + _step + ", Messages " + MessageCenter.totalMessages());
		
		ArrayList<CustomAgent> agentList = new ArrayList<CustomAgent>();
		for (Agent ag : agents.values()) {
			if(ag instanceof CustomAgent){
				agentList.add((CustomAgent)ag);
			}else{
				Action act = ag.step();
				if(act != null){
					try {
						Agent.getEnvironmentInterface().performAction(ag.getName(), act);
					} catch (ActException e) { /* Always use CustomAgent */ }
				}
			}
		}
		
		if(agentList.size() > 0){
			for(int i = 0; i < agentList.size(); i++){
				agentList.get(i).step();
			}
			
			while(PlanningCenter.getReplanningAgentsCount() > 0){
				Queue<CustomAgent> agentQueue = PlanningCenter.getReplanningAgents();
				while(!agentQueue.isEmpty()){
					CustomAgent ag = agentQueue.dequeue();
					ag.pingForNewAction(/*_step*/);
				}
			}
			
			for(int i = 0; i < agentList.size(); i++){
				CustomAgent ag = agentList.get(i);
				Action act = ag.getPlannedAction();
				if(act != null){
					try {
						Agent.getEnvironmentInterface().performAction(ag.getName(), act);
					} catch (ActException e) {
						System.out.println("agent \"" + ag.getName() + "\" action \"" + act.toProlog() + "\" failed!");
						System.out.println("message:" + e.getMessage());
						System.out.println("cause:" + e.getCause());
						if(verbose){
							e.printStackTrace();
						}
					}
				}
			}
		}

		return new StepResult();
	}
	
	public StepResult stepAsync(){
		_step = PlanningCenter.newStep(_step + 1, agents.size());
		
		for (Agent ag : agents.values()) {
			AgentStepper agentStepper = _agentSteppers.get(ag.getName());
			if(agentStepper == null){
				agentStepper = new AgentStepper(ag);
				agentStepper.start();
				_agentSteppers.put(ag.getName(), agentStepper);
			}
			
			agentStepper.interrupt();
		}
		
		while(!PlanningCenter.donePlanning()){
			try {
				Thread.sleep(150);
				System.out.println(Calendar.getInstance().getTime().toString() + " [ACTIONS-CONFIRMED] " + PlanningCenter.getActionsConfirmed());
			} catch (InterruptedException e) {
				break;
			}
		}
		
		for (Agent ag : agents.values()) {
			Action theAction = null;
			if(ag instanceof CustomAgent){
				theAction = ((CustomAgent)ag).getPlannedAction();
			}else{
				AgentStepper agentStepper = _agentSteppers.get(ag.getName());
				theAction = agentStepper.getAction();
			}

			if(theAction != null){
				try {
					Agent.getEnvironmentInterface().performAction(ag.getName(), theAction);
				} catch (ActException e) {
					System.out.println("agent \"" + ag.getName() + "\" action \"" + theAction.toProlog() + "\" failed!");
					System.out.println("message:" + e.getMessage());
					System.out.println("cause:" + e.getCause());
					if(verbose){
						e.printStackTrace();
					}
				}
			}
		}

		return new StepResult();
	}
	
	class AgentStepper {
		private Agent _agent;
		private Action _action;
		private Thread _thread;
		
		public AgentStepper(Agent agent){
			_agent = agent;
			_thread = new Thread(new Runnable(){
				public void run(){
					while(true){
						try {
							Thread.sleep(10000000);
						} catch (InterruptedException e) {
							_action = _agent.step();
						}
					}
				}
			});
			_thread.setDaemon(true);
		}
		
		public void start(){
			_thread.start();
		}
		
		public void interrupt(){
			_thread.interrupt();
		}
		
		public Action getAction(){
			return _action;
		}
	}
}
