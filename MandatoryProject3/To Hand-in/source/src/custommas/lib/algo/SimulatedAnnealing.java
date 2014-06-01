package custommas.lib.algo;

import java.util.*;

import custommas.common.SharedKnowledge;
import custommas.lib.Node;
import custommas.lib.SimpleGraph;
import custommas.lib.algo.ConnectedComponent;
import custommas.lib.algo.ComponentMaximumSum;
import custommas.lib.algo.Subgraph.NodePair;

//Morten (s133304)

public class SimulatedAnnealing {
	private static Subgraph _subgraph;
	private static Set<Node> _checkedNodes;
	
	private static double acceptable(int currentEnergy, int newEnergy, double temperature) {
		if(currentEnergy < newEnergy) {
			return 1.0;
		}
		return Math.exp((currentEnergy - newEnergy) / temperature);
	}
	
	public static Subgraph getSubgraph(int agents) {
		SimpleGraph sg = new SimpleGraph(SharedKnowledge.getGraph());
		ConnectedComponent maxSumComponent = ComponentMaximumSum.getMaximumSumComponent(sg, agents);
		
		_checkedNodes = new HashSet<Node>();
		
		_subgraph = new Subgraph(maxSumComponent);
		System.out.println("Initial score: "+_subgraph.score());
		
		double temp = 1000;
		
		double coolingRate = 0.025;
		
		while(temp > 1) {
			NodePair pair = _subgraph.getOuterPlacement(_checkedNodes);
			
			if(pair != null) {
				Subgraph newSol = new Subgraph(_subgraph);
				System.out.println("OLD PLACE - NEW PLACE: "+pair.getOldPlace() +" - "+pair.getNewPlace());
				newSol.addPlacement(pair.getOldPlace(), pair.getNewPlace());
				_checkedNodes.add(pair.getOldPlace());
				
				if (acceptable(_subgraph.score(), newSol.score(), temp) > Math.random()) {
		            _subgraph = new Subgraph(newSol);
		        }
			}
			
			
			temp *= 1-coolingRate;
		}
		
		return _subgraph;
	}
}
