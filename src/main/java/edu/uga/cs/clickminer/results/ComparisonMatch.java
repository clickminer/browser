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

/**
 * <p>ComparisonMatch class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ComparisonMatch.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ComparisonMatch {

	private Integer recordedClick, matchingMinedClick;
	private Boolean clickMatchable , clickConfirmed, matchHasLocator, 
		urlMatch, locatorMatch;
	
	/**
	 * <p>Constructor for ComparisonMatch.</p>
	 *
	 * @param recordedClick a int.
	 * @param clickMatchable a boolean.
	 * @param clickConfirmed a boolean.
	 */
	public ComparisonMatch(int recordedClick, boolean clickMatchable, boolean clickConfirmed) {
		this.recordedClick = recordedClick;
		this.clickMatchable = clickMatchable;
		this.clickConfirmed = clickConfirmed;
	}
	
	/**
	 * <p>Constructor for ComparisonMatch.</p>
	 *
	 * @param recordedClick a int.
	 * @param clickMatchable a boolean.
	 * @param clickConfirmed a boolean.
	 * @param matchingMinedClick a int.
	 * @param matchHasLocator a boolean.
	 * @param urlMatch a boolean.
	 * @param locatorMatch a boolean.
	 */
	public ComparisonMatch(int recordedClick, boolean clickMatchable, 
			boolean clickConfirmed, int matchingMinedClick, boolean matchHasLocator,
			boolean urlMatch, boolean locatorMatch) {
		this.recordedClick = recordedClick;
		this.clickMatchable = clickMatchable;
		this.clickConfirmed = clickConfirmed;
		this.matchingMinedClick = matchingMinedClick;
		this.matchHasLocator = matchHasLocator;
		this.urlMatch = urlMatch;
		this.locatorMatch = locatorMatch;
	}

	/**
	 * <p>Getter for the field <code>recordedClick</code>.</p>
	 */
	public int getRecordedClick() {
		return recordedClick;
	}

	/**
	 * <p>Getter for the field <code>matchingMinedClick</code>.</p>
	 */
	public int getMatchingMinedClick() {
		return matchingMinedClick;
	}

	/**
	 * <p>Getter for the field <code>clickMatchable</code>.</p>
	 */
	public boolean getClickMatchable() {
		return clickMatchable;
	}

	/**
	 * <p>Getter for the field <code>clickConfirmed</code>.</p>
	 */
	public boolean getClickConfirmed() {
		return clickConfirmed;
	}

	/**
	 * <p>Getter for the field <code>matchHasLocator</code>.</p>
	 */
	public boolean getMatchHasLocator() {
		return matchHasLocator;
	}

	/**
	 * <p>Getter for the field <code>urlMatch</code>.</p>
	 */
	public boolean getUrlMatch() {
		return urlMatch;
	}

	/**
	 * <p>Getter for the field <code>locatorMatch</code>.</p>
	 */
	public boolean getLocatorMatch() {
		return locatorMatch;
	}
		
	/**
	 * <p>toCSVArray.</p>
	 */
	public String[] toCSVArray(){
		String[] retval = new String[7];
		retval[0] = recordedClick.toString();
		retval[1] = clickMatchable.toString();
		retval[2] = clickConfirmed.toString();
		if(matchingMinedClick != null){
			retval[3] = matchingMinedClick.toString();
			retval[4] = matchHasLocator.toString();
			retval[5] = urlMatch.toString();
			retval[6] = locatorMatch.toString();
		} else {
			retval[3] = "";
			retval[4] = "";
			retval[5] = "";
			retval[6] = "";
		}
		return retval;		
	}
}
