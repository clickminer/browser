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
package edu.uga.cs.clickminer.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;

import edu.uga.cs.clickminer.ProxyClient;
import edu.uga.cs.clickminer.exception.ProxyErrorException;
import edu.uga.cs.clickminer.index.FramePathIndex;
import edu.uga.cs.clickminer.index.WindowInteractionIndex;

/**
 * <p>BrowserState class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: BrowserState.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class BrowserState {

	private final List<WindowState> active;
	private final List<WindowState> resting;
	private final ProxyClient pclient;
	private final WebDriver wdriver;
	private boolean reinit = true;
	private final double restingRatio;
	private final WindowInteractionIndex windowIMap;
	private final FramePathIndex framePathIndex;

	private static Log log = LogFactory.getLog(BrowserState.class);

	/**
	 * <p>Constructor for BrowserState.</p>
	 *
	 * @param pclient a {@link edu.uga.cs.clickminer.ProxyClient} object.
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 * @param windowIMap a {@link edu.uga.cs.clickminer.index.WindowInteractionIndex} object.
	 * @param framePathIndex a {@link edu.uga.cs.clickminer.index.FramePathIndex} object.
	 */
	public BrowserState(ProxyClient pclient, WebDriver wdriver, 
			WindowInteractionIndex windowIMap, FramePathIndex framePathIndex) {
		this(pclient, wdriver, windowIMap, framePathIndex, 0.95);
	}

	/**
	 * <p>Constructor for BrowserState.</p>
	 *
	 * @param pclient a {@link edu.uga.cs.clickminer.ProxyClient} object.
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 * @param windowIMap a {@link edu.uga.cs.clickminer.index.WindowInteractionIndex} object.
	 * @param framePathIndex a {@link edu.uga.cs.clickminer.index.FramePathIndex} object.
	 * @param restingRatio a double.
	 */
	public BrowserState(ProxyClient pclient, WebDriver wdriver, 
			WindowInteractionIndex windowIMap, FramePathIndex framePathIndex,
			double restingRatio) {
		this.pclient = pclient;
		this.wdriver = wdriver;
		this.restingRatio = restingRatio;
		this.active = new ArrayList<WindowState>();
		this.resting = new ArrayList<WindowState>();
		this.windowIMap = windowIMap;
		this.framePathIndex = framePathIndex;
	}

	private void init() {
		active.clear();
		resting.clear();
		Set<String> windowHandles = wdriver.getWindowHandles();
		for (String handle : windowHandles) {
			try{
				active.add(new WindowState(pclient, wdriver, handle));
			} catch (Exception e) {
				if(log.isErrorEnabled()){
					log.error("Unable to create window state.", e);
				}
				windowIMap.removeWindow(handle);
				framePathIndex.removeWindow(handle);
				if(windowHandles.size() > 1){
					wdriver.switchTo().window(handle);
					wdriver.close();
				}
			}
		}
	}

	/**
	 * <p>isResting.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public boolean isResting() throws ProxyErrorException {
		if (reinit) {
			init();
			reinit = false;
		}

		List<WindowState> restingwin = new ArrayList<WindowState>();
		List<WindowState> errorwin = new ArrayList<WindowState>();
		for (WindowState window : active) {
			try{
				if (window.isResting()) {
					restingwin.add(window);
				}
			} catch (Exception e){
				if(log.isErrorEnabled()){
					log.error("Error testing window state. Skipping window.", e);
				}
				errorwin.add(window);
			}
		}
		active.removeAll(errorwin);
		active.removeAll(restingwin);
		resting.addAll(restingwin);

		double ratio = resting.size()
				/ (double) (active.size() + resting.size());

		if (log.isInfoEnabled()) {
			log.info("Calculated resting ratio: " + ratio);
		}

		return ratio >= this.restingRatio;
	}

	/**
	 * <p>reset.</p>
	 */
	public void reset() {
		this.reinit = true;
	}

	/**
	 * <p>toString.</p>
	 */
	public String toString() {
		String retval = "Active Windows:\n";
		for (WindowState window : active) {
			retval += window + "\n";
		}
		retval += "\nResting Windows:\n";
		for (WindowState window : resting) {
			retval += window + "\n";
		}
		return retval;
	}
}
