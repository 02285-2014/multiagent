package custommas.lib.algo;

//Morten (s133304)
public class DijkstraVertex implements Comparable<DijkstraVertex> {
	private String id;
	private double distance;
	private DijkstraVertex previous;
	private int step;
	
	public DijkstraVertex(String id, double distance) {
		this.id = id;
		this.distance = distance;
		this.previous = null;
		step = -1;
	}
	
	public String getId() {
		return id;
	}
	
	public double setDistance(double dist) {
		this.distance = dist;
		return distance;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public int getStep() {
		return step;
	}
	
	public void setStep(int step) {
		this.step = step;
	}
	
	public DijkstraVertex getPrevious() {
		return previous;
	}
	
	public void setPrevious(DijkstraVertex p) {
		this.previous = p;
	}
	
	public int compareTo(DijkstraVertex v) {
		return Double.compare(getDistance(), v.getDistance());
	}
	
}
