package custommas.lib.algo;

import java.util.*;

import custommas.lib.*;

public class Dijkstra {
	private static final int maxDistance = Integer.MAX_VALUE; 
	private static Set<String> nodes;
	private static PriorityQueue<DijkstraVertex> pq;
	private static Map<String, DijkstraVertex> vertices;
	
	public static List<Node> getPath(EdgeWeightedGraph g, Node initial, Node goal) {
		if(initial.equals(goal)) {
			List<Node> i = new ArrayList<Node>();
			i.add(initial);
			return i;
		}
		nodes = (Set<String>) g.getAllNodeIds();
		pq = new PriorityQueue<DijkstraVertex>();
		vertices = new HashMap<String,DijkstraVertex>();
		HashSet<String> visited = new HashSet<String>();

		DijkstraVertex v = new DijkstraVertex(initial.getId(), 0);
		pq.add(v);
		vertices.put(initial.getId(), v);
		
		for(String s : nodes) {
			if(!s.equals(initial.getId())) {
				DijkstraVertex vs = new DijkstraVertex(s, maxDistance);
				pq.add(vs);
				vertices.put(s,vs);
			}
		}
		
		while (!pq.isEmpty()) {
		    DijkstraVertex u = pq.poll();
		    visited.add(u.getId());
		    for(Node n : g.getAdjacentTo(g.getNode(u.getId()))) {
		    	if(visited.contains(n.getId())) continue;
		    	Edge e = g.getEdgeFromNodes(g.getNode(u.getId()), n);
		    	int weight = e.getWeight();
		    	int distanceThroughU = u.setDistance(u.getDistance() + weight);
		    	DijkstraVertex curVertex = vertices.get(n.getId());
		    	if(distanceThroughU < curVertex.getDistance()) {
		    		pq.remove(curVertex);
		    		curVertex.setDistance(distanceThroughU);
		    		curVertex.setPrevious(u);
		    		pq.add(curVertex);
		    	}
		    }
		}

		List<Node> path = new ArrayList<Node>();
		DijkstraVertex target = vertices.get(goal.getId());
		for (DijkstraVertex dv = target; dv != null; dv = dv.getPrevious()) {
			path.add(g.getNode(dv.getId()));
		}
		Collections.reverse(path);
		return path;
	}
}

