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

import org.jgrapht.Graph;

/**
 * <p>GraphPruner interface.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: GraphPruner.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public interface GraphPruner<T extends Graph<?, ?>> {

	/**
	 * <p>pruneGraph.</p>
	 *
	 * @param graph a T object.
	 */
	public void pruneGraph(T graph);
}
