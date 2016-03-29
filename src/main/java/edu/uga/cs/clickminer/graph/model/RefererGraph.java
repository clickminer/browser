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
package edu.uga.cs.clickminer.graph.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.EdgeFactory;

import pcap.reconst.http.datamodel.RecordedHttpFlow;
import pcap.reconst.http.datamodel.RecordedHttpRequestMessage;

/**
 * <p>RefererGraph class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RefererGraph.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class RefererGraph extends 
	ReplacableDirectedGraph<AbstractFlowVertex, AbstractRefererEdge> {

	private static final long serialVersionUID = -6059772182975243075L;

	/**
	 * <p>Constructor for RefererGraph.</p>
	 *
	 * @param arg0 a {@link org.jgrapht.EdgeFactory} object.
	 */
	public RefererGraph(
			EdgeFactory<AbstractFlowVertex, AbstractRefererEdge> arg0) {
		super(arg0);
	}

	/**
	 * <p>Constructor for RefererGraph.</p>
	 *
	 * @param arg0 a {@link java.lang.Class} object.
	 */
	public RefererGraph(Class<? extends AbstractRefererEdge> arg0) {
		super(arg0);
	}
	
	/**
	 * <p>totalFlows.</p>
	 */
	public int totalFlows(){
		return getFlows().size();
	}
	
	/**
	 * <p>copy.</p>
	 */
	public RefererGraph copy(){
		Map<AbstractFlowVertex, AbstractFlowVertex> vertMap = new
				HashMap<AbstractFlowVertex, AbstractFlowVertex>();
		RefererGraph retval = new RefererGraph(getEdgeFactory());
		for(AbstractFlowVertex vert : this.vertexSet()){
			AbstractFlowVertex copy = vert.copy();
			retval.addVertex(copy);
			vertMap.put(vert, copy);
		}
		for(AbstractRefererEdge edge : this.edgeSet()){
			AbstractRefererEdge copy = edge.copy();
			retval.addEdge(vertMap.get(retval.getEdgeSource(edge)), 
					vertMap.get(retval.getEdgeTarget(edge)), copy);
		}
		return retval;
	}
	
	/**
	 * <p>getRequests.</p>
	 */
	public List<RecordedHttpRequestMessage> getRequests(){
		List<RecordedHttpRequestMessage> retval = new 
				ArrayList<RecordedHttpRequestMessage>();
		List<RecordedHttpFlow> flows = this.getFlows();
		for(RecordedHttpFlow flow : flows){
			retval.add(flow.getRequest());
		}
		return retval;
	}
	
	/**
	 * <p>getFlows.</p>
	 */
	public List<RecordedHttpFlow> getFlows(){
		List<RecordedHttpFlow> retval = new ArrayList<RecordedHttpFlow>();
		for(AbstractFlowVertex vert : vertexSet()){
			if(vert instanceof FlowVertex){
				FlowVertex fvert = (FlowVertex)vert;
				retval.add(fvert.getFlow());
			}
		}
		return retval;
	}
	
	/**
	 * <p>findMatchingVertex.</p>
	 *
	 * @param flow a {@link pcap.reconst.http.datamodel.RecordedHttpFlow} object.
	 */
	public FlowVertex findMatchingVertex(RecordedHttpFlow flow){
		for (AbstractFlowVertex vertex : vertexSet()) {
			if (vertex instanceof FlowVertex) {
				FlowVertex vert = (FlowVertex) vertex;
				if(vert.getFlow().equals(flow)){
					return vert;
				}
			}
		}
		return null;
	}
	
	/**
	 * <p>findMatchingVertex.</p>
	 *
	 * @param request a {@link pcap.reconst.http.datamodel.RecordedHttpRequestMessage} object.
	 */
	public FlowVertex findMatchingVertex(RecordedHttpRequestMessage request) {
		for (AbstractFlowVertex vertex : vertexSet()) {
			if (vertex instanceof FlowVertex) {
				FlowVertex vert = (FlowVertex) vertex;
				if (vert.getRequest().equals(request)) {
					return vert;
				}
			}
		}
		return null;
	}

	/**
	 * <p>findMatchingVertex.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 */
	public MissingFlowVertex findMatchingVertex(String url) {
		for (AbstractFlowVertex vertex : vertexSet()) {
			if (vertex instanceof MissingFlowVertex) {
				MissingFlowVertex vert = (MissingFlowVertex) vertex;
				if (vert.getUrl().equals(url)) {
					return vert;
				}
			}
		}
		return null;
	}
	
	/**
	 * <p>getRefererDelay.</p>
	 *
	 * @param vert a {@link edu.uga.cs.clickminer.graph.model.FlowVertex} object.
	 */
	public double getRefererDelay(FlowVertex vert){
		double retval = -1;
		Set<AbstractRefererEdge> edges = this.incomingEdgesOf(vert);
		if(edges.size() > 0){
			AbstractRefererEdge edge = edges.iterator().next();
			if(edge instanceof RefererEdge){
				retval = ((RefererEdge)edge).getTimeDelta();
			}
		} else {
			retval = 0;
		}
		return retval;
	}

}
