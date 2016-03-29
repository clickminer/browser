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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * <p>FlashUtils class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: FlashUtils.java 843 2013-10-03 16:26:11Z cjneasbitt $Id
 */
public class FlashUtils {

	private static final transient Log log = LogFactory.getLog(FlashUtils.class);

	/**
	 * Find elements that could contain an Adobe Flash object from the current
	 * frame and window.
	 *
	 * @param wdriver
	 *            the WebDriver object to use
	 * @return the list of matching objects
	 */
	public static List<WebElement> findFlashElements(WebDriver wdriver) {
		List<WebElement> elems = findFlashEmbedElements(wdriver);
		elems.addAll(findFlashObjectElements(wdriver));
		return elems;
	}

	/**
	 * Finds the Adobe Flash elements in the current frame and window that
	 * contain the search string as any subset of the value of one of its flash
	 * vars. The search string is normalized along with the flash vars using
	 * URLDecoder.decode and the UTF-8 charset before comparison.
	 *
	 * @param wdriver
	 *            the WebDriver object to use
	 * @param searchstr
	 *            the search string
	 * @return the list of matching objects.
	 */
	public static List<WebElement> findFlashElementsFromFlashVars(
			WebDriver wdriver, String searchstr) {
		List<WebElement> retval = new ArrayList<WebElement>();
		try {
			searchstr = URLDecoder.decode(searchstr, "UTF-8");
			List<WebElement> elems = findFlashElements(wdriver);
			for (WebElement elem : elems) {
				Map<String, String> flashVars = parseFlashVars(elem);
				for (String k : flashVars.keySet()) {
					if (flashVars.get(k).contains(searchstr)) {
						retval.add(elem);
						break;
					}
				}
			}
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
		}
		return retval;
	}

	/**
	 * Finds all embed elements that contain an Adobe Flash object from the
	 * current window and frame.
	 * 
	 * @param wdriver
	 *            the WebDriver object to use
	 * @return the list of matching objects
	 */
	private static List<WebElement> findFlashEmbedElements(WebDriver wdriver) {
		// Sorry for the terrible xpath, forced to stick with v1.0
		return wdriver
				.findElements(By
						.xpath("//embed[translate("
								+ "substring(@src, string-length(@src) - 3), "
								+ "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'swf' or "
								+ "@type='application/x-shockwave-flash' or @FlashVars]"));
	}

	/**
	 * Finds all object elements that contain an Adobe Flash object having 
	 * a child 'Flashvars' param from within the current window and frame.
	 * 
	 * @param wdriver
	 *            the WebDriver object to use
	 * @return the list
	 */	
	private static List<WebElement> findFlashObjectElements(WebDriver wdriver) {
		return wdriver
				.findElements(By
						.xpath("//object[@classid='clsid:d27cdb6e-ae6d-11cf-96b8-444553540000' "
								+ "or @type='application/x-shockwave-flash' " +
								"and child::param[@name='FlashVars']]"));
	}

	// returns empty map if tag is not an embed nor an object tag or if
	// no flash vars exist
	/**
	 * Parses the flash vars.
	 * 
	 * @param elem
	 *            the elem
	 * @return the map
	 */
	private static Map<String, String> parseFlashVars(WebElement elem) {
		Map<String, String> retval = new HashMap<String, String>();
		String flashVarStr = null;
		if (elem.getTagName().toLowerCase().equals("embed")) {
			flashVarStr = elem.getAttribute("FlashVars");
		} else if (elem.getTagName().toLowerCase().equals("object")) {
			flashVarStr = elem.getAttribute("flashvars");
			if (flashVarStr != null) {
				try {
					WebElement param = elem.findElement(By
							.xpath("child::param[@name='FlashVars']"));
					flashVarStr = param.getAttribute("value");
				} catch (NoSuchElementException e) {
					flashVarStr = null;
				}
			}
		}

		if (flashVarStr != null) {
			try {
				String[] varparts = flashVarStr.split("&");
				for (String vartup : varparts) {
					String[] tupparts = vartup.split("=");
					String value = "";
					if (tupparts.length > 1) {
						value = URLDecoder.decode(tupparts[1], "UTF-8");
					}
					retval.put(URLDecoder.decode(tupparts[0], "UTF-8"), value);
				}

			} catch (UnsupportedEncodingException e) {
				if (log.isErrorEnabled()) {
					log.error("", e);
				}
				retval.clear();
			}
		}

		return retval;
	}
}
