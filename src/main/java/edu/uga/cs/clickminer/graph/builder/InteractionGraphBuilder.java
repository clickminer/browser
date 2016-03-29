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
package edu.uga.cs.clickminer.graph.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.json.JSONException;

import edu.uga.cs.clickminer.datamodel.log.InteractionRecord;
import edu.uga.cs.clickminer.datamodel.log.PageEntry;
import edu.uga.cs.clickminer.graph.model.InteractionGraph;
import edu.uga.cs.clickminer.graph.model.MinedInteractionVertex;
import edu.uga.cs.clickminer.graph.model.MissingInteractionVertex;
import edu.uga.cs.clickminer.util.ResultsUtils;
import edu.uga.cs.json.JSONReader;

/**
 * <p>InteractionGraphBuilder class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: InteractionGraphBuilder.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class InteractionGraphBuilder implements
		DirectedGraphBuilder<InteractionGraph> {

	private List<InteractionRecord> interactions;

	private static transient final Log log = LogFactory.getLog(InteractionGraphBuilder.class);

	/**
	 * <p>Constructor for InteractionGraphBuilder.</p>
	 *
	 * @param resultsdir a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 * @throws org.json.JSONException if any.
	 */
	public InteractionGraphBuilder(String resultsdir) throws IOException,
			JSONException {
		this(ResultsUtils.getMinedClicks(resultsdir));
	}
	
	/**
	 * <p>Constructor for InteractionGraphBuilder.</p>
	 *
	 * @param ilog a {@link java.io.File} object.
	 * @throws java.io.IOException if any.
	 * @throws org.json.JSONException if any.
	 */
	public InteractionGraphBuilder(File ilog) throws IOException, 
		JSONException{
		JSONReader<InteractionRecord> ireader = new JSONReader<InteractionRecord>();
		interactions = ireader.read(ilog,InteractionRecord.class);
		if (log.isDebugEnabled()) {
			log.debug("Loaded " + interactions.size() + " interaction(s).");
		}
	}

	/** {@inheritDoc} */
	@Override
	public InteractionGraph getGraph() {
		InteractionGraph retval = new InteractionGraph(
				DefaultWeightedEdge.class);
		if(log.isDebugEnabled()){
			log.debug("Creating graph interaction nodes.");
		}
		this.createGraphInteractionNodes(retval);
		if(log.isDebugEnabled()){
			log.debug("Graph nodes created.");
			log.debug("Creating graph edges.");
		}
		this.createGraphEdges(retval);
		if(log.isDebugEnabled()){
			log.debug("Graph edges created.");
			log.debug("Creating graph missing interaction nodes.");
		}
		this.createGraphMissingNodes(retval);
		if(log.isDebugEnabled()){
			log.debug("Graph nodes created.");
		}
		return retval;
	}

	private void createGraphInteractionNodes(InteractionGraph graph) {
		for (InteractionRecord record : interactions) {
			int index = interactions.indexOf(record);
			graph.addVertex(new MinedInteractionVertex(record, index));
		}
	}

	private void createGraphMissingNodes(InteractionGraph graph) {
		try {
			for (InteractionRecord record : interactions) {
				MinedInteractionVertex vertex = graph.findMatchingVertex(record);

				if (vertex.getInteractionRecord().getRequest().containsHeader("Referer")
						&& graph.inDegreeOf(vertex) == 0) {
					double ts =record.getRequest().getStartTS();
					String refererurl = record.getRequest().getFirstHeader("Referer").getValue();
					MissingInteractionVertex parent = graph.findMatchingVertex(refererurl);
					if (parent == null) {
						parent = new MissingInteractionVertex(refererurl,ts);
						graph.addVertex(parent);
					} else if(parent.getTimestampLimit() > ts) {
						parent.setTimestampLimit(ts);
					}
					graph.addEdge(parent, vertex);
				}
			}
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("Unable to create missing nodes.", e);
			}
		}
	}

	private void createGraphEdges(InteractionGraph graph) {
		outer: for (int i = 0; i < interactions.size(); i++) {
			InteractionRecord child = interactions.get(i);
			if (!child.possibleAddressBarInteraction()) {
				List<String> possSourceUrls = new ArrayList<String>();
				for (PageEntry page : child.getSrcPageEntries()) {
					possSourceUrls.add(page.getUrl());
				}
				for (int j = 0; j < interactions.size(); j++) {
					if (i != j) {
						InteractionRecord posparent = interactions.get(j);
						if (child.getRequest().getStartTS() > posparent
								.getRequest().getStartTS()) {
							String parDestUrl = posparent.getDestPageEntry()
									.getUrl();
							if (possSourceUrls.contains(parDestUrl)) {
								graph.addEdge(graph.findMatchingVertex(posparent), 
										graph.findMatchingVertex(child));
								continue outer;
							}
						}
					}
				}
			}
		}
	}
}
