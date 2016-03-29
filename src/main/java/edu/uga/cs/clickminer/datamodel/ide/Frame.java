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


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uga.cs.json.JSONSerializable;

/**
 * <p>Frame class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: Frame.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class Frame extends JSONSerializable {

	private static final long serialVersionUID = -1656171098021429827L;
	private String id = null, windowId = null;
	private List<String> path = null;

	private static transient final Log log = LogFactory.getLog(Frame.class);

	/**
	 * <p>Constructor for Frame.</p>
	 *
	 * @param jval a {@link org.json.JSONObject} object.
	 */
	public Frame(JSONObject jval) {
		super(jval);
		fromJSONObject(jval);
	}
	
	/**
	 * <p>build.</p>
	 *
	 * @param jval a {@link org.json.JSONObject} object.
	 */
	public static Frame build(JSONObject jval){
		return new Frame(jval);
	}

	/** {@inheritDoc} */
	public void fromJSONObject(JSONObject jval) {
		id = jval.optString("id");
		windowId = jval.optString("w_id");
		JSONArray patharr = jval.optJSONArray("path");
		if (patharr != null) {
			path = new ArrayList<String>();
			for (int i = 0; i < patharr.length(); i++) {
				path.add(patharr.optString(i));
			}
		} else {
			path = null;
		}
	}

	/**
	 * <p>toJSONObject.</p>
	 */
	public JSONObject toJSONObject() {
		JSONObject retval = new JSONObject();
		try {
			retval.put("id", this.id);
			retval.put("w_id", this.windowId);
			if (path != null){
				retval.put("path", JSONObject.NULL);
			} else {
				retval.put("path", new JSONArray(path));
			}
		} catch (JSONException e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
		}

		return retval;
	}

	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 */
	public String getId() {
		return id;
	}

	/**
	 * <p>Setter for the field <code>id</code>.</p>
	 *
	 * @param id a {@link java.lang.String} object.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * <p>Getter for the field <code>windowId</code>.</p>
	 */
	public String getWindowId() {
		return windowId;
	}

	/**
	 * <p>Setter for the field <code>windowId</code>.</p>
	 *
	 * @param windowId a {@link java.lang.String} object.
	 */
	public void setWindowId(String windowId) {
		this.windowId = windowId;
	}

	/**
	 * <p>Getter for the field <code>path</code>.</p>
	 */
	public List<String> getPath() {
		return path;
	}

	/**
	 * <p>Setter for the field <code>path</code>.</p>
	 *
	 * @param path a {@link java.util.List} object.
	 */
	public void setPath(List<String> path) {
		this.path = path;
	}
}
