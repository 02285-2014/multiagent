package custommas.agents;

public class OpponentAgent {
	private String _name;
	private String _team;
	private String _role;
	private int _health;
	private int _maxHealth;
	private int _energy;
	private int _maxEnergy;
	
	public OpponentAgent(String name, String team) {
		_name = name;
		_team = team;
		_role = "unknown";
		_health = 0;
		_maxHealth = 0;
	}
	
	public String getName(){
		return _name;
	}
	
	public String getTeam(){
		return _team;
	}
	
	public String getRole(){
		return _role;
	}
	
	public void setRole(String role){
		_role = role;
	}
	
	public int getHealth(){
		return _health;
	}
	
	public void setHealth(int health){
		_health = health;
	}
	
	public int getMaxHealth(){
		return _maxHealth;
	}
	
	public void setMaxHealth(int maxHealth){
		_maxHealth = maxHealth;
	}
	
	public int getEnergy(){
		return _energy;
	}
	
	public void setEnergy(int energy){
		_energy = energy;
	}
	
	public int getMaxEnergy(){
		return _maxEnergy;
	}
	
	public void setMaxEnergy(int maxEnergy){
		_maxEnergy = maxEnergy;
	}
	
	public boolean inspected(){
		return _maxHealth > 0;
	}
	
	@Override
	public int hashCode(){
		return _name.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this) return true;
		if(!(o instanceof OpponentAgent)) return false;
		OpponentAgent other = (OpponentAgent)o;
		return _name.equals(other._name) && _team.equals(other._team);
	}
}
