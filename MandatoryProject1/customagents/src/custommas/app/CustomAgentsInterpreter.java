package custommas.app;

import java.util.ArrayList;

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
	
	public CustomAgentsInterpreter(){
		super();
	}
	
	public CustomAgentsInterpreter(String arg){
		super(arg);
	}
	
	@Override
	public StepResult step(){
		_step = PlanningCenter.newStep(_step + 1);
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
			
			while(PlanningCenter.getReplanningAgentsCount() > 0 && PlanningCenter.getPlanningTimeSpendInMillis() < 1900){
				Queue<CustomAgent> agentQueue = PlanningCenter.getReplanningAgents();
				while(!agentQueue.isEmpty()){
					CustomAgent ag = agentQueue.dequeue();
					ag.planNewAction();
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
}
