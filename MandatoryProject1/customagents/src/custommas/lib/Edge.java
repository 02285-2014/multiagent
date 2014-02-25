package custommas.lib;

public class Edge implements Comparable<Edge> {
	private Node v;
	private Node w;
	private int weight;
	
	public Edge(Node from, Node to, int weight) {
		this.v = from;
		this.w = to;
		this.weight = weight;
	}
	
	public Node either() {
		return v;
	}
	
	public Node other(Node n) {
		if(n.equals(v)) {
			return w;
		} else if(n.equals(w)) {
			return v;
		} else {
			return null;
		}
	}
	
	public int weight() {
		return weight;
	}
	
	public void setWeight(int newWeight) {
		this.weight = newWeight;
	}

	public int compareTo(Edge e) {
		if      (this.weight() < e.weight()) { return -1; }
        else if (this.weight() > e.weight()) { return +1; }
        else { return  0; }
	}
}