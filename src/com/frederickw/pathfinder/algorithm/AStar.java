package com.frederickw.pathfinder.algorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.frederickw.pathfinder.Algorithm;
import com.frederickw.pathfinder.Node;
import com.frederickw.pathfinder.PathFinder;
import com.frederickw.pathfinder.struct.BinaryHeap;

public class AStar implements Algorithm {

	public static final Heuristic MANHATTAN_DISTANCE = new ManhattanDistance();
	public static final Heuristic CHEBYSHEV_DISTANCE = new ChebyshevDistance();
	public static final Heuristic EUCLIDEAN_DISTANCE = new EuclideanDistance();

	private final Heuristic heuristic;

	private final BinaryHeap<AStarNode> openQueue = new BinaryHeap<AStarNode>();
	private final Set<AStarNode> openSet = Collections
			.synchronizedSet(new HashSet<AStarNode>());
	private final Set<AStarNode> closedSet = Collections
			.synchronizedSet(new HashSet<AStarNode>());

	private AStarNode current = null;

	public AStar(Heuristic heuristic) {
		this.heuristic = heuristic;
	}

	@Override
	public void init() {
		AStarNode source = new AStarNode(PathFinder.getSource());
		openQueue.add(source);
		openSet.add(source);
	}

	@Override
	public boolean step() {
		if (!openQueue.isEmpty()) {
			current = openQueue.poll();
			openSet.remove(current);
			closedSet.add(current);
			if (current.equals(PathFinder.getTarget())) {
				return true;
			}
			for (final AStarNode next : PathFinder.getTraversable(current)) {
				if (!closedSet.contains(next)) {
					final double dist = current.distance
							+ distance(current, next);
					if (!openSet.contains(next)) {
						next.parent = current;
						next.distance = dist;
						next.cost = dist + heuristic.calculate(next);
						openQueue.add(next);
						openSet.add(next);
					} else {
						final AStarNode old = getNode(openSet, next);
						if (dist < old.distance) {
							old.parent = current;
							old.distance = dist;
							old.cost = dist + heuristic.calculate(next);
							openQueue.resort(old);
						}
					}
				}
			}
			return false;
		} else {
			current = null;
			return true;
		}
	}

	private double distance(final Node start, final Node end) {
		if (start.x != end.x && start.y != end.y) {
			return 1.414213562373;
		} else {
			return 1.0;
		}
	}

	private AStarNode getNode(Set<AStarNode> nodes, AStarNode n) {
		for (AStarNode node : nodes) {
			if (node.equals(n)) {
				return node;
			}
		}
		return null;
	}

	@Override
	public Collection<AStarNode> getUnvisitedNodes() {
		return openSet;
	}

	@Override
	public Collection<AStarNode> getVisitedNodes() {
		return closedSet;
	}

	@Override
	public Node[] getPaths() {
		return new Node[] { current };
	}

	@Override
	public void reset() {
		openQueue.clear();
		openSet.clear();
		closedSet.clear();
		current = null;
	}

	private class AStarNode extends Node {

		double cost = 0.0;

		public AStarNode(int x, int y) {
			super(x, y);
		}

		public AStarNode(Node node) {
			super(node);
		}

		@Override
		public AStarNode newNode(int x, int y) {
			return new AStarNode(x, y);
		}

		@Override
		public int compareTo(Node node) {
			double difference = cost - ((AStarNode) node).cost;
			if (difference < 0) {
				return -1;
			} else if (difference > 0) {
				return 1;
			} else {
				return 0;
			}
		}

	}

	public static abstract class Heuristic {

		public final double calculate(Node current) {
			return calculate(current, PathFinder.getTarget());
		}

		protected abstract double calculate(Node current, Node target);

	}

	private static class ManhattanDistance extends Heuristic {

		@Override
		protected double calculate(Node current, Node target) {
			return Math.abs(current.x - target.x)
					+ Math.abs(current.y - target.y);
		}

	}

	private static class ChebyshevDistance extends Heuristic {

		@Override
		protected double calculate(Node current, Node target) {
			return Math.max(Math.abs(current.x - target.x),
					Math.abs(current.y - target.y));
		}

	}

	private static class EuclideanDistance extends Heuristic {

		@Override
		protected double calculate(Node current, Node target) {
			return Math.hypot(current.x - target.x, current.y - target.y);
		}

	}

}
