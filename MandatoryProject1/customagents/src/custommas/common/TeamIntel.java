package custommas.common;

import java.util.List;
import java.util.Vector;

import eis.iilang.Parameter;
import eis.iilang.Percept;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.Message;

//Andreas (s092638)

public class TeamIntel {
	private static final int TypeIsBelief = 0;
	private static final int TypeIsPercept = 1;
	
	private String _name = null;
	private String[] _parameters;
	private String _sender = null;
	private int _typeIs = 0;
	
	private TeamIntel(){}
	
	public TeamIntel (LogicBelief belief){
		_name = belief.getPredicate();
		Vector<String> pars = belief.getParameters();
		_parameters = new String[pars.size()];
		
		int i = 0;
		for(String param : belief.getParameters()){
			_parameters[i++] = param;
		}
		_typeIs = TypeIsBelief;
	}
	
	@SuppressWarnings("deprecation")
	public TeamIntel(Percept percept){
		_name = percept.getName();
		List<Parameter> pars = percept.getParameters();
		_parameters = new String[pars.size()];
		
		int i = 0;
		for(Parameter param : pars){
			_parameters[i++] = param.toProlog();
		}
		_typeIs = TypeIsPercept;
	}
	
	public String getName(){
		return _name;
	}
	
	public String[] getParameters(){
		return _parameters;
	}
	
	public String getSender(){
		return _sender;
	}
	
	public void setSender(String sender){
		_sender = sender;
	}
	
	public boolean isBelief(){
		return _typeIs == TypeIsBelief;
	}
	
	public boolean isPercept(){
		return _typeIs == TypeIsPercept;
	}
}
