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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.graph.DefaultWeightedEdge;

import edu.uga.cs.clickminer.graph.model.AbstractFlowVertex;
import edu.uga.cs.clickminer.graph.model.FlowVertex;
import edu.uga.cs.clickminer.graph.model.RefererEdge;
import edu.uga.cs.clickminer.graph.model.RefererGraph;

/**
 * <p>RefererGraphDelayPruner class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RefererGraphDelayPruner.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class RefererGraphDelayPruner implements GraphPruner<RefererGraph> {

	private double timeDeltaThresh;
	
	private static final transient Log log = LogFactory.getLog(RefererGraphDelayPruner.class);
	
	/**
	 * <p>Constructor for RefererGraphDelayPruner.</p>
	 */
	public RefererGraphDelayPruner(){
		this(Double.MIN_VALUE);
	}
	
	/**
	 * <p>Constructor for RefererGraphDelayPruner.</p>
	 *
	 * @param timeDeltaThresh a double.
	 */
	public RefererGraphDelayPruner(double timeDeltaThresh) {
		setTimeDeltaThresh(timeDeltaThresh);
	}

	/**
	 * <p>Getter for the field <code>timeDeltaThresh</code>.</p>
	 */
	public double getTimeDeltaThresh() {
		return timeDeltaThresh;
	}

	/**
	 * <p>Setter for the field <code>timeDeltaThresh</code>.</p>
	 *
	 * @param timeDeltaThresh a double.
	 */
	public void setTimeDeltaThresh(double timeDeltaThresh) {
		this.timeDeltaThresh = timeDeltaThresh;
	}

	/** {@inheritDoc} */
	@Override
	public void pruneGraph(RefererGraph refgraph) {
		Set<RefererEdge> affedges = this.findAffectedEdges(
				refgraph, timeDeltaThresh);
		this.modifyGraph(affedges, refgraph);
	}
	
	private Set<RefererEdge> findAffectedEdges(RefererGraph refgraph,
			double timeDeltaThresh) {
		Set<RefererEdge> retval = new HashSet<RefererEdge>();
		for (DefaultWeightedEdge edge : refgraph.edgeSet()) {
			if (edge instanceof RefererEdge) {
				RefererEdge refedge = (RefererEdge) edge;
				if (refedge.getTimeDelta() < timeDeltaThresh) {
					retval.add(refedge);
				}
			}
		}
		return retval;
	}
	
	private void modifyGraph(Set<RefererEdge> affedges, RefererGraph refgraph) {
		refgraph.removeAllEdges(affedges);
		Set<AbstractFlowVertex> verts = new HashSet<AbstractFlowVertex>();
		for (AbstractFlowVertex vert : refgraph.vertexSet()) {
			if (vert instanceof FlowVertex) {
				FlowVertex reqvert = (FlowVertex)vert;
				if (refgraph.inDegreeOf(reqvert) == 0
						&& reqvert.getRequest().containsHeader("Referer")) {
					verts.add(reqvert);
				}
			}
		}

		if(log.isDebugEnabled()){
			log.debug("Removing " + verts.size() + " vertices under threshold.");
		}
		refgraph.removeAllVertices(verts);
	}
}
