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

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.ext.ComponentAttributeProvider;

import edu.uga.cs.clickminer.graph.model.AbstractFlowVertex;
import edu.uga.cs.clickminer.graph.model.AdChainFlowVertex;
import edu.uga.cs.clickminer.graph.model.FlowVertex;
import edu.uga.cs.clickminer.graph.model.RedirectionChainVertex;

/**
 * <p>RequestVertexAttributeProvider class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RequestVertexAttributeProvider.java 843 2013-10-03 16:26:11Z cjneasbitt $Id
 */
public class RequestVertexAttributeProvider implements
		ComponentAttributeProvider<AbstractFlowVertex> {

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getComponentAttributes(AbstractFlowVertex arg0) {
		Map<String, String> retval = new HashMap<String, String>();
		if (arg0 instanceof FlowVertex) {
			if(arg0 instanceof AdChainFlowVertex){
				retval.put("color", "red");
			} else if(arg0 instanceof RedirectionChainVertex){
				retval.put("color", "blue");
			} else {
				FlowVertex vert = (FlowVertex) arg0;
				retval.put("color", "green");
				retval.put(
						"startTS",
						String.format("%9.6f", vert.getRequest().getStartTS()));
				retval.put(
						"endTS",
						String.format("%9.6f", vert.getRequest().getEndTS()));
			}
		} else {
			retval.put("color", "yellow");
		}
		return retval;
	}

}
