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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uga.cs.json.JSONSerializable;

/**
 * <p>WindowEntry class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: WindowEntry.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class WindowEntry extends JSONSerializable {

	private static final long serialVersionUID = 2042905925519617210L;
	//the ids must be the automatically generated handles from webdriver
	private String windowID = null;
	private String openerID = null;

	private static transient final Log log = LogFactory.getLog(WindowEntry.class);

	/**
	 * <p>Constructor for WindowEntry.</p>
	 *
	 * @param windowID a {@link java.lang.String} object.
	 * @param openerID a {@link java.lang.String} object.
	 */
	public WindowEntry(String windowID, String openerID) {
		super(null);
		this.windowID = windowID;
		this.openerID = openerID;
	}

	/**
	 * <p>Constructor for WindowEntry.</p>
	 *
	 * @param jval a {@link org.json.JSONObject} object.
	 */
	public WindowEntry(JSONObject jval) {
		super(jval);
		this.fromJSONObject(jval);
	}

	/**
	 * <p>Getter for the field <code>windowID</code>.</p>
	 */
	public String getWindowID() {
		return this.windowID;
	}

	/**
	 * <p>gerOpenerID.</p>
	 */
	public String gerOpenerID() {
		return this.openerID;
	}

	/** {@inheritDoc} */
	@Override
	public void fromJSONObject(JSONObject jval) {
		this.windowID = jval.optString("windowID");
		this.openerID = jval.optString("openerID");
	}

	/** {@inheritDoc} */
	@Override
	public JSONObject toJSONObject() {
		JSONObject retval = new JSONObject();
		try {
			retval.put("windowID", this.windowID);
			retval.put("openerID", this.openerID);
		} catch (JSONException e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
		}
		return retval;
	}
}
