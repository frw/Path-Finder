package fred;

public class Node implements Comparable<Node> {

    public final int x;
    public final int y;
    public Node parent = null;
    public double distance = 0.0;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Node(Node node) {
        this.x = node.x;
        this.y = node.y;
    }

    public Node newNode(int x, int y) {
        return new Node(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Node) {
            Node n = (Node) o;
            return n.x == x && n.y == y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return x << 16 + y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public int compareTo(Node node) {
        double difference = distance - node.distance;
        if (difference < 0) {
            return -1;
        } else if (difference > 0) {
            return 1;
        } else {
            return 0;
        }
    }

}
