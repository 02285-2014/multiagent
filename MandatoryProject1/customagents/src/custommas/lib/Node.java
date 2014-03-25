package custommas.lib;

public class Node {
	private static final int NonProbed = Integer.MIN_VALUE;
	private String id;
	private int value;
	private int index;
	private int lowlink;
	private boolean flag = false;
	
	public Node(String name) {
		this.id = name;
		this.value = NonProbed;
		this.index = Integer.MAX_VALUE;
		this.lowlink = Integer.MAX_VALUE;
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
	
	public void setIndex(int newIndex) {
		this.index = newIndex;
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getLowLink() {
		return lowlink;
	}
	
	public void flag() {
		flag = true;
	}
	
	public void unFlag() {
		flag = false;
	}
	
	public boolean isFlagged() {
		return flag;
	}
	
	public void setLowLink(int newLowLink) {
		this.lowlink = newLowLink;
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
