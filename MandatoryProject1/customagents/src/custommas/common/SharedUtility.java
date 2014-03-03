package custommas.common;

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
}
