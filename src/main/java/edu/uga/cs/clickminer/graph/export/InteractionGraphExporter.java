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
import java.io.FileWriter;

import org.jgrapht.Graph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DefaultWeightedEdge;

import edu.uga.cs.clickminer.graph.model.AbstractInteractionVertex;
import edu.uga.cs.clickminer.graph.model.InteractionGraph;
import edu.uga.cs.clickminer.graph.provider.InteractionRecordEdgeLabelProvider;
import edu.uga.cs.clickminer.graph.provider.InteractionRecordVertexAttributeProvider;
import edu.uga.cs.clickminer.graph.provider.InteractionRecordVertexLabelProvider;
import edu.uga.cs.clickminer.graph.provider.InteractionVertexIdProvider;

/**
 * <p>InteractionGraphExporter class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: InteractionGraphExporter.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class InteractionGraphExporter implements
		DirectedGraphExporter<InteractionGraph> {

	private final long refererDelayThreshold;

	/**
	 * <p>Constructor for InteractionGraphExporter.</p>
	 */
	public InteractionGraphExporter() {
		this(-1);
	}
	
	/**
	 * <p>Constructor for InteractionGraphExporter.</p>
	 *
	 * @param refererDelayThreshold a long.
	 */
	public InteractionGraphExporter(long refererDelayThreshold) {
		this.refererDelayThreshold = refererDelayThreshold;
	}

	/** {@inheritDoc} */
	@Override
	public void graphToFile(InteractionGraph graph, String outpath) throws Exception{
		FileWriter filewriter = new FileWriter(outpath);
		Graph<AbstractInteractionVertex, DefaultWeightedEdge> results = graph;
		DOTExporter<AbstractInteractionVertex, DefaultWeightedEdge> out = new DOTExporter<AbstractInteractionVertex, DefaultWeightedEdge>(
				new InteractionVertexIdProvider(),
				new InteractionRecordVertexLabelProvider(),
				new InteractionRecordEdgeLabelProvider(
						(InteractionGraph) results),
				((refererDelayThreshold > -1) ? new InteractionRecordVertexAttributeProvider(refererDelayThreshold)
						: new InteractionRecordVertexAttributeProvider()), 
				null);
		out.export(filewriter, results);
		filewriter.close();
	}
	
	/** {@inheritDoc} */
	@Override
	public void graphToFile(InteractionGraph graph, File outputdir) throws Exception {
		if(outputdir.isDirectory()){
			graphToFile(graph, outputdir.getCanonicalPath() + File.separatorChar
							+ "results_graph.dot");
		} else {
			throw new Exception("Directory does not exist " + outputdir.getCanonicalPath());
		}
	}

}
