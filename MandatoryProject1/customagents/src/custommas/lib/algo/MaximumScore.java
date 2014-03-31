package custommas.lib.algo;

import java.util.*;
import java.util.Stack;

import custommas.lib.*;

public class MaximumScore {
	private static int index;
	private static Stack<Node> nodes;

	public static Set<Node> MaximizeScore(EdgeWeightedGraph g, Set<Node> dominatedSet) {
		Set<Set<Node>> sccs = TarjanSCC(g);
		int maxValue = Integer.MIN_VALUE;
		Set<Node> sccToReturn = null;
		
		for(Set<Node> s : sccs) {
			boolean enemyInScc = false;
			int val = 0;
			for(Node n : s) {
				if(dominatedSet.contains(n)) {
					enemyInScc = true;
					break;
				} else {
					val += n.getValue();
				}
			}
			
			if(!enemyInScc && val > maxValue) {
				sccToReturn = s;
			}
		}
		
		for(Node n : g.getAllNodes()) {
			n.setIndex(Integer.MAX_VALUE);
			n.setLowLink(Integer.MAX_VALUE);
			n.unFlag();
		}
		
		return sccToReturn;
	}
	
	private static Set<Set<Node>> TarjanSCC(EdgeWeightedGraph g) {
		index = 0;
		
		Set<Set<Node>> scc = new HashSet<Set<Node>>();
		
		nodes = new Stack<Node>();
		for(Node v : g.getAllNodes()) {
			if(v.getIndex() == Integer.MAX_VALUE) {
				Set<Node> s = StrongConnect(g, v);
				if(s != null) {
					scc.add(s);
				}
			}
		}
		
		return scc;
	}
	
	private static Set<Node> StrongConnect(EdgeWeightedGraph g, Node n) {
		n.setIndex(index);
		n.setLowLink(index);
		index++;
		
		nodes.push(n);
		n.flag();
		
		for(Node v : g.getAdjacentTo(n)) {
			if(v.getIndex() == Integer.MAX_VALUE) {
				StrongConnect(g,v);
				n.setLowLink(Math.min(n.getLowLink(), v.getLowLink()));
			} else if(v.isFlagged()) { //nodes.contains(v)
				n.setLowLink(Math.min(n.getLowLink(),v.getIndex()));
			}
		}
		Set<Node> scc = null;
		if(n.getLowLink() == n.getIndex()) {
			scc = new HashSet<Node>();
			Node an;
			while((an = nodes.pop()) != n) {
				scc.add(an);
			}
			
		}
		return scc;
	}
}
