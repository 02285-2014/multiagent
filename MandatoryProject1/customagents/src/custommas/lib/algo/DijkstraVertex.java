package custommas.lib.algo;


public class DijkstraVertex implements Comparable<DijkstraVertex> {
	private String id;
	private int distance;
	private DijkstraVertex previous;
	
	public DijkstraVertex(String id, int distance) {
		this.id = id;
		this.distance = distance;
		this.previous = null;
	}
	
	public String getId() {
		return id;
	}
	
	public int setDistance(int dist) {
		this.distance = dist;
		return distance;
	}
	
	public int getDistance() {
		return distance;
	}
	
	public DijkstraVertex getPrevious() {
		return previous;
	}
	
	public void setPrevious(DijkstraVertex p) {
		this.previous = p;
	}
	
	public int compareTo(DijkstraVertex v) {
		return Integer.compare(getDistance(), v.getDistance());
	}
	
}
