/*
* Copyright (C) 2012 Chris Neasbitt
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

import java.util.List;

import org.openqa.selenium.WebElement;

/**
 * <p>ElementSearchResult class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ElementSearchResult.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ElementSearchResult {

	private FramePath framePath;
	private List<WebElement> matchingElements;

	/**
	 * <p>Constructor for ElementSearchResult.</p>
	 *
	 * @param framePath a {@link edu.uga.cs.clickminer.datamodel.FramePath} object.
	 * @param matchingElements a {@link java.util.List} object.
	 */
	public ElementSearchResult(FramePath framePath, List<WebElement> matchingElements) {
		setFramePath(framePath);
		setMatchingElements(matchingElements);
	}
	
	/**
	 * <p>copy.</p>
	 */
	public ElementSearchResult copy(){
		return new ElementSearchResult(this.framePath, this.matchingElements);
	}

	/*public List<String> getFramePathUrls() {
		return framePathUrls;
	}*/

	/**
	 * <p>Getter for the field <code>matchingElements</code>.</p>
	 */
	public List<WebElement> getMatchingElements() {
		return matchingElements;
	}

	/*public void setFramePathUrls(List<String> framePathUrl) {
		if (framePathUrl == null) {
			throw new RuntimeException("framePathUrl should never be null.");
		}
		this.framePathUrls = framePathUrl;
	}*/

	/**
	 * <p>Setter for the field <code>matchingElements</code>.</p>
	 *
	 * @param matchingElements a {@link java.util.List} object.
	 */
	public void setMatchingElements(List<WebElement> matchingElements) {
		if (matchingElements == null) {
			throw new RuntimeException("matchingElements should never be null.");
		}
		this.matchingElements = matchingElements;
	}

	/**
	 * <p>Getter for the field <code>framePath</code>.</p>
	 */
	public FramePath getFramePath() {
		return framePath;
	}

	/**
	 * <p>Setter for the field <code>framePath</code>.</p>
	 *
	 * @param framePath a {@link edu.uga.cs.clickminer.datamodel.FramePath} object.
	 */
	public void setFramePath(FramePath framePath) {
		if (framePath == null) {
			throw new RuntimeException("framePath should never be null.");
		}
		this.framePath = framePath;
	}

	/**
	 * <p>matchesInDefaultFrame.</p>
	 */
	public boolean matchesInDefaultFrame() {
		return framePath.length() == 0;
	}

}
