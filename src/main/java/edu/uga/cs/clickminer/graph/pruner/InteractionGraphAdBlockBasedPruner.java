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
package edu.uga.cs.clickminer.graph.pruner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.graph.DefaultWeightedEdge;

import edu.uga.cs.adblock.AdBlockRuleMatcher;
//import edu.uga.cs.clickminer.graph.model.AbstractFlowVertex;
import edu.uga.cs.clickminer.graph.model.AbstractInteractionVertex;
//import edu.uga.cs.clickminer.graph.model.AbstractRefererEdge;
import edu.uga.cs.clickminer.graph.model.AdChainInteractionVertex;
import edu.uga.cs.clickminer.graph.model.InteractionGraph;
import edu.uga.cs.clickminer.graph.model.SingleInteractionVertex;

/**
 * <p>Abstract InteractionGraphAdBlockBasedPruner class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: InteractionGraphAdBlockBasedPruner.java 850 2014-03-13 17:54:22Z cjneasbitt $Id
 */
public abstract class InteractionGraphAdBlockBasedPruner implements GraphPruner<InteractionGraph> {
	private final AdBlockRuleMatcher matcher;
	
	private static final transient Log log = LogFactory.getLog(InteractionGraphAdBlockBasedPruner.class);
	
	/**
	 * <p>Constructor for InteractionGraphAdBlockBasedPruner.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	protected InteractionGraphAdBlockBasedPruner() throws Exception {
		matcher = initMatcher();
	}
	
	/**
	 * <p>initMatcher.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	protected abstract AdBlockRuleMatcher initMatcher() throws Exception;
	
	/**
	 * <p>Getter for the field <code>matcher</code>.</p>
	 */
	protected AdBlockRuleMatcher getMatcher(){
		return matcher;
	}
	
	private List<AbstractInteractionVertex> getLeafNodes(InteractionGraph graph){
		List<AbstractInteractionVertex> retval = new ArrayList<AbstractInteractionVertex>();
		for(AbstractInteractionVertex vert : graph.vertexSet()){
			if(graph.outDegreeOf(vert) == 0){
				retval.add(vert);
			}
		}
		return retval;
	}
	
	private List<AbstractInteractionVertex> getRootNodes(InteractionGraph graph){
		List<AbstractInteractionVertex> retval = new ArrayList<AbstractInteractionVertex>();
		for(AbstractInteractionVertex vert : graph.vertexSet()){
			if(graph.inDegreeOf(vert) == 0){
				retval.add(vert);
			}
		}
		return retval;
	}

	
	private Set<List<SingleInteractionVertex>> findAdSections(InteractionGraph graph){
		Set<List<SingleInteractionVertex>> retval = new HashSet<List<SingleInteractionVertex>>();
		for(AbstractInteractionVertex root : getRootNodes(graph)){
			this.findAdSectionsHelper(root, graph, null, retval);
		}
		return retval;
	}
	
	private void findAdSectionsHelper(AbstractInteractionVertex curnode, InteractionGraph graph, 
			List<SingleInteractionVertex> cursection, Set<List<SingleInteractionVertex>> adSections){
		
		List<SingleInteractionVertex> nextCurSection = cursection;
		if(curnode instanceof SingleInteractionVertex && graph.containsVertex(curnode) && 
				matches(curnode.getUrl())){
			if(nextCurSection == null){
				nextCurSection = new ArrayList<SingleInteractionVertex>();
				adSections.add(nextCurSection);
			}
			nextCurSection.add((SingleInteractionVertex)curnode);
		} else {
			nextCurSection = null;
		}
		
		Set<DefaultWeightedEdge> outEdges = graph.outgoingEdgesOf(curnode);
		Iterator<DefaultWeightedEdge> iter = outEdges.iterator();
		while(iter.hasNext()){
			AbstractInteractionVertex nextnode = graph.getEdgeTarget(iter.next());
			findAdSectionsHelper(nextnode, graph, nextCurSection, adSections);
		}
	}
	
	/**
	 * <p>matches.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 */
	public abstract boolean matches(String url);
	
	private void compressInternalAdSections(InteractionGraph graph){
		Set<List<SingleInteractionVertex>> sections = findAdSections(graph);
		Iterator<List<SingleInteractionVertex>> iter = sections.iterator();
		while(iter.hasNext()){
			List<SingleInteractionVertex> section = iter.next();
			AdChainInteractionVertex newVert = null;
			for(int i = 0; i < section.size(); i++){
				SingleInteractionVertex vert = section.get(i);
				if(newVert == null){
					newVert = new AdChainInteractionVertex(vert.getInteraction());
					graph.addVertex(newVert);
				} else {
					newVert.add(vert.getInteraction());
				}
				
				if(i == 0){
					Set<DefaultWeightedEdge> inEdges = graph.incomingEdgesOf(vert);
					for(DefaultWeightedEdge inEdge : inEdges){
						graph.addEdge(graph.getEdgeSource(inEdge), newVert);
					}
				}
				
				Set<DefaultWeightedEdge> outEdges = graph.outgoingEdgesOf(vert);
				for(DefaultWeightedEdge outEdge : outEdges){
					graph.addEdge(newVert, graph.getEdgeTarget(outEdge));
				}
				graph.removeVertex(vert);
			}
			
			Set<DefaultWeightedEdge> inedges = graph.incomingEdgesOf(newVert);
			if(inedges.size() > 0){
				AbstractInteractionVertex replace = graph.getEdgeSource(inedges.iterator().next());
				Set<DefaultWeightedEdge> outedges = graph.outgoingEdgesOf(newVert);
				for(DefaultWeightedEdge outedge : outedges){
					graph.addEdge(replace, graph.getEdgeTarget(outedge));
				}
			}
			graph.removeVertex(newVert);
		}
	}
	
	/**
	 * <p>pruneTerminalAdSections.</p>
	 *
	 * @param graph a {@link edu.uga.cs.clickminer.graph.model.InteractionGraph} object.
	 */
	public void pruneTerminalAdSections(InteractionGraph graph){
		for(AbstractInteractionVertex leaf : getLeafNodes(graph)){
			AbstractInteractionVertex curnode = leaf;
			while(curnode != null){
				AbstractInteractionVertex next = null;
				if(graph.containsVertex(curnode) && graph.outDegreeOf(curnode) == 0 
						&& matches(curnode.getUrl())){
					Set<DefaultWeightedEdge> inedges = graph.incomingEdgesOf(curnode);
					
					if(inedges.size() == 1){
						next = graph.getEdgeSource(inedges.iterator().next());
					} else {
						if(inedges.size() > 1){
							throw new RuntimeException("Each node in the graph can have at most one parent.");
						}
					}
					
					if(log.isDebugEnabled()){
						log.debug("Pruning ad related vertex " + curnode.getUrl());
					}
					graph.removeVertex(curnode);
				}
				curnode = next;
			}
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void pruneGraph(InteractionGraph graph){
		try{
			pruneTerminalAdSections(graph);
			compressInternalAdSections(graph);
		} catch (Exception e) {
			if(log.isErrorEnabled()){
				log.error("Unable to prune graph.", e);
			}
		}
	}
}
