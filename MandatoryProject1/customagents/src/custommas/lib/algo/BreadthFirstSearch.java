package custommas.lib.algo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import custommas.lib.Graph;
import custommas.lib.Node;
import custommas.lib.Queue;
import custommas.lib.Stack;
import custommas.lib.interfaces.INodePredicate;

//Andreas (s092638)

public class BreadthFirstSearch {
    private HashSet<Node> marked; // marked[v] = is there an s-v path
    private HashMap<Node, Node> edgeTo; // edgeTo[v] = previous edge on shortest s-v path
    private Graph graph;

    public BreadthFirstSearch(Graph g) {
    	this.graph = g;
    }
    
    public Node findClosestUnexploredNode(Node startNode){
    	return findClosestNodeSatisfyingPredicate(startNode, new INodePredicate(){
    		public boolean evaluate(Node node, int comparableValue){
    			return !node.isProbed();
    		}
    	});
    }
    
    public Node findClosestNodeSatisfyingPredicate(Node startNode, INodePredicate predicate){
    	marked = new HashSet<Node>();
        edgeTo = new HashMap<Node, Node>();
        marked.add(startNode);
        
        int currentDepth = 0; 
        int elementsToDepthIncrease = 1;
        int nextElementsToDepthIncrease = 0;
        
        if(predicate.evaluate(startNode, currentDepth)) return startNode;
    	Queue<Node> q = new Queue<Node>();
        q.enqueue(startNode);

        while (!q.isEmpty()) {
            Node v = q.dequeue();
            Collection<Node> adjacent = graph.getAdjacentTo(v);
            nextElementsToDepthIncrease += adjacent.size();
            for (Node w : adjacent) {
            	if(--elementsToDepthIncrease < 1){
            		currentDepth++;
            		elementsToDepthIncrease = nextElementsToDepthIncrease;
            		nextElementsToDepthIncrease = 0;
            	}
                if (!marked.contains(w)) {
                	edgeTo.put(w, v);
                	marked.add(w);
                    q.enqueue(w);
                	if(predicate.evaluate(w, currentDepth)) return w;
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