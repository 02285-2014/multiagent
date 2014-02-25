package custommas.lib;

import java.util.*;

public class Node {
	private String id;
	private int value;
	private Set<String> adjacents;
	
	public Node(String name) {
		this.id = name;
		adjacents = new HashSet<String>();
		this.value = Integer.MIN_VALUE;
	}
	
	public Node(String name, int value) {
		this(name);
		this.value = value;;
	}
	
	public String getId() {
		return id;
	}
	
	public int value() {
		return value;
	}
	
	public void setValue(int newValue) {
		this.value = newValue;
	}
	
	public Set<String> getAdjacents() {
		return adjacents;
	}
	
	public void addAdjacent(String otherNode) {
		adjacents.add(otherNode);
	}
	
	@Override
	public boolean equals(Object o) {
	    if (o instanceof Node) 
	    {
	      Node c = (Node) o;
	      if ( this.getId().equals(c.getId()) )
	         return true;
	    }
	    return false;
	}
}
