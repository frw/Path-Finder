package fred.algorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import fred.Algorithm;
import fred.Node;
import fred.PathFinder;
import fred.struct.BinaryHeap;

public class Dijkstra implements Algorithm {

	private final BinaryHeap<Node> unvisitedQueue = new BinaryHeap<Node>();
	private final Set<Node> unvisitedSet = Collections
			.synchronizedSet(new HashSet<Node>());
	private final Set<Node> visitedSet = Collections
			.synchronizedSet(new HashSet<Node>());

	private Node current;

	@Override
	public void init() {
		Node source = new Node(PathFinder.getSource());
		unvisitedQueue.add(source);
		unvisitedSet.add(source);
	}

	@Override
	public boolean step() {
		if (!unvisitedQueue.isEmpty()) {
			current = unvisitedQueue.poll();
			unvisitedSet.remove(current);
			visitedSet.add(current);
			if (current.equals(PathFinder.getTarget())) {
				return true;
			}
			for (final Node next : PathFinder.getTraversable(current)) {
				if (!visitedSet.contains(next)) {
					final double dist = current.distance
							+ distance(current, next);
					if (!unvisitedSet.contains(next)) {
						next.parent = current;
						next.distance = dist;
						unvisitedQueue.add(next);
						unvisitedSet.add(next);
					} else {
						final Node old = getNode(unvisitedSet, next);
						if (dist < old.distance) {
							old.parent = current;
							old.distance = dist;
							unvisitedQueue.resort(old);
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

	private Node getNode(Set<Node> nodes, Node n) {
		for (Node node : nodes) {
			if (node.equals(n)) {
				return node;
			}
		}
		return null;
	}

	@Override
	public Collection<? extends Node> getUnvisitedNodes() {
		return unvisitedSet;
	}

	@Override
	public Collection<? extends Node> getVisitedNodes() {
		return visitedSet;
	}

	@Override
	public Node getCurrentNode() {
		return current;
	}

	@Override
	public void reset() {
		unvisitedQueue.clear();
		unvisitedSet.clear();
		visitedSet.clear();
		current = null;
	}

}
