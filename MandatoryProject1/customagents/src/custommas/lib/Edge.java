package custommas.lib;

//Andreas (s092638)

public class Edge implements Comparable<Edge> {
	public static final int NonSurveyed = Integer.MAX_VALUE;
	private Node v;
	private Node w;
	private int weight;
	
	public Edge(Node node1, Node node2) {
		if(node1.hashCode() >= node2.hashCode()){
			this.v = node1;
			this.w = node2;
		}else{
			this.v = node2;
			this.w = node1;
		}
		this.weight = NonSurveyed;
	}
	
	public Edge(Node node1, Node node2, int weight) {
		this(node1, node2);
		this.weight = weight;
	}
	
	public Node either() {
		return v;
	}
	
	public Node other(Node n) {
		return n.getId().equals(v.getId())? w : v;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public void setWeight(int newWeight) {
		this.weight = newWeight;
	}
	
	public boolean isSurveyed(){
		return this.weight != NonSurveyed;
	}

	public int compareTo(Edge e) {
		return Integer.compare(getWeight(), e.getWeight());
	}
	
	public String getId(){
		return EdgeWeightedGraph.getEdgeId(v.getId(), w.getId());
	}
	
	@Override
	public String toString(){
		return "Edge(" + v.toString() + ", " + w.toString() + ")";
	}
}
