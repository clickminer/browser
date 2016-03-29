/*
* Copyright (C) 2013 Chris Neasbitt
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
package edu.uga.cs.clickminer.datamodel;

import java.util.ArrayList;

import org.json.JSONArray;

/**
 * <p>ElementLocator class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ElementLocator.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ElementLocator extends ArrayList<String> {

	private static final long serialVersionUID = 6616512604067852424L;

	/**
	 * <p>toString.</p>
	 */
	public String toString(){
		String retval = "";
		for(int i = 0; i < this.size(); i++){
			retval += this.get(i);
			if(i < this.size() - 1){
				retval += " => ";
			}
		}
		return retval;
	}
		
	/**
	 * <p>fromJSONArray.</p>
	 *
	 * @param arr a {@link org.json.JSONArray} object.
	 */
	public static ElementLocator fromJSONArray(JSONArray arr){
		ElementLocator locator = new ElementLocator();
		for (int i = 0; i < arr.length(); i++) {
			locator.add(arr.optString(i));
		}
		return locator;
	}
	
	/**
	 * <p>fromString.</p>
	 *
	 * @param source a {@link java.lang.String} object.
	 */
	public static ElementLocator fromString(String source){
		ElementLocator retval = new ElementLocator();
		for(String part : source.split(" => ")){
			retval.add(part);
		}
		return retval;
	}
}
