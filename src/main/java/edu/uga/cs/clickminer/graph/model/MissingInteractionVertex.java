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
 * <p>MissingInteractionVertex class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: MissingInteractionVertex.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class MissingInteractionVertex extends AbstractInteractionVertex {

	private static int RECORDCOUNTER = 0;
	private static String IDTEXT = "MISSING";

	private final String destUrl;
	private double timestampLimit;
	private final String id;

	/**
	 * <p>Constructor for MissingInteractionVertex.</p>
	 *
	 * @param destUrl a {@link java.lang.String} object.
	 * @param timestampLimit a double.
	 */
	public MissingInteractionVertex(String destUrl, double timestampLimit) {
		this.destUrl = destUrl;
		this.id = IDTEXT + "_" + RECORDCOUNTER++;
		this.timestampLimit = timestampLimit;
	}
	
	/**
	 * <p>Setter for the field <code>timestampLimit</code>.</p>
	 *
	 * @param timestampLimit a double.
	 */
	public void setTimestampLimit(double timestampLimit){
		this.timestampLimit = timestampLimit;
	}
	
	/**
	 * <p>Getter for the field <code>timestampLimit</code>.</p>
	 */
	public double getTimestampLimit(){
		return timestampLimit;
	}

	/** {@inheritDoc} */
	@Override
	public String getId() {
		return this.id;
	}

	/** {@inheritDoc} */
	@Override
	public String getUrl() {
		return this.destUrl;
	}

}
