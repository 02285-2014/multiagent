package custommas.lib;

import java.util.*;

public class Node {
	private String id;
	private int value;
	private ArrayList<Edge> adjacents;
	
	public Node(String name) {
		this.id = name;
		adjacents = new ArrayList<Edge>();
		this.value = Integer.MIN_VALUE;
	}
	
	public Node(String name, int value) {
		this.id = name;
		this.value = value;
		adjacents = new ArrayList<Edge>();
	}
	
	public String getID() {
		return id;
	}
	
	public int value() {
		return value;
	}
	
	public void setValue(int newValue) {
		this.value = newValue;
	}
	
	public ArrayList<Edge> getAdjacents() {
		return adjacents;
	}
	
	public void addAdjacent(Edge e) {
		adjacents.add(e);
	}
	
	@Override
	public boolean equals(Object o) {
	    if (o instanceof Node) 
	    {
	      Node c = (Node) o;
	      if ( this.getID().equals(c.getID()) )
	         return true;
	    }
	    return false;
	}
}
