package fred.algorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import fred.Algorithm;
import fred.Node;
import fred.struct.BinaryHeap;

public class Dijkstra implements Algorithm {

    private final BinaryHeap<Node> unvisitedQueue = new BinaryHeap<Node>();
    private final Set<Node> unvisitedSet = Collections.synchronizedSet(new HashSet<Node>());
    private final Set<Node> visitedSet = Collections.synchronizedSet(new HashSet<Node>());

    @Override
    public void init() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean step() {
        // TODO Auto-generated method stub
        return false;
    }

    private double distance(final Node start, final Node end) {
        if (start.x != end.x && start.y != end.y) {
            return 1.414213562373;
        } else {
            return 1.0;
        }
    }

    @Override
    public Collection<? extends Node> getUnvisitedNodes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<? extends Node> getVisitedNodes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node getCurrentNode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

}
