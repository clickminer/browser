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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import edu.uga.cs.clickminer.datamodel.ElementLocator;
import edu.uga.cs.clickminer.util.JSUtils;
import edu.uga.cs.json.JSONSerializable;

/**
 * <p>ElementEntry class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ElementEntry.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ElementEntry extends JSONSerializable {

	private static final long serialVersionUID = 2455814357286713907L;
	private String tag, name, id = null; 
	private ElementLocator locator = null;
	private boolean selected = false;
	private static transient final Log log = LogFactory.getLog(ElementEntry.class);

	/**
	 * <p>Constructor for ElementEntry.</p>
	 *
	 * @param jval a {@link org.json.JSONObject} object.
	 */
	public ElementEntry(JSONObject jval) {
		super(jval);
		this.fromJSONObject(jval);
	}

	/**
	 * <p>Constructor for ElementEntry.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 * @param elem a {@link org.openqa.selenium.WebElement} object.
	 */
	public ElementEntry(WebDriver wdriver, WebElement elem) {
		super(null);
		tag = elem.getTagName();
		name = elem.getAttribute("name");
		id = elem.getAttribute("id");
		try {
			locator = JSUtils.getElementLocator(wdriver, elem);
		} catch (Exception e) {
			if(log.isErrorEnabled()){
				log.error("Unable to get element locator for tag " + tag + 
						".  Setting empty locator.", e);
			}
			locator = new ElementLocator();
		}
	}

	/** {@inheritDoc} */
	public void fromJSONObject(JSONObject jval) {
		this.tag = jval.optString("tag");
		this.name = jval.optString("name");
		this.id = jval.optString("id");
		
		this.selected = jval.optBoolean("selected");
		this.locator = ElementLocator.fromJSONArray(
				jval.optJSONArray("locator"));
	}

	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 */
	public String getId() {
		return id;
	}

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Getter for the field <code>selected</code>.</p>
	 *
	 * @param selected a boolean.
	 */
	public boolean getSelected(boolean selected) {
		return this.selected;
	}

	/**
	 * <p>Getter for the field <code>tag</code>.</p>
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * <p>Getter for the field <code>locator</code>.</p>
	 */
	public ElementLocator getLocator() {
		return locator;
	}
	
	/**
	 * <p>getLocatorString.</p>
	 */
	public String getLocatorString(){
		String retval = "";
		for(int i = 0; i < this.locator.size(); i++){
			retval += this.locator.get(i);
			if(i < this.locator.size() - 1){
				retval += " => ";
			}
		}
		return retval;
	}

	/**
	 * <p>matches.</p>
	 *
	 * @param elem a {@link org.openqa.selenium.WebElement} object.
	 */
	public boolean matches(WebElement elem) {
		String elemname = elem.getAttribute("name");
		String elemid = elem.getAttribute("id");

		if (tag.equals(elem.getTagName())) {
			if ((name == null && elemname == name) || name.equals(elemname)) {
				if ((id == null && elemid == id) || id.equals(elemid)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * <p>Setter for the field <code>selected</code>.</p>
	 *
	 * @param selected a boolean.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * <p>toJSONObject.</p>
	 */
	public JSONObject toJSONObject() {
		JSONObject retval = new JSONObject();
		try {
			retval.put("tag", this.tag);
			retval.put("name", this.name);
			retval.put("id", this.id);
			retval.put("selected", this.selected);
			retval.put("locator", this.locator);
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
