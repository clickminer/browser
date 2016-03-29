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

import org.jgrapht.ext.VertexNameProvider;

import edu.uga.cs.clickminer.graph.model.AbstractInteractionVertex;
import edu.uga.cs.clickminer.graph.model.AdChainInteractionVertex;
import edu.uga.cs.clickminer.graph.model.MinedInteractionVertex;
import edu.uga.cs.clickminer.graph.model.SingleInteractionVertex;

/**
 * <p>InteractionRecordVertexLabelProvider class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: InteractionRecordVertexLabelProvider.java 852 2014-03-31 22:28:30Z cjneasbitt $Id
 */
public class InteractionRecordVertexLabelProvider implements
		VertexNameProvider<AbstractInteractionVertex> {

	/** {@inheritDoc} */
	@Override
	public String getVertexName(AbstractInteractionVertex arg0) {
		if (arg0 instanceof SingleInteractionVertex) {
			SingleInteractionVertex vert = (SingleInteractionVertex)arg0;
			String name = null;
			if(vert instanceof MinedInteractionVertex){
				name = "Interaction";
			} else {
				name = "Inferred";
			}
			return String.format("%s %s %9.6f %9.6f", name, vert.getUrl(), 
					vert.getStartTS(), vert.getEndTS());
		} else if(arg0 instanceof AdChainInteractionVertex){
			AdChainInteractionVertex vert = (AdChainInteractionVertex)arg0;
			return String.format("%s %s", "AdChain", vert.getUrl());
		} else {
			return "Missing " + arg0.getUrl();
		}
	}

}
