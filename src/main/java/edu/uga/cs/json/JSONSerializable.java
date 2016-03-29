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
package edu.uga.cs.json;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

/**
 * <p>Abstract JSONSerializable class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: JSONSerializable.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public abstract class JSONSerializable implements Serializable, JSONString {
	
	/**
	 * <p>Constructor for JSONSerializable.</p>
	 *
	 * @param jval a {@link org.json.JSONObject} object.
	 */
	protected JSONSerializable(JSONObject jval){};
	
	private static final long serialVersionUID = -1262032570235357587L;

	private static final transient Log log = LogFactory.getLog(JSONSerializable.class);
	
	/**
	 * <p>fromJSONObject.</p>
	 *
	 * @param jval a {@link org.json.JSONObject} object.
	 */
	public abstract void fromJSONObject(JSONObject jval);

	/**
	 * <p>toJSONObject.</p>
	 */
	public abstract JSONObject toJSONObject();
	
	/**
	 * <p>toJSONString.</p>
	 */
	public final String toJSONString() {
		try {
			return this.toJSONObject().toString(4);
		} catch (JSONException e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
		}
		return new String();
	}
	
	/** {@inheritDoc} */
	public boolean equals(Object obj){
		if(obj instanceof JSONSerializable){
			JSONSerializable op = (JSONSerializable)obj;
			if(this.toJSONString().equals(op.toJSONString())){
				return true;
			}
		}
		return false;
	}
	
}
