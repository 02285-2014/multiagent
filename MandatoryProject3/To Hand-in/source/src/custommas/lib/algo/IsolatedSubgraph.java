package custommas.lib.algo;

import custommas.lib.Node;
import custommas.lib.Graph;
import custommas.common.SharedKnowledge;
import java.util.*;

//Morten (s133304)

public class IsolatedSubgraph {
	private static final int _THRESHOLD = 6;
	
	public static Set<Node> formSubGraph(Node center, int agents){
		if(!allowedCenter(center)) { return null; }
		
		Graph g = SharedKnowledge.getGraph();
		Set<Node> explored = new HashSet<Node>();
		explored.add(center);
		Set<Node> subgraph = new HashSet<Node>();
		
		subgraph.addAll(g.getAdjacentTo(center));
		
		while(subgraph.size() < agents) {
			explored.addAll(subgraph);
			Set<Node> temp = new HashSet<Node>();
			for(Node n : subgraph) {
				for(Node neigh : g.getAdjacentTo(n)) {
					if(!explored.contains(neigh)) {
						temp.add(neigh);
					}
				}
			}
			subgraph.clear();
			subgraph.addAll(temp);			
		}
		
		if(subgraph.size() == agents) {
			return subgraph;
		} else {
			int noagentnode = subgraph.size() - agents;
			int nodesfound = 0;
			Set<Node> nodestodelete = new HashSet<Node>();
			for(Node n : subgraph) {
				int i = 0;
				for(Node neigh : g.getAdjacentTo(n)) {
					if(subgraph.contains(neigh)) {
						i++;
					}
					if(i >= 2) {
						nodesfound++;
						//subgraph.remove(n);
						nodestodelete.add(n);
					}
				}
				if(nodesfound == noagentnode) {
					break;
				}
			}
			subgraph.removeAll(nodestodelete);
			if(subgraph.size() != agents) {
				return null;
			}
			
			return subgraph;
		}
	}
	
	
	public static boolean allowedCenter(Node n) {
		return n.getValue() >= _THRESHOLD;
	}
}
