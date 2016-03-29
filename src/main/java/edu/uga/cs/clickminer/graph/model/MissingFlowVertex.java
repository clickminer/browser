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
 * <p>MissingFlowVertex class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: MissingFlowVertex.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class MissingFlowVertex extends AbstractFlowVertex {

	private static int VERTCOUNTER = 0;
	private static String IDTEXT = "MISSING_REQUEST";
	private final String url;
	private final String id;

	/**
	 * <p>Constructor for MissingFlowVertex.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 */
	public MissingFlowVertex(String url) {
		this.url = url;
		this.id = IDTEXT + "_" + VERTCOUNTER++;
	}
	
	/**
	 * <p>Constructor for MissingFlowVertex.</p>
	 *
	 * @param vert a {@link edu.uga.cs.clickminer.graph.model.MissingFlowVertex} object.
	 */
	public MissingFlowVertex(MissingFlowVertex vert){
		this(vert.getUrl());
	}

	/**
	 * <p>Getter for the field <code>url</code>.</p>
	 */
	public String getUrl() {
		return url;
	}

	/** {@inheritDoc} */
	@Override
	public String getId() {
		return id;
	}

	/** {@inheritDoc} */
	@Override
	public AbstractFlowVertex copy() {
		return new MissingFlowVertex(this);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj){
		if(obj instanceof MissingFlowVertex){
			MissingFlowVertex vert = (MissingFlowVertex)obj;
			return url.equals(vert.getUrl()) && id.equals(vert.getId());
		}
		return false;
	}

}
