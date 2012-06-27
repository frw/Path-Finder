package com.frederickw.pathfinder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class MapData {

	public static int width = 50;
	public static int height = 30;
	public static Node source = new Node(1, 1);
	public static Node target = new Node(2, 2);
	public static final Set<Node> walls = new HashSet<Node>();

	public static <E extends Node> List<E> getTraversable(E node) {
		List<E> traversable = new LinkedList<E>();
		for (int dX = -1; dX <= 1; dX++) {
			for (int dY = -1; dY <= 1; dY++) {
				if ((dX == 0 && dY == 0) || (dX < 0 && node.x == 0)
						|| (dX > 0 && node.x == width - 1)
						|| (dY < 0 && node.y == 0)
						|| (dY > 0 && node.y == height - 1)) {
					continue;
				}
				@SuppressWarnings("unchecked")
				E n = (E) node.newNode(node.x + dX, node.y + dY);
				if (!walls.contains(n)
						&& (dX == 0 || dY == 0 || (!walls.contains(new Node(
								node.x + dX, node.y)) && !walls
								.contains(new Node(node.x, node.y + dY))))) {
					traversable.add(n);
				}
			}
		}
		return traversable;
	}

}
