package custommas.common;

import java.util.HashSet;
import java.util.Set;

import custommas.agents.CustomAgent;
import custommas.lib.Node;
import custommas.lib.algo.BreadthFirstSearch;
import custommas.lib.interfaces.INodePredicate;

// Andreas (s092638)
// Peter (s113998)

public class DistressCenter {
	private static HashSet<CustomAgent> _agentsInNeedOfHelp = new HashSet<CustomAgent>();
	private static HashSet<CustomAgent> _agentsGettingHelp = new HashSet<CustomAgent>();
	private static HashSet<String> _positionsOfAgentsInNeedOfHelp = new HashSet<String>();
	public static double DistressThreshold = 0.5;
	public static double DistressThresholdLow = 0.2;
	
	/*private INodePredicate nearestNextHelpPredicate = new INodePredicate(){
		public boolean evaluate(Node node, int comparableValue) {
			return PlanningCenter.isNextHelpAtNode(node.getId());
		}
	};*/

	public static void newStep(){
		System.out.println("Distress Center: New step, clearing old registrations");
		_agentsInNeedOfHelp.clear();
		_agentsGettingHelp.clear();
		_positionsOfAgentsInNeedOfHelp.clear();
	}
	
	public static void requestHelp(CustomAgent agent){
		_agentsInNeedOfHelp.add(agent);
		if(_positionsOfAgentsInNeedOfHelp.contains(agent.getPosition())) return;
		_positionsOfAgentsInNeedOfHelp.add(agent.getPosition());
	}
	
	public static void respondToHelp(CustomAgent agent){
		if(_agentsGettingHelp.contains(agent)) return;
		_agentsGettingHelp.add(agent);
	}
	
	public static Set<CustomAgent> getDistressedAgents(){
		return _agentsInNeedOfHelp;
	}
	
	public static Set<CustomAgent> getAgentsGettingHelp(){
		return _agentsGettingHelp;
	}
	
	/*public static boolean AgentGettingHelp(CustomAgent agent){
		if(_agentsGettingHelp.contains(agent);
	}*/

	public static Set<String> getDistressedAgentPositions(){
		return _positionsOfAgentsInNeedOfHelp;
	}
	
	/*public static Set<String> getPositionsOfNextHelp(){
		return _positionsOfNextHelp;
	}*/
	
	public static int agentsStillInNeed(){
		return _agentsInNeedOfHelp.size() - _agentsGettingHelp.size();
	}
	
	/*public static String findNearestNextHelp(){
		BreadthFirstSearch nearestNextHelpSearch = new BreadthFirstSearch(_graph);
		Node moveToNode = nearestNextHelpSearch.findClosestNodeSatisfyingPredicate(currentNode, nearestNextHelpPredicate);
	}*/
}
