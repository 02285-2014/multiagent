package custommas.common;

import custommas.lib.EdgeWeightedGraph;
import custommas.lib.algo.ConnectedComponent;

public class SharedKnowledge {
	private static EdgeWeightedGraph _graph = new EdgeWeightedGraph();
	
	public static EdgeWeightedGraph getGraph(){
		return _graph;
	}
	
	private static boolean _maxSumInitiated = false;
	private static ConnectedComponent _maxSumComponent = null;
	public static ConnectedComponent getMaxSumComponent(){
		return _maxSumComponent;
	}
	
	public static void setMaxSumComponent(ConnectedComponent maxSumComponent){
		_maxSumComponent = maxSumComponent;
	}
	
	public static boolean getMaxSumInitiated(){
		return _maxSumInitiated;
	}
	
	public static void setMaxSumInitiated(boolean val){
		_maxSumInitiated = val;
	}
}
