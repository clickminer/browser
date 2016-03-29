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
package edu.uga.cs.adblock;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uga.cs.json.JSONSerializable;

/**
 * <p>Represents a parsed AdBlock rule consisting of a regex, a list of options, 
 * and a flag to signify whether or not this rule is an exception rule.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: AdBlockRule.java 845 2013-10-03 17:21:16Z cjneasbitt $Id
 */
public class AdBlockRule extends JSONSerializable {

	private static final long serialVersionUID = -3078841771709740372L;
	private String regex;
	private List<String> options;
	private boolean exception;

	private static transient final Log log = LogFactory.getLog(AdBlockRule.class);

	/**
	 * <p>Constructs an {@link edu.uga.cs.adblock.AdBlockRule} object from 
	 * it's JSON representation.</p>
	 *
	 * @param jval a {@link org.json.JSONObject} object representing the 
	 * {@link edu.uga.cs.adblock.AdBlockRule}.
	 */
	public AdBlockRule(JSONObject jval) {
		super(jval);
		this.fromJSONObject(jval);
	}

	/**
	 * <p>Constructs an {@link org.json.JSONObject} object from it's
	 * constituent components.</p>
	 *
	 * @param regex a {@link java.lang.String} object.
	 * @param options a {@link java.util.List} object.
	 * @param exception a boolean.
	 */
	public AdBlockRule(String regex, List<String> options, boolean exception) {
		super(null);
		this.regex = regex;
		this.options = options;
		this.exception = exception;
	}

	/** {@inheritDoc} */
	@Override
	public void fromJSONObject(JSONObject jval) {
		this.regex = jval.optString("regex");
		this.options = new ArrayList<String>();
		JSONArray optarr = jval.optJSONArray("options");
		for (int i = 0; i < optarr.length(); i++) {
			this.options.add(optarr.optString(i));
		}

		this.exception = jval.optBoolean("exception");
	}

	/** {@inheritDoc} */
	@Override
	public JSONObject toJSONObject() {
		JSONObject retval = new JSONObject();
		try {
			retval.put("regex", this.regex);
			retval.put("options", this.options);
			retval.put("exception", this.exception);
		} catch (JSONException e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
		}
		return retval;
	}

	/**
	 * <p>Getter for the field <code>regex</code>.</p>
	 *
	 * @return a {@link java.lang.String} object representing the 
	 * parsed AdBlock rule.
	 */
	public String getRegex() {
		return this.regex;
	}

	/**
	 * <p>Getter for the field <code>options</code>.</p>
	 *
	 * @return a {@link java.util.List} object of AdBlock rule options.
	 */
	public List<String> getOptions() {
		return this.options;
	}

	/**
	 * <p>Returns true if this rule is an exception rule.</p>
	 *
	 * @return a boolean signifying whether or not this rule is an exception rule.
	 */
	public boolean isException() {
		return this.exception;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.toJSONString();
	}

}
