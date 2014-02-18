package custommas.agents;

import eis.iilang.Action;
import eis.iilang.Percept;
import massim.javaagents.Agent;

public class ExplorerAgent extends Agent{
	
	private String _name;
	private String _team;
	
	public ExplorerAgent(String name, String team) {
		super(name, team);
		_name = name;
		_team = team;
	}

	@Override
	public void handlePercept(Percept percept) {
		System.out.println("[Percept " + _name + "/" + _team + "]: " + percept);
	}

	@Override
	public Action step() {
		// TODO Auto-generated method stub
		System.out.println("[Action " + _name + "/" + _team + "] TODO");
		return new Action("skip");
	}
	
	public static void main(String[] args){
		
	}
}
