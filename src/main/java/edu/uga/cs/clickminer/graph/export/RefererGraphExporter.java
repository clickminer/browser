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
import org.jgrapht.ext.IntegerEdgeNameProvider;

import edu.uga.cs.clickminer.graph.model.AbstractFlowVertex;
import edu.uga.cs.clickminer.graph.model.AbstractRefererEdge;
import edu.uga.cs.clickminer.graph.model.RefererGraph;
import edu.uga.cs.clickminer.graph.provider.RefererEdgeAttributeProvider;
import edu.uga.cs.clickminer.graph.provider.RequestVertexAttributeProvider;
import edu.uga.cs.clickminer.graph.provider.RequestVertexIdProvider;
import edu.uga.cs.clickminer.graph.provider.RequestVertexLabelProvider;

/**
 * <p>RefererGraphExporter class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RefererGraphExporter.java 843 2013-10-03 16:26:11Z cjneasbitt $Id
 */
public class RefererGraphExporter implements
		DirectedGraphExporter<RefererGraph> {

	/** {@inheritDoc} */
	@Override
	public void graphToFile(RefererGraph graph, String outpath) throws Exception {
		FileWriter filewriter = new FileWriter(outpath);
		Graph<AbstractFlowVertex, AbstractRefererEdge> results = graph;
		DOTExporter<AbstractFlowVertex, AbstractRefererEdge> out = new DOTExporter<AbstractFlowVertex, AbstractRefererEdge>(
				new RequestVertexIdProvider(),
				new RequestVertexLabelProvider(),
				new IntegerEdgeNameProvider<AbstractRefererEdge>(),
				new RequestVertexAttributeProvider(),
				new RefererEdgeAttributeProvider());
		out.export(filewriter, results);
		filewriter.close();
	}

	/** {@inheritDoc} */
	@Override
	public void graphToFile(RefererGraph graph, File outputdir)
			throws Exception {
		if(outputdir.isDirectory()){
			graphToFile(graph, outputdir.getCanonicalPath() + File.separatorChar
							+ "referer_graph.dot");
		} else {
			throw new Exception("Directory does not exist " + outputdir.getCanonicalPath());
		}
	}

}
