package custommas.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Pattern;

public class SharedUtil {
	// We believe node IDs are consistent with their syntax: s[0-9]+
	// Therefore we can parse it directly without defensive checks
	// Not in use any longer, but might be something we could use later
	public static int parseNodeId(final String nodeId ){
	    int num  = 0;
	    final int len  = nodeId.length();

	    // Build the number, skip first letter 's'.
	    int i = 1;
	    while (i < len){
	    	num = num*10 + ('0' - nodeId.charAt(i++));
	    }

	    return num;
	} 
	
	private static Pattern _nodeIdPattern = Pattern.compile("^v[0-9]+$");
	public static boolean isValidNodeId(String nodeId){
		return _nodeIdPattern.matcher(nodeId).find();
	}
	
	public static <T> HashSet<T> newHashSetFromArray(T[] arr){
		HashSet<T> set = new HashSet<T>();
		for(T item : arr){
			set.add(item);
		}
		return set;
	}
	
	private static Random _random = new Random();
	public static int randInt(int min, int max){
		return max >= min ?_random.nextInt(max) + min : randInt(max, min);
	}
	
	private static ArrayList<Integer> _agentRandIds = new ArrayList<Integer>();;
	private static int _agentRandIdsBatch = 0;
	private static int _agentRandIdsIndex = 0;
	
	public static int getUnassignedAgentId(){
		synchronized(_agentRandIds){
			if(_agentRandIdsIndex >= _agentRandIds.size()){
				for(int i = 1000 * _agentRandIdsBatch + 1; i <= 1000 * (_agentRandIdsBatch + 1); i++){
					_agentRandIds.add(i);
				}
				Collections.shuffle(_agentRandIds);
				_agentRandIdsBatch++;
				_agentRandIdsIndex = 0;
			}
			return _agentRandIds.get(_agentRandIdsIndex++);
		}
	}

	public class Actions {
		public static final String Attack = "attack";
		public static final String Buy = "buy";
		public static final String GoTo = "goto";
		public static final String Inspect = "inspect";
		public static final String Parry = "parry";
		public static final String Probe = "probe";
		public static final String Recharge = "recharge";
		public static final String Repair = "repair";
		public static final String Survey = "survey";
		
		public class Custom {
			public static final String GoToAndProbe = "gotoANDprobe";
		}
	}
}
