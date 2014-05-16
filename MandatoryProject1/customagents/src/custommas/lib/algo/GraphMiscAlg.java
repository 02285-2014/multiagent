package custommas.lib.algo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import custommas.lib.Node;
import custommas.lib.Queue;
import custommas.lib.SimpleGraph;

//Andreas (s092638)

public class GraphMiscAlg {
	public static Node nodeFarthestAway(SimpleGraph graph, Collection<Node> sourceNodes){
		HashSet<Node> visited = new HashSet<Node>();
		Queue<Node> queue = new Queue<Node>();
		for(Node n : sourceNodes){
			queue.enqueue(n);
		}
		
		Node lastNode = null;
		while(!queue.isEmpty()){
			Node n = queue.dequeue();
			visited.add(n);
			lastNode = n;
			for(Node adjacent : graph.getAdjacentTo(n)){
				if(!visited.contains(adjacent)){
					queue.enqueue(adjacent);
				}
			}
		}
		
		return lastNode;
	}
	
	public static Set<Node> maxDistanceNodes(SimpleGraph graph, int nodesToPlace){
		if(nodesToPlace >= graph.vertexCount()){
			return new HashSet<Node>(graph.getAllNodes());
		}else if(nodesToPlace < 1){
			return new HashSet<Node>();
		}
		
		HashSet<Node> maxDistance = new HashSet<Node>();
		Node minDegree = null;
		for(Node n : graph.getAllNodes()){
			if(minDegree == null || graph.getNodeDegree(minDegree) > graph.getNodeDegree(n)){
				minDegree = n;
			}
		}
		
		maxDistance.add(minDegree);
		while(maxDistance.size() < nodesToPlace){
			Node farthestAway = nodeFarthestAway(graph, maxDistance);
			maxDistance.add(farthestAway);
		}
		
		return maxDistance;
	}
}
