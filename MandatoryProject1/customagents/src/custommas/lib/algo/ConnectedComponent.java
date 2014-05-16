package custommas.lib.algo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import custommas.common.SharedKnowledge;
import custommas.lib.Edge;
import custommas.lib.EdgeWeightedGraph;
import custommas.lib.Node;
import custommas.lib.Queue;
import custommas.lib.SimpleGraph;

//Andreas (s092638)

public class ConnectedComponent {
	private HashSet<Node> _nodes;
	private Node _minValueNode;
	private int _sum;
	private SimpleGraph _graph;
	
	private ConnectedComponent(){
		_nodes = new HashSet<Node>();
		_minValueNode = null;
		_sum = 0;
	}
	
	private void addNode(Node n){
		if(_minValueNode == null || n.getValue() < _minValueNode.getValue()){
			_minValueNode = n;
		}
		_sum += n.getValue();
		_nodes.add(n);
	}
	
	public int size(){
		return _nodes.size();
	}
	
	public int getSum(){
		return _sum;
	}
	
	public Node getMinValueNode(){
		return _minValueNode;
	}
	
	public Set<Node> getNodes(){
		return _nodes;
	}
	
	public SimpleGraph getGraph(){
		if(_graph == null){
			SimpleGraph graph = new SimpleGraph();
			
			for(Node node : _nodes){
				graph.addNode(node.getId());
				for(Node adjacent : SharedKnowledge.getGraph().getAdjacentTo(node)){
					if(!_nodes.contains(adjacent)) continue;
					graph.addEdge(node, adjacent);
				}
			}
			
			_graph = graph;
		}
		return _graph;
	}
	
	public static ArrayList<ConnectedComponent> getComponents(SimpleGraph graph){
		ArrayList<ConnectedComponent> components = new ArrayList<ConnectedComponent>();
		HashSet<String> visited = new HashSet<String>();
		ConnectedComponent currentComponent = null;
		
		for(Node n : graph.getAllNodes()){
			if(!visited.contains(n.getId())){
				if(currentComponent != null){
					components.add(currentComponent);
				}
				currentComponent = new ConnectedComponent();
				Queue<Node> queue = new Queue<Node>();
				queue.enqueue(n);
				visited.add(n.getId());
				currentComponent.addNode(n);
				
				while(!queue.isEmpty()){
					Node currentNode = queue.dequeue();
					for(Node adjacent : graph.getAdjacentTo(currentNode)){
						if(!visited.contains(adjacent.getId())){
							currentComponent.addNode(adjacent);
							queue.enqueue(adjacent);
							visited.add(adjacent.getId());
						}
					}
				}
			}
		}
		
		if(currentComponent != null && currentComponent.size() > 0){
			components.add(currentComponent);
		}
		
		return components;
	}
	
	public static int getSum(SimpleGraph graph, Node startingNode){
		HashSet<String> visited = new HashSet<String>();
		return getSum(graph, startingNode, visited);
	}
	
	private static int getSum(SimpleGraph graph, Node currentNode, HashSet<String> visited){
		visited.add(currentNode.getId());
		int sum = currentNode.getValue();
		for(Node adjacent : graph.getAdjacentTo(currentNode)){
			if(!visited.contains(adjacent.getId())){
				sum += getSum(graph, adjacent, visited);
			}
		}
		return sum;
	}
	
	public static int getSize(SimpleGraph graph, Node startingNode){
		HashSet<String> visited = new HashSet<String>();
		return getSize(graph, startingNode, visited);
	}
	
	private static int getSize(SimpleGraph graph, Node currentNode, HashSet<String> visited){
		visited.add(currentNode.getId());
		int size = 1;
		for(Node adjacent : graph.getAdjacentTo(currentNode)){
			if(!visited.contains(adjacent.getId())){
				size += getSize(graph, adjacent, visited);
			}
		}
		return size;
	}
}
