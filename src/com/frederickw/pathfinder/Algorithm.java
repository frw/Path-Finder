package com.frederickw.pathfinder;

import java.util.Collection;

public interface Algorithm {

	public abstract void init();

	public abstract boolean step();

	public abstract Collection<? extends Node> getUnvisitedNodes();

	public abstract Collection<? extends Node> getVisitedNodes();

	public abstract Node[] getPaths();

	public abstract void reset();

}
