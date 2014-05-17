package custommas.common;

import java.util.ArrayList;
import java.util.HashMap;

import massim.javaagents.Agent;

//Andreas (s092638)

public class MessageCenter {
	private static ArrayList<TeamIntel> _messages = new ArrayList<TeamIntel>();
	private static HashMap<String, Integer> _agentIndex = new HashMap<String, Integer>();
	
	public static TeamIntel[] getMessages(Agent agent){
		Integer index = _agentIndex.get(agent.getName());
		if(index == null){
			index = 0;
			_agentIndex.put(agent.getName(), index);
		}
		
		int newMessages = _messages.size() - index;
		
		if(newMessages < 1){
			return new TeamIntel[]{};
		}
		
		TeamIntel[] msgs = new TeamIntel[newMessages];
		
		for(int i = 0; i < newMessages; i++){
			msgs[i] = _messages.get(index + i);
		}
		_agentIndex.put(agent.getName(), index + newMessages);
		return msgs;
	}
	
	public static int messagesWaiting(Agent agent){
		Integer index = _agentIndex.get(agent.getName());
		if(index == null){
			index = 0;
			_agentIndex.put(agent.getName(), index);
		}
		
		return _messages.size() - index;
	}
	
	public static boolean unreadMessages(Agent agent){
		return messagesWaiting(agent) > 0;
	}
	
	public static void broadcastMessage(TeamIntel message){
		_messages.add(message);
	}
	
	public static int totalMessages(){
		return _messages.size();
	}
}
