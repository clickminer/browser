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
package edu.uga.cs.clickminer.datamodel;

import java.util.ArrayList;
import java.util.List;

import pcap.reconst.http.datamodel.RecordedHttpRequestMessage;

/**
 * <p>InferredInteraction class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: InferredInteraction.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class InferredInteraction implements Interaction {

	private RecordedHttpRequestMessage message;
	private double refererDelay;
	
	/**
	 * <p>Constructor for InferredInteraction.</p>
	 *
	 * @param message a {@link pcap.reconst.http.datamodel.RecordedHttpRequestMessage} object.
	 * @param refererDelay a double.
	 */
	public InferredInteraction(RecordedHttpRequestMessage message, 
			double refererDelay) {
		this.message = message;
		this.refererDelay = refererDelay;
	}

	/** {@inheritDoc} */
	@Override
	public String getInteractionUrl() {
		return message.getUrl();
	}

	/** {@inheritDoc} */
	@Override
	public List<ElementLocator> getLocators() {
		return new ArrayList<ElementLocator>();
	}

	/** {@inheritDoc} */
	@Override
	public double getDelayFromReferer() {
		return refererDelay;
	}

	/** {@inheritDoc} */
	@Override
	public boolean possibleAddressBarInteraction() {
		return !message.containsHeader("Referer");
	}
	
	/**
	 * <p>toString.</p>
	 */
	public String toString() {
		return "Target URL: " + getInteractionUrl() +  "\n"
				+ "Target Locators:" + "\n";
	}

}
