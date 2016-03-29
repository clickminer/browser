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

/**
 * This class if basically a placeholder to differentiate from RefererEdge objects.
 *
 * @author cjneasbi
 * @version $Id: MissingFlowEdge.java 843 2013-10-03 16:26:11Z cjneasbitt $Id
 */
public class MissingFlowEdge extends AbstractRefererEdge {

	private static final long serialVersionUID = -679041509608166880L;

	/**
	 * <p>Constructor for MissingFlowEdge.</p>
	 */
	public MissingFlowEdge() {}

	/** {@inheritDoc} */
	@Override
	public AbstractRefererEdge copy() {
		return new MissingFlowEdge();
	}

}
