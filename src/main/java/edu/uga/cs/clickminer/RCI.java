package edu.uga.cs.clickminer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import edu.uga.cs.clickminer.graph.builder.RefererGraphBuilder;
import edu.uga.cs.clickminer.graph.model.RefererGraph;
import edu.uga.cs.clickminer.graph.pruner.GraphPruner;
import edu.uga.cs.clickminer.graph.pruner.RefererGraphAdPruner;
import edu.uga.cs.clickminer.graph.pruner.RefererGraphDelayPruner;
import edu.uga.cs.clickminer.graph.pruner.RefererGraphHeaderPruner;
import edu.uga.cs.clickminer.graph.pruner.RefererGraphMissingPruner;
import edu.uga.cs.clickminer.graph.pruner.RefererGraphSocialMediaPruner;

public class RCI {

	private RefererGraphBuilder builder;
	
	public RCI(RefererGraphBuilder builder) {
		this.builder = builder;
	}
	
	public Pair<RefererGraph, RefererGraph> applyRCI(double refererDelayThresh, boolean pruneAds) throws Exception{
		List<GraphPruner<RefererGraph>> pruners = new ArrayList<GraphPruner<RefererGraph>>();
		pruners.add(new RefererGraphDelayPruner(refererDelayThresh));
		pruners.add(new RefererGraphMissingPruner());
		pruners.add(new RefererGraphHeaderPruner());
		if (pruneAds) {
			pruners.add(new RefererGraphAdPruner());
			pruners.add(new RefererGraphSocialMediaPruner());
		}
		
		RefererGraph beforeGraph = builder.getGraph();
		RefererGraph afterGraph = builder.getGraph();
		for (GraphPruner<RefererGraph> p : pruners) {
			p.pruneGraph(afterGraph);
		}
		return new ImmutablePair<RefererGraph, RefererGraph>(beforeGraph, afterGraph);
	}

}
