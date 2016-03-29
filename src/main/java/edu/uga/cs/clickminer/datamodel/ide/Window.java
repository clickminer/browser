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
 * <p>Window class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: Window.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class Window extends JSONSerializable {

	private static final long serialVersionUID = -8221647234266521055L;
	private String id = null, opener = null;
	private int historyLength = 0;

	private static transient final Log log = LogFactory.getLog(Window.class);

	/**
	 * <p>Constructor for Window.</p>
	 *
	 * @param jval a {@link org.json.JSONObject} object.
	 */
	public Window(JSONObject jval) {
		super(jval);
		fromJSONObject(jval);
	}

	/** {@inheritDoc} */
	public void fromJSONObject(JSONObject jval) {
		id = jval.optString("id");
		opener = jval.optString("opener");
		historyLength = jval.optInt("h_len");

	}

	/**
	 * <p>toJSONObject.</p>
	 */
	public JSONObject toJSONObject() {
		JSONObject retval = new JSONObject();
		try {
			retval.put("id", this.id);
			retval.put("opener", this.opener);
			retval.put("h_len", this.historyLength);
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
	 * <p>Getter for the field <code>opener</code>.</p>
	 */
	public String getOpener() {
		return opener;
	}

	/**
	 * <p>Setter for the field <code>opener</code>.</p>
	 *
	 * @param opener a {@link java.lang.String} object.
	 */
	public void setOpener(String opener) {
		this.opener = opener;
	}

	/**
	 * <p>Getter for the field <code>historyLength</code>.</p>
	 */
	public int getHistoryLength() {
		return historyLength;
	}

	/**
	 * <p>Setter for the field <code>historyLength</code>.</p>
	 *
	 * @param historyLength a int.
	 */
	public void setHistoryLength(int historyLength) {
		this.historyLength = historyLength;
	}

}
