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
package edu.uga.cs.clickminer.results;

import java.util.List;

/**
 * <p>ComparisonResult class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ComparisonResult.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ComparisonResult {

	private final List<ComparisonMatch> matches;
	private final ComparisonSummary summary;
	private final List<Integer> falsePositiveClicks, addressBarInteractions,
		confirmedClicks, unconfirmedClicks, augmentedClicks;
	
	
	/**
	 * <p>Constructor for ComparisonResult.</p>
	 *
	 * @param summary a {@link edu.uga.cs.clickminer.results.ComparisonSummary} object.
	 * @param matches a {@link java.util.List} object.
	 * @param falsePositiveClicks a {@link java.util.List} object.
	 * @param addressBarInteractions a {@link java.util.List} object.
	 * @param confirmedClicks a {@link java.util.List} object.
	 * @param unconfirmedClicks a {@link java.util.List} object.
	 * @param augmentedClicks a {@link java.util.List} object.
	 */
	public ComparisonResult(ComparisonSummary summary, List<ComparisonMatch> matches,
			List<Integer> falsePositiveClicks, List<Integer> addressBarInteractions,
			List<Integer> confirmedClicks, List<Integer> unconfirmedClicks, 
			List<Integer> augmentedClicks) {
		this.summary = summary;
		this.matches = matches;
		this.falsePositiveClicks = falsePositiveClicks;
		this.addressBarInteractions = addressBarInteractions;
		this.confirmedClicks = confirmedClicks;
		this.unconfirmedClicks = unconfirmedClicks;
		this.augmentedClicks = augmentedClicks;
	}

	/**
	 * <p>Getter for the field <code>matches</code>.</p>
	 */
	public List<ComparisonMatch> getMatches() {
		return matches;
	}

	/**
	 * <p>Getter for the field <code>summary</code>.</p>
	 */
	public ComparisonSummary getSummary() {
		return summary;
	}

	/**
	 * <p>Getter for the field <code>falsePositiveClicks</code>.</p>
	 */
	public List<Integer> getFalsePositiveClicks() {
		return falsePositiveClicks;
	}

	/**
	 * <p>Getter for the field <code>addressBarInteractions</code>.</p>
	 */
	public List<Integer> getAddressBarInteractions() {
		return addressBarInteractions;
	}

	/**
	 * <p>Getter for the field <code>confirmedClicks</code>.</p>
	 */
	public List<Integer> getConfirmedClicks() {
		return confirmedClicks;
	}

	/**
	 * <p>Getter for the field <code>unconfirmedClicks</code>.</p>
	 */
	public List<Integer> getUnconfirmedClicks() {
		return unconfirmedClicks;
	}

	/**
	 * <p>Getter for the field <code>augmentedClicks</code>.</p>
	 */
	public List<Integer> getAugmentedClicks() {
		return augmentedClicks;
	}

}
