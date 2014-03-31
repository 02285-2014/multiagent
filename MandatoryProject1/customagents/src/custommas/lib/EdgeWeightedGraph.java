package custommas.lib;

import java.util.*;

public class EdgeWeightedGraph extends Graph {
	private Map<String, Node> _nodes;
	private Map<String, Edge> _edges;
	private Map<Node, Set<Node>> _adjacent;
	private Map<String, String> _agentLocations;
	private Set<String> _occupiedNodes;
	private Set<String> _probedNodes;
	
	public EdgeWeightedGraph() {
		_nodes = new HashMap<String, Node>();
		_edges = new HashMap<String, Edge>();
		_adjacent = new HashMap<Node, Set<Node>>();
		_agentLocations = new HashMap<String, String>();
		_occupiedNodes = new HashSet<String>();
		_probedNodes = new HashSet<String>();
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
	
	public void setAgentLocation(String agent, String location){
		_agentLocations.put(agent, location);
	}
	
	public void updateOccupiedNodes(){
		_occupiedNodes.clear();
		for(String location : _agentLocations.values()){
			_occupiedNodes.add(location);
		}
	}
	
	public boolean isNodeOccupied(String nodeId){
		return _occupiedNodes.contains(nodeId);
	}
	
	public void setNodeProbedValue(String nodeId, int probedValue){
		setNodeProbedValue(getNode(nodeId), probedValue);
	}
	
	public void setNodeProbedValue(Node node, int probedValue){
		_probedNodes.add(node.getId());
		node.setValue(probedValue);
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
	
	public boolean allNodesProbed(){
		return _probedNodes.size() >= _nodes.size();
	}
}
