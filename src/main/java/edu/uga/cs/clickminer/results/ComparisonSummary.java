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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>ComparisonSummary class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ComparisonSummary.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ComparisonSummary {
	private final int totalRecordedInteractions, matchableRecordedInteractions,
		matchableRecordedClicks, totalMinedInteractions, 
		totalMinedAddressBarInteractions, totalMinedFalsePositiveClicks, 
		minedMatchingPossibleClicks, minedMatchingPossibleNonClick, 
		minedMatchingClicks, minedMatchingNonClicks, confirmedClicks,
		unconfirmedClicks, augmentedClicks;

	/**
	 * <p>Constructor for ComparisonSummary.</p>
	 *
	 * @param totalRecordedInteractions a int.
	 * @param matchableRecordedInteractions a int.
	 * @param matchableRecordedClicks a int.
	 * @param totalMinedInteractions a int.
	 * @param totalMinedAddressBarInteractions a int.
	 * @param totalMinedFalsePositiveClicks a int.
	 * @param minedMatchingPossibleClicks a int.
	 * @param minedMatchingPossibleNonClick a int.
	 * @param minedMatchingClicks a int.
	 * @param minedMatchingNonClicks a int.
	 * @param confirmedClicks a int.
	 * @param unconfirmedClicks a int.
	 * @param augmentedClicks a int.
	 */
	public ComparisonSummary(int totalRecordedInteractions, int matchableRecordedInteractions,
			int matchableRecordedClicks, int totalMinedInteractions, 
			int totalMinedAddressBarInteractions, int totalMinedFalsePositiveClicks, 
			int minedMatchingPossibleClicks, int minedMatchingPossibleNonClick, 
			int minedMatchingClicks, int minedMatchingNonClicks, int confirmedClicks,
			int unconfirmedClicks, int augmentedClicks) {
		this.totalRecordedInteractions = totalRecordedInteractions;
		this.matchableRecordedInteractions = matchableRecordedInteractions;
		this.matchableRecordedClicks = matchableRecordedClicks;
		this.totalMinedInteractions = totalMinedInteractions;
		this.totalMinedAddressBarInteractions = totalMinedAddressBarInteractions;
		this.totalMinedFalsePositiveClicks = totalMinedFalsePositiveClicks;
		this.minedMatchingPossibleClicks = minedMatchingPossibleClicks;
		this.minedMatchingPossibleNonClick = minedMatchingPossibleNonClick;
		this.minedMatchingClicks = minedMatchingClicks;
		this.minedMatchingNonClicks = minedMatchingNonClicks;
		this.confirmedClicks = confirmedClicks;
		this.unconfirmedClicks = unconfirmedClicks;
		this.augmentedClicks = augmentedClicks;
	}

	/**
	 * <p>Getter for the field <code>totalRecordedInteractions</code>.</p>
	 */
	public int getTotalRecordedInteractions() {
		return totalRecordedInteractions;
	}

	/**
	 * <p>Getter for the field <code>matchableRecordedInteractions</code>.</p>
	 */
	public int getMatchableRecordedInteractions() {
		return matchableRecordedInteractions;
	}

	/**
	 * <p>Getter for the field <code>matchableRecordedClicks</code>.</p>
	 */
	public int getMatchableRecordedClicks() {
		return matchableRecordedClicks;
	}

	/**
	 * <p>Getter for the field <code>totalMinedInteractions</code>.</p>
	 */
	public int getTotalMinedInteractions() {
		return totalMinedInteractions;
	}

	/**
	 * <p>Getter for the field <code>totalMinedAddressBarInteractions</code>.</p>
	 */
	public int getTotalMinedAddressBarInteractions() {
		return totalMinedAddressBarInteractions;
	}

	/**
	 * <p>Getter for the field <code>totalMinedFalsePositiveClicks</code>.</p>
	 */
	public int getTotalMinedFalsePositiveClicks() {
		return totalMinedFalsePositiveClicks;
	}

	/**
	 * <p>Getter for the field <code>minedMatchingPossibleClicks</code>.</p>
	 */
	public int getMinedMatchingPossibleClicks() {
		return minedMatchingPossibleClicks;
	}

	/**
	 * <p>Getter for the field <code>minedMatchingPossibleNonClick</code>.</p>
	 */
	public int getMinedMatchingPossibleNonClick() {
		return minedMatchingPossibleNonClick;
	}

	/**
	 * <p>Getter for the field <code>minedMatchingClicks</code>.</p>
	 */
	public int getMinedMatchingClicks() {
		return minedMatchingClicks;
	}

	/**
	 * <p>Getter for the field <code>minedMatchingNonClicks</code>.</p>
	 */
	public int getMinedMatchingNonClicks() {
		return minedMatchingNonClicks;
	}
	
	/**
	 * <p>Getter for the field <code>confirmedClicks</code>.</p>
	 */
	public int getConfirmedClicks() {
		return confirmedClicks;
	}
	
	/**
	 * <p>Getter for the field <code>unconfirmedClicks</code>.</p>
	 */
	public int getUnconfirmedClicks() {
		return unconfirmedClicks;
	}
	
	/**
	 * <p>Getter for the field <code>augmentedClicks</code>.</p>
	 */
	public int getAugmentedClicks() {
		return augmentedClicks;
	}

	/**
	 * <p>toCSVData.</p>
	 */
	public List<String[]> toCSVData(){
		List<String[]> retval = new ArrayList<String[]>();
		String[] header = {"Total Recorded Interactions", 
				"Matchable Recorded Interactions",
				"Matchable Recorded Clicks", 
				"Total Mined Interactions",
				"Total Mined Address Bar Interactions",
				"Total Mined False Positive Clicks", //url and loc, no match
				"Mined Matching Possible Clicks",  //url, confirmed recorded
				"Mined Matching Possible Non-Click",  //url, unconfirmed recorded
				"Mined Matching Clicks", // url and loc, confirmed recorded
				"Mined Matching Non-Clicks", // url and loc, unconfirmed recorded
				"Confirmed Clicks",
				"Unconfirmed Clicks",
				"Augmented Clicks"};
		
		String[] data = new String[13];
		data[0] = Integer.toString(totalRecordedInteractions);
		data[1] = Integer.toString(matchableRecordedInteractions);
		data[2] = Integer.toString(matchableRecordedClicks);
		data[3] = Integer.toString(totalMinedInteractions);
		data[4] = Integer.toString(totalMinedAddressBarInteractions);
		data[5] = Integer.toString(totalMinedFalsePositiveClicks);
		data[6] = Integer.toString(minedMatchingPossibleClicks);
		data[7] = Integer.toString(minedMatchingPossibleNonClick);
		data[8] = Integer.toString(minedMatchingClicks);
		data[9] = Integer.toString(minedMatchingNonClicks);
		data[10] = Integer.toString(confirmedClicks);
		data[11] = Integer.toString(unconfirmedClicks);
		data[12] = Integer.toString(augmentedClicks);
		
		retval.add(header);
		retval.add(data);
		
		return retval;
	}

}
