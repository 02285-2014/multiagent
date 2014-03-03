package custommas.lib;

public class Node {
	private static final int NonProbed = Integer.MIN_VALUE;
	private String id;
	private int value;
	
	public Node(String name) {
		this.id = name;
		this.value = NonProbed;
	}
	
	public Node(String name, int value) {
		this(name);
		this.value = value;;
	}
	
	public String getId() {
		return id;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int newValue) {
		this.value = newValue;
	}
	
	public boolean isProbed(){
		return this.value != NonProbed;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Node && this.getId().equals(((Node)o).getId());
	}
	
	@Override
	public int hashCode(){
		return id.hashCode();
	}
	
	@Override
	public String toString(){
		return "Node(" + id + ")";
	}
}
