package custommas.lib.algo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import custommas.common.SharedKnowledge;
import custommas.lib.Node;
import custommas.lib.SimpleGraph;

//Andreas (s092638)

public class ComponentMaximumSum {
	private SimpleGraph _graph;
	private int _maxComponentSize;
	private ConnectedComponent _maxSumComponent;
	
	private ComponentMaximumSum(SimpleGraph graph, int maxComponentSize){
		_graph = graph;
		_maxComponentSize = maxComponentSize;
		_maxSumComponent = null;
	}
	
	private void findMaxComponent(){
		List<Node> preRemove = new LinkedList<Node>();
		for(Node n : _graph.getAllNodes()){
			if(n.getNumberOfOccupantsForTeam(SharedKnowledge.OpponentTeam) > 0){
				preRemove.add(n);
			}
		}
		for(Node n : preRemove){
			_graph.removeNode(n);
		}
		
		int maxSize = Integer.MAX_VALUE;
		while(maxSize > _maxComponentSize && _graph.vertexCount() > _maxComponentSize){
			ArrayList<ConnectedComponent> components = ConnectedComponent.getComponents(_graph);
			if(components.size() < 1) break;
			
			_maxSumComponent = components.get(0);			
			maxSize = 0;
			
			for(int i = 0; i < components.size(); i++){
				ConnectedComponent component = components.get(i);
				if(maxSize < component.size()){
					maxSize = component.size();
				}
				if(_maxSumComponent.getSum() < component.getSum()){
					_maxSumComponent = component;
				}
				if(component.size() > _maxComponentSize){
					_graph.removeNode(component.getMinValueNode());
				}
			}
		}
	}
	
	public static ConnectedComponent getMaximumSumComponent(SimpleGraph graph, int maxComponentSize){
		ComponentMaximumSum cms = new ComponentMaximumSum(graph, maxComponentSize);
		cms.findMaxComponent();
		return cms._maxSumComponent;
	}
}
