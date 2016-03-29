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
import java.util.List;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultWeightedEdge;

import pcap.reconst.http.datamodel.RecordedHttpFlow;
import pcap.reconst.http.datamodel.RecordedHttpRequestMessage;
import pcap.reconst.http.datamodel.Utils;

import edu.uga.cs.clickminer.datamodel.Interaction;
import edu.uga.cs.clickminer.datamodel.MitmHttpRequest;
import edu.uga.cs.clickminer.datamodel.log.InteractionRecord;

/**
 * <p>InteractionGraph class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: InteractionGraph.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class InteractionGraph extends
	ReplacableDirectedGraph<AbstractInteractionVertex, DefaultWeightedEdge> {

	private static final long serialVersionUID = 2271919505582413069L;

	/**
	 * <p>Constructor for InteractionGraph.</p>
	 *
	 * @param arg0 a {@link org.jgrapht.EdgeFactory} object.
	 */
	public InteractionGraph(
			EdgeFactory<AbstractInteractionVertex, DefaultWeightedEdge> arg0) {
		super(arg0);
	}

	/**
	 * <p>Constructor for InteractionGraph.</p>
	 *
	 * @param arg0 a {@link java.lang.Class} object.
	 */
	public InteractionGraph(Class<? extends DefaultWeightedEdge> arg0) {
		super(arg0);
	}
	
	/**
	 * <p>findMatchingVertex.</p>
	 *
	 * @param flow a {@link pcap.reconst.http.datamodel.RecordedHttpFlow} object.
	 */
	public AbstractInteractionVertex findMatchingVertex(RecordedHttpFlow flow) {
		RecordedHttpRequestMessage freq = flow.getRequest();
		for (AbstractInteractionVertex vertex : vertexSet()) {
			if (vertex instanceof InferredInteractionVertex) {
				InferredInteractionVertex vert = (InferredInteractionVertex) vertex;
				if (vert.getFlow().equals(flow)) {
					return vert;
				}
			}
			if(vertex instanceof MinedInteractionVertex){
				MinedInteractionVertex vert = (MinedInteractionVertex) vertex;
				MitmHttpRequest ireq =  vert.getInteractionRecord().getRequest();
				if(ireq.getStartTS() == freq.getStartTS() &&
						ireq.getEndTS() == freq.getEndTS() &&
						Utils.equals(ireq.getAllHeaders(), freq.getAllHeaders()) &&
						ireq.getProtocolVersion().equals(freq.getProtocolVersion()) &&
						Utils.equals(ireq.getRequestLine(), freq.getRequestLine())){
					return vert;
				}
				
			}
		}
		return null;
	}
	
	/**
	 * <p>findMatchingVertex.</p>
	 *
	 * @param destUrl a {@link java.lang.String} object.
	 */
	public MissingInteractionVertex findMatchingVertex(String destUrl) {
		for (UrlVertex vertex : vertexSet()) {
			if (vertex instanceof MissingInteractionVertex) {
				MissingInteractionVertex vert = (MissingInteractionVertex) vertex;
				if (vert.getUrl().equals(destUrl)) {
					return vert;
				}
			}
		}
		return null;
	}

	/**
	 * <p>findMatchingVertex.</p>
	 *
	 * @param record a {@link edu.uga.cs.clickminer.datamodel.log.InteractionRecord} object.
	 */
	public MinedInteractionVertex findMatchingVertex(InteractionRecord record) {
		for (UrlVertex vertex : vertexSet()) {
			if (vertex instanceof MinedInteractionVertex) {
				MinedInteractionVertex vert = (MinedInteractionVertex) vertex;
				if (vert.getInteractionRecord().equals(record)) {
					return vert;
				}
			}
		}
		return null;
	}
	
	/**
	 * <p>extractInteractions.</p>
	 */
	public List<Interaction> extractInteractions(){
		List<Interaction> retval = new ArrayList<Interaction>();
		for(AbstractInteractionVertex vertex : vertexSet()){
			if(vertex instanceof HasInteraction){
				HasInteraction ivert = (HasInteraction)vertex;
				retval.add(ivert.getInteraction());
			}
		}
		return retval;
	}
}
