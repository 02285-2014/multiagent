package custommas.common;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import eis.iilang.Parameter;
import eis.iilang.Percept;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.Message;

public class TeamIntel {
	private static final int TypeIsBelief = 0;
	private static final int TypeIsMessage = 1;
	private static final int TypeIsPercept = 2;
	
	private String _name = null;
	private String[] _parameters;
	private String _sender = null;
	private int _typeIs = 0;
	
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
	
	public TeamIntel(Message message){
		this((LogicBelief)message.value);
		_sender = message.sender;
		_typeIs = TypeIsMessage;
	}
	
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
	
	public boolean isBelief(){
		return _typeIs == TypeIsBelief;
	}
	
	public boolean isMessage(){
		return _typeIs == TypeIsMessage;
	}
	
	public boolean isPercept(){
		return _typeIs == TypeIsPercept;
	}
	
	public LogicBelief toBelief(){
		return new LogicBelief(_name, _parameters);
	}
}
