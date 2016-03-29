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
package edu.uga.cs.clickminer.datamodel.log;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import edu.uga.cs.clickminer.datamodel.ElementSearchResult;
import edu.uga.cs.clickminer.util.FrameUtils;
import edu.uga.cs.json.JSONSerializable;

/**
 * <p>FrameEntry class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: FrameEntry.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class FrameEntry extends JSONSerializable {

	private static final long serialVersionUID = 1362038047792809888L;
	private List<String> framePath;
	//private ElementLocator locator;
	private List<ElementEntry> entries;
	private static transient final Log log = LogFactory.getLog(FrameEntry.class);

	/**
	 * <p>getFrameEntry.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 * @param result a {@link edu.uga.cs.clickminer.datamodel.ElementSearchResult} object.
	 */
	public static FrameEntry getFrameEntry(WebDriver wdriver,
			ElementSearchResult result) {

		List<ElementEntry> entries = new ArrayList<ElementEntry>();
		/*List<WebElement> framePath = result.getFramePath();
		ElementLocator locator = null;
		if (framePath.size() > 0) {
			List<WebElement> subFramePath = framePath.subList(0,
					framePath.size() - 1);
			FrameUtils.traverseFramePath(wdriver, subFramePath);
			WebElement curframe = framePath.get(framePath.size() - 1);
			try {
				locator = JSUtils.getElementLocator(wdriver, curframe);
			} catch (Exception e) {
				if(log.isErrorEnabled()){
					log.error("Unable to get element locator for frame.  " +
							"Setting empty locator.", e);
				}
				locator = new ElementLocator();
			}
		}*/
		FrameUtils.traverseFramePath(wdriver, result.getFramePath());
		for (WebElement elem : result.getMatchingElements()) {
			entries.add(new ElementEntry(wdriver, elem));
		}
		return new FrameEntry(result.getFramePath().getUrls(), entries);
	}

	/**
	 * <p>Constructor for FrameEntry.</p>
	 *
	 * @param jval a {@link org.json.JSONObject} object.
	 */
	public FrameEntry(JSONObject jval) {
		super(jval);
		this.fromJSONObject(jval);
	}
	
	/**
	 * <p>Constructor for FrameEntry.</p>
	 *
	 * @param framePath a {@link java.util.List} object.
	 * @param entries a {@link java.util.List} object.
	 */
	public FrameEntry(List<String> framePath, List<ElementEntry> entries) {
		super(null);
		this.framePath = framePath;
		this.entries = entries;
	}

	/*public FrameEntry(List<String> framePath, List<ElementEntry> entries,
			ElementLocator locator) {
		super(null);
		this.framePath = framePath;
		this.entries = entries;
		this.locator = locator;
	}*/

	/** {@inheritDoc} */
	public void fromJSONObject(JSONObject jval) {
		JSONArray fpArray = jval.optJSONArray("framepath");
		framePath = new ArrayList<String>();
		for (int i = 0; i < fpArray.length(); i++) {
			framePath.add(fpArray.optString(i));
		}

		fpArray = jval.optJSONArray("elements");
		entries = new ArrayList<ElementEntry>();
		for (int i = 0; i < fpArray.length(); i++) {
			entries.add(new ElementEntry(fpArray.optJSONObject(i)));
		}
		
		//locator = ElementLocator.fromJSONArray(jval.optJSONArray("locator"));
	}

	/**
	 * <p>Getter for the field <code>entries</code>.</p>
	 */
	public List<ElementEntry> getEntries() {
		return entries;
	}

	/**
	 * <p>Getter for the field <code>framePath</code>.</p>
	 */
	public List<String> getFramePath() {
		return framePath;
	}

	/*public ElementLocator getLocator() {
		return locator;
	}*/

	/**
	 * <p>getMatchingElementEntry.</p>
	 *
	 * @param elem a {@link org.openqa.selenium.WebElement} object.
	 */
	public ElementEntry getMatchingElementEntry(WebElement elem) {
		for (ElementEntry ee : entries) {
			if (ee.matches(elem)) {
				return ee;
			}
		}
		return null;
	}

	/**
	 * <p>matches.</p>
	 *
	 * @param framePath a {@link java.util.List} object.
	 */
	public boolean matches(List<String> framePath) {
		if (framePath.size() == this.framePath.size()) {
			int i;
			for (i = 0; i < framePath.size(); i++) {
				if (!framePath.get(i).equals(this.framePath.get(i))) {
					break;
				}
			}
			if (i == framePath.size()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>toJSONObject.</p>
	 */
	public JSONObject toJSONObject() {
		JSONObject retval = new JSONObject();
		try {
			retval.put("framepath", framePath);

			JSONArray fp = new JSONArray();
			for (ElementEntry ee : entries) {
				fp.put(ee.toJSONObject());
			}
			retval.put("elements", fp);
			
			//retval.put("locator", this.locator);
		} catch (JSONException e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
		}
		return retval;

	}

	/**
	 * <p>toString.</p>
	 */
	public String toString() {
		return this.toJSONObject().toString();
	}
}
