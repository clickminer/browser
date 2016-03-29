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
package edu.uga.cs.clickminer.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import edu.uga.cs.clickminer.datamodel.FramePath;

/**
 * <p>FrameUtils class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: FrameUtils.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class FrameUtils {

	private static final transient Log log = LogFactory.getLog(FrameUtils.class);

	/**
	 * Traverse frame path.
	 * 
	 * @param framePath
	 *            the frame path
	 */
	private static void traverseFramePath(WebDriver wdriver,
			List<WebElement> framePath) {
		wdriver.switchTo().defaultContent();
		for (WebElement elem : framePath) {
			wdriver.switchTo().frame(elem);
		}
	}
	
	/**
	 * <p>traverseFramePath.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 * @param framePath a {@link edu.uga.cs.clickminer.datamodel.FramePath} object.
	 */
	public static void traverseFramePath(WebDriver wdriver,
			FramePath framePath) {
		traverseFramePath(wdriver, framePath.getPath());
	}
	

	/**
	 * Gets the child frames.
	 *
	 * @return the child frames
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 */
	public static List<WebElement> getChildFrames(WebDriver wdriver) {
		List<WebElement> retval = new ArrayList<WebElement>();
		try {
			retval.addAll(wdriver.findElements(By.tagName("frame")));
		} catch (NoSuchElementException e) {
		}
		try {
			retval.addAll(wdriver.findElements(By.tagName("iframe")));
		} catch (NoSuchElementException e) {
		}
		if(log.isDebugEnabled()){
			log.debug("Located " + retval.size() + " child frames.");
		}
		return retval;
	}
	
	/**
	 * Finds and returns a list of paths in the frame tree to frames in the
	 * current window whose source is equal to absolute url.
	 *
	 * @param wdriver
	 *            the WebDriver object to use
	 * @param srcurl
	 *            the source url, if null matches all paths
	 * @return the paths from the default frame to the matching frame, null if
	 *         the a matching frame is not found, an empty path signifies that
	 *         the default frame is a match
	 */
	public static List<FramePath> findFramePaths(WebDriver wdriver,
			String srcurl) {
		List<FramePath> buf = new ArrayList<FramePath>();
		wdriver.switchTo().defaultContent();
		findFramePaths(wdriver, srcurl, new FramePath(), buf);
		wdriver.switchTo().defaultContent();
		if(buf.size() > 0){
			return buf;
		}
		return null;
	}
	
	/**
	 * Recursive helper method of findFramePaths(String srcurl).
	 * 
	 * @param wdriver
	 *            the WebDriver object to use
	 * @param srcurl
	 *            the source url, if null matches all paths
	 * @param curpath
	 *            the currently explored path
	 * @param buf
	 *            the list of paths to populate
	 */
	private static void findFramePaths(WebDriver wdriver, String srcurl,
			FramePath curpath, List<FramePath> buf) {
		if (log.isDebugEnabled()) {
			log.debug("Checking frame url: " + wdriver.getCurrentUrl()
					+ " against " + srcurl);
		}
		String cururl = wdriver.getCurrentUrl();
		if (srcurl == null || cururl.equals(srcurl)) {
			curpath.appendUrl(cururl);
			buf.add(curpath);
		}
		List<WebElement> frames = FrameUtils.getChildFrames(wdriver);
		for (WebElement frame : frames) {
			try{
				FramePath nFramePath = new FramePath(curpath);
				nFramePath.appendFrame(frame);
				wdriver.switchTo().frame(frame);
				findFramePaths(wdriver, srcurl, nFramePath, buf);
				FrameUtils.traverseFramePath(wdriver, curpath);
			} catch(StaleElementReferenceException e){
				if(log.isErrorEnabled()){
					log.error("A frame could not be switched to, skipping.", e);
				}
			} catch (NoSuchFrameException e) {
				if(log.isErrorEnabled()){
					log.error("A frame could not be switched to, skipping.", e);
				}
			}
		}
	}


	/**
	 * <p>getAllFrameNames.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 */
	public static List<String> getAllFrameNames(WebDriver wdriver) {
		ArrayList<String> fnames = new ArrayList<String>();
		getAllFrameNames(wdriver, new ArrayList<WebElement>(), fnames);

		if (log.isDebugEnabled()) {
			if (fnames.size() > 0) {
				String framenames = "";
				for (String fname : fnames) {
					framenames += fname + " ";
				}
				log.debug("Found the frames: " + framenames);

			} else {
				log.debug("Retrieved no frame names.");
			} 
		}

		return fnames;
	}

	private static void getAllFrameNames(WebDriver wdriver,
			List<WebElement> curpath, List<String> buf) {
		List<WebElement> frames = FrameUtils.getChildFrames(wdriver);
		for (WebElement frame : frames) {
			try{
				String fname = frame.getAttribute("name");
				if (fname != null && !buf.contains(fname)) {
					buf.add(fname);
				}
				List<WebElement> nFramePath = new ArrayList<WebElement>();
				for (WebElement elem : curpath) {
					nFramePath.add(elem);
				}
				nFramePath.add(frame);
				wdriver.switchTo().frame(frame);
				getAllFrameNames(wdriver, nFramePath, buf);
				FrameUtils.traverseFramePath(wdriver, curpath);
			} catch(StaleElementReferenceException e){
				if(log.isErrorEnabled()){
					log.error("A frame could not be switched to, skipping.", e);
				}
			} catch (NoSuchFrameException e) {
				if(log.isErrorEnabled()){
					log.error("A frame could not be switched to, skipping.", e);
				}
			}
		}
	}
}
