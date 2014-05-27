package custommas.app;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import massim.javaagents.Agent;
import custommas.common.DistressCenter;
import custommas.common.PlanningCenter;
import custommas.common.SharedKnowledge;
import custommas.lib.algo.IsolatedSubgraph;
import custommas.lib.EdgeWeightedGraph;
import custommas.lib.Graph;
import custommas.lib.Node;
import custommas.lib.Queue;
import custommas.lib.SimpleGraph;
import custommas.lib.algo.SimulatedAnnealing;
import custommas.lib.algo.ComponentMaximumSum;
import custommas.lib.algo.ConnectedComponent;
import custommas.lib.algo.Subgraph;
import custommas.agents.CustomAgent;
import custommas.agents.RepairerAgent;
import eis.exceptions.ActException;
import eis.iilang.Action;
import eis.iilang.Parameter;
import apltk.core.StepResult;

//Andreas (s092638)

public class CustomAgentsInterpreter extends AgentsInterpreter {
	private int _step = 0;
	private static final int _timeoutSkip = 1900;
	private static final int _zoneControlModeStepStart = 100;
	
	public CustomAgentsInterpreter(){
		super();
	}
	
	public CustomAgentsInterpreter(String arg){
		super(arg);
	}
	
	@Override
	public StepResult step(){
		_step = PlanningCenter.newStep(_step + 1);
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
			
			/*
			if(SharedKnowledge.zoneControlMode()){
				if(_step % 50 == 0){
					SharedKnowledge.setMaxSumComponent(null);
				}
				
				if(SharedKnowledge.getMaxSumComponent() == null){
					long startTime = System.currentTimeMillis();
					System.out.println("[MAXSUMFIND]");
					
					int maxSize = agentList.size();
					//SimpleGraph sg = new SimpleGraph(SharedKnowledge.getGraph());
					//ConnectedComponent maxSumComponent = ComponentMaximumSum.getMaximumSumComponent(sg, maxSize);
					Node center = null;
					Set<Node> subgraph = null;
					EdgeWeightedGraph g = SharedKnowledge.getGraph();
					for(Node n : g.getAllNodes()){
						if(n.getValue() >= 6) {
							center = n;
						}
					}
					if(center != null) {
						subgraph = IsolatedSubgraph.formSubGraph(center,maxSize);
					}
					long endTime = System.currentTimeMillis();
					//System.out.println("Found max sum component of size " + maxSumComponent.size() + ", with sum " + maxSumComponent.getSum() + " in " + (endTime - startTime) + " ms");
					System.out.println("Found max sum component of size " + subgraph.size() + " in " + (endTime - startTime) + " ms");
					List<CustomAgent> ags = new ArrayList<CustomAgent>(agentList);
					//Set<Node> zoneNodes = new HashSet<Node>(maxSumComponent.getNodes());
					Set<Node> zoneNodes = null;
					zoneNodes = subgraph;
					
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
					
					//System.out.println("[MAXSUMNODES]: " + maxSumComponent.getNodes());
					System.out.println("[MAXSUMNODES]: " + subgraph);
					
					//SharedKnowledge.setMaxSumComponent(maxSumComponent);
					SharedKnowledge.setZone(subgraph);
				}
			}*/
			
			if(SharedKnowledge.zoneControlMode()){
				if(_step % 50 == 0){
					//SharedKnowledge.setZone(null);
				}
				
				if(SharedKnowledge.getZone() == null){
					//try {
					long startTime = System.currentTimeMillis();
					System.out.println("[MAXSUMFIND]");
					
					int maxSize = agentList.size();
					//SimpleGraph sg = new SimpleGraph(SharedKnowledge.getGraph());
					//ConnectedComponent maxSumComponent = ComponentMaximumSum.getMaximumSumComponent(sg, maxSize);
					Subgraph subgraph = SimulatedAnnealing.getSubgraph(maxSize);
					long endTime = System.currentTimeMillis();
					//System.out.println("Found max sum component of size " + maxSumComponent.size() + ", with sum " + maxSumComponent.getSum() + " in " + (endTime - startTime) + " ms");
					System.out.println("Found max sum component of size " + subgraph.getDominated().size() + " in " + (endTime - startTime) + " ms");
					List<CustomAgent> ags = new ArrayList<CustomAgent>(agentList);
					//Set<Node> zoneNodes = new HashSet<Node>(maxSumComponent.getNodes());
					Set<Node> zoneNodes = null;
					zoneNodes = subgraph.getPlacements();
					
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
					
					//System.out.println("[MAXSUMNODES]: " + maxSumComponent.getNodes());
					System.out.println("[MAXSUMNODES]: " + subgraph.getPlacements());
					System.out.println("[DOMINATEDNODES]: " + subgraph.getDominated());
					//SharedKnowledge.setMaxSumComponent(maxSumComponent);
					SharedKnowledge.setZone(subgraph);
					} //catch(Exception e) {}
				//}
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
