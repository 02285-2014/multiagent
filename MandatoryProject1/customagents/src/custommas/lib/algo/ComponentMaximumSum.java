package custommas.lib.algo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import custommas.lib.Node;
import custommas.lib.SimpleGraph;

public class ComponentMaximumSum {
	private SimpleGraph _graph;
	private int _maxComponentSize;
	private ConnectedComponent _maxSumComponent;
	private String _team;
	
	private ComponentMaximumSum(SimpleGraph graph, int maxComponentSize, String team){
		_graph = graph;
		_maxComponentSize = maxComponentSize;
		_team = team;
		_maxSumComponent = null;
	}
	
	private void findMaxComponent(){
		List<Node> preRemove = new LinkedList<Node>();
		for(Node n : _graph.getAllNodes()){
			if(_team != null && !n.getOwner().equals("none") && !n.getOwner().equals(_team)){
				preRemove.add(n);
			}
		}
		for(Node n : preRemove){
			//System.out.println("Pre removing node: " + n + " (" + _team + " != " + n.getOwner() + ")");
			_graph.removeNode(n);
		}
		
		int maxSize = Integer.MAX_VALUE;
		while(maxSize > _maxComponentSize && _graph.vertexCount() > _maxComponentSize){
			ArrayList<ConnectedComponent> components = ConnectedComponent.getComponents(_graph);
			if(components.size() < 1) break;
			//System.out.println("Found " + components.size() + " components");
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
					//System.out.println("Removing node: " + component.getMinValueNode());
					_graph.removeNode(component.getMinValueNode());
				}
			}
			//System.out.println("Current max size is " + maxSize);
			//System.out.println("Current max sum is " + _maxSumComponent.getSum());
		}
	}
	
	public static ConnectedComponent getMaximumSumComponent(SimpleGraph graph, int maxComponentSize){
		return getMaximumSumComponent(graph, maxComponentSize, null);
	}
	
	public static ConnectedComponent getMaximumSumComponent(SimpleGraph graph, int maxComponentSize, String team){
		ComponentMaximumSum cms = new ComponentMaximumSum(graph, maxComponentSize, team);
		cms.findMaxComponent();
		return cms._maxSumComponent;
	}
}
