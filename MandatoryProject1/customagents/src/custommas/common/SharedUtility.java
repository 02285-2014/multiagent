package custommas.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SharedUtility {
	// We believe node IDs are consistent with their syntax: s[0-9]+
	// Therefore we can parse it directly without defensive checks
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
	
	public static boolean isValidNodeId(String nodeId){
		return _nodeIdPattern.matcher(nodeId).find();
	}
	
	private static Pattern _nodeIdPattern;
	static {
		_nodeIdPattern = Pattern.compile("^v[0-9]+$");
	}
}
