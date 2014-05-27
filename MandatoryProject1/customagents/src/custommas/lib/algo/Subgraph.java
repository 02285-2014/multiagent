package custommas.lib.algo;

import custommas.common.SharedKnowledge;
import custommas.lib.Node;

import java.io.PrintWriter;
import java.util.*;
import custommas.lib.algo.ConnectedComponent;
import custommas.lib.EdgeWeightedGraph;

//Morten (s133304)

public class Subgraph {
	private Set<Node> _placements;
	private Set<Node> _dominated;
	private int _score;

	public Subgraph() {
		_placements = new HashSet<Node>();
		_dominated = new HashSet<Node>();
		_score = 0;
	}
	
	public Subgraph(ConnectedComponent component) {
		_placements = new HashSet<Node>(component.getNodes());
		_dominated = new HashSet<Node>(component.getNodes());
		_score = component.getSum();
	}
	
	public Subgraph(Subgraph sg) {
		_placements = new HashSet<Node>(sg.getPlacements());
		_dominated = new HashSet<Node>(sg.getDominated());
		_score = sg.score();
	}
	
	public void addPlacement(Node oldPlace, Node newPlace) {
		_placements.remove(oldPlace);
		_placements.add(newPlace);
		
		int oldPlaceValue = (oldPlace.getValue() == Integer.MIN_VALUE ? 1 : oldPlace.getValue()); 
		int newPlaceValue = (newPlace.getValue() == Integer.MIN_VALUE ? 1 : newPlace.getValue());
		
		_dominated.add(newPlace);
		_score += newPlaceValue;
		
		Set<Node> newDominatedNodes = new HashSet<Node>();
		if(checkPlace(oldPlace, newPlace)) {
			for(Node n : SharedKnowledge.getGraph().getAdjacentTo(newPlace)) {
				if(!_dominated.contains(n)) {
					for(Node neighbour : SharedKnowledge.getGraph().getAdjacentTo(n)) {
						if(!neighbour.equals(newPlace) && _placements.contains(neighbour)) {
							System.out.println(neighbour+" IS NOW DOMINATED");
							newDominatedNodes.add(neighbour);
							break;
						}
					}
				}
			}
			for(Node n : newDominatedNodes) {
				_dominated.add(n);
			}
		} else {
			_score = _score - oldPlaceValue;
		}
	}
	
	public void addPlacement(Node placement) {
		_placements.add(placement);
	}
	
	
	public Set<Node> getPlacements() {
		return _placements;
	}
	
	public Set<Node> getDominated() {
		return _dominated;
	}
	
	public void addDominated(Node dominatedNode) {
		_dominated.add(dominatedNode);
		_score += dominatedNode.getValue();
	}
	
	public void removeDominated(Node dominatedNode) {
		_dominated.remove(dominatedNode);
	}
	
	public void removePlacement(Node placementNode) {
		_placements.remove(placementNode);
	}
	
	public int score() {
		return _score;
	}
	
	// Returns true if new placement expands subgraph and false otherwise
	public boolean checkPlacement(Node oldPlace, Node newPlace) {
		EdgeWeightedGraph ewg = SharedKnowledge.getGraph();
		for(Node n : ewg.getAdjacentTo(newPlace)) {
			if(!n.equals(oldPlace) && _placements.contains(n)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean checkPlace(Node oldPlace, Node newPlace) {
		EdgeWeightedGraph ewg = SharedKnowledge.getGraph();
		for(Node n : ewg.getAdjacentTo(oldPlace)) {
			if(!n.equals(newPlace) && _placements.contains(n)) {
				return true;
			}
		}
		return false;
	}
	
	public NodePair getOuterPlacement(Set<Node> checkedNodes) {
		for(Node n : _placements) {
			if(!checkedNodes.contains(n)) {
				for(Node neighbor : SharedKnowledge.getGraph().getAdjacentTo(n)) {
					if(!_placements.contains(neighbor) && !_dominated.contains(neighbor)) {
						return new NodePair(n, neighbor);
					}
				}
			}
		}
		return null;
	}
	
	public class NodePair {
		  private final Node _oldPlace;
		  private final Node _newPlace;

		  public NodePair(Node oldPlace, Node newPlace) {
		    _oldPlace = oldPlace;
		    _newPlace = newPlace;
		  }
		  
		  public Node getOldPlace() {
			  return _oldPlace;
		  }
		  
		  public Node getNewPlace() {
			  return _newPlace;
		  }
		}
}
