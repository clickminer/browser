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
import java.util.List;

import org.openqa.selenium.WebElement;

/**
 * <p>FramePath class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: FramePath.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class FramePath {

	//if path is empty then it describes the root frame of a window
	private List<WebElement> path;
	//urls should always have at least one element
	private List<String> urls;
	
	/**
	 * <p>Constructor for FramePath.</p>
	 */
	public FramePath(){
		this.path = new ArrayList<WebElement>();
		this.urls = new ArrayList<String>();
	}
	
	/**
	 * <p>Constructor for FramePath.</p>
	 *
	 * @param src a {@link edu.uga.cs.clickminer.datamodel.FramePath} object.
	 */
	public FramePath(FramePath src){
		this.path = src.getPath();
		this.urls = src.getUrls();
	}
	
	/**
	 * <p>appendFrame.</p>
	 *
	 * @param elem a {@link org.openqa.selenium.WebElement} object.
	 */
	public void appendFrame(WebElement elem){
		this.path.add(elem);
	}
	
	/**
	 * <p>appendUrl.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 */
	public void appendUrl(String url){
		this.urls.add(url);
	}
	
	/**
	 * <p>Getter for the field <code>path</code>.</p>
	 */
	public List<WebElement> getPath(){
		return new ArrayList<WebElement>(path);
	}
	
	/**
	 * <p>Getter for the field <code>urls</code>.</p>
	 */
	public List<String> getUrls(){
		return new ArrayList<String>(urls);
	}
	
	/**
	 * <p>getRefUrl.</p>
	 */
	public String getRefUrl(){
		String retval = null;
		if(urls.size() > 0){
			retval = urls.get(urls.size() - 1);
		}
		return retval;
	}
	
	/**
	 * <p>pathString.</p>
	 *
	 * @param max a int.
	 */
	public String pathString(int max){
		String retval = new String();
		int pathSize = urls.size();
		if(pathSize < max){
			max = pathSize;
		}
		for(int i = 0; i < max; i++){
			retval += urls.get(i);
			if(i < pathSize - 1){
				retval += " -> ";
			}
		}
		return retval;
	}
	
	/**
	 * <p>length.</p>
	 */
	public int length(){
		return path.size();
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString(){
		return pathString(Integer.MAX_VALUE);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj){
		if(obj instanceof FramePath){
			FramePath temp = (FramePath)obj;
			return temp.path.equals(path) && temp.urls.equals(urls);
		}
		return false;
	}

}
