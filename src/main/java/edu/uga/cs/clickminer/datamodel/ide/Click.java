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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uga.cs.json.JSONSerializable;

/**
 * <p>Click class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: Click.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class Click extends JSONSerializable {

	private static final long serialVersionUID = -8985837308209309262L;
	/** Constant <code>JS_EMBEDDED_H="[javascipt+embedded_object]"</code> */
	public static final String JS_EMBEDDED_H = "[javascipt+embedded_object]";
	/** Constant <code>EMBEDDED_H="[embedded_object]"</code> */
	public static final String EMBEDDED_H = "[embedded_object]";
	/** Constant <code>JS_H="[javascipt]"</code> */
	public static final String JS_H = "[javascipt]";

	private Page origin = null, destination = null;
	private String targetUrl = null, absoluteTargetUrl = null, targetObject = null;
	private double timestamp = 0.0;
	private boolean confirmed = false, loadedFromCache = false;
	private List<String> handlers = null, targetLocator = null;

	private static transient final Log log = LogFactory.getLog(Click.class);

	
	/**
	 * <p>Constructor for Click.</p>
	 *
	 * @param jval a {@link org.json.JSONObject} object.
	 */
	public Click(JSONObject jval) {
		super(jval);
		fromJSONObject(jval);
	}
	
	/**
	 * <p>Getter for the field <code>origin</code>.</p>
	 */
	public Page getOrigin() {
		return origin;
	}

	/**
	 * <p>Setter for the field <code>origin</code>.</p>
	 *
	 * @param origin a {@link edu.uga.cs.clickminer.datamodel.ide.Page} object.
	 */
	public void setOrigin(Page origin) {
		this.origin = origin;
	}

	/**
	 * <p>Getter for the field <code>destination</code>.</p>
	 */
	public Page getDestination() {
		return destination;
	}

	/**
	 * <p>Setter for the field <code>destination</code>.</p>
	 *
	 * @param destination a {@link edu.uga.cs.clickminer.datamodel.ide.Page} object.
	 */
	public void setDestination(Page destination) {
		this.destination = destination;
	}

	/**
	 * <p>Getter for the field <code>handlers</code>.</p>
	 */
	public List<String> getHandlers() {
		return handlers;
	}

	/**
	 * <p>Setter for the field <code>handlers</code>.</p>
	 *
	 * @param handlers a {@link java.util.List} object.
	 */
	public void setHandlers(List<String> handlers) {
		this.handlers = handlers;
	}

	/**
	 * <p>Getter for the field <code>targetUrl</code>.</p>
	 */
	public String getTargetUrl() {
		return targetUrl;
	}

	/**
	 * <p>Setter for the field <code>targetUrl</code>.</p>
	 *
	 * @param target_url a {@link java.lang.String} object.
	 */
	public void setTargetUrl(String target_url) {
		this.targetUrl = target_url;
	}
	
	/**
	 * <p>Getter for the field <code>absoluteTargetUrl</code>.</p>
	 */
	public String getAbsoluteTargetUrl(){
		return absoluteTargetUrl;
	}
	
	/**
	 * <p>Setter for the field <code>absoluteTargetUrl</code>.</p>
	 *
	 * @param absoluteTargetUrl a {@link java.lang.String} object.
	 */
	public void setAbsoluteTargetUrl(String absoluteTargetUrl){
		this.absoluteTargetUrl = absoluteTargetUrl;
	}

	/**
	 * <p>Getter for the field <code>targetLocator</code>.</p>
	 */
	public List<String> getTargetLocator() {
		return targetLocator;
	}

	/**
	 * <p>Setter for the field <code>targetLocator</code>.</p>
	 *
	 * @param target_locator a {@link java.util.List} object.
	 */
	public void setTargetLocator(List<String> target_locator) {
		this.targetLocator = target_locator;
	}

	/**
	 * <p>Getter for the field <code>targetObject</code>.</p>
	 */
	public String getTargetObject() {
		return targetObject;
	}

	/**
	 * <p>Setter for the field <code>targetObject</code>.</p>
	 *
	 * @param target_object a {@link java.lang.String} object.
	 */
	public void setTargetObject(String target_object) {
		this.targetObject = target_object;
	}
	
	//in sec
	/**
	 * <p>getTimestampSec.</p>
	 */
	public double getTimestampSec(){
		return timestamp/1000;
	}

	//in ms
	/**
	 * <p>Getter for the field <code>timestamp</code>.</p>
	 */
	public double getTimestamp() {
		return timestamp;
	}

	//in ms
	/**
	 * <p>Setter for the field <code>timestamp</code>.</p>
	 *
	 * @param timestamp a double.
	 */
	public void setTimestamp(double timestamp) {
		this.timestamp = timestamp;
	}

	/** {@inheritDoc} */
	public void fromJSONObject(JSONObject jval) {
		JSONObject originobj = jval.optJSONObject("origin");
		if (originobj != null) {
			this.origin = new Page(originobj);
		} else {
			this.origin = null;
		}

		JSONObject destobj = jval.optJSONObject("destination");
		if (destobj != null) {
			this.destination = new Page(destobj);
		} else {
			this.destination = null;
		}

		JSONArray handlerarr = jval.optJSONArray("handlers");
		if(handlerarr != null){
			this.handlers = new ArrayList<String>();
			for(int i = 0; i < handlerarr.length(); i++){
				handlers.add(handlerarr.optString(i));
			}
		} else {
			this.handlers = null;
		}
		
		JSONArray targetarr = jval.optJSONArray("target_locator");
		if(targetarr != null){
			this.targetLocator = new ArrayList<String>();
			for(int i = 0; i < targetarr.length(); i++){
				targetLocator.add(targetarr.optString(i));
			}
		} else {
			this.targetLocator = null;
		}
				
		this.targetUrl = jval.optString("target_url");
		this.absoluteTargetUrl = jval.optString("absolute_target_url");
		this.targetObject = jval.optString("target_object");
		this.timestamp = jval.optDouble("timestamp");
		this.confirmed = jval.optBoolean("confirmed");
		this.loadedFromCache = jval.optBoolean("loaded_from_cache");
	}

	/**
	 * <p>toJSONObject.</p>
	 */
	public JSONObject toJSONObject() {
		JSONObject retval = new JSONObject();
		try {
			if (this.origin != null) {
				retval.put("origin", this.origin.toJSONObject());
			} else {
				retval.put("origin", JSONObject.NULL);
			}
			
			if (this.destination != null) {
				retval.put("destination", this.destination.toJSONObject());
			} else {
				retval.put("destination", JSONObject.NULL);
			}
			
			if (this.handlers != null){
				retval.put("handlers", new JSONArray(this.handlers));
			} else {
				retval.put("handlers", JSONObject.NULL);
			}
			
			if (this.targetLocator != null){
				retval.put("target_locator", new JSONArray(this.targetLocator));
			} else {
				retval.put("target_locator", JSONObject.NULL);
			}
			
			retval.put("target_url", this.targetUrl);
			retval.put("absolute_target_url", this.absoluteTargetUrl);
			retval.put("target_object", this.targetObject);
			retval.put("timestamp", this.timestamp);
			retval.put("confirmed", this.confirmed);
			retval.put("loaded_from_cache", this.loadedFromCache);
		} catch (JSONException e) {
			if (log.isErrorEnabled()) {
				log.error("",e);
			}
		}

		return retval;
	}
	
	/**
	 * <p>toString.</p>
	 */
	public String toString(){
		String retval = "Target URL: " + this.getAbsoluteTargetUrl() + "\n";
		String loc = "";
		for(int i = 0; i < this.targetLocator.size(); i++){
			loc += this.targetLocator.get(i);
			if(i < this.targetLocator.size() - 1){
				loc += " => ";
			}
		}
		retval += "Target Locator: " + loc + "\n"
			+ "Confirmed: " + this.confirmed + "\n"
			+ "Timestamp: " + this.timestamp + "\n";
		return retval;
	}

	/**
	 * <p>isConfirmed.</p>
	 */
	public boolean isConfirmed() {
		return confirmed;
	}
	
	/**
	 * <p>isMatchable.</p>
	 */
	public boolean isMatchable() {
		try {
			URL target = new URL(this.absoluteTargetUrl);
			if(target.getProtocol().equalsIgnoreCase("https")){
				return false;
			}
		} catch (MalformedURLException e) {
			if (log.isDebugEnabled()) {
				log.debug("",e);
			}
			return false;
		}
		return true;
	}

	/**
	 * <p>Setter for the field <code>confirmed</code>.</p>
	 *
	 * @param confirmed a boolean.
	 */
	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	/**
	 * <p>isLoadedFromCache.</p>
	 */
	public boolean isLoadedFromCache() {
		return loadedFromCache;
	}

	/**
	 * <p>Setter for the field <code>loadedFromCache</code>.</p>
	 *
	 * @param loadedFromCache a boolean.
	 */
	public void setLoadedFromCache(boolean loadedFromCache) {
		this.loadedFromCache = loadedFromCache;
	}

}
