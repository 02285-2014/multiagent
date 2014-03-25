package custommas.lib.interfaces;

import custommas.lib.Node;

public interface INodePredicate {
	boolean evaluate(Node node, int comparableValue);
}
