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

import edu.uga.cs.clickminer.datamodel.Interaction;

/**
 * <p>Abstract SingleInteractionVertex class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: SingleInteractionVertex.java 843 2013-10-03 16:26:11Z cjneasbitt $Id
 */
public abstract class SingleInteractionVertex extends
		AbstractInteractionVertex implements HasInteraction {

	/**
	 * <p>getStartTS.</p>
	 */
	public abstract double getStartTS();
	/**
	 * <p>getEndTS.</p>
	 */
	public abstract double getEndTS();
	/**
	 * <p>getInteraction.</p>
	 */
	public abstract Interaction getInteraction();
}
