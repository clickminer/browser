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
import edu.uga.cs.clickminer.datamodel.log.InteractionRecord;

/**
 * <p>MinedInteractionVertex class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: MinedInteractionVertex.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class MinedInteractionVertex extends SingleInteractionVertex {

	private static int RECORDCOUNTER = 0;
	private static String IDTEXT = "INTERACTION";

	private final InteractionRecord record;
	private final int recordIndex;
	private final String id;

	/**
	 * <p>Constructor for MinedInteractionVertex.</p>
	 *
	 * @param record a {@link edu.uga.cs.clickminer.datamodel.log.InteractionRecord} object.
	 * @param recordIndex a int.
	 */
	public MinedInteractionVertex(InteractionRecord record, int recordIndex) {
		this.record = record;
		this.recordIndex = recordIndex;
		this.id = IDTEXT + "_" + RECORDCOUNTER++;
	}

	/** {@inheritDoc} */
	@Override
	public String getId() {
		return this.id;
	}

	/**
	 * <p>getInteractionRecord.</p>
	 */
	public InteractionRecord getInteractionRecord() {
		return this.record;
	}

	/**
	 * <p>Getter for the field <code>recordIndex</code>.</p>
	 */
	public int getRecordIndex() {
		return this.recordIndex;
	}

	/** {@inheritDoc} */
	@Override
	public String getUrl() {
		return record.getRequest().getUrl();
	}

	/** {@inheritDoc} */
	@Override
	public double getStartTS() {
		return record.getRequest().getStartTS();
	}

	/** {@inheritDoc} */
	@Override
	public double getEndTS() {
		return record.getRequest().getEndTS();
	}

	/** {@inheritDoc} */
	@Override
	public Interaction getInteraction() {
		return record;
	}

}
