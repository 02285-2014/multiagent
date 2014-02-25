package custommas.lib;

import java.util.*;

public class EdgeWeightedGraph {
	private int edges;
	private Map<String, Node> nodes;
	
	public EdgeWeightedGraph() {
		this.edges = 0;
		this.nodes = new TreeMap<String, Node>();
	}
	
	public Collection<Edge> getPossibleEdges(Node n) {
		return n.getAdjacents();
	}
	
	public int edges() {
		return edges;
	}
	
	public Edge addEdge(String from, String to, int weight) {
		Node n = new Node(from);
		Node n2 = new Node(to);
		Edge e = new Edge(n, n2, weight);
		n.addAdjacent(e);
		n2.addAdjacent(e);
		this.edges++;
		this.nodes.put(from, n);
		this.nodes.put(to,n2);
		return e;
	}
	
	public Node getNode(String str) {
		return this.nodes.get(str);
	}
}
