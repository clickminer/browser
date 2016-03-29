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
package edu.uga.cs.clickminer.datamodel.ide;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uga.cs.json.JSONSerializable;

/**
 * <p>Page class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: Page.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class Page extends JSONSerializable {

	private static final long serialVersionUID = -314731024176928837L;
	private Window window = null;
	private Frame frame = null;
	private String title = null, url = null, referer = null;
	private double timestamp = 0.0;

	private static transient final Log log = LogFactory.getLog(Page.class);

	/**
	 * <p>Constructor for Page.</p>
	 *
	 * @param jval a {@link org.json.JSONObject} object.
	 */
	public Page(JSONObject jval) {
		super(jval);
		fromJSONObject(jval);
	}

	/** {@inheritDoc} */
	public void fromJSONObject(JSONObject jval) {
		JSONObject winobj = jval.optJSONObject("window");
		if (winobj != null) {
			this.window = new Window(winobj);
		} else {
			this.window = null;
		}

		JSONObject frameobj = jval.optJSONObject("frame");
		if (frameobj != null) {
			this.frame = new Frame(frameobj);
		} else {
			this.frame = null;
		}

		this.title = jval.optString("title");
		this.url = jval.optString("url");
		this.referer = jval.optString("referer");
		this.timestamp = jval.optDouble("timestamp");
	}

	/**
	 * <p>toJSONObject.</p>
	 */
	public JSONObject toJSONObject() {
		JSONObject retval = new JSONObject();
		try {
			
			if (this.window != null) {
				retval.put("window", this.window.toJSONObject());
			} else {
				retval.put("window", JSONObject.NULL);
			}
			
			if (this.frame != null) {
				retval.put("frame", this.frame.toJSONObject());
			} else {
				retval.put("frame", JSONObject.NULL);
			}
			
			retval.put("title", this.title);
			retval.put("url", this.url);
			retval.put("referer", this.referer);
			retval.put("timestamp", this.timestamp);
		} catch (JSONException e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
		}

		return retval;
	}

	/**
	 * <p>Getter for the field <code>window</code>.</p>
	 */
	public Window getWindow() {
		return window;
	}

	/**
	 * <p>Setter for the field <code>window</code>.</p>
	 *
	 * @param window a {@link edu.uga.cs.clickminer.datamodel.ide.Window} object.
	 */
	public void setWindow(Window window) {
		this.window = window;
	}

	/**
	 * <p>Getter for the field <code>frame</code>.</p>
	 */
	public Frame getFrame() {
		return frame;
	}

	/**
	 * <p>Setter for the field <code>frame</code>.</p>
	 *
	 * @param frame a {@link edu.uga.cs.clickminer.datamodel.ide.Frame} object.
	 */
	public void setFrame(Frame frame) {
		this.frame = frame;
	}

	/**
	 * <p>Getter for the field <code>title</code>.</p>
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * <p>Setter for the field <code>title</code>.</p>
	 *
	 * @param title a {@link java.lang.String} object.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * <p>Getter for the field <code>url</code>.</p>
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * <p>Setter for the field <code>url</code>.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * <p>Getter for the field <code>referer</code>.</p>
	 */
	public String getReferer() {
		return referer;
	}

	/**
	 * <p>Setter for the field <code>referer</code>.</p>
	 *
	 * @param referer a {@link java.lang.String} object.
	 */
	public void setReferer(String referer) {
		this.referer = referer;
	}

	/**
	 * <p>Getter for the field <code>timestamp</code>.</p>
	 */
	public double getTimestamp() {
		return timestamp;
	}

	/**
	 * <p>Setter for the field <code>timestamp</code>.</p>
	 *
	 * @param timestamp a double.
	 */
	public void setTimestamp(double timestamp) {
		this.timestamp = timestamp;
	}

}
