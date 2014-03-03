package custommas.lib;

import java.util.Collection;

public abstract class Graph {
	public abstract int vertexCount();
	public abstract int edgeCount();
	public abstract Collection<Node> getAdjacentTo(Node n);
}
