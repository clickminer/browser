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
 * <p>RefererEdge class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RefererEdge.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class RefererEdge extends AbstractRefererEdge {

	private static final long serialVersionUID = -7510184104034268942L;
	// in seconds
	private final double timeDelta;

	/**
	 * <p>Constructor for RefererEdge.</p>
	 */
	public RefererEdge(){
		timeDelta = -1;
	}
	
	/**
	 * <p>Constructor for RefererEdge.</p>
	 *
	 * @param timeDelta a double.
	 */
	public RefererEdge(double timeDelta) {
		this.timeDelta = timeDelta;
	}
	
	/**
	 * <p>Constructor for RefererEdge.</p>
	 *
	 * @param orig a {@link edu.uga.cs.clickminer.graph.model.RefererEdge} object.
	 */
	public RefererEdge(RefererEdge orig){
		this.timeDelta = orig.timeDelta;
	}

	/**
	 * <p>Getter for the field <code>timeDelta</code>.</p>
	 */
	public double getTimeDelta() {
		return timeDelta;
	}

	/** {@inheritDoc} */
	@Override
	public AbstractRefererEdge copy() {
		return new RefererEdge(this);
	}

}
