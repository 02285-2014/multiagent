package custommas.app;

import java.util.ArrayList;
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
import custommas.ui.AgentMonitor;
import custommas.agents.CustomAgent;
import custommas.agents.ExplorerAgent;
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
		
		if(SharedKnowledge.getGraph().vertexCount() > 0 && SharedKnowledge.getGraph().allNodesProbed()){
			if(SharedKnowledge.getMaxSumComponent() == null){
				//long startTime = System.currentTimeMillis();
				//System.out.println("[MAXSUMFIND]");
				
				int maxSize = 0;
				for (Agent ag : agents.values()) {
					if(!(ag instanceof CustomAgent) || !ag.getTeam().equals("A")) continue;
					maxSize += 2;
				}
				maxSize -= 2;
				
				if(maxSize > 0){
					SimpleGraph sg = new SimpleGraph(SharedKnowledge.getGraph());
					ConnectedComponent maxSumComponent = ComponentMaximumSum.getMaximumSumComponent(sg, maxSize, "A");
					//long endTime = System.currentTimeMillis();
					//System.out.println("Found max sum component of size " + maxSumComponent.size() + ", with sum " + maxSumComponent.getSum() + " in " + (endTime - startTime) + " ms");
					SharedKnowledge.setMaxSumComponent(maxSumComponent);
				}
				
			}else if(!SharedKnowledge.getMaxSumInitiated()){
				ArrayList<CustomAgent> ags = new ArrayList<CustomAgent>();
				for (Agent ag : agents.values()) {
					if(!(ag instanceof CustomAgent) || !ag.getTeam().equals("A")) continue;
					ags.add((CustomAgent)ag);
				}
				
				SimpleGraph sg = new SimpleGraph(SharedKnowledge.getGraph(), SharedKnowledge.getMaxSumComponent().getNodes());
				Set<Node> nodesToPlaceAt = GraphMiscAlg.maxDistanceNodes(sg, ags.size());
				
				int i = 0;
				for(Node n : nodesToPlaceAt){
					ags.get(i++).gotoNode(n.getId());
				}
				SharedKnowledge.setMaxSumInitiated(true);
			}
		}
		
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
			
			// Make them plan their next action.
			for(int i = 0; i < agentList.size(); i++){
				agentList.get(i).planNewAction();
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
						e.printStackTrace();
					}
				}
			}
		}
		
		AgentMonitor.getInstance().update();
		return new StepResult();
	}
}
