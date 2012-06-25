package fred;

import java.util.Collection;

public interface Algorithm {

    public abstract void init();

    public abstract boolean step();

    public abstract Collection<? extends Node> getUnvisitedNodes();

    public abstract Collection<? extends Node> getVisitedNodes();

    public abstract Node getCurrentNode();

    public abstract void reset();

}
