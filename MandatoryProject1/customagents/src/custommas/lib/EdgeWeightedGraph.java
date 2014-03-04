package custommas.lib;

import java.util.*;

public class EdgeWeightedGraph extends Graph {
	private Map<String, Node> _nodes;
	private Map<String, Edge> _edges;
	private Map<Node, Set<Node>> _adjacent;
	
	public EdgeWeightedGraph() {
		this._nodes = new HashMap<String, Node>();
		this._edges = new HashMap<String, Edge>();
		this._adjacent = new HashMap<Node, Set<Node>>();
	}
	
	public int vertexCount(){
		return _nodes.size();
	}
	
	public int edgeCount() {
		return _edges.size();
	}
	
	public Edge addEdge(Node n1, Node n2) {
		if(!_nodes.containsKey(n1.getId())){
			_nodes.put(n1.getId(), n1);
			_adjacent.put(n1, new HashSet<Node>());
		}
		
		if(!_nodes.containsKey(n2.getId())){
			_nodes.put(n2.getId(), n2);
			_adjacent.put(n2,  new HashSet<Node>());
		}
		
		if(!_adjacent.get(n1).contains(n2)){
			_adjacent.get(n1).add(n2);
			_adjacent.get(n2).add(n1);
			
			Edge e = new Edge(n1, n2);
			this._edges.put(e.getId(), e);
			return e;
		}
		
		return getEdgeFromNodes(n1, n2);
	}
	
	public Edge addEdgeFromNodeIds(String from, String to) {
		Node n = getNode(from);
		if(n == null) n = new Node(from);
		
		Node n2 = getNode(to);
		if(n2 == null) n2 = new Node(to);
		
		return addEdge(n, n2);
	}
	
	public Edge addEdgeWithWeightFromNodeIds(String from, String to, int weight) {
		Edge e = addEdgeFromNodeIds(from, to);
		e.setWeight(weight);
		return e;
	}
	
	public Collection<Node> getAdjacentTo(Node n) {
		return _adjacent.get(n);
	}
	
	public Node getNode(String nodeId) {
		return this._nodes.get(nodeId);
	}
	
	public Edge getEdge(String edgeId){
		return this._edges.get(edgeId);
	}
	
	public Edge getEdgeFromNodes(Node node1, Node node2){
		return getEdgeFromNodeIds(node1.getId(), node2.getId());
	}
	
	public Edge getEdgeFromNodeIds(String nodeId1, String nodeId2){
		return this._edges.get(EdgeWeightedGraph.getEdgeId(nodeId1, nodeId2));
	}
	
	public static String getEdgeId(String node1, String node2){
		return node1.hashCode() <= node2.hashCode() ? node1 + node2 : node2 + node1;
	}
	
	public Collection<String> getAllNodes() {
		return _nodes.keySet();
	}
	
	public Collection<String> getAllEdges() {
		return _edges.keySet();
	}
}
