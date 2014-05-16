package custommas.common;

import java.util.HashSet;
import java.util.Random;
import java.util.regex.Pattern;

//Andreas (s092638)

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
	
	public class Agents {
		public static final String Explorer = "explorer";
		public static final String Inspector = "inspector";
		public static final String Repairer = "repairer";
		public static final String Saboteur = "saboteur";
		public static final String Sentinel = "sentinel";
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
