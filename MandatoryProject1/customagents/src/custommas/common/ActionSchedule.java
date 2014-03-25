package custommas.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;

import eis.iilang.Action;

public class ActionSchedule {
	private Map<String, Map<String, Collection<Integer>>> _schedule;
	
	public ActionSchedule(){
		clear();
	}
	
	public void add(TeamIntel intel){
		String[] pars = intel.getParameters();
		
		if(pars.length < 3) return;
		String actionName = pars[0];
		if(!_schedule.containsKey(actionName)){
			_schedule.put(actionName, new HashMap<String, Collection<Integer>>());
		}
		
		Map<String, Collection<Integer>> actionSchedule = _schedule.get(actionName);
		String targetName = pars[1];
		System.out.println("TARGET NAME: '" + targetName + "' for '" + actionName + "' - " + actionSchedule);
		if(!actionSchedule.containsKey(targetName)){
			actionSchedule.put(targetName, new LinkedList<Integer>());
		}
		
		Collection<Integer> targetSchedule = actionSchedule.get(targetName);
		
		int weight = Integer.parseInt(pars[2]);
		targetSchedule.add(weight);
	}
	
	public boolean Proceed(String action, String targetName, int weight){
		if(!_schedule.containsKey(action)) return true;
		
		Collection<Integer> actionWeights = _schedule.get(action).get(targetName);
		
		if(actionWeights != null && actionWeights.size() > 0){
			for(int actionWeight : actionWeights){
				if(weight > actionWeight){
					return false;
				}
			}
		}
		
		return true;
	}
	
	public void clear(){
		_schedule = new HashMap<String, Map<String, Collection<Integer>>>();
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(String keyA : _schedule.keySet()){
			sb.append("{" + keyA + ": ");
			for(String keyB : _schedule.get(keyA).keySet()){
				sb.append("\t" + keyB + ": " + _schedule.get(keyA).get(keyB).toArray().toString());
			}
			sb.append("}\n");
		}
		return sb.toString();
	}
}
