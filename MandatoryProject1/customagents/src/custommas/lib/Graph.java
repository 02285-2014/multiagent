package custommas.lib;

import java.util.Collection;

public abstract class Graph {
	public abstract int vertexCount();
	public abstract int edgeCount();
	public abstract Collection<Node> getAdjacentTo(Node n);
	
	public static String getEdgeId(String node1, String node2){
		return node1.hashCode() <= node2.hashCode() ? node1 + node2 : node2 + node1;
	}
}
