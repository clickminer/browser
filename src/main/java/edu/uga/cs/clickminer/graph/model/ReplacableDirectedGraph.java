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

import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

/**
 * <p>ReplacableDirectedGraph class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ReplacableDirectedGraph.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ReplacableDirectedGraph<V, E> extends DefaultDirectedGraph<V, E> {

	private static final long serialVersionUID = 7167091402897921260L;

	/**
	 * <p>Constructor for ReplacableDirectedGraph.</p>
	 *
	 * @param arg0 a {@link org.jgrapht.EdgeFactory} object.
	 */
	public ReplacableDirectedGraph(EdgeFactory<V, E> arg0) {
		super(arg0);
	}

	/**
	 * <p>Constructor for ReplacableDirectedGraph.</p>
	 *
	 * @param arg0 a {@link java.lang.Class} object.
	 */
	public ReplacableDirectedGraph(Class<? extends E> arg0) {
		super(arg0);
	}
	
	//TODO test to make sure this works.
	/**
	 * <p>replaceVertex.</p>
	 *
	 * @param orig a V object.
	 * @param replacement a V object.
	 */
	public void replaceVertex(V orig, V replacement){
		if(this.containsVertex(orig)){
			this.addVertex(replacement);
			Set<E> incoming = incomingEdgesOf(orig);
			Set<E> outgoing = outgoingEdgesOf(orig);
			for(E inedge : incoming){
				addEdge(getEdgeSource(inedge), replacement);
			}
			for(E outedge : outgoing){
				addEdge(replacement, getEdgeTarget(outedge));
			}
			removeVertex(orig);
		}
	}

}
