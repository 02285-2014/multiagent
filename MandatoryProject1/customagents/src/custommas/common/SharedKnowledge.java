package custommas.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import custommas.agents.CustomAgent;
import custommas.agents.OpponentAgent;
import custommas.lib.EdgeWeightedGraph;
import custommas.lib.algo.ConnectedComponent;
import custommas.lib.Node;
import custommas.lib.algo.Subgraph;

//Andreas (s092638)

public class SharedKnowledge {
	public static final String OurTeam = "A";
	public static final String OpponentTeam = "B";
	private static Map<String, CustomAgent> _agents = new HashMap<String, CustomAgent>();
	private static Map<String, OpponentAgent> _opponents = new HashMap<String, OpponentAgent>();
	private static boolean _zoneControlMode = false;
	private static Subgraph _zone;
	
	private static int _teamScore = 0;
	private static int _zoneScore = 0;
	
	private static EdgeWeightedGraph _graph = new EdgeWeightedGraph();
	
	public static EdgeWeightedGraph getGraph(){
		return _graph;
	}
	
	private static ConnectedComponent _maxSumComponent = null;
	public static ConnectedComponent getMaxSumComponent(){
		return _maxSumComponent;
	}
	
	public static void setMaxSumComponent(ConnectedComponent maxSumComponent){
		_maxSumComponent = maxSumComponent;
	}
	
	public static boolean zoneControlMode(){
		return _zoneControlMode;
	}
	
	public static void setZone(Subgraph zone) {
		_zone = zone;
	}
	
	public static Subgraph getZone() {
		return _zone;
	}
	
	public static void enableZoneControlMode(){
		_zoneControlMode = true;
	}
	
	public static void addCustomAgent(CustomAgent agent){
		if(_agents.containsKey(agent.getName())) return;
		_agents.put(agent.getName(), agent);
	}
	
	public static Collection<CustomAgent> getCustomAgents(){
		return _agents.values();
	}
	
	public static CustomAgent getCustomAgent(String name){
		return _agents.get(name);
	}
	
	public static OpponentAgent getOpponentAgent(String name){
		OpponentAgent agent = _opponents.get(name);
		if(agent != null) return agent;
		
		agent = new OpponentAgent(name, OpponentTeam);
		_opponents.put(name, agent);
		return agent;
	}
	
	public static int getTeamScore(){
		return _teamScore;
	}
	
	public static void setTeamScore(int teamScore){
		_teamScore = teamScore;
	}
	
	public static int getZoneScore(){
		return _zoneScore;
	}
	
	public static void setZoneScore(int zoneScore){
		_zoneScore = zoneScore;
	}
	
	public static void reset(){
		_graph = new EdgeWeightedGraph();
		_opponents = new HashMap<String, OpponentAgent>();
		_zoneControlMode = false;
		_maxSumComponent = null;
	}
}
