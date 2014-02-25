package custommas.lib;

import java.util.*;

public class EdgeWeightedGraph {
	private int _edgeCount;
	private Map<String, Node> _nodes;
	private Map<String, Edge> _edges;
	
	public EdgeWeightedGraph() {
		this._edgeCount = 0;
		this._nodes = new HashMap<String, Node>();
		this._edges = new HashMap<String, Edge>();
	}
	
	public Collection<String> getPossibleEdges(Node n) {
		return n.getAdjacents();
	}
	
	public int edges() {
		return _edgeCount;
	}
	
	public Edge addEdge(String from, String to, int weight) {
		Node n = getNode(from);
		if(n == null) n = new Node(from);
		
		Node n2 = getNode(to);
		if(n2 == null) n2 = new Node(to);
		
		if(!n.getAdjacents().contains(to)){
			n.addAdjacent(to);
			n2.addAdjacent(from);
			this._nodes.put(from, n);
			this._nodes.put(to,n2);
			
			Edge e = new Edge(n, n2, weight);
			this._edges.put(e.toString(), e);
			this._edgeCount++;
			return e;
		}
		
		return getEdge(from, to);
	}
	
	public Node getNode(String str) {
		return this._nodes.get(str);
	}
	
	public Edge getEdge(String from, String to){
		return from.compareTo(to) >= 0 ? this._edges.get(from + to) : this._edges.get(to + from);
	}
}
