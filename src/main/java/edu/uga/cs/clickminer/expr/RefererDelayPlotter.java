package edu.uga.cs.clickminer.expr;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.DefaultWeightedEdge;

import edu.uga.cs.clickminer.graph.builder.RefererGraphBuilder;
import edu.uga.cs.clickminer.graph.model.RefererEdge;
import edu.uga.cs.clickminer.graph.model.RefererGraph;
import edu.uga.cs.clickminer.util.OctaveUtils;
import edu.uga.cs.clickminer.util.ResultsUtils.FileType;

/**
 * <p>RefererDelayPlotter class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RefererDelayPlotter.java 901 2014-04-25 03:27:51Z cjneasbitt $Id
 */
public class RefererDelayPlotter {

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(OctaveUtils.histOctavePlot(getRefererDelays(args[0], FileType.PCAP)));
	}
	
	private static List<Double> getRefererDelays(String resultsdir, FileType type) throws Exception{
		List<Double> retval = new ArrayList<Double>();
		RefererGraphBuilder builder = new RefererGraphBuilder(resultsdir, type);
		RefererGraph graph = builder.getGraph();
		for(DefaultWeightedEdge edge : graph.edgeSet()){
			if(edge instanceof RefererEdge){
				RefererEdge refedge = (RefererEdge)edge;
				retval.add(refedge.getTimeDelta());
			}
		}
		return retval;
	}
}
