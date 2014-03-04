package custommas.lib.algo;

import java.util.HashMap;
import java.util.HashSet;

import custommas.lib.Graph;
import custommas.lib.Node;
import custommas.lib.Queue;
import custommas.lib.Stack;

public class BreadthFirstExplorerSearch {
    private HashSet<Node> marked; // marked[v] = is there an s-v path
    private HashMap<Node, Node> edgeTo; // edgeTo[v] = previous edge on shortest s-v path
    private Graph graph;

    public BreadthFirstExplorerSearch(Graph g) {
    	this.graph = g;
    }
    
    public Node findClosestUnexploredNode(Node startNode){
    	marked = new HashSet<Node>();
        edgeTo = new HashMap<Node, Node>();
        marked.add(startNode);
        
        //if(!startNode.isProbed()) return startNode;
    	Queue<Node> q = new Queue<Node>();
        q.enqueue(startNode);

        while (!q.isEmpty()) {
            Node v = q.dequeue();
            for (Node w : graph.getAdjacentTo(v)) {
                if (!marked.contains(w)) {
                	edgeTo.put(w, v);
                	marked.add(w);
                    q.enqueue(w);
                	if(!w.isProbed()) return w;
                }
            }
        }
        return null;
    }
    
    public boolean hasPathTo(Node to) {
        return marked.contains(to);
    }

    public Stack<Node> pathTo(Node to) {
        if (!hasPathTo(to)) return new Stack<Node>();
        Stack<Node> path = new Stack<Node>();
        
        Node w = to;
        do {
        	path.push(w);
        } while((w = edgeTo.get(w)) != null);

        return path;
    }
}