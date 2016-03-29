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

import edu.uga.cs.clickminer.datamodel.FramePath;

/**
 * <p>FramePathIndexEntry class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: FramePathIndexEntry.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class FramePathIndexEntry{

	private FramePath framePath;
	private boolean leafPath = false;
	private FramePathIndex index;
	private String window;
	
	/**
	 * <p>Constructor for FramePathIndexEntry.</p>
	 *
	 * @param framePath a {@link edu.uga.cs.clickminer.datamodel.FramePath} object.
	 * @param window a {@link java.lang.String} object.
	 * @param index a {@link edu.uga.cs.clickminer.index.FramePathIndex} object.
	 */
	public FramePathIndexEntry(FramePath framePath, String window, FramePathIndex index) {
		this.framePath = framePath;
		this.index = index;
		this.window = window;
	}
	
	/**
	 * <p>Setter for the field <code>leafPath</code>.</p>
	 *
	 * @param leafPath a boolean.
	 */
	protected void setLeafPath(boolean leafPath){
		this.leafPath = leafPath;
	}
	
	/**
	 * <p>Getter for the field <code>window</code>.</p>
	 */
	public String getWindow(){
		return window;
	}
	
	/**
	 * <p>isLeafPath.</p>
	 */
	public boolean isLeafPath(){
		return leafPath;
	}
	
	/**
	 * <p>Getter for the field <code>index</code>.</p>
	 */
	public FramePathIndex getIndex(){
		return index;
	}
	
	/**
	 * <p>Getter for the field <code>framePath</code>.</p>
	 */
	public FramePath getFramePath(){
		return framePath;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString(){
		return framePath.toString();
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj){
		if(obj instanceof FramePathIndexEntry){
			FramePathIndexEntry temp = (FramePathIndexEntry)obj;
			return temp.framePath.equals(framePath) && temp.window.equals(window);
		}
		return false;
	}
	
	/**
	 * <p>removeFromIndex.</p>
	 */
	public void removeFromIndex(){
		index.removeEntry(window, this);
	}
	
	/*public void setWindowNeedsUpdate(){
		index.addNeedsUpdate(window);
	}*/
	
}
