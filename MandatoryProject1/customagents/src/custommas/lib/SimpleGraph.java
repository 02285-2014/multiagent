package custommas.lib;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Andreas (s092638)

public class SimpleGraph extends Graph {
	private Map<String, Node> _nodes;
	private Map<String, Edge> _edges;
	private Map<Node, Set<Node>> _adjacent;
	
	public SimpleGraph() {
		_nodes = new HashMap<String, Node>();
		_edges = new HashMap<String, Edge>();
		_adjacent = new HashMap<Node, Set<Node>>();
	}
	
	public SimpleGraph(EdgeWeightedGraph ewg){
		this();
		for(Node node : ewg.getAllNodes()){
			_nodes.put(node.getId(), node);
			_adjacent.put(node, new HashSet<Node>());
		}
		for(Edge edge : ewg.getAllEdges()){
			_edges.put(edge.getId(), edge);
			Node n1 = edge.either();
			Node n2 = edge.other(n1);
			
			if(!_adjacent.get(n1).contains(n2)){
				_adjacent.get(n1).add(n2);
				_adjacent.get(n2).add(n1);
			}
		}
	}
	
	public SimpleGraph(EdgeWeightedGraph ewg, Set<Node> onlyTheseNodes){
		this();
		for(Node node : ewg.getAllNodes()){
			if(!onlyTheseNodes.contains(node)) continue;
			_nodes.put(node.getId(), node);
			_adjacent.put(node, new HashSet<Node>());
		}
		for(Edge edge : ewg.getAllEdges()){
			Node n1 = edge.either();
			Node n2 = edge.other(n1);
			
			if(!onlyTheseNodes.contains(n1) || !onlyTheseNodes.contains(n2)) continue;
			_edges.put(edge.getId(), edge);
			if(!_adjacent.get(n1).contains(n2)){
				_adjacent.get(n1).add(n2);
				_adjacent.get(n2).add(n1);
			}
		}
	}
	
	public int vertexCount(){
		return _nodes.size();
	}
	
	public int edgeCount() {
		return _edges.size();
	}
	
	public Node addNode(String nodeId){
		if(_nodes.containsKey(nodeId)) return _nodes.get(nodeId);
		Node n = new Node(nodeId);
		_nodes.put(nodeId, n);
		_adjacent.put(n, new HashSet<Node>());
		return n;
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
	
	public void removeNode(Node n){
		if(n == null) return;
		_nodes.remove(n.getId());
		List<Edge> toRemove = new LinkedList<Edge>();
		for(Node adjacent : _adjacent.get(n)){
			Edge e = _edges.get(Graph.getEdgeId(n.getId(), adjacent.getId()));
			if(e != null){
				toRemove.add(e);
			}
		}
		for(Edge e : toRemove){
			removeEdge(e);
		}
	}
	
	public void removeEdge(Edge e){
		if(e == null) return;
		_edges.remove(e.getId());
		Node n1 = e.either();
		Node n2 = e.other(n1);
		
		_adjacent.get(n1).remove(n2);
		_adjacent.get(n2).remove(n1);
	}
	
	public void removeEdgeFromNodeIds(String from, String to){
		Edge e = getEdgeFromNodeIds(from, to);
		removeEdge(e);
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
	
	public int getNodeDegree(Node node){
		return _adjacent.get(node).size();
	}
	
	public Collection<String> getAllNodeIds() {
		return _nodes.keySet();
	}
	
	public Collection<Node> getAllNodes() {
		return _nodes.values();
	}
	
	public Collection<String> getAllEdgeIds() {
		return _edges.keySet();
	}
	
	public Collection<Edge> getAllEdges() {
		return _edges.values();
	}
}
