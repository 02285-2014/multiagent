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
	
	public static double DistressThreshold = 0.4;

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

	public static Set<String> getDistressedAgentPositions(){
		return _positionsOfAgentsInNeedOfHelp;
	}
	
	public static int agentsStillInNeed(){
		return _agentsInNeedOfHelp.size() - _agentsGettingHelp.size();
	}
}
