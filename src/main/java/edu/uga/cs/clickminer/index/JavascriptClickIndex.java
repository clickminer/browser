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
package edu.uga.cs.clickminer.index;

import java.util.HashMap;

import edu.uga.cs.clickminer.datamodel.FramePath;

/**
 * <p>JavascriptClickIndex class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: JavascriptClickIndex.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class JavascriptClickIndex{

	private final HashMap<String, String> index = new HashMap<String, String>();
	
	/**
	 * <p>Constructor for JavascriptClickIndex.</p>
	 */
	public JavascriptClickIndex() {}
	
	/**
	 * <p>entryExists.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 * @param framePath a {@link edu.uga.cs.clickminer.datamodel.FramePath} object.
	 * @param elementid a {@link java.lang.String} object.
	 */
	public boolean entryExists(String windowHandle, 
			FramePath framePath, String elementid){
		return index.containsKey(this.createKey(windowHandle, 
				framePath, elementid));
	}
	
	/**
	 * <p>getEntry.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 * @param framePath a {@link edu.uga.cs.clickminer.datamodel.FramePath} object.
	 * @param elementid a {@link java.lang.String} object.
	 */
	public String getEntry(String windowHandle, 
			FramePath framePath, String elementid){
		return index.get(this.createKey(windowHandle, framePath, elementid));
	}
	
	/**
	 * <p>setEntry.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 * @param framePath a {@link edu.uga.cs.clickminer.datamodel.FramePath} object.
	 * @param elementid a {@link java.lang.String} object.
	 * @param url a {@link java.lang.String} object.
	 */
	public String setEntry(String windowHandle, 
			FramePath framePath, String elementid, String url){
		return index.put(this.createKey(windowHandle, framePath, elementid), url);
	}
	
	/**
	 * <p>removeEntry.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 * @param framePath a {@link edu.uga.cs.clickminer.datamodel.FramePath} object.
	 * @param elementid a {@link java.lang.String} object.
	 */
	public void removeEntry(String windowHandle, 
			FramePath framePath, String elementid){
		index.remove(this.createKey(windowHandle, framePath, elementid));
	}
	
	private String createKey(String windowHandle, 
			FramePath framePath, String elementid){
		String key = windowHandle;
		for(String url : framePath.getUrls()){
			key += url;
		}
		key += elementid;
		return key;
	}

}
