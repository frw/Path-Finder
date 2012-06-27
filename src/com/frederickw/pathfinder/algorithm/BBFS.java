package com.frederickw.pathfinder.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.frederickw.pathfinder.Algorithm;
import com.frederickw.pathfinder.Node;
import com.frederickw.pathfinder.PathFinder;
import com.frederickw.pathfinder.struct.BinaryHeap;

public class BBFS implements Algorithm {

	private final BinaryHeap<Node> unvisitedQueueS = new BinaryHeap<Node>();
	private final Set<Node> unvisitedSetS = Collections
			.synchronizedSet(new HashSet<Node>());
	private final Set<Node> visitedSetS = Collections
			.synchronizedSet(new HashSet<Node>());

	private final BinaryHeap<Node> unvisitedQueueT = new BinaryHeap<Node>();
	private final Set<Node> unvisitedSetT = Collections
			.synchronizedSet(new HashSet<Node>());
	private final Set<Node> visitedSetT = Collections
			.synchronizedSet(new HashSet<Node>());

	private Node currentS = null;
	private Node currentT = null;

	private Node path = null;

	@Override
	public void init() {
		Node source = new Node(PathFinder.getSource());
		unvisitedQueueS.add(source);
		unvisitedSetS.add(source);
		Node target = new Node(PathFinder.getTarget());
		unvisitedQueueT.add(target);
		unvisitedSetT.add(target);
	}

	@Override
	public boolean step() {
		if (!unvisitedQueueS.isEmpty() && !unvisitedQueueT.isEmpty()) {
			currentS = unvisitedQueueS.poll();
			if (unvisitedSetT.contains(currentS)) {
				path = makePath(currentS, unvisitedSetT);
				return true;
			} else if (visitedSetT.contains(currentS)) {
				path = makePath(currentS, visitedSetT);
				return true;
			}
			unvisitedSetS.remove(currentS);
			visitedSetS.add(currentS);
			for (final Node next : PathFinder.getTraversable(currentS)) {
				if (!visitedSetS.contains(next)) {
					final double dist = currentS.distance
							+ distance(currentS, next);
					if (!unvisitedSetS.contains(next)) {
						next.parent = currentS;
						next.distance = dist;
						unvisitedQueueS.add(next);
						unvisitedSetS.add(next);
					} else {
						final Node old = getNode(unvisitedSetS, next);
						if (dist < old.distance) {
							old.parent = currentS;
							old.distance = dist;
							unvisitedQueueS.resort(old);
						}
					}
				}
			}

			currentT = unvisitedQueueT.poll();
			if (unvisitedSetS.contains(currentT)) {
				path = makePath(currentT, unvisitedSetS);
				return true;
			} else if (visitedSetS.contains(currentT)) {
				path = makePath(currentT, visitedSetS);
				return true;
			}
			unvisitedSetT.remove(currentT);
			visitedSetT.add(currentT);
			for (final Node next : PathFinder.getTraversable(currentT)) {
				if (!visitedSetT.contains(next)) {
					final double dist = currentT.distance
							+ distance(currentT, next);
					if (!unvisitedSetT.contains(next)) {
						next.parent = currentT;
						next.distance = dist;
						unvisitedQueueT.add(next);
						unvisitedSetT.add(next);
					} else {
						final Node old = getNode(unvisitedSetT, next);
						if (dist < old.distance) {
							old.parent = currentT;
							old.distance = dist;
							unvisitedQueueT.resort(old);
						}
					}
				}
			}

			return false;
		} else {
			currentS = null;
			currentT = null;
			return true;
		}
	}

	private Node makePath(Node intersect, Set<Node> set) {
		Node n = getNode(set, intersect);
		Node p = n;
		Node c = intersect;
		do {
			Node temp = c.parent;
			c.parent = p;
			p = c;
			c = temp;
		} while (c != null);
		return p;
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
		List<Node> nodes = new ArrayList<Node>(unvisitedSetS.size()
				+ unvisitedSetT.size());
		nodes.addAll(unvisitedSetS);
		nodes.addAll(unvisitedSetT);
		return nodes;
	}

	@Override
	public Collection<? extends Node> getVisitedNodes() {
		List<Node> nodes = new ArrayList<Node>(visitedSetS.size()
				+ visitedSetT.size());
		nodes.addAll(visitedSetS);
		nodes.addAll(visitedSetT);
		return nodes;
	}

	@Override
	public Node[] getPaths() {
		if (path != null) {
			return new Node[] { path };
		} else {
			return new Node[] { currentS, currentT };
		}
	}

	@Override
	public void reset() {
		unvisitedQueueS.clear();
		unvisitedSetS.clear();
		visitedSetS.clear();
		unvisitedQueueT.clear();
		unvisitedSetT.clear();
		visitedSetT.clear();
		currentS = null;
		currentT = null;
		path = null;
	}

}
