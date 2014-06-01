package custommas.lib;

import java.util.Collection;
import java.util.HashMap;

import custommas.common.PlanningCenter;
import custommas.common.SharedKnowledge;

//Andreas (s092638)
//Morten (s133304)

public class Node implements Comparable<Node> {
	private static final int NonProbed = Integer.MIN_VALUE;
	private String _id;
	private int _value;
	private HashMap<String, OccupyInfo> _occupantsOurTeam;
	private HashMap<String, OccupyInfo> _occupantsOpponentTeam;
	
	public Node(String name) {
		if(name == null) throw new NullPointerException("Name cannot be null");
		_id = name;
		_value = NonProbed;
		_occupantsOurTeam = new HashMap<String, OccupyInfo>();
		_occupantsOpponentTeam = new HashMap<String, OccupyInfo>();
	}
	
	public Node(String name, int value) {
		this(name);
		_value = value;;
	}
	
	public String getId() {
		return _id;
	}
	
	public int getValue() {
		return _value;
	}
	
	public void setValue(int newValue) {
		_value = newValue;
	}
	
	public boolean isOccupied(){
		return isOccupied(0);
	}
	
	public boolean isOccupied(int amountOfStepsToLookBack){
		int occupants = getNumberOfOccupants();
		if(occupants < 1) return false;
		if(occupants > 0 && amountOfStepsToLookBack < 1) return true;
		
		for(OccupyInfo nfo : _occupantsOurTeam.values()){
			if(nfo.getStepsAgo() <= amountOfStepsToLookBack){
				return true;
			}
		}
		
		for(OccupyInfo nfo : _occupantsOpponentTeam.values()){
			if(nfo.getStepsAgo() <= amountOfStepsToLookBack){
				return true;
			}
		}
		
		return false;
	}
	
	public int getNumberOfOccupants(){
		return _occupantsOurTeam.size() + _occupantsOpponentTeam.size();
	}
	
	public int getNumberOfOccupantsForTeam(String agentTeam){
		if(agentTeam.equals(SharedKnowledge.OurTeam)){
			return _occupantsOurTeam.size();
		}else{
			return _occupantsOpponentTeam.size();
		}
	}
	
	public Collection<OccupyInfo> getOccupantsForTeam(String agentTeam){
		if(agentTeam.equals(SharedKnowledge.OurTeam)){
			return _occupantsOurTeam.values();
		}else{
			return _occupantsOpponentTeam.values();
		}
	}
	
	public boolean isProbed(){
		return _value != NonProbed;
	}
	
	public void addAgent(String agentName, String agentTeam){
		if(agentTeam == null || agentTeam.length() < 1) return;
		if(agentTeam.equals(SharedKnowledge.OurTeam)){
			_occupantsOurTeam.put(agentName, new OccupyInfo(PlanningCenter.getStep(), agentName, agentTeam));
		}else{
			_occupantsOpponentTeam.put(agentName, new OccupyInfo(PlanningCenter.getStep(), agentName, agentTeam));
		}
	}
	
	public void removeAgent(String agentName, String agentTeam){
		if(agentTeam == null || agentTeam.length() < 1) return;
		if(agentTeam.equals(SharedKnowledge.OurTeam)){
			_occupantsOurTeam.remove(agentName);
		}else{
			_occupantsOpponentTeam.remove(agentName);
		}
	}
	
	//@Override
	public boolean equals(Object o) {
		return o instanceof Node && this.getId().equals(((Node)o).getId());
	}
	
	//@Override
	public int hashCode(){
		return _id.hashCode();
	}
	
	//@Override
	public String toString(){
		return "Node(" + _id + ")";
	}

	//@Override
	public int compareTo(Node o) {
		return Integer.compare(_value, o._value);
	}
	
	public class OccupyInfo {
		private int _seenAtStep;
		private String _agentName;
		private String _teamName;
		
		private OccupyInfo(int step, String agentName, String team){
			_seenAtStep = step;
			_agentName = agentName;
			_teamName = team;
		}
		
		public int getStep(){
			return _seenAtStep;
		}
		
		public int getStepsAgo(){
			return PlanningCenter.getStep() - _seenAtStep;
		}
		
		public String getAgentName(){
			return _agentName;
		}
		
		public String getTeam(){
			return _teamName;
		}
	}
}
