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
package edu.uga.cs.clickminer.graph.provider;

import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.graph.DefaultWeightedEdge;

import edu.uga.cs.clickminer.graph.model.AbstractInteractionVertex;
import edu.uga.cs.clickminer.graph.model.InferredInteractionVertex;
import edu.uga.cs.clickminer.graph.model.InteractionGraph;
import edu.uga.cs.clickminer.graph.model.MissingInteractionVertex;

/**
 * <p>InteractionRecordEdgeLabelProvider class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: InteractionRecordEdgeLabelProvider.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class InteractionRecordEdgeLabelProvider implements
		EdgeNameProvider<DefaultWeightedEdge> {

	private final InteractionGraph graph;

	/**
	 * <p>Constructor for InteractionRecordEdgeLabelProvider.</p>
	 *
	 * @param graph a {@link edu.uga.cs.clickminer.graph.model.InteractionGraph} object.
	 */
	public InteractionRecordEdgeLabelProvider(InteractionGraph graph) {
		this.graph = graph;
	}

	/** {@inheritDoc} */
	@Override
	public String getEdgeName(DefaultWeightedEdge arg0) {
		AbstractInteractionVertex src = graph.getEdgeSource(arg0);
		AbstractInteractionVertex target = graph.getEdgeTarget(arg0);
		if (src instanceof MissingInteractionVertex
				|| target instanceof MissingInteractionVertex
				|| src instanceof InferredInteractionVertex
				|| target instanceof InferredInteractionVertex) {
			return "Estimated";
		} else {
			return "Actual";
		}
	}

}
