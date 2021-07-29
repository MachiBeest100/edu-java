package com.shaharyi.strategy;

import java.util.*;

/*
 * Selection:
 *   Traverse the tree to find a leaf node = not "visited" = numRollouts==0
 * Expansion:
 *   Choose or create a child of this leaf with numRollouts=0.
 * Simulation:
 *   Do a complete roll-out while creating nodes on the go, with "numRollouts=0".
 *   For this, use a snapshot of the given board.
 * Back-propagation:
 *   Propagate the result up: 
 *   nominator: +1 if win for this node's player, +0.5 for draw.
 *   denom:     +1 always.
 */
public class MCTS implements Algorithm, NodeFactory {
    static double C_PARAM = Math.sqrt(2.0);

	Runtime runtime = Runtime.getRuntime();
	Random rand = new Random();
	long allotedMillis = 5 * 1000;
	int NSIM = 100;
	long timer;

	
	// time, memory
	boolean resources_left() {
		return (System.currentTimeMillis() < timer + allotedMillis);
		/*-
		 * long maxMemory = runtime.maxMemory();
		 * long allocatedMemory = runtime.totalMemory();
		 * long freeMemory = runtime.freeMemory();
		 */
	}

    public NodeFactory getNodeFactory() {
        return this;
    }

    public Node createNode(Node parent, int[] move, int color) {
        Node<MCData> n = new Node<>(parent, move);
        MCData data = new MCData(color);
        n.setData(data);
        return n;
    }
	
    public Node search(Board board) {
        return search(board, NSIM);
    }
	public Node search(Board board, int nsim) {
	    int simulation_result = 0;
		Node<MCData> leaf;
		Node<MCData> current = (Node<MCData>)board.getCurrentNode();
		timer = System.currentTimeMillis();
		int i = 0;
		while (resources_left() && i < nsim) {
			leaf = traverse(board, current); // select + expand
		    simulation_result = rollout(board, leaf);
		    backpropagate(board, leaf, current, simulation_result);
		    i++;
		}
        System.out.println("simulations=" + current.getData().getNumRollouts());
        Node<MCData> n = current;
        while (n.getParent()!=null) 
            n = n.getParent();
        System.out.println("total simulations=" + n.getData().getNumRollouts());
        board.print();
        return bestUCT(current, 0);  //exploitation only        
//		return bestChild(current);
	}

	// Select - child with zero rollouts
	// Expand - if no children create all and select one
	Node traverse(Board board, Node<MCData> node) {
	    while (nonTerminal(board) && fullyExpanded(node)) {
            node = bestUCT(node);
            board.makeMove(node, node.getData().getPlayer());
        }
		if (node.getChildren() == null)
		    board.generateMoves(-node.getData().getPlayer());
		Node unvisited = pickUnivisted(node.getChildren());
		
		// in case no children are present / node is terminal
		//@TODO but returning the node itself accumulates duplicate results
		return (unvisited != null) ? unvisited : node;
	}

	// Are all children visited?
	boolean fullyExpanded(Node<MCData> node) {
	    if (node.getChildren() == null)
	        return false;
		for (Node<MCData> child : node.getChildren()) {
			if (child.getData().getNumRollouts() == 0)
				return false;
		}
		return true;
	}

    Node bestUCT(Node<MCData> node) {
        return bestUCT(node, C_PARAM);
    }
        
    Node bestUCT(Node<MCData> node, double cparam) {
//	    final static double EPSILON = 1e-6;	    
		Node<MCData> selected = null;
		double bestValue = Double.MIN_VALUE;
		for (Node<MCData> c : node.getChildren()) {
			double w = c.getData().getNumWins();
			int n = c.getData().getNumRollouts();
			int N = node.getData().getNumRollouts();
			double uctValue = (w / n) + cparam * Math.sqrt(Math.log(N) / n);
//            double uctValue = totValue / (nVisits + EPSILON)
//                    + Math.sqrt(Math.log(parentVisits + 1) / (nVisits + EPSILON)) + rand.nextDouble() * EPSILON;
            // small random number to break ties randomly in unexpanded nodes
			// System.out.println("UCT value = " + uctValue);
			if (uctValue > bestValue) {
				selected = c;
				bestValue = uctValue;
			}
		}
		return selected;
	}

	boolean nonTerminal(Board board) {
		boolean t = board.isTerminal();
		Node<MCData> node = board.getCurrentNode();
		if (!t && node.getChildren() == null)
	            board.generateMoves(-node.getData().getPlayer());
		return !t && node.getChildren().length > 0;
	}

	// function for the result of the simulation
	int rollout(Board board, Node<MCData> node) {
	    // make this (unvisited) move
        board.makeMove(node, node.getData().getPlayer());
	    
		while (nonTerminal(board)) {
			node = rolloutPolicy(board, node);
			board.makeMove(node, node.getData().getPlayer());
		}
		return board.getWinner();
	}

	// function for randomly selecting a child node
	Node rolloutPolicy(Board board, Node<MCData> node) {
		if (node.getChildren() == null)
			board.generateMoves(-node.getData().getPlayer());

		Node[] children = node.getChildren();
		int i = rand.nextInt(children.length);
		return children[i];
	}

	void backpropagate(Board board, Node<MCData> leaf, Node<MCData> current, int result) {
	    boolean update = false;
	    boolean undo = true;
	    Node<MCData> node = board.getCurrentNode();
	    while (node != null) {
    		if (node == leaf)
    			update = true;
    		if (node == current)
    		    undo = false;
	        if (update)
	            node.getData().update(result);
	        if (undo)
	            board.undoMove(node);
    		node = node.getParent();
	    }
	}

	/*
	 * function for selecting the best child node with highest number of visits
	 */
	Node bestChild(Node node) {
		int max = -1;
		Node best = null;
		for (Node<MCData> child : node.getChildren()) {
			int nRollouts = child.getData().getNumRollouts();
			if (nRollouts > max) {
				max = nRollouts;
				best = child;
			}
		}
		return best;
	}

	Node pickUnivisted(Node[] nodes) {
		for (Node<MCData> node : nodes) {
			if (node.getData().getNumRollouts() == 0)
				return node;
		}
		return null;
	}
}
