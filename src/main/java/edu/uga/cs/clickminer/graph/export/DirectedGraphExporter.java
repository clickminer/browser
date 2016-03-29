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
package edu.uga.cs.clickminer.graph.export;

import java.io.File;

import org.jgrapht.DirectedGraph;

/**
 * <p>DirectedGraphExporter interface.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: DirectedGraphExporter.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public interface DirectedGraphExporter<V extends DirectedGraph<?, ?>> {
	
	// output to the specified path
	/**
	 * <p>graphToFile.</p>
	 *
	 * @param graph a V object.
	 * @param outpath a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public void graphToFile(V graph, String outpath) throws Exception;
	
	// output to the specified directory with the default file name 
	/**
	 * <p>graphToFile.</p>
	 *
	 * @param graph a V object.
	 * @param outputdir a {@link java.io.File} object.
	 * @throws java.lang.Exception if any.
	 */
	public void graphToFile(V graph, File outputdir) throws Exception;
}
