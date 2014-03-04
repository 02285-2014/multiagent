package custommas.lib.algo;

import java.util.*;

import custommas.lib.*;

public class Dijkstra {
	private static final int maxDistance = Integer.MAX_VALUE; 
	private static Set<String> nodes;
	private static Map<String, Integer> distance;
	private static PriorityQueue<DijkstraVertex> pq;
	private static Map<String, DijkstraVertex> vertices;
	
	public static Collection<Node> getPath(EdgeWeightedGraph g, Node initial, Node goal) {
		if(initial.equals(goal)) {
			Set<Node> i = new HashSet<Node>();
			i.add(initial);
			return i;
		}
		nodes = (Set<String>) g.getAllNodes();
		pq = new PriorityQueue<DijkstraVertex>();
		vertices = new HashMap<String,DijkstraVertex>();
		
		distance = new HashMap<String, Integer>();
		distance.put(initial.getId(), 0);
		DijkstraVertex v = new DijkstraVertex(initial.getId(), 0);
		pq.add(v);
		vertices.put(initial.getId(), v);
		
		for(String s : nodes) {
			if(!s.equals(initial.getId())) {
				pq.add(new DijkstraVertex(s, maxDistance));
				vertices.put(s,v);
			}
		}
		
		while (!pq.isEmpty()) {
		    DijkstraVertex u = pq.poll();
		    
		    for(Node n : g.getAdjacentTo(g.getNode(u.getId()))) {
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

