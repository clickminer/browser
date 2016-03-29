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

import edu.uga.cs.clickminer.graph.model.AbstractRefererEdge;
import edu.uga.cs.clickminer.graph.model.RefererEdge;

/**
 * <p>RefererEdgeAttributeProvider class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RefererEdgeAttributeProvider.java 843 2013-10-03 16:26:11Z cjneasbitt $Id
 */
public class RefererEdgeAttributeProvider implements
		ComponentAttributeProvider<AbstractRefererEdge> {

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getComponentAttributes(AbstractRefererEdge arg0) {
		Map<String, String> retval = new HashMap<String, String>();
		if (arg0 instanceof RefererEdge) {
			RefererEdge edge = (RefererEdge) arg0;
			retval.put("timeDelta", Double.toString(edge.getTimeDelta()));
		}
		return retval;
	}

}
