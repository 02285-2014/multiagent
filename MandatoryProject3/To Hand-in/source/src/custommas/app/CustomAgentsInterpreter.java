package custommas.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import massim.javaagents.Agent;
import custommas.common.DistressCenter;
import custommas.common.PlanningCenter;
import custommas.common.SharedKnowledge;
import custommas.lib.Node;
import custommas.lib.Queue;
import custommas.lib.algo.SimulatedAnnealing;
import custommas.lib.algo.Subgraph;
import custommas.agents.CustomAgent;
import custommas.agents.RepairerAgent;
import eis.exceptions.ActException;
import eis.iilang.Action;
import eis.iilang.Parameter;
import apltk.core.StepResult;

//Andreas (s092638)
//Morten (s133304)

public class CustomAgentsInterpreter extends AgentsInterpreter {
	private int _step = 0;
	private static final int _timeoutSkip = 1900;
	private static final int _zoneControlModeStepStart = 150;
	
	public CustomAgentsInterpreter(){
		super();
	}
	
	public CustomAgentsInterpreter(String arg){
		super(arg);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public StepResult step(){
		_step = PlanningCenter.newStep();
		DistressCenter.newStep();
		
		if(!SharedKnowledge.zoneControlMode() && _step >= _zoneControlModeStepStart){
			System.out.println("ZONE CONTROL ENABLED!");
			SharedKnowledge.enableZoneControlMode();
		}
		System.out.println("Step " + _step);
		
		ArrayList<CustomAgent> agentList = new ArrayList<CustomAgent>();
		ArrayList<RepairerAgent> repairerList = new ArrayList<RepairerAgent>();
		for (Agent ag : agents.values()) {
			if(ag instanceof CustomAgent){
				agentList.add((CustomAgent)ag);
				if(ag instanceof RepairerAgent){
					repairerList.add((RepairerAgent)ag);
				}
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
				if(_step % 150 == 0){
					SharedKnowledge.setZone(null);
				}
				
				if(SharedKnowledge.getZone() == null){
					long startTime = System.currentTimeMillis();
					System.out.println("[MAXSUMFIND]");
					
					int maxSize = agentList.size();
					Subgraph subgraph = SimulatedAnnealing.getSubgraph(maxSize);
					long endTime = System.currentTimeMillis();
					System.out.println("Found max sum component of size " + subgraph.getDominated().size() + " in " + (endTime - startTime) + " ms");
					List<CustomAgent> ags = new ArrayList<CustomAgent>(agentList);
					Set<Node> zoneNodes = subgraph.getPlacements();
					
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
					
					System.out.println("[MAXSUMNODES]: " + subgraph.getPlacements());
					System.out.println("[DOMINATEDNODES]: " + subgraph.getDominated());
					SharedKnowledge.setZone(subgraph);
				}
			}
			
			// Make them plan their next action.
			for(int i = 0; i < agentList.size(); i++){
				agentList.get(i).planNewAction();
			}
			
			if(repairerList.size() > 0 && DistressCenter.getDistressedAgents().size() > 0){
				for(RepairerAgent ag : repairerList){
					ag.planNewAction();
				}
			}
			
			while(PlanningCenter.getReplanningAgentsCount() > 0 && PlanningCenter.getPlanningTimeSpendInMillis() < _timeoutSkip){
				Queue<CustomAgent> agentQueue = PlanningCenter.getReplanningAgents();
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
