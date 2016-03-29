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

import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import edu.uga.cs.clickminer.datamodel.ElementLocator;

/**
 * <p>JSUtils class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: JSUtils.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class JSUtils {

	private static final String JSCODE_GETATTRS = "{var elem = arguments[0]; "
			+ "var retval = new Array(); "
			+ "for(var j = 0; j < elem.attributes.length; j++){ "
			+ "retval.push(new Array(elem.attributes[j].name, elem.attributes[j].value)); } "
			+ "return retval;} ";
	
	private static final String JSCODE_GETLOCATOR = "{var et = arguments[0]; "
			+ "var locatorPath = new Array(); "
			+ "lb = new LocatorBuilders(window); "
			+ "locatorPath[0] = lb.build(et); "
			+ "var i = 1; "
 			+ "while (et.parentNode.parentNode != null) {  "
			+ "locatorPath[i++] = lb.build(et.parentNode);  "
			+ "et = et.parentNode; } " 
			+ "return locatorPath;}";
	
	private static final String JSCODE_SETATTR = "{ arguments[0].%s = \"%s\"; return null; }";
	
	private static final transient Log log = LogFactory.getLog(JSUtils.class);

	/**
	 * <p>getElementAttributes.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 * @param elem a {@link org.openqa.selenium.WebElement} object.
	 */
	public static Map<String, String> getElementAttributes(WebDriver wdriver, 
			WebElement elem){
		Map<String, String> retval = new HashMap<String, String>();
		@SuppressWarnings("unchecked")
		List<Object> result = (List<Object>) ((JavascriptExecutor) wdriver).executeScript(
				JSCODE_GETATTRS, elem);
		for(Object obj : result){
			@SuppressWarnings("unchecked")
			List<String> temp = (List<String>)obj;
			retval.put(temp.get(0), temp.get(1));
		}
		return retval;
	}
	
	/**
	 * <p>setElementAttribute.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 * @param elem a {@link org.openqa.selenium.WebElement} object.
	 * @param attr a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 */
	public static void setElementAttribute(WebDriver wdriver, WebElement elem, 
			String attr, String value){
		StringBuffer buf = new StringBuffer();
		Formatter formatter = new Formatter(buf);
		formatter.format(JSCODE_SETATTR, attr, value);
		formatter.close();
		if(log.isDebugEnabled()){
			log.debug("Executing javascript " + buf);
		}
		((JavascriptExecutor) wdriver).executeScript(buf.toString(), elem);
	}
	
	/**
	 * <p>getElementLocator.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 * @param elem a {@link org.openqa.selenium.WebElement} object.
	 */
	public static ElementLocator getElementLocator(WebDriver wdriver, WebElement elem) {
		@SuppressWarnings("unchecked")
		List<Object> result = (List<Object>) ((JavascriptExecutor) wdriver).executeScript(
				JSCODE_GETLOCATOR, elem);
		Collections.reverse(result);
		ElementLocator retval = new ElementLocator();
		for(Object obj : result){
			retval.add((String) obj);
		}
		if(log.isDebugEnabled()){
			log.debug("Retrieve locator " + retval);
		}
		return retval;
	}
	
	//http://stackoverflow.com/questions/3613584/webdriver-dismiss-a-alert-box
	/**
	 * <p>disableAlerts.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 */
	public static void disableAlerts(WebDriver wdriver){
		((JavascriptExecutor)wdriver).executeScript("window.alert = function(msg){};");
	}
	
	/**
	 * <p>autoAcceptConfirm.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 */
	public static void autoAcceptConfirm(WebDriver wdriver){
		((JavascriptExecutor)wdriver).executeScript("window.confirm = function(msg){return true;};");
	}
	
	/**
	 * <p>autoReturnPrompt.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 */
	public static void autoReturnPrompt(WebDriver wdriver){
		((JavascriptExecutor)wdriver).executeScript("window.prompt = function(msg){return null;};");
	}
}
