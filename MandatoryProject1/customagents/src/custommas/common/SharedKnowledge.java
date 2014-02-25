package custommas.common;

import custommas.lib.EdgeWeightedGraph;

public class SharedKnowledge {
	private static EdgeWeightedGraph _graph = new EdgeWeightedGraph();
	
	public EdgeWeightedGraph graph(){
		return _graph;
	}
}
