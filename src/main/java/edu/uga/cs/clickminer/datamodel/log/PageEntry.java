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

import edu.uga.cs.clickminer.datamodel.ElementSearchResult;
import edu.uga.cs.clickminer.index.WindowInteractionIndex;
import edu.uga.cs.json.JSONSerializable;

/**
 * <p>PageEntry class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: PageEntry.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class PageEntry extends JSONSerializable  {

	private static final long serialVersionUID = 4068796575649952307L;
	private WindowEntry winentry;
	private List<FrameEntry> frames = new ArrayList<FrameEntry>();
	private String url;
	private static transient final Log log = LogFactory.getLog(PageEntry.class);

	/**
	 * <p>getPageEntry.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 * @param windowIMap a {@link edu.uga.cs.clickminer.index.WindowInteractionIndex} object.
	 * @param windowhandle a {@link java.lang.String} object.
	 * @param results a {@link java.util.List} object.
	 */
	public static PageEntry getPageEntry(WebDriver wdriver,
			WindowInteractionIndex windowIMap, String windowhandle,
			List<ElementSearchResult> results) {
		WindowEntry winentry = new WindowEntry(windowhandle,
				windowIMap.getWindowOpener(windowhandle));
		List<FrameEntry> entries = new ArrayList<FrameEntry>();
		for (ElementSearchResult result : results) {
			entries.add(FrameEntry.getFrameEntry(wdriver, result));
		}
		return new PageEntry(winentry, entries, wdriver.getCurrentUrl());
	}

	/**
	 * <p>getPageEntry.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 * @param windowIMap a {@link edu.uga.cs.clickminer.index.WindowInteractionIndex} object.
	 * @param windowhandle a {@link java.lang.String} object.
	 */
	public static PageEntry getPageEntry(WebDriver wdriver,
			WindowInteractionIndex windowIMap, String windowhandle) {
		WindowEntry winentry = new WindowEntry(windowhandle,
				windowIMap.getWindowOpener(windowhandle));
		List<FrameEntry> entries = new ArrayList<FrameEntry>();
		return new PageEntry(winentry, entries, wdriver.getCurrentUrl());
	}

	/**
	 * <p>Constructor for PageEntry.</p>
	 *
	 * @param jval a {@link org.json.JSONObject} object.
	 */
	public PageEntry(JSONObject jval) {
		super(jval);
		this.fromJSONObject(jval);
	}

	/**
	 * <p>Constructor for PageEntry.</p>
	 *
	 * @param winentry a {@link edu.uga.cs.clickminer.datamodel.log.WindowEntry} object.
	 * @param frames a {@link java.util.List} object.
	 * @param url a {@link java.lang.String} object.
	 */
	public PageEntry(WindowEntry winentry, List<FrameEntry> frames, String url) {
		super(null);
		this.winentry = winentry;
		this.frames = frames;
		this.url = url;
	}

	/** {@inheritDoc} */
	public void fromJSONObject(JSONObject jval) {
		JSONArray fpArray = jval.optJSONArray("frames");
		frames = new ArrayList<FrameEntry>();
		for (int i = 0; i < fpArray.length(); i++) {
			frames.add(new FrameEntry(fpArray.optJSONObject(i)));
		}
		this.url = jval.optString("url");
		this.winentry = new WindowEntry(jval.optJSONObject("window"));
	}

	/**
	 * <p>Getter for the field <code>frames</code>.</p>
	 */
	public List<FrameEntry> getFrames() {
		return frames;
	}

	/**
	 * <p>getWindow.</p>
	 */
	public WindowEntry getWindow() {
		return winentry;
	}

	/**
	 * <p>getMatchingFrameEntry.</p>
	 *
	 * @param framepath a {@link java.util.List} object.
	 */
	public FrameEntry getMatchingFrameEntry(List<String> framepath) {
		for (FrameEntry fentry : this.frames) {
			if (fentry.matches(framepath)) {
				return fentry;
			}
		}
		return null;
	}

	/**
	 * <p>Getter for the field <code>url</code>.</p>
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * <p>toJSONObject.</p>
	 */
	public JSONObject toJSONObject() {
		JSONObject retval = new JSONObject();
		try {
			JSONArray fp = new JSONArray();
			for (FrameEntry fent : frames) {
				fp.put(fent.toJSONObject());
			}
			retval.put("frames", fp);
			retval.put("url", url);
			retval.put("window", winentry.toJSONObject());
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
