/*
* Copyright (C) 2013 Chris Neasbitt
* Author: Chris Neasbitt
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package edu.uga.cs.clickminer.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.graph.DefaultWeightedEdge;

import pcap.reconst.http.datamodel.TimestampedHttpMessage;

import edu.uga.cs.clickminer.graph.model.AbstractFlowVertex;
import edu.uga.cs.clickminer.graph.model.AbstractInteractionVertex;
import edu.uga.cs.clickminer.graph.model.AbstractRefererEdge;
import edu.uga.cs.clickminer.graph.model.FlowVertex;
import edu.uga.cs.clickminer.graph.model.InferredInteractionVertex;
import edu.uga.cs.clickminer.graph.model.InteractionGraph;
import edu.uga.cs.clickminer.graph.model.MinedInteractionVertex;
import edu.uga.cs.clickminer.graph.model.MissingInteractionVertex;
import edu.uga.cs.clickminer.graph.model.RefererGraph;

/**
 * <p>InteractionRefererGraphMapper class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: InteractionRefererGraphMapper.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class InteractionRefererGraphMapper {

	private static transient final Log log = LogFactory.getLog(InteractionRefererGraphMapper.class);
	
	/**
	 * <p>augmentInteractionGraph.</p>
	 *
	 * @param igraph a {@link edu.uga.cs.clickminer.graph.model.InteractionGraph} object.
	 * @param rgraph a {@link edu.uga.cs.clickminer.graph.model.RefererGraph} object.
	 */
	public static void augmentInteractionGraph(InteractionGraph igraph, 
			RefererGraph rgraph){
		Map<String, List<FlowVertex>> refGraphIndex = 
				InteractionRefererGraphMapper.buildRefererGraphIndex(rgraph);
		Map<String, List<AbstractInteractionVertex>> intGraphIndex = 
				InteractionRefererGraphMapper.buildInteractionGraphIndex(igraph);
		List<MissingInteractionVertex> startVerts = getMissingInteractionVertices(igraph);
		for(MissingInteractionVertex vert : startVerts){
			if(igraph.containsVertex(vert)){
				do{
					vert = replaceMissingInteraction(vert, igraph, rgraph, refGraphIndex,
							intGraphIndex);
				} while(vert != null);
			}
		}
	}
	
	private static MissingInteractionVertex replaceMissingInteraction(
			MissingInteractionVertex vert, InteractionGraph igraph, RefererGraph rgraph,
			Map<String, List<FlowVertex>> refGraphIndex,
			Map<String, List<AbstractInteractionVertex>> intGraphIndex){
		MissingInteractionVertex retval = null;
		FlowVertex match = findBestMatch(vert, refGraphIndex);
		if(match != null){
			AbstractInteractionVertex curinfvert = igraph.findMatchingVertex(match.getFlow());
			if(curinfvert != null){
				if(log.isDebugEnabled()){
					log.debug("Found already existing inferred interaction.");
				}
				replaceInInteractionGraphIndex(vert, curinfvert, intGraphIndex);
				for(DefaultWeightedEdge edge : igraph.outgoingEdgesOf(vert)){
					igraph.addEdge(curinfvert, igraph.getEdgeTarget(edge));
				}
				igraph.removeVertex(vert);
			} else { 
				InferredInteractionVertex infvert = new InferredInteractionVertex(match.getFlow(),
						rgraph.getRefererDelay(match));
				replaceInInteractionGraphIndex(vert, infvert, intGraphIndex);
				igraph.replaceVertex(vert, infvert);
				AbstractInteractionVertex parent = findBestMatchForReferer(infvert, intGraphIndex);
				if(parent != null){
					igraph.addEdge(parent, infvert);
					if(parent instanceof MissingInteractionVertex){
						retval = (MissingInteractionVertex)parent;
					}
				} else {
					Set<AbstractRefererEdge> inEdges = rgraph.incomingEdgesOf(match);
					if(inEdges.size() > 1){
						throw new RuntimeException("FlowVertex of type " + match.getClass() + " has " + inEdges.size() + " incoming edges.");
					} else if(inEdges.size() == 1){
						AbstractFlowVertex temp = rgraph.getEdgeSource(inEdges.iterator().next());
						if(temp instanceof FlowVertex){
							FlowVertex parentVert = (FlowVertex)temp;
							retval = new MissingInteractionVertex(parentVert.getUrl(), 
									infvert.getRequest().getStartTS());
							igraph.addVertex(retval);
							igraph.addEdge(retval, infvert);
						}
					}	
				}
			}
		}
		return retval;
	}
	
	private static AbstractInteractionVertex findBestMatchForReferer(InferredInteractionVertex infvert, 
			Map<String, List<AbstractInteractionVertex>> intGraphIndex){
		AbstractInteractionVertex retval = null;
		if(infvert.getRequest().containsHeader("Referer")){
			String refurl = infvert.getRequest().getFirstHeader("Referer").getValue();
			List<AbstractInteractionVertex> endtimes = 
					new ArrayList<AbstractInteractionVertex>();
			double limit = infvert.getRequest().getStartTS();
			List<AbstractInteractionVertex> matches = intGraphIndex.get(refurl);
			if(matches != null){
				for(AbstractInteractionVertex vert : intGraphIndex.get(refurl)){
					boolean addend = false;
					addend = vert instanceof MissingInteractionVertex && 
							((MissingInteractionVertex)vert).getTimestampLimit() < limit;
					addend = !addend && vert instanceof MinedInteractionVertex &&
							((MinedInteractionVertex)vert).getInteractionRecord()
							.getRequest().getEndTS() < limit;
					addend = !addend && vert instanceof InferredInteractionVertex &&
							((InferredInteractionVertex)vert).getRequest().getEndTS() < limit;
					if(addend){
						endtimes.add(vert);	
					}
				}
			}
			if(endtimes.size() > 0){
				Collections.sort(endtimes, new Comparator<AbstractInteractionVertex>(){
					@Override
					public int compare(AbstractInteractionVertex o1,
							AbstractInteractionVertex o2) {
						ImmutablePair<Double, Double> o1times = this.getTimestamps(o1);
						ImmutablePair<Double, Double> o2times = this.getTimestamps(o2);
						int val = o1times.getRight().compareTo(o2times.getRight());// sort by endtime
						if(val == 0 && (o1times.getLeft() == null && o2times.getLeft() == null)){ //sort by start time
							if(o1times.getLeft() == null){
								val = -1;
							} else if(o2times.getLeft() == null){
								val = 1;
							} else {
								val = o1times.getLeft().compareTo(o2times.getLeft());
							}
						}
						return val;
					}
					
					private ImmutablePair<Double, Double> getTimestamps(
							AbstractInteractionVertex o1){
						ImmutablePair<Double, Double> retval = null;
						if(o1 instanceof MissingInteractionVertex){
							MissingInteractionVertex vert = (MissingInteractionVertex)o1;
							retval = new ImmutablePair<Double, Double>(null, vert.getTimestampLimit());
						} else if(o1 instanceof MinedInteractionVertex) {
							TimestampedHttpMessage vert = ((MinedInteractionVertex)o1).getInteractionRecord().getRequest();
							retval = new ImmutablePair<Double, Double>(vert.getStartTS(), vert.getEndTS());
						} else if(o1 instanceof InferredInteractionVertex) {
							TimestampedHttpMessage vert = ((InferredInteractionVertex)o1).getRequest();
							retval = new ImmutablePair<Double, Double>(vert.getStartTS(), vert.getEndTS());
						}
						return retval;
					}
				});
				retval = endtimes.get(endtimes.size() - 1);
			}
		}
		return retval;
	}
	
	private static FlowVertex findBestMatch(MissingInteractionVertex vert, 
			Map<String, List<FlowVertex>> refGraphIndex){
		FlowVertex retval = null;
		List<FlowVertex> verts = refGraphIndex.get(vert.getUrl());
		if(verts != null){
			for(FlowVertex fvert : verts){
				if(fvert.getRequest().getEndTS() <= 
						vert.getTimestampLimit() 
						&& (retval == null || 
						retval.getRequest().getStartTS() <
						fvert.getRequest().getStartTS())){
					retval = fvert;
				}
			}
		}
		return retval;
	}
	
	private static void replaceInInteractionGraphIndex(AbstractInteractionVertex orig,
			AbstractInteractionVertex replacement, 
			Map<String, List<AbstractInteractionVertex>> intGraphIndex){
		if(orig.getUrl() == replacement.getUrl()){
			List<AbstractInteractionVertex> vals = intGraphIndex.get(orig.getUrl());
			if(vals.contains(replacement)){
				vals.remove(vals.indexOf(orig));
			} else {
				vals.set(vals.indexOf(orig), replacement);
			}
		}
	}
	
	private static Map<String, List<AbstractInteractionVertex>> buildInteractionGraphIndex(
			InteractionGraph igraph){
		Map<String, List<AbstractInteractionVertex>> retval = 
				new HashMap<String, List<AbstractInteractionVertex>>();
		for(AbstractInteractionVertex abvert : igraph.vertexSet()){
			String url = abvert.getUrl();
			List<AbstractInteractionVertex> verts = retval.get(url);
			if(verts == null){
				verts = new ArrayList<AbstractInteractionVertex>();
			}
			verts.add(abvert);
			retval.put(url, verts);
		}
		return retval;
	}
	
	private static List<MissingInteractionVertex> getMissingInteractionVertices(
			InteractionGraph igraph){
		List<MissingInteractionVertex> retval = 
				new ArrayList<MissingInteractionVertex>();
		for(AbstractInteractionVertex abvert : igraph.vertexSet()){
			if(abvert instanceof MissingInteractionVertex){
				retval.add((MissingInteractionVertex)abvert);
			}
		}
		return retval;
	}
	
	private static Map<String, List<FlowVertex>> buildRefererGraphIndex(
			RefererGraph rgraph){
		Map<String, List<FlowVertex>> retval = 
				new HashMap<String, List<FlowVertex>>();
		for(AbstractFlowVertex abvert : rgraph.vertexSet()){
			if(abvert instanceof FlowVertex){
				FlowVertex vert = (FlowVertex)abvert;
				String url = vert.getRequest().getUrl();
				List<FlowVertex> verts = retval.get(url);
				if(verts == null){
					verts = new ArrayList<FlowVertex>();
				}
				verts.add(vert);
				retval.put(url, verts);
			}
		}
		return retval;
	}
}
