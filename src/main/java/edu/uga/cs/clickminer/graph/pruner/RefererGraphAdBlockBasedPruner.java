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

import edu.uga.cs.adblock.AdBlockRuleMatcher;
import edu.uga.cs.clickminer.graph.model.AbstractFlowVertex;
import edu.uga.cs.clickminer.graph.model.AbstractRefererEdge;
import edu.uga.cs.clickminer.graph.model.AdChainFlowVertex;
import edu.uga.cs.clickminer.graph.model.FlowVertex;
import edu.uga.cs.clickminer.graph.model.RefererGraph;

/**
 * <p>Abstract RefererGraphAdBlockBasedPruner class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RefererGraphAdBlockBasedPruner.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public abstract class RefererGraphAdBlockBasedPruner implements
		GraphPruner<RefererGraph> {
	private final AdBlockRuleMatcher matcher;
	
	private static final transient Log log = LogFactory.getLog(RefererGraphAdBlockBasedPruner.class);

	/**
	 * <p>Constructor for RefererGraphAdBlockBasedPruner.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public RefererGraphAdBlockBasedPruner() throws Exception {
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
	
	private List<AbstractFlowVertex> getLeafNodes(RefererGraph graph){
		List<AbstractFlowVertex> retval = new ArrayList<AbstractFlowVertex>();
		for(AbstractFlowVertex vert : graph.vertexSet()){
			if(graph.outDegreeOf(vert) == 0){
				retval.add(vert);
			}
		}
		return retval;
	}
	
	private List<AbstractFlowVertex> getRootNodes(RefererGraph graph){
		List<AbstractFlowVertex> retval = new ArrayList<AbstractFlowVertex>();
		for(AbstractFlowVertex vert : graph.vertexSet()){
			if(graph.inDegreeOf(vert) == 0){
				retval.add(vert);
			}
		}
		return retval;
	}
	
	private Set<List<FlowVertex>> findAdSections(RefererGraph graph){
		Set<List<FlowVertex>> retval = new HashSet<List<FlowVertex>>();
		for(AbstractFlowVertex root : getRootNodes(graph)){
			this.findAdSectionsHelper(root, graph, null, retval);
		}
		return retval;
	}
	
	private void findAdSectionsHelper(AbstractFlowVertex curnode, RefererGraph graph, 
			List<FlowVertex> cursection, Set<List<FlowVertex>> adSections){
		
		List<FlowVertex> nextCurSection = cursection;
		if(curnode instanceof FlowVertex && graph.containsVertex(curnode) && 
				matches(curnode.getUrl())){
			if(nextCurSection == null){
				nextCurSection = new ArrayList<FlowVertex>();
				adSections.add(nextCurSection);
			}
			nextCurSection.add((FlowVertex)curnode);
		} else {
			nextCurSection = null;
		}
		
		Set<AbstractRefererEdge> outEdges = graph.outgoingEdgesOf(curnode);
		Iterator<AbstractRefererEdge> iter = outEdges.iterator();
		while(iter.hasNext()){
			AbstractFlowVertex nextnode = graph.getEdgeTarget(iter.next());
			findAdSectionsHelper(nextnode, graph, nextCurSection, adSections);
		}
	}
	
	/**
	 * <p>matches.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 */
	public abstract boolean matches(String url);
	
	private void compressInternalAdSections(RefererGraph graph){
		Set<List<FlowVertex>> sections = findAdSections(graph);
		Iterator<List<FlowVertex>> iter = sections.iterator();
		while(iter.hasNext()){
			List<FlowVertex> section = iter.next();
			AdChainFlowVertex newVert = null;
			for(int i = 0; i < section.size(); i++){
				FlowVertex vert = section.get(i);
				if(newVert == null){
					newVert = new AdChainFlowVertex(vert.getFlow());
					graph.addVertex(newVert);
				} else {
					newVert.add(vert.getFlow());
				}
				
				if(i == 0){
					Set<AbstractRefererEdge> inEdges = graph.incomingEdgesOf(vert);
					for(AbstractRefererEdge inEdge : inEdges){
						graph.addEdge(graph.getEdgeSource(inEdge), newVert, inEdge.copy());
					}
				}
				
				Set<AbstractRefererEdge> outEdges = graph.outgoingEdgesOf(vert);
				for(AbstractRefererEdge outEdge : outEdges){
					graph.addEdge(newVert, graph.getEdgeTarget(outEdge), outEdge.copy());
				}
				graph.removeVertex(vert);
			}
			
			Set<AbstractRefererEdge> inedges = graph.incomingEdgesOf(newVert);
			if(inedges.size() > 0){
				AbstractFlowVertex replace = graph.getEdgeSource(inedges.iterator().next());
				Set<AbstractRefererEdge> outedges = graph.outgoingEdgesOf(newVert);
				for(AbstractRefererEdge outedge : outedges){
					graph.addEdge(replace, graph.getEdgeTarget(outedge), outedge.copy());
				}
			}
			graph.removeVertex(newVert);
			
		}
	}
	
	/**
	 * <p>pruneTerminalAdSections.</p>
	 *
	 * @param graph a {@link edu.uga.cs.clickminer.graph.model.RefererGraph} object.
	 */
	public void pruneTerminalAdSections(RefererGraph graph){
		for(AbstractFlowVertex leaf : getLeafNodes(graph)){
			AbstractFlowVertex curnode = leaf;
			while(curnode != null){
				AbstractFlowVertex next = null;
				if(graph.containsVertex(curnode) && graph.outDegreeOf(curnode) == 0 
						&& matches(curnode.getUrl())){
					Set<AbstractRefererEdge> inedges = graph.incomingEdgesOf(curnode);
					
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
	public void pruneGraph(RefererGraph graph){
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
