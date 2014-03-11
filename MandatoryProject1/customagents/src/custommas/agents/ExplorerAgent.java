package custommas.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import custommas.common.SharedUtility;
import custommas.lib.Edge;
import custommas.lib.EdgeWeightedGraph;
import custommas.lib.Node;
import custommas.lib.Queue;
import custommas.lib.Stack;
import custommas.lib.algo.BreadthFirstExplorerSearch;
import custommas.lib.algo.Dijkstra;
import custommas.ui.ExplorerInput;
import custommas.ui.IInputCallback;

import apltk.interpreter.data.LogicBelief;

import eis.iilang.Action;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import massim.javaagents.agents.MarsUtil;

public class ExplorerAgent extends CustomAgent{

	private BreadthFirstExplorerSearch _unprobedSearch;
	private ExplorerInput _input;
	private Queue<String> _destinationGoals;
	private Queue<String> _goalInsertQueue;
	private List<Node> _pathToDestinationGoal;
	private boolean allProbed;
	
	public ExplorerAgent(String name, String team) {
		super(name, team);
		_destinationGoals = new Queue<String>();
		_goalInsertQueue = new Queue<String>();
		_pathToDestinationGoal = null;
		_input = new ExplorerInput(name, new IInputCallback() {
			@Override
			public void inputReceived(String input){
				boolean validId = SharedUtility.isValidNodeId(input);
				println("Received destination goal: " + input + " [" + (validId ? "VALID" : "INVALID") + "]");
				if(validId){
					_goalInsertQueue.enqueue(input);
				}
			};
		});
	}

	@Override
	protected Action nextAction() {
		Action act = null;
		
		// Add the goals here to take care of race conditions.
		if(_goalInsertQueue.size() > 0){
			_destinationGoals.enqueue(_goalInsertQueue.dequeue());
		}
		
		//1. recharging
		act = planRecharge(1.0/3.0);
		if (act != null) return act;
		
//		// 2. buying battery with a certain probability
//		act = planBuyBattery();
//		if ( act != null ) return act;

		
		LinkedList<LogicBelief> positionBeliefs = getAllBeliefs("position");
		if (positionBeliefs.size() == 0) {
			println("strangely I do not know my position, I'll recharge");
			return MarsUtil.rechargeAction();
		}
		String position = positionBeliefs.getFirst().getParameters().firstElement();
		
		// 3. probing if necessary (neighbour, probed, position)
		act = planProbe(position);
		if (act != null) return act;
		
		// 4. surveying if necessary (visibleEdge,surveyEdge,position)
		act = planSurvey(position);
		if (act != null) return act;
		
		Node currentNode = _graph.getNode(position);
		println("Current node: " + currentNode);
		
		Node moveToNode = null;
		
		if(_destinationGoals.size() > 0){
			println("Trying to reach goal: " + _destinationGoals.peek());
			if(_destinationGoals.peek().equals(currentNode.getId())){
				// Goal reached
				_pathToDestinationGoal = null;
				_destinationGoals.dequeue();
				_input.showGoalFound(currentNode.getId());
			}else{
				if(_pathToDestinationGoal == null){
					Node goalNode = _graph.getNode(_destinationGoals.peek());
					if(goalNode != null){
						// New goal
						_pathToDestinationGoal = Dijkstra.getPath(_graph, currentNode, goalNode);
					}
				}
				if(_pathToDestinationGoal != null){
					while(_pathToDestinationGoal.size() > 0 && _pathToDestinationGoal.get(0).equals(currentNode)){
						_pathToDestinationGoal.remove(0);
					}
					if(_pathToDestinationGoal.size() > 0){
						moveToNode = _pathToDestinationGoal.get(0);
					}
				}
			}
		}
		
		if(moveToNode != null && !_graph.getAdjacentTo(currentNode).contains(moveToNode)){
			println("Trying to move to node i cant move to!");
			moveToNode = null;
		}
		
		if(moveToNode != null){
			println("On my way to my goal I will move from " + currentNode.getId() + " to " + moveToNode.getId());
			return MarsUtil.gotoAction(moveToNode.getId());
		}
		
		if(allProbed){
			if(_destinationGoals.size() > 0 && _graph.getNode(_destinationGoals.peek()) == null){
				println("Invalid goal input, removing it!");
				_destinationGoals.dequeue();
			}
			println("All nodes probed, recharging while waiting for commands!");
			return MarsUtil.rechargeAction();
		}
		
		_unprobedSearch = new BreadthFirstExplorerSearch(_graph);
		moveToNode = _unprobedSearch.findClosestUnexploredNode(currentNode);
		
		if(moveToNode == null){
			println("No unprobed node found!");
			allProbed = true;
		}
		
		// 5. Find closest unprobed node
		act = planNextUnprobed(position, moveToNode);
		if(act != null) return act;
		
		
		return hasMaxEnergy() ? MarsUtil.skipAction() : MarsUtil.rechargeAction();
		
	}
	
	@SuppressWarnings("deprecation")
	protected void handlePercepts(){
		if(!_name.equals("agentA1")) return;
		String position = null;

		// check percepts
		Collection<Percept> percepts = getAllPercepts();
		List<Percept> neighbourPercepts = new ArrayList<Percept>();
		
		// Remove beliefs for last action
		removeBeliefs("visibleEntity");
		removeBeliefs("visibleEdge");
		
		for (Percept p : percepts) {
			//println("Handling percept: " + p.getName());
			if (p.getName().equals("step")) {
				println(p);
				continue;
			}
			
			if (p.getName().equals("visibleEntity")) {
				LinkedList<Parameter> parameters = p.getParameters();
				if(parameters.size() >= 4){
					ListIterator<Parameter> parametersIterator = parameters.listIterator();
					String entityName = parametersIterator.next().toProlog();
					String entityNode = parametersIterator.next().toProlog();
					String entityTeam = parametersIterator.next().toProlog();
					String entityStatus = parametersIterator.next().toProlog();
					
					println("Visible entity with name " + entityName + " on node " + entityNode + 
						" with status " + entityStatus + " for team " + entityTeam);
				}
				
				LogicBelief b = MarsUtil.perceptToBelief(p);
				if (!containsBelief(b)) {
					addBelief(b);
				}
				continue;
			}
			
			if (p.getName().equals("visibleEdge")) {
				LogicBelief b = MarsUtil.perceptToBelief(p);
				if (!containsBelief(b)) {
					addBelief(b);
				}
				neighbourPercepts.add(p);
				
				LinkedList<Parameter> parameters = p.getParameters();
				if(parameters.size() >= 2){
					ListIterator<Parameter> parametersIterator = parameters.listIterator();
					
					String node1 = parametersIterator.next().toProlog();
					String node2 = parametersIterator.next().toProlog();
					
					println("Visible Edge Nodes "+ node1 + " AND " + node2);
					
					_graph.addEdgeFromNodeIds(node1, node2);
				}
				continue;
			}
			
			if (p.getName().equals("probedVertex")) {
				LogicBelief b = MarsUtil.perceptToBelief(p);
				if (!containsBelief(b)) {
					LinkedList<Parameter> parameters = p.getParameters();
					if(parameters.size() >= 2){
						ListIterator<Parameter> parametersIterator = parameters.listIterator();
						
						String nodeId = parametersIterator.next().toProlog();
						int value = Integer.parseInt(parametersIterator.next().toProlog());
						
						Node node = _graph.getNode(nodeId);
						node.setValue(value);
						
						println("I perceive the value of " + node.toString() + " that I have not known before: " + value);
					}else{
						println("I perceive the value of a vertex that I have not known before");
					}
					addBelief(b);
					broadcastBelief(b);
				}
				continue;
			}
			
			if (p.getName().equals("surveyedEdge")) {
				LogicBelief b = MarsUtil.perceptToBelief(p);
				if (!containsBelief(b)) {
					LinkedList<Parameter> parameters = p.getParameters();
					if(parameters.size() >= 3){
						ListIterator<Parameter> parametersIterator = parameters.listIterator();
						
						String node1 = parametersIterator.next().toProlog();
						String node2 = parametersIterator.next().toProlog();
						int weight = Integer.parseInt(parametersIterator.next().toProlog());
						
						Edge edge = _graph.getEdgeFromNodeIds(node1, node2);
						edge.setWeight(weight);
						
						println("I perceive the weight of " + edge.toString() + " that I have not known before: " + weight);
					}else{
						println("I perceive the weight of an edge that I have not known before [Non-specific]");
					}
					addBelief(b);
					broadcastBelief(b);
				}
				continue;
			}
			
			if (p.getName().equals("health")) {
				int health = Integer.parseInt(p.getParameters().get(0).toString());
				
				removeBeliefs("health");
				addBelief(new LogicBelief("health", Integer.toString(health)));
				
				println("my health is " +health );
				if (health == 0 ) {
					println("my health is zero. asking for help");
					broadcastBelief(new LogicBelief("iAmDisabled"));
				}
				continue;
			}
			
			if (p.getName().equals("maxHealth")) {
				int maxHealth = Integer.parseInt(p.getParameters().get(0).toString());
				
				removeBeliefs("maxHealth");
				addBelief(new LogicBelief("maxHealth", Integer.toString(maxHealth)));
				continue;
			}
			
			if (p.getName().equals("position")) {
				position = p.getParameters().get(0).toString();
				removeBeliefs("position");
				addBelief(new LogicBelief("position", position));
				println("Found my position to be: " + position);
				continue;
			}
			
			if (p.getName().equals("energy")) {
				int energy = Integer.parseInt(p.getParameters().get(0).toString());
				removeBeliefs("energy");
				addBelief(new LogicBelief("energy", Integer.toString(energy)));
				continue;
			}
			
			if (p.getName().equals("maxEnergy")) {
				Integer maxEnergy = new Integer(p.getParameters().get(0).toString());
				removeBeliefs("maxEnergy");
				addBelief(new LogicBelief("maxEnergy",maxEnergy.toString()));
				continue;
			}
			
			if (p.getName().equals("money")) {
				int money = Integer.parseInt(p.getParameters().get(0).toString());
				removeBeliefs("money");
				addBelief(new LogicBelief("money", Integer.toString(money)));
				continue;
			}
			
			if (p.getName().equals("achievement")) {
				println("reached achievement " + p);
				continue;
			}
		}
		
		// again for checking neighbors
		this.removeBeliefs("neighbor");
		if(position != null){
			EdgeWeightedGraph graph = _graph;
			Node currentNode = graph.getNode(position);
			println("Current node: " + currentNode);
			for (Percept p : neighbourPercepts) {
				String vertex1 = p.getParameters().get(0).toString();
				String vertex2 = p.getParameters().get(1).toString();
				if (vertex1.equals(position)){ 
					addBelief(new LogicBelief("neighbor",vertex2));
					Node neighbourNode = graph.getNode(vertex2);
					graph.addEdge(currentNode,  neighbourNode);
				}else if (vertex2.equals(position)){ 
					addBelief(new LogicBelief("neighbor",vertex1));
					Node neighbourNode = graph.getNode(vertex1);
					graph.addEdge(currentNode,  neighbourNode);
				}
			}
		}
	}
	
	private Action planProbe(String position) {
		// probe current position if not known
		boolean probed = false;
		LinkedList<LogicBelief> vertices = getAllBeliefs("probedVertex");
		for (LogicBelief v : vertices) {
			if (v.getParameters().get(0).equals(position)) {
				probed = true;
				break;
			}
		}
		
		if (!probed) {
			println("I do not know the value of my position. I will probe.");
			return MarsUtil.probeAction();
		}
		
		println("I know the value of my position");
		return null;
	}
	
	private Action planSurvey(String position) {
		// get all neighbors
		LinkedList<LogicBelief> visible = getAllBeliefs("visibleEdge");
		LinkedList<LogicBelief> surveyed = getAllBeliefs("surveyedEdge");
		
		println("I know " + visible.size() + " visible edges");
		println("I know " + surveyed.size() + " surveyed edges");
		
		int unsurveyedNum = 0;
		int adjacentNum = 0;
		
		for (LogicBelief v : visible) {
			String vVertex0 = v.getParameters().elementAt(0);
			String vVertex1 = v.getParameters().elementAt(1);

			boolean adjacent = false;
			if (vVertex0.equals(position) || vVertex1.equals(position)){
				adjacent = true;
			}
			
			if (!adjacent) continue;
			adjacentNum ++;
			
			boolean isSurveyed = false;
			for (LogicBelief s : surveyed) {
				String sVertex0 = s.getParameters().elementAt(0);
				String sVertex1 = s.getParameters().elementAt(1);
				if (sVertex0.equals(vVertex0) &&  sVertex1.equals(vVertex1)) {
					isSurveyed = true;
					break;
				}
				if (sVertex0.equals(vVertex1) &&  sVertex1.equals(vVertex0)) {
					isSurveyed = true;
					break;
				}
			}
			if (!isSurveyed) unsurveyedNum++;
		}

		println("" + unsurveyedNum + " out of " + adjacentNum + " adjacent edges are unsurveyed");
		
		if (unsurveyedNum > 0) {
			println("I will survey");
			return MarsUtil.surveyAction();
		}
		
		return null;
	}
	
	private Action planNextUnprobed(String position, Node firstUnprobed) {
		println("Trying to find unprobed node from position: " + position);
		if(firstUnprobed != null){
			Stack<Node> pathToUnprobed = _unprobedSearch.pathTo(firstUnprobed);
			if(pathToUnprobed.size() > 1){
				if(pathToUnprobed.peek().getId().equals(position)){
					pathToUnprobed.pop();
				}
				println("Found unprobed node, can reach it by movin from " + position + " to " + pathToUnprobed.peek().getId());
				return MarsUtil.gotoAction(pathToUnprobed.peek().getId());
			}
		}
		
		println("Couldn't find an unprobed node");
		return null;
	}
}
