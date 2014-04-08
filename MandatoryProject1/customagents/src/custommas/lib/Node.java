package custommas.lib;

import java.util.Collection;
import java.util.HashMap;

import custommas.common.PlanningCenter;

public class Node implements Comparable<Node> {
	private static final int NonProbed = Integer.MIN_VALUE;
	private String _id;
	private int _value;
	private HashMap<String, OccupyInfo> _occupantsTeamA;
	private HashMap<String, OccupyInfo> _occupantsTeamB;
	
	public Node(String name) {
		if(name == null) throw new NullPointerException("Name cannot be null");
		_id = name;
		_value = NonProbed;
		_occupantsTeamA = new HashMap<String, OccupyInfo>();
		_occupantsTeamB = new HashMap<String, OccupyInfo>();
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
		
		for(OccupyInfo nfo : _occupantsTeamA.values()){
			if(nfo.getStepsAgo() <= amountOfStepsToLookBack){
				return true;
			}
		}
		
		for(OccupyInfo nfo : _occupantsTeamB.values()){
			if(nfo.getStepsAgo() <= amountOfStepsToLookBack){
				return true;
			}
		}
		
		return false;
	}
	
	public int getNumberOfOccupants(){
		return _occupantsTeamA.size() + _occupantsTeamB.size();
	}
	
	public int getNumberOfOccupantsForTeam(String agentTeam){
		if(agentTeam.equals("A")){
			return _occupantsTeamA.size();
		}else{
			return _occupantsTeamB.size();
		}
	}
	
	public Collection<OccupyInfo> getOccupantsForTeam(String agentTeam){
		if(agentTeam.equals("A")){
			return _occupantsTeamA.values();
		}else{
			return _occupantsTeamB.values();
		}
	}
	
	public String getOwnerTeam(){
		return _occupantsTeamA.size() > _occupantsTeamB.size() ? "A" : _occupantsTeamA.size() < _occupantsTeamB.size() ? "B" : "None";
	}
	
	public boolean isProbed(){
		return _value != NonProbed;
	}
	
	public void addAgent(String agentName, String agentTeam){
		if(agentTeam == null || agentTeam.length() < 1) return;
		if(agentTeam.equals("A")){
			_occupantsTeamA.put(agentName, new OccupyInfo(PlanningCenter.getStep(), agentName, agentTeam));
		}else{
			_occupantsTeamB.put(agentName, new OccupyInfo(PlanningCenter.getStep(), agentName, agentTeam));
		}
	}
	
	public void removeAgent(String agentName, String agentTeam){
		if(agentTeam == null || agentTeam.length() < 1) return;
		if(agentTeam.equals("A")){
			_occupantsTeamA.remove(agentName);
		}else{
			_occupantsTeamB.remove(agentName);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Node && this.getId().equals(((Node)o).getId());
	}
	
	@Override
	public int hashCode(){
		return _id.hashCode();
	}
	
	@Override
	public String toString(){
		return "Node(" + _id + ")";
	}

	@Override
	public int compareTo(Node o) {
		return Integer.compare(_value, o._value);
	}
	
	public class OccupyInfo {
		private int _seenAtStep;
		private String _agentName;
		private String _teamName;
		
		private OccupyInfo(int step, String agent, String team){
			_seenAtStep = step;
			_agentName = agent;
			_teamName = team;
		}
		
		public int getStep(){
			return _seenAtStep;
		}
		
		public int getStepsAgo(){
			return PlanningCenter.getStep() - _seenAtStep;
		}
		
		public String getAgent(){
			return _agentName;
		}
		
		public String getTeam(){
			return _teamName;
		}
	}
}
