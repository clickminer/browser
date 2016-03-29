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
package edu.uga.cs.clickminer.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;

/**
 * <p>WindowInteractionIndex class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: WindowInteractionIndex.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class WindowInteractionIndex {

	private final Map<String, String> windowOpener = new HashMap<String, String>();
	private final Map<String, Long> windowTS = new HashMap<String, Long>();
	private final WebDriver wdriver;
	
	private static final transient Log log = LogFactory.getLog(WindowInteractionIndex.class);

	/**
	 * <p>Constructor for WindowInteractionIndex.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 */
	public WindowInteractionIndex(WebDriver wdriver) {
		this.wdriver = wdriver;
	}

	/**
	 * <p>updateNewWindows.</p>
	 */
	public Set<String> updateNewWindows() {
		/*Set<String> handles = wdriver.getWindowHandles();
		for (String handle : handles) {
			if (!windowTS.containsKey(handle)) {
				windowTS.put(handle, this.getCurrentTimestamp());
				windowOpener.put(handle, null);
			}
		}*/
		return updateNewWindows(null);
	}

	/**
	 * <p>updateNewWindows.</p>
	 *
	 * @param opener a {@link java.lang.String} object.
	 */
	public Set<String> updateNewWindows(String opener) {
		Set<String> retval = new HashSet<String>();
		Set<String> handles = wdriver.getWindowHandles();
		for (String handle : handles) {
			if (!windowTS.containsKey(handle)) {
				windowTS.put(handle, this.getCurrentTimestamp());
				windowOpener.put(handle, opener);
				retval.add(handle);
			}
		}
		return retval;
	}
	
	/**
	 * <p>removeWindow.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 */
	public void removeWindow(String windowHandle){
		if(windowTS.containsKey(windowHandle)){
			windowTS.remove(windowHandle);
		}
		if(windowOpener.containsKey(windowHandle)){
			windowOpener.remove(windowHandle);
		}
	}

	/**
	 * <p>updateWindow.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 */
	public void updateWindow(String windowHandle) {
		if (windowTS.containsKey(windowHandle)) {
			windowTS.put(windowHandle, this.getCurrentTimestamp());
		}
	}
	
	/**
	 * <p>getWindowTimestamp.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 */
	public long getWindowTimestamp(String windowHandle){
		if(windowTS.containsKey(windowHandle)){
			return windowTS.get(windowHandle);
		}
		return -1L;
	}

	/**
	 * <p>Setter for the field <code>windowOpener</code>.</p>
	 *
	 * @param window a {@link java.lang.String} object.
	 * @param opener a {@link java.lang.String} object.
	 */
	public void setWindowOpener(String window, String opener) {
		if (windowOpener.containsKey(window)) {
			windowOpener.put(window, opener);
		}
	}

	/**
	 * <p>Getter for the field <code>windowOpener</code>.</p>
	 *
	 * @param window a {@link java.lang.String} object.
	 */
	public String getWindowOpener(String window) {
		return windowOpener.get(window);
	}

	/**
	 * <p>getWindowsMRUOrder.</p>
	 */
	public List<String> getWindowsMRUOrder() {
		//checkIndexState();
		
		List<String> retval = new ArrayList<String>();
		List<Pair<String, Long>> temp = new ArrayList<Pair<String, Long>>();
		for (String key : windowTS.keySet()) {
			temp.add(new MutablePair<String, Long>(key, windowTS.get(key)));
		}

		Collections.sort(temp, new Comparator<Pair<String, Long>>() {
			public int compare(Pair<String, Long> arg0, Pair<String, Long> arg1) {
				// multiply by -1 to sort in descending order
				return arg0.getRight().compareTo(arg1.getRight()) * -1;
			}
		});

		for (Pair<String, Long> tup : temp) {
			retval.add(tup.getLeft());
		}

		return retval;
	}
	
	/*private void checkIndexState(){
		Set<String> openwindows = wdriver.getWindowHandles();
		Set<String> curwindows = new HashSet<String>(openwindows);
		curwindows.removeAll(windowTS.keySet());
		if(curwindows.size() > 0){
			if(log.isErrorEnabled()){
				log.error("Window interaction map does not contain handles to all currently open windows.");
			}
		}
		List<String> deadwindows = new ArrayList<String>(windowTS.keySet());
		deadwindows.removeAll(openwindows);
		if(deadwindows.size() > 0){
			if(log.isErrorEnabled()){
				log.error("Window interaction map contains " + deadwindows.size() + " closed windows. " +
						"Removing these windows handles from map.");
			}
			for(String deadwindow : deadwindows){
				windowOpener.remove(deadwindow);
				windowTS.remove(deadwindow);
			}
		}		
	}*/
	
	/**
	 * <p>checkForDeadWindows.</p>
	 */
	public void checkForDeadWindows(){
		Set<String> openwindows = wdriver.getWindowHandles();
		List<String> deadwindows = new ArrayList<String>(windowTS.keySet());
		deadwindows.removeAll(openwindows);
		if(deadwindows.size() > 0){
			if(log.isErrorEnabled()){
				log.error("Window interaction map contains " + deadwindows.size() + " closed windows. " +
						"Removing these windows handles from map.");
			}
			for(String deadwindow : deadwindows){
				windowOpener.remove(deadwindow);
				windowTS.remove(deadwindow);
			}
		}
	}

	private long getCurrentTimestamp() {
		return new Date().getTime();
	}

}
