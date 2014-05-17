package custommas.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import massim.javaagents.Agent;
import custommas.common.MessageCenter;
import custommas.common.PlanningCenter;
import custommas.common.SharedKnowledge;
import custommas.lib.Node;
import custommas.lib.Queue;
import custommas.lib.SimpleGraph;
import custommas.lib.algo.ComponentMaximumSum;
import custommas.lib.algo.ConnectedComponent;
import custommas.lib.algo.GraphMiscAlg;
import custommas.agents.CustomAgent;
import custommas.agents.ExplorerAgent;
import eis.exceptions.ActException;
import eis.iilang.Action;
import eis.iilang.Parameter;
import apltk.core.StepResult;

//Andreas (s092638)

public class CustomAgentsInterpreter extends AgentsInterpreter {
	private int _step = 0;
	private static final int _timeoutSkip = 1900;
	private static final int _zoneControlModeStepStart = 300;
	
	public CustomAgentsInterpreter(){
		super();
	}
	
	public CustomAgentsInterpreter(String arg){
		super(arg);
	}
	
	@Override
	public StepResult step(){
		_step = PlanningCenter.newStep(_step + 1);
		if(!SharedKnowledge.zoneControlMode() && _step >= _zoneControlModeStepStart){
			System.out.println("ZONE CONTROL ENABLED!");
			SharedKnowledge.enableZoneControlMode();
		}
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
			// Make them handle percepts and messages so they all have the same map to work with
			for(int i = 0; i < agentList.size(); i++){
				agentList.get(i).step();
			}
			
			if(SharedKnowledge.zoneControlMode()){
				if(_step % 50 == 0){
					SharedKnowledge.setMaxSumComponent(null);
				}
				
				if(SharedKnowledge.getMaxSumComponent() == null){
					long startTime = System.currentTimeMillis();
					System.out.println("[MAXSUMFIND]");
					
					int maxSize = agentList.size();
					SimpleGraph sg = new SimpleGraph(SharedKnowledge.getGraph());
					ConnectedComponent maxSumComponent = ComponentMaximumSum.getMaximumSumComponent(sg, maxSize);
					long endTime = System.currentTimeMillis();
					System.out.println("Found max sum component of size " + maxSumComponent.size() + ", with sum " + maxSumComponent.getSum() + " in " + (endTime - startTime) + " ms");
					
					List<CustomAgent> ags = new ArrayList<CustomAgent>(agentList);
					Set<Node> zoneNodes = new HashSet<Node>(maxSumComponent.getNodes());
					
					int i = 0;
					for(i = ags.size() - 1; i >= 0; i--){
						if(zoneNodes.contains(ags.get(i).getNode())){
							zoneNodes.remove(ags.get(i).getNode());
							ags.remove(i);
						}
					}
					
					i = 0;
					for(Node n : zoneNodes){
						if(i >= ags.size()) break;
						agentList.get(i++).gotoNode(n.getId());
					}
					
					System.out.println("[MAXSUMNODES]: " + maxSumComponent.getNodes());
					
					SharedKnowledge.setMaxSumComponent(maxSumComponent);
				}
			}
			
			// Make them plan their next action.
			for(int i = 0; i < agentList.size(); i++){
				agentList.get(i).planNewAction();
			}
			
			while(PlanningCenter.getReplanningAgentsCount() > 0 && PlanningCenter.getPlanningTimeSpendInMillis() < _timeoutSkip){
				Queue<CustomAgent> agentQueue = PlanningCenter.getReplanningAgents();
				System.out.println("Replanning agents:");
				for(CustomAgent agent : agentQueue){
					StringBuilder pars = new StringBuilder();
					for(Parameter par : agent.getPlannedAction().getParameters()){
						pars.append(par.toProlog() + ", ");
					}
					
					System.out.println("\t" + agent.getName() + " - " + agent.getRole() + " - " + agent.getPlannedAction().getName() + " - " + pars.toString());
				}
				while(!agentQueue.isEmpty()){
					CustomAgent ag = agentQueue.dequeue();
					ag.planNewAction();
				}
			}
			
			for(int i = 0; i < agentList.size(); i++){
				CustomAgent ag = agentList.get(i);
				
				StringBuilder pars = new StringBuilder();
				for(Parameter par : ag.getPlannedAction().getParameters()){
					pars.append(par.toProlog() + ", ");
				}
				
				System.out.println(ag.getName() + " will perform " + ag.getPlannedAction().getName() + " - " + pars.toString());
				Action act = ag.getPlannedAction();
				if(act != null){
					try {
						Agent.getEnvironmentInterface().performAction(ag.getName(), act);
					} catch (ActException e) {
						System.out.println("agent \"" + ag.getName() + "\" action \"" + act.toProlog() + "\" failed!");
						System.out.println("message:" + e.getMessage());
						System.out.println("cause:" + e.getCause());
						e.printStackTrace();
					}
				}
			}
		}
		
		return new StepResult();
	}
}
