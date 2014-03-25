package custommas.lib;

public class Node {
	private static final int NonProbed = Integer.MIN_VALUE;
	private String _id;
	private String _owner;
	private int _value;
	
	public Node(String name) {
		if(name == null) throw new NullPointerException("Name cannot be null");
		_id = name;
		_value = NonProbed;
	}
	
	public Node(String name, int value) {
		this(name);
		_value = value;;
	}
	
	public String getId() {
		return _id;
	}
	
	public int getValue() {
		return _value;
	}
	
	public void setValue(int newValue) {
		_value = newValue;
	}
	
	public void setOwner(String owner){
		_owner = owner;
	}
	
	public boolean isProbed(){
		return _value != NonProbed;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Node && this.getId().equals(((Node)o).getId());
	}
	
	@Override
	public int hashCode(){
		return _id.hashCode();
	}
	
	@Override
	public String toString(){
		return "Node(" + _id + ")";
	}
}
