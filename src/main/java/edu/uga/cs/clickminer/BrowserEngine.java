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
package edu.uga.cs.clickminer;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import edu.uga.cs.clickminer.ProxyClient.ProxyMode;
import edu.uga.cs.clickminer.datamodel.BrowserState;
import edu.uga.cs.clickminer.datamodel.ElementSearchResult;
import edu.uga.cs.clickminer.datamodel.FramePath;
import edu.uga.cs.clickminer.datamodel.MitmHttpRequest;
import edu.uga.cs.clickminer.datamodel.log.ElementEntry;
import edu.uga.cs.clickminer.datamodel.log.FrameEntry;
import edu.uga.cs.clickminer.datamodel.log.InteractionRecord;
import edu.uga.cs.clickminer.datamodel.log.PageEntry;
import edu.uga.cs.clickminer.datamodel.log.InteractionRecord.ResultLocation;
import edu.uga.cs.clickminer.exception.ProxyErrorException;
import edu.uga.cs.clickminer.index.FramePathIndex;
import edu.uga.cs.clickminer.index.FramePathIndexEntry;
import edu.uga.cs.clickminer.index.JavascriptClickIndex;
import edu.uga.cs.clickminer.index.RequestSearchIndex;
import edu.uga.cs.clickminer.index.WindowInteractionIndex;
import edu.uga.cs.clickminer.util.FlashUtils;
import edu.uga.cs.clickminer.util.FrameUtils;
import edu.uga.cs.clickminer.util.JSUtils;

/**
 * The Class BrowserEngine.
 *
 * @author Chris Neasbitt
 * @version $Id: BrowserEngine.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class BrowserEngine implements Runnable {

	// http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Longest_common_substring#Java
	/**
	 * Finds the longest common prefix of the two parameter strings. See <a
	 * href=
	 * "http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Longest_common_substring#Java"
	 * >LCS impl.</a>
	 * 
	 * @param a
	 * @param b
	 * @return the lcs of the two strings
	 */
	private static String longestCommonPrefix(String a, String b) {
		int minLength = Math.min(a.length(), b.length());
		for (int i = 0; i < minLength; i++) {
			if (a.charAt(i) != b.charAt(i)) {
				return a.substring(0, i);
			}
		}
		String commonprefix = a.substring(0, minLength);
		
		//TODO should alter this to make sure the common prefix ends with a / or contains the 
		//entire url value minus parameters for the window or base url
		return commonprefix;
	}

	/** The pclient. */
	private final ProxyClient pclient;

	/** The wdriver. */
	private final WebDriver wdriver;

	/** The bstate. */
	private final BrowserState bstate;

	/** The state poll interval. */
	//measured in milliseconds
	private final int statePollInterval;
	
	//measured in seconds
	private final double delayFromRefererThreshold;
	
	private final int numRequestsReferedThreshold;

	/** The log. */
	private static transient final Log log = LogFactory.getLog(BrowserEngine.class);

	/** The interaction log. */
	private final List<InteractionRecord> interactionLog;

	private final WindowInteractionIndex windowIMap;
	
	private final RequestSearchIndex requestSI;
	
	private final FramePathIndex framePathIndex;
	
	private final boolean javascriptSearch;
	
	private final boolean parseFlashVars;
	
	private final JavascriptClickIndex jsClickIndex;
	
	
	private static final List<String> formEventAttrs;
	static {
		formEventAttrs = new ArrayList<String>();
		formEventAttrs.add("onblur");
		formEventAttrs.add("onchange");
		formEventAttrs.add("oncontextmenu");
		formEventAttrs.add("onfocus");
		formEventAttrs.add("onformchange");
		formEventAttrs.add("onforminput");
		formEventAttrs.add("oninput");
		formEventAttrs.add("oninvalid");
		formEventAttrs.add("onreset");
		formEventAttrs.add("onselect");
		formEventAttrs.add("onsubmit");

	}
	
	private static final List<String> mouseKBEventAttrs;
	static {
		mouseKBEventAttrs = new ArrayList<String>();
		mouseKBEventAttrs.add("onkeydown");
		mouseKBEventAttrs.add("onkeypress");
		mouseKBEventAttrs.add("onkeyup");
		mouseKBEventAttrs.add("onclick");
		mouseKBEventAttrs.add("ondblclick");
		mouseKBEventAttrs.add("ondrag");
		mouseKBEventAttrs.add("ondragend");
		mouseKBEventAttrs.add("ondragenter");
		mouseKBEventAttrs.add("ondragleave");
		mouseKBEventAttrs.add("ondragover");
		mouseKBEventAttrs.add("ondragstart");
		mouseKBEventAttrs.add("ondrop");
		mouseKBEventAttrs.add("onmousedown");
		mouseKBEventAttrs.add("onmousemove");
		mouseKBEventAttrs.add("onmouseout");
		mouseKBEventAttrs.add("onmouseover");
		mouseKBEventAttrs.add("onmouseup");
		mouseKBEventAttrs.add("onmousewheel");
		mouseKBEventAttrs.add("onscroll");	
	}
	
	private static final List<String> htmlMimeTypes;
	static {
		htmlMimeTypes = new ArrayList<String>();
		htmlMimeTypes.add("text/html");
		htmlMimeTypes.add("application/xhtml+xml");
	}
	
	private static final List<String> eventClickableOnlyHTMLElements;
	static {
		eventClickableOnlyHTMLElements = new ArrayList<String>();
		eventClickableOnlyHTMLElements.add("body");
		eventClickableOnlyHTMLElements.add("pre");
		eventClickableOnlyHTMLElements.add("img");  //TODO check the case of image-map
		eventClickableOnlyHTMLElements.add("span");
		eventClickableOnlyHTMLElements.add("div");
		eventClickableOnlyHTMLElements.add("abbr");
		eventClickableOnlyHTMLElements.add("acronym");
		eventClickableOnlyHTMLElements.add("address");
		eventClickableOnlyHTMLElements.add("article");
		eventClickableOnlyHTMLElements.add("aside");
		eventClickableOnlyHTMLElements.add("b");
		eventClickableOnlyHTMLElements.add("bdi");
		eventClickableOnlyHTMLElements.add("bdo");
		eventClickableOnlyHTMLElements.add("big");
		eventClickableOnlyHTMLElements.add("blockquote");
		eventClickableOnlyHTMLElements.add("br");
		eventClickableOnlyHTMLElements.add("caption");
		eventClickableOnlyHTMLElements.add("center");
		eventClickableOnlyHTMLElements.add("cite");
		eventClickableOnlyHTMLElements.add("em");
		eventClickableOnlyHTMLElements.add("strong");
		eventClickableOnlyHTMLElements.add("dfn");
		eventClickableOnlyHTMLElements.add("code");
		eventClickableOnlyHTMLElements.add("samp");
		eventClickableOnlyHTMLElements.add("kbd");
		eventClickableOnlyHTMLElements.add("var");
		eventClickableOnlyHTMLElements.add("col");
		eventClickableOnlyHTMLElements.add("colgroup");
		eventClickableOnlyHTMLElements.add("command");
		eventClickableOnlyHTMLElements.add("datalist");
		eventClickableOnlyHTMLElements.add("dd");
		eventClickableOnlyHTMLElements.add("dl");
		eventClickableOnlyHTMLElements.add("dt");
		eventClickableOnlyHTMLElements.add("del");
		eventClickableOnlyHTMLElements.add("ins");
		eventClickableOnlyHTMLElements.add("details");
		eventClickableOnlyHTMLElements.add("summary");
		eventClickableOnlyHTMLElements.add("dir");
		eventClickableOnlyHTMLElements.add("fieldset");
		eventClickableOnlyHTMLElements.add("legend");
		eventClickableOnlyHTMLElements.add("figcaption");
		eventClickableOnlyHTMLElements.add("figure");
		eventClickableOnlyHTMLElements.add("footer");
		eventClickableOnlyHTMLElements.add("header");
		eventClickableOnlyHTMLElements.add("hgroup");
		eventClickableOnlyHTMLElements.add("h1");
		eventClickableOnlyHTMLElements.add("h2");
		eventClickableOnlyHTMLElements.add("h3");
		eventClickableOnlyHTMLElements.add("h4");
		eventClickableOnlyHTMLElements.add("h5");
		eventClickableOnlyHTMLElements.add("h6");
		eventClickableOnlyHTMLElements.add("hr");
		eventClickableOnlyHTMLElements.add("i");
		eventClickableOnlyHTMLElements.add("keygen");
		eventClickableOnlyHTMLElements.add("label");
		eventClickableOnlyHTMLElements.add("li");
		eventClickableOnlyHTMLElements.add("ol");
		eventClickableOnlyHTMLElements.add("ul");
		eventClickableOnlyHTMLElements.add("menu");
		eventClickableOnlyHTMLElements.add("link");
		eventClickableOnlyHTMLElements.add("mark");
		eventClickableOnlyHTMLElements.add("meter");
		eventClickableOnlyHTMLElements.add("progress");
		eventClickableOnlyHTMLElements.add("nav");
		eventClickableOnlyHTMLElements.add("noframes");
		eventClickableOnlyHTMLElements.add("optgroup");
		eventClickableOnlyHTMLElements.add("option");
		eventClickableOnlyHTMLElements.add("output");
		eventClickableOnlyHTMLElements.add("p");
		eventClickableOnlyHTMLElements.add("param");
		eventClickableOnlyHTMLElements.add("q");
		eventClickableOnlyHTMLElements.add("rp");
		eventClickableOnlyHTMLElements.add("rt");
		eventClickableOnlyHTMLElements.add("ruby");
		eventClickableOnlyHTMLElements.add("s");
		eventClickableOnlyHTMLElements.add("section");
		eventClickableOnlyHTMLElements.add("select");
		eventClickableOnlyHTMLElements.add("small");
		eventClickableOnlyHTMLElements.add("source");
		eventClickableOnlyHTMLElements.add("strike");
		eventClickableOnlyHTMLElements.add("style");
		eventClickableOnlyHTMLElements.add("sub");
		eventClickableOnlyHTMLElements.add("sup");
		eventClickableOnlyHTMLElements.add("table");
		eventClickableOnlyHTMLElements.add("tr");
		eventClickableOnlyHTMLElements.add("th");
		eventClickableOnlyHTMLElements.add("td");
		eventClickableOnlyHTMLElements.add("textarea");
		eventClickableOnlyHTMLElements.add("tfoot");
		eventClickableOnlyHTMLElements.add("thead");
		eventClickableOnlyHTMLElements.add("tbody");
		eventClickableOnlyHTMLElements.add("time");
		eventClickableOnlyHTMLElements.add("track");
		eventClickableOnlyHTMLElements.add("tt");
		eventClickableOnlyHTMLElements.add("u");
		eventClickableOnlyHTMLElements.add("video");
		eventClickableOnlyHTMLElements.add("audio");
		eventClickableOnlyHTMLElements.add("wbr");
	}
	
	private static final List<String> nonClickableHTMLElements;
	static {
		nonClickableHTMLElements = new ArrayList<String>();
		nonClickableHTMLElements.add("base");
		nonClickableHTMLElements.add("bdo");
		nonClickableHTMLElements.add("br");
		nonClickableHTMLElements.add("frame");
		nonClickableHTMLElements.add("frameset");
		nonClickableHTMLElements.add("iframe");
		nonClickableHTMLElements.add("param");
		nonClickableHTMLElements.add("script");
		nonClickableHTMLElements.add("title");
		nonClickableHTMLElements.add("html");
		nonClickableHTMLElements.add("head");
		// since we don't handle java applets and the tag is deprecated
		// we have added applet to this list
		nonClickableHTMLElements.add("applet");
		nonClickableHTMLElements.add("basefont");
		nonClickableHTMLElements.add("font");
		nonClickableHTMLElements.add("meta");
		nonClickableHTMLElements.add("noscript");
		
		//the following are elements in http://www.adobe.com/xml/dtds/cross-domain-policy.dtd
		//since these can be placed in a response with an html mime type
		//if the proper doctype is specified we include them here.
		nonClickableHTMLElements.add("cross-domain-policy");
		nonClickableHTMLElements.add("site-control");
		nonClickableHTMLElements.add("allow-access-from");
		nonClickableHTMLElements.add("allow-http-request-headers-from");
		nonClickableHTMLElements.add("allow-access-from-identity");
		nonClickableHTMLElements.add("signatory");
		nonClickableHTMLElements.add("certificate");
	}
	
	private static final List<String> objectHTMLElements;
	static {
		objectHTMLElements = new ArrayList<String>();
		objectHTMLElements.add("object");
		objectHTMLElements.add("embed");
	}
	

	/**
	 * Instantiates a new browser engine.
	 *
	 * @param pclient
	 *            the pclient
	 * @param wdriver
	 *            the wdriver
	 */
	public BrowserEngine(ProxyClient pclient, WebDriver wdriver) {
		this(pclient, wdriver, 10000, 0.0, 0, false, false);
	}
	
	/**
	 * Instantiates a new browser engine.
	 *
	 * @param pclient
	 *            the pclient
	 * @param wdriver
	 *            the wdriver
	 * @param javascriptSearch a boolean.
	 * @param parseFlashVars a boolean.
	 */
	public BrowserEngine(ProxyClient pclient, WebDriver wdriver, 
			boolean javascriptSearch, boolean parseFlashVars) {
		this(pclient, wdriver, 10000, 0.0, 0, javascriptSearch, parseFlashVars);
	}

	/**
	 * Instantiates a new browser engine.
	 *
	 * @param pclient
	 *            the pclient
	 * @param wdriver
	 *            the wdriver
	 * @param statePollInterval
	 *            the state poll interval in milliseconds
	 * @param delayFromRefererThreshold a double.
	 * @param javascriptSearch a boolean.
	 * @param numRequestsReferedThreshold a int.
	 * @param parseFlashVars a boolean.
	 */
	public BrowserEngine(ProxyClient pclient, WebDriver wdriver,
			int statePollInterval, double delayFromRefererThreshold,
			int numRequestsReferedThreshold, boolean javascriptSearch,
			boolean parseFlashVars) {
		this.pclient = pclient;
		this.wdriver = wdriver;
		this.statePollInterval = statePollInterval;
		this.delayFromRefererThreshold = delayFromRefererThreshold;
		this.numRequestsReferedThreshold = numRequestsReferedThreshold;
		this.interactionLog = new ArrayList<InteractionRecord>();
		this.windowIMap = new WindowInteractionIndex(wdriver);
		this.requestSI = new RequestSearchIndex();
		this.framePathIndex = new FramePathIndex(wdriver);
		this.javascriptSearch = javascriptSearch;
		this.parseFlashVars = parseFlashVars;
		this.jsClickIndex = new JavascriptClickIndex();
		this.bstate = new BrowserState(pclient, wdriver, windowIMap, framePathIndex);
	}

	private ElementActivationResult activateTargetElement(MitmHttpRequest req){
		boolean elementActivated = false;
		boolean framePathFound = false;
		List<String> windowhandles = windowIMap.getWindowsMRUOrder();
		List<PageMatchPair> matches = new ArrayList<PageMatchPair>();
		InteractionRecord irecord = null;

		// checks the windows first that contain a frame that is equal to the
		// referer field from the request
		for (String windowhandle : windowhandles) {
			
			if(!searchForRequest(req, windowhandle)){
				if(log.isInfoEnabled()){
					log.info("Skipping request based on search index: " + req.getUrl());
				}
				if(!framePathFound){
					framePathFound = this.requestSI.foundFramePaths(req, windowhandle);
				}
				continue;
			}
		
			List<FramePathIndexEntry> framepaths = 
					framePathIndex.getMatchingFramePathsFromReferer(windowhandle, req);
			
			if (framepaths != null) {
				
				//make sure we can still access the window
				wdriver.switchTo().window(windowhandle);
				try{
					wdriver.switchTo().defaultContent();
				} catch (Exception e){
					if(log.isErrorEnabled()){
						log.error("Error switching to window.  Skipping window.", e);
					}
					windowIMap.removeWindow(windowhandle);
					framePathIndex.removeWindow(windowhandle);
					try{
						if(windowIMap.getWindowsMRUOrder().size() > 1){
							wdriver.close();
						}
					} catch (Exception e1){
						if(log.isErrorEnabled()){
							log.error("Error closing window.", e1);
						}
					}
					continue;
				}
				
				if(log.isInfoEnabled()){
					log.info("Found " + framepaths.size() + " frame path(s) in window " 
							+ wdriver.getCurrentUrl()  + " for request " + req.getUrl());
				}
				framePathFound = true;
				List<ElementSearchResult> searchResults = this
						.generateElementSearchResults(req, framepaths, false);
				if (searchResults.size() > 0) {
					wdriver.switchTo().defaultContent();
					matches.add(new PageMatchPair(
							PageEntry.getPageEntry(wdriver, windowIMap,
									windowhandle, searchResults), searchResults));
				}	
				this.requestSI.setEntry(req, windowhandle, 
						windowIMap.getWindowTimestamp(windowhandle), true);
			}
		}
		
		if (matches.size() > 0) {
			PageMatchPair selmatch = matches
					.get(0);
			wdriver.switchTo().window(selmatch.getPageEntry().getWindow().getWindowID());
			wdriver.switchTo().defaultContent();
			irecord = this.activateTargetElementFromResults(selmatch.getElementSearchResults(),
					selmatch.getPageEntry(), req);
			for (int i = 1; i < matches.size(); i++) {
				irecord.addPageEntry(matches.get(i).getPageEntry());
			}
			elementActivated = true;
		} else {
			if (this.javascriptSearch && responseIsHTML(req)){ //dynamically execute javascript elements
				if(log.isInfoEnabled()){
					log.info("Attempting to dynamically execute javascript elements.");
				}
				
				try{
					pclient.setModeNoContent(req.getUrl());
					windowhandles = windowIMap.getWindowsMRUOrder();
					for (String windowhandle : windowhandles) {
						
						if(!this.searchForRequest(req, windowhandle)){
							if(!framePathFound){
								framePathFound = this.requestSI.foundFramePaths(req, windowhandle);
							}
						}
						
						List<FramePathIndexEntry> framepaths = 
								framePathIndex.getMatchingFramePathsFromReferer(windowhandle, req);
						if(framepaths != null){
							
							//make sure we can still access the window
							wdriver.switchTo().window(windowhandle);
							try{
								wdriver.switchTo().defaultContent();
							} catch (Exception e){
								if(log.isErrorEnabled()){
									log.error("Error switching to window.  Skipping window.", e);
								}
								windowIMap.removeWindow(windowhandle);
								framePathIndex.removeWindow(windowhandle);
								try{
									if(windowIMap.getWindowsMRUOrder().size() > 1){
										wdriver.close();
									}
								} catch (Exception e1){
									if(log.isErrorEnabled()){
										log.error("Error closing window.", e1);
									}
								}
								continue;
							}
							
							List<ElementSearchResult> searchResults = generateElementSearchResults(req, framepaths, true);
							if (searchResults.size() > 0) {
								wdriver.switchTo().defaultContent();
								ElementSearchResult clickres = clickJSElementFromSearchResults(windowhandle, searchResults, req.getUrl());
								if(clickres != null){
									searchResults.clear();
									searchResults.add(clickres);
									irecord = this.activateTargetElementFromResults(searchResults,
											PageEntry.getPageEntry(wdriver, windowIMap,
													windowhandle, searchResults), req);
									elementActivated = true;
								}
							}	
						}			
					}		
				} catch(ProxyErrorException e){
					if(log.isErrorEnabled()){
						log.error("Failed to test javascript elements.", e);
					}

				} finally {
					try {
						if(pclient.getMode() == ProxyMode.NOCONTENT){
							pclient.setModeContent();
						}
					} catch (ProxyErrorException e1) {
						if(log.isErrorEnabled()){
							log.error("Unable to set proxy to content mode.", e1);
						}	
					}
				}
			}
		}

		if (irecord != null) {
			interactionLog.add(irecord);
		}

		return new ElementActivationResult(elementActivated, framePathFound);
	}
	
	
	
	//Returns true if we should search for an element matching the request
	//in the current window.
	private boolean searchForRequest(MitmHttpRequest req, String windowHandle){
		long mapts = this.windowIMap.getWindowTimestamp(windowHandle);
		long indexts = this.requestSI.getTimestamp(req, windowHandle);
		return !(mapts > -1 && indexts > -1 && mapts <= indexts);
	}
	
	private ElementSearchResult clickJSElementFromSearchResults(String windowHandle,
			List<ElementSearchResult> searchResults, String requrl) throws ProxyErrorException{
		ElementSearchResult retval = null;
		outer:
		for(ElementSearchResult result : searchResults){
			FramePath fp = result.getFramePath();
			List<WebElement> elements = result.getMatchingElements();
			FrameUtils.traverseFramePath(wdriver, fp);
			String referer = wdriver.getCurrentUrl();
			
			//disable javascript dialog windows
			JSUtils.disableAlerts(wdriver);
			JSUtils.autoAcceptConfirm(wdriver);
			JSUtils.autoReturnPrompt(wdriver);
			
			for(WebElement element : elements){
				try{
					if(element.isDisplayed() && element.isEnabled()){
						//we set the element id if it doesn't exist to make indexing easier
						String elemid = element.getAttribute("id");
						if(elemid == null){
							elemid = "elem" + (System.currentTimeMillis() / 1000L) 
									+ (int)(Math.random() * Integer.MAX_VALUE);
							JSUtils.setElementAttribute(wdriver, element, "id", elemid);
						}
						
						//check the jsClickIndex
						if(jsClickIndex.entryExists(windowHandle, result.getFramePath(), elemid) &&
								jsClickIndex.getEntry(windowHandle, result.getFramePath(), elemid).equals(requrl)){
							if(log.isInfoEnabled()){
								log.info("Found matching javascript element via index.");
							}
							retval = result.copy();
							List<WebElement> elems = new ArrayList<WebElement>();
							elems.add(element);
							retval.setMatchingElements(elems);
							break outer;
						}
						
						Set<String> oldhandles = wdriver.getWindowHandles();
						try{
							element.click();
						} catch (Exception e){
							if(log.isErrorEnabled()){
								log.error("Error clicking on element. Skipping.", e);
							}
							continue;
						}
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							if(log.isWarnEnabled()){
								log.warn(e);
							}
						}
						
						//close any windows the click might have opened
						Set<String> newhandles = wdriver.getWindowHandles();
						newhandles.removeAll(oldhandles);
						if(newhandles.size() > 0){
							for(String handle : newhandles){
								try{
									wdriver.switchTo().window(handle);
									wdriver.close();
								} catch(Exception e) {
									if(log.isWarnEnabled()){
										log.warn(e);
									}
								}
							}
							wdriver.switchTo().window(windowHandle);
						}
						
						//update the jsClickIndex
						String url = pclient.getLastRequestByReferer(referer);
						if(url != null){
							jsClickIndex.setEntry(windowHandle, result.getFramePath(), 
									elemid, url);
						}
							
						ProxyMode pmode = pclient.getMode();
						if(pmode == ProxyMode.CONTENT){
							if(log.isInfoEnabled()){
								log.info("Found matching javascript element by clicking.");
							}
							retval = result.copy();
							List<WebElement> elems = new ArrayList<WebElement>();
							elems.add(element);
							retval.setMatchingElements(elems);
							break outer;
						}
					}
				} catch (Exception e){
					if(log.isErrorEnabled()){
						log.error("Error activating javascript click element. Skipping.", e);
					}
				}
			}
		}
		return retval;
	}

	/**
	 * Activates a clickable a html element from the list of search results and
	 * generates an InteractionRecord object of the interaction. Activating the
	 * element depends on the element's type. Form elements have their fields
	 * filled from the content of the request and submitted. An element besides
	 * an object or an embed whose target attribute is a frame within the
	 * current window has a click event sent to it. In all other cases the url
	 * of the request is submitted to the address bar of a new window.
	 * 
	 * @param req
	 *            the request
	 * @return true, if successful an element was found and activated, false
	 *         otherwise
	 */
	private InteractionRecord activateTargetElementFromResults(
			List<ElementSearchResult> searchResults, PageEntry pageentry,
			MitmHttpRequest req){

		// If the element contains the url in the request then it must be
		// clickable or submittable

		// get the names of the frames in the current frames
		wdriver.switchTo().defaultContent();
		List<String> fnames = FrameUtils.getAllFrameNames(wdriver);
		fnames.add("_self");
		fnames.add("_parent");
		
		ElementSearchResult selectedResult = searchResults.get(0);
		if (!selectedResult.matchesInDefaultFrame()) {
			FrameUtils.traverseFramePath(wdriver, selectedResult.getFramePath());
		}

		WebElement selelem = null;
		for(WebElement elem : selectedResult.getMatchingElements()){
			if(elem.isDisplayed() && elem.isEnabled()){
				selelem = elem;
				break;
			}
		}
		if(selelem == null){
			selelem = selectedResult.getMatchingElements().get(0);
		}
		FrameEntry selectedFrame = pageentry
				.getMatchingFrameEntry(selectedResult.getFramePath().getUrls());
		ElementEntry selectedElement = selectedFrame
				.getMatchingElementEntry(selelem);
		if (log.isInfoEnabled()) {
			log.info("Activating element tag: " + selelem.getTagName() + 
					" locator: " + selectedElement.getLocatorString());
		}

		Set<String> oldWinHandles = wdriver.getWindowHandles();
		String seltagname = selelem.getTagName().toLowerCase();

		// TODO must take the target of the base tag into account.
		String seltagtarget = selelem.getAttribute("target");
		
		String newWinHandle = null;

		if (log.isInfoEnabled()) {
			log.info("Element target: " + seltagtarget);
		}

		if (seltagname.equals("form")) {
			submitFormElement(selelem, req.getContent());
		} else if ((fnames.contains(seltagtarget) || (seltagtarget == null && !selectedResult
				.matchesInDefaultFrame())) && !objectHTMLElements.contains(seltagname)
				&& selelem.isDisplayed() && selelem.isEnabled()) {
			// The frame is supposed to alter the current page in someway
			// which could alter the presence of elements within the DOM
			// and search order of the windows.
			
			try{
				selelem.click();
			} catch (Exception e) {
				if(log.isErrorEnabled()){
					log.error("Error clicking on element.  " +
							"Opening url in new window.", e);
				}
				newWinHandle = this.openUrlInNewWindow(req.getUrl());
			}
		} else {
			// open all non-form elements in a new window
			// we do this to deal with force opening tabs
			// and caching
			newWinHandle = this.openUrlInNewWindow(req.getUrl());
		}

		// TODO might want to change this from a sleep
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
		Set<String> newWinHandles = wdriver.getWindowHandles();

		InteractionRecord irecord = null;

		if (newWinHandles.size() != oldWinHandles.size()) {
			if(newWinHandle == null){
				newWinHandles.removeAll(oldWinHandles);
				newWinHandle = newWinHandles.iterator().next();
			}
			PageEntry destpage = PageEntry.getPageEntry(wdriver, windowIMap,
					newWinHandle);
			irecord = new InteractionRecord(ResultLocation.NEW_WINDOW, req,
					destpage);
		} else {
			PageEntry destpage = PageEntry.getPageEntry(wdriver, windowIMap,
					pageentry.getWindow().getWindowID());
			irecord = new InteractionRecord(ResultLocation.CURRENT_WINDOW, req,
					destpage);
		}

		selectedElement.setSelected(true);
		irecord.addPageEntry(pageentry);

		String windowhandle = wdriver.getWindowHandle();
		
		// update the ts for the window we are interacting with
		windowIMap.updateWindow(windowhandle);
		framePathIndex.updateIndex(windowhandle, 
				windowIMap.getWindowTimestamp(windowhandle));

		// update the ts for any new windows opened
		Set<String> newWindows = windowIMap.updateNewWindows(windowhandle);
		for(String window : newWindows){
			framePathIndex.updateIndex(window, 
					windowIMap.getWindowTimestamp(window));
		}
		
		//Dismiss any alerts.
		try{
			Alert alert = wdriver.switchTo().alert();
			alert.dismiss();
		} catch (NoAlertPresentException e){
			if(log.isDebugEnabled()){
				log.debug("No modal dialogs, skipping");
			}
		} catch(Exception e1){
			if(log.isErrorEnabled()){
				log.error("Could not dismiss dialog", e1);
			}
		}
		// After the click, must always switch back to the default content.
		wdriver.switchTo().window(windowhandle);
		wdriver.switchTo().defaultContent();

		return irecord;
	}

	/**
	 * Closes the WebDriver.
	 */
	public void close() {
		this.wdriver.quit();
	}

	/**
	 * Finds a list of all clickable elements from the current frame in the
	 * current window which contains the search url as at least a part of one of
	 * their attributes. An attempt is made to search for the absolute and
	 * relative versions of the supplied absolute url.
	 *
	 * @param url
	 *            the absolute url to search for within the element attributes.
	 * @return the list of matching elements, empty if none found.
	 */
	public List<WebElement> extractClickableElements(String url) {
		Set<WebElement> retval = new HashSet<WebElement>();

		String relpath = null;
		try {
			
			//We trim off an ending / because a url without the trailing /
			//is functionally equivalent to one containing it and we make
			//make matches based on equality or containment
			if(url.endsWith("/")){
				url = url.substring(0, url.length() - 1);
			}
			
			URL requesturl = new URL(url);
			URL currenturl = null;

			String baseurl = null;
			try {
				// look for base tag
				WebElement baseelem = wdriver.findElement(By.tagName("base"));
				baseurl = baseelem.getAttribute("href");
				if (baseurl != null) {
					currenturl = new URL(baseurl);
				}
			} catch (NoSuchElementException e) {
				if (log.isDebugEnabled()) {
					log.debug("No base tag found. Using current url as base.");
				}
			} catch (MalformedURLException e) {
				if (log.isWarnEnabled()) {
					log.warn("Base tag href url " + baseurl +  " is malformed. Using current url as base.");
				}
			}

			try{
				if (currenturl == null) {
					currenturl = new URL(wdriver.getCurrentUrl());
				}
	
				if (currenturl.getHost().equalsIgnoreCase(requesturl.getHost())) {
					// looking possibly for a relative url
	
					// attempts to find relative urls not relative to the docroot
					String lcp = longestCommonPrefix(requesturl.toString(),
							currenturl.toString());
					if (lcp != null && !lcp.trim().equals("")) {
						relpath = requesturl.toString().replaceFirst(
								"\\Q" + lcp + "\\E", "");
						if(relpath.trim().equals("")){
							relpath = null;
						}
					}
					if(log.isInfoEnabled()){
						log.info("The relative path of " + currenturl.toString() + " and " 
								+ requesturl.toString() + " is " + relpath);
					}
				}
			} catch (MalformedURLException e) {
				if(log.isErrorEnabled()){
					log.error("The current url " + wdriver.getCurrentUrl() + " is malformed.  " +
							"Can not determine a relative url.");
				}
			}

			List<WebElement> contelems = new ArrayList<WebElement>();
			String escapedrelpath = null;
			String escapedurl = null;
			if(relpath != null && !relpath.equals(StringEscapeUtils.escapeHtml4(relpath))){
				escapedrelpath = StringEscapeUtils.escapeHtml4(relpath);
			}
			if(!url.equals(StringEscapeUtils.escapeHtml4(url))){
				escapedurl = StringEscapeUtils.escapeHtml4(url);
			}
			
			// Begin relative path element search
			if (relpath != null) {
				contelems = this.findClickableElements(relpath);
				if (contelems.size() > 0) {
					retval.addAll(contelems);
					if (log.isInfoEnabled()) {
						log.info("Found " + contelems.size()
								+ " matching elements using relative value "
								+ relpath);
					}
				} else {
					if (log.isInfoEnabled()) {
						log.info("Found no matching elements using relative value "
								+ relpath);
					}
				}
			}
			// End relative path element search
			
			
			//Begin HTML escaped relative path element search
			if (escapedrelpath != null) {
				contelems = this.findClickableElements(escapedrelpath);
				if (contelems.size() > 0) {
					retval.addAll(contelems);
					if (log.isInfoEnabled()) {
						log.info("Found " + contelems.size()
								+ " matching elements using HTML escaped relative value "
								+ escapedrelpath);
					}
				} else {
					if (log.isInfoEnabled()) {
						log.info("Found no matching elements using HTML escaped relative value "
								+ escapedrelpath);
					}
				}
			}
			//End HTML escaped relative path element search
			
			

			// Begin absolute path element search
			contelems = this.findClickableElements(url);
			if (contelems.size() > 0) {
				retval.addAll(contelems);
				if (log.isInfoEnabled()) {
					log.info("Found " + contelems.size()
							+ " matching elements using absolute value " + url);
				}
			} else {
				if (log.isInfoEnabled()) {
					log.info("Found no matching elements using absolute value "
							+ url);
				}
			}
			// End absolute path element search
			
			
			// Begin HTML escaped absolute path element search
			if(escapedurl != null){
				contelems = this.findClickableElements(escapedurl);
				if (contelems.size() > 0) {
					retval.addAll(contelems);
					if (log.isInfoEnabled()) {
						log.info("Found " + contelems.size()
								+ " matching elements using HTML escaped absolute value " + escapedurl);
					}
				} else {
					if (log.isInfoEnabled()) {
						log.info("Found no matching elements using HTML escaped absolute value "
								+ escapedurl);
					}
				}
			}
			// End HTML escaped absolute path element search
			
			if(parseFlashVars){
				// Begin relative path flash element search
				if (relpath != null) {
					contelems = FlashUtils.findFlashElementsFromFlashVars(wdriver,
							relpath);
	
					if (contelems.size() > 0) {
						retval.addAll(contelems);
						if (log.isInfoEnabled()) {
							log.info("Found " + contelems.size()
									+ " matching elements flash elements "
									+ "using relative path " + relpath
									+ " in FlashVars.");
						}
					} else {
						if (log.isInfoEnabled()) {
							log.info("Found no matching elements flash elements "
									+ "using relative path " + relpath
									+ " in FlashVars.");
						}
					}
				}
				// End relative path flash element search
				
				
				// Begin HTML escaped relative path flash element search
				if (escapedrelpath != null) {
					contelems = FlashUtils.findFlashElementsFromFlashVars(wdriver,
							escapedrelpath);
	
					if (contelems.size() > 0) {
						retval.addAll(contelems);
						if (log.isInfoEnabled()) {
							log.info("Found " + contelems.size()
									+ " matching elements flash elements "
									+ "using HTML escaped relative path " + escapedrelpath
									+ " in FlashVars.");
						}
					} else {
						if (log.isInfoEnabled()) {
							log.info("Found no matching elements flash elements "
									+ "using HTML escaped relative path " + escapedrelpath
									+ " in FlashVars.");
						}
					}
				}
				// End HTML escaped relative path flash element search
	
				// Begin absolute path flash element search
				contelems = FlashUtils.findFlashElementsFromFlashVars(wdriver, url);
				if (contelems.size() > 0) {
					retval.addAll(contelems);
					if (log.isInfoEnabled()) {
						log.info("Found " + contelems.size()
								+ " matching elements flash elements "
								+ "using absolute path " + url + " in FlashVars.");
					}
				} else {
					if (log.isInfoEnabled()) {
						log.info("Found no matching elements flash elements "
								+ "using absolute path " + url + " in FlashVars.");
					}
				}
				// End absolute path flash element search
				
				// Begin HTML escaped absolute path flash element search
				if(escapedurl != null){
					contelems = FlashUtils.findFlashElementsFromFlashVars(wdriver, escapedurl);
					if (contelems.size() > 0) {
						retval.addAll(contelems);
						if (log.isInfoEnabled()) {
							log.info("Found " + contelems.size()
									+ " matching elements flash elements "
									+ "using HTML escaped absolute path " + escapedurl + " in FlashVars.");
						}
					} else {
						if (log.isInfoEnabled()) {
							log.info("Found no matching elements flash elements "
									+ "using HTML escaped absolute path " + escapedurl + " in FlashVars.");
						}
					}
				}
				// End HTML escaped absolute path flash element search
			}

		} catch (MalformedURLException e) {
			if (log.isErrorEnabled()) {
				log.error("Error processing request url.", e);
			}
		}
		return new ArrayList<WebElement>(retval);
	}
	
	
	//returns true if the element has any attributes that occur in the supplied list
	private boolean containsEventAttribute(WebDriver wdriver, WebElement elem, 
			List<String> attrList){
		Map<String, String> attrs = JSUtils.getElementAttributes(wdriver, elem);
		Set<String> attrNames = attrs.keySet();
		attrNames.retainAll(attrList);
		return attrNames.size() > 0;
	}
	
	
	private boolean containsMouseKBEventAttribute(WebDriver wdriver, WebElement elem){
		return this.containsEventAttribute(wdriver, elem, mouseKBEventAttrs);
	}
	
	private boolean matchEventAttribute(WebElement elem, String matchval, 
			List<String> attrlist){
		Map<String, String> attrs = JSUtils.getElementAttributes(wdriver, elem);
		Set<String> attrNames = attrs.keySet();
		attrNames.retainAll(attrlist);
		
		for(String attr : attrNames){
			if(attrs.get(attr).contains(matchval)){
				return true;
			}
		}
		return false;		
	}
	
	private boolean matchMouseKBEventAttribute(WebElement elem, String matchval){
		return this.matchEventAttribute(elem, matchval, mouseKBEventAttrs);
	}
	
	private boolean matchFormEventAttribute(WebElement elem, String matchval){
		return this.matchEventAttribute(elem, matchval, formEventAttrs);
	}

	/**
	 * Returns a new list containing only the clickable elements of the source
	 * list.  Param elements are substituted with their parent object elements.
	 * Embed elements are filtered unless the match is made on the flashvars
	 * attribute.  Form elements are filtered unless the match is made on the
	 * target attribute or any of the possible form event attributes.  Base, bdo, br,
	 * frame, frameset, head, html, iframe, meta, param, script, style and title
	 * are filtered out as not clickable. A elements are filtered unless the match
	 * is match on the href attribute or any of the possible keyboard and mouse event
	 * attributes.  All other elements are filtered unless the match is made on a
	 * keyboard and mouse event attribute.
	 *
	 * See <a
	 * href="http://www.w3schools.com/TAgs/ref_eventattributes.asp">Source
	 * list.</a>
	 *
	 * @param elements
	 *            the elements to filter
	 * @param matchval
	 * 			  the value used in the search for matching elements
	 * @return the filtered list
	 */
	public List<WebElement> filterNonClickableElements(List<WebElement> elements, 
			String matchval) {
		List<WebElement> retval = new ArrayList<WebElement>();
		//outer:
		for (WebElement elem : elements) {
			String tagName = elem.getTagName().toLowerCase();
			
			if (tagName.equals("param")) {
				WebElement parent = elem.findElement(By.xpath(".."));
				if (!retval.contains(parent)) {
					retval.add(parent);
				}
				continue;
			} else if (tagName.equals("embed")) {
				String attrval = elem.getAttribute("flashvars");
				if(attrval == null){
					attrval = elem.getAttribute("FlashVars");
				}
				if(!(attrval != null && attrval.contains(matchval))){
					continue;
				}
			} else if (tagName.equals("form")){
				String attrval = elem.getAttribute("action");
				if(!(attrval != null && attrval.contains(matchval)) && 
						!this.matchFormEventAttribute(elem, matchval) ){
					continue;
				}
				
			} else if (nonClickableHTMLElements.contains(tagName)) {
				if(log.isInfoEnabled()){
					log.info("Filtering element due to tag name " + tagName);
				}
				continue;
			} else if(tagName.equals("a")){
				String attrval = elem.getAttribute("href");
				if(!(attrval != null && attrval.contains(matchval)) && 
						!this.matchMouseKBEventAttribute(elem, matchval)){
					if(log.isInfoEnabled()){
						log.info("Filtering element with a tag");
					}
					continue;
				}
			} else {
				if(!this.matchMouseKBEventAttribute(elem, matchval)){
					if(log.isInfoEnabled()){
						log.info("Filtering element with tag name " + tagName);
					}
					continue;
				}
			}
			retval.add(elem);
		}
		return retval;
	}

	/**
	 * Finds only clickable elements in the current frame and window that
	 * contain the supplied value. This method will search for the value as a
	 * strict substring or as the complete value depended upon the boolean
	 * parameter.
	 *
	 * @param val
	 *            tthe value for which to search.
	 * @param asSubstring
	 *            if true will search for the value as a strict substring,
	 *            otherwise will search for the value as the complete attribute
	 *            value
	 * @return the list of matching elements.
	 */
	public List<WebElement> findClickableElements(String val,
			boolean asSubstring) {
		return filterNonClickableElements(findContainingElements(val,
				asSubstring), val);
	}
	
	
	/**
	 * <p>findClickableElements.</p>
	 *
	 * @param val a {@link java.lang.String} object.
	 */
	public List<WebElement> findClickableElements(String val) {
		return filterNonClickableElements(findContainingElements(val), val);
	}

	/**
	 * Finds all elements in the current frame and window that contain the
	 * supplied value. This method will search for the value as a strict
	 * substring or as the complete value depended upon the boolean parameter.
	 *
	 * @param val
	 *            the value for which to search.
	 * @param asSubstring
	 *            if true will search for the value as a strict substring,
	 *            otherwise will search for the value as the complete attribute
	 *            value
	 * @return the list of matching elements.
	 */
	public List<WebElement> findContainingElements(String val,
			boolean asSubstring) {
		// Note, if the attribute's value is strictly equal to the supplied val
		// then the xpath contains function will return false
		if (asSubstring) {
			return wdriver.findElements(By.xpath("//*[contains(@*, '" + val
					+ "')]"));
		} else {
			return wdriver.findElements(By.xpath("//*[@*='" + val + "']"));
		}
	}
	
	/**
	 * <p>findContainingElements.</p>
	 *
	 * @param val a {@link java.lang.String} object.
	 */
	public List<WebElement> findContainingElements(String val) {
		return wdriver.findElements(By.xpath("//*[contains(@*, '" + 
				val + "') or @*='" + val + "']"));
	}
	
	
	/**
	 * <p>findJSClickableElements.</p>
	 *
	 * @return the list of elements with click javascript attributes
	 */
	public List<WebElement> findJSClickableElements(){
		List<WebElement> retval = new ArrayList<WebElement>(
				findClickableElements("javascript:", true));
		List<WebElement> temp = wdriver.findElements(By.xpath(
				"//*[@onclick] | //*[@ondblclick] | //*[@onmousedown]"));
		retval.removeAll(temp); //removes any element we might have located twice
		retval.addAll(temp);
		return retval;
	}
	
	

	/**
	 * Find target elements at path.
	 *
	 * @param searchstr
	 *            the searchstr
	 * @param path
	 *            the path
	 * @return the element search result
	 */
	public ElementSearchResult findTargetElementsAtPath(String searchstr,
			FramePathIndexEntry path) {
		try{
			FrameUtils.traverseFramePath(wdriver, path.getFramePath());
			List<WebElement> contelems = extractClickableElements(searchstr);
			if (contelems.size() > 0) {
				return new ElementSearchResult(path.getFramePath(), contelems);
			}
		} catch (StaleElementReferenceException e){
			throw e;
		} catch(Exception e){
			if(log.isErrorEnabled()){
				log.error("Error retrieving elements.", e);
			}
		}
		return null;
	}
	
	/**
	 * <p>findJSSClickableElementsAtPath.</p>
	 *
	 * @param path a {@link edu.uga.cs.clickminer.index.FramePathIndexEntry} object.
	 */
	public ElementSearchResult findJSSClickableElementsAtPath(FramePathIndexEntry path){
		try{
			FrameUtils.traverseFramePath(wdriver, path.getFramePath());
			List<WebElement> jsclickelems = this.findJSClickableElements();
			if (jsclickelems.size() > 0) {
				return new ElementSearchResult(path.getFramePath(), jsclickelems);
			}
		} catch(StaleElementReferenceException e) {
			throw e;
		} catch(Exception e){
			if(log.isErrorEnabled()){
				log.error("Error retrieving elements.", e);
			}
		}
		return null;
	}

	/**
	 * Looks for and activates a clickable html element from within the current
	 * window and logs the event in the interaction log based upon the url of an
	 * HTTPRequest. Activating the element depends on the element's type. Form
	 * elements have their fields filled from the content of the request and
	 * submitted. Due to the difficulty of interacting plugins, the request url
	 * is submitted to the address bar of a new window in the case of object and
	 * embed tags. Click events are sent to all other elements.
	 * 
	 * @param req
	 *            the request
	 * @return true, if successful an element was found and activated, false
	 *         otherwise
	 * @throws UnsupportedEncodingException
	 *             if the request content can not be decoded so that it can be
	 *             used to fill in a form
	 */

	private List<ElementSearchResult> generateElementSearchResults(
			MitmHttpRequest req, List<FramePathIndexEntry> framepaths, 
			boolean findJavascriptElements) {
		List<ElementSearchResult> searchResults = new ArrayList<ElementSearchResult>();

		if (framepaths != null) {
			for (FramePathIndexEntry framepath : framepaths) {
				try{
					ElementSearchResult result = null;
					if(findJavascriptElements){
						result = findJSSClickableElementsAtPath(framepath);
					} else {
						result = findTargetElementsAtPath(req.getUrl(), framepath);
					}
					if (result != null) {
						searchResults.add(result);
					}
				} catch (StaleElementReferenceException e){
					if(log.isErrorEnabled()){
						log.error("Frame path: " + framepath + " is stale. Skipping", e);
						framepath.removeFromIndex();
						String window = framepath.getWindow();
						windowIMap.updateWindow(window);
						framePathIndex.updateIndex(window, 
								windowIMap.getWindowTimestamp(window));
						//framepath.setWindowNeedsUpdate();
					}
				}
			}
		}

		if (log.isInfoEnabled()) {

			int elems = 0;
			for (ElementSearchResult result : searchResults) {
				elems += result.getMatchingElements().size();
			}

			if (elems > 0) {
				log.info("Found " + elems
						+ " matching elements from findTargetElements.");
			} else {
				log.info("No matching elements found from findTargetElements.");
			}
		}

		return searchResults;
	}

	/**
	 * Gets the interaction log.
	 *
	 * @return the interaction log
	 */
	public List<InteractionRecord> getInteractionLog() {
		return this.interactionLog;
	}

	/**
	 * Open url in a new browser window. Note: this requires Javascript to work
	 *
	 * @param url
	 *            the url
	 * @return the handle of the newly opened window or null if the new window
	 * 			can not be detected.
	 */
	public String openUrlInNewWindow(String url) {
		int checkAttempts = 0;
		Set<String> newhandles = null;
		Set<String> oldhandles = wdriver.getWindowHandles();

		Iterator<String> handleiter = oldhandles.iterator();
		while(handleiter.hasNext()){
			try{
				wdriver.switchTo().window(handleiter.next());
				break;
			} catch (NoSuchWindowException e) {
				if(log.isErrorEnabled()){
					log.error("A window handle return by wdriver is not accessable." +
							"  This should not occur.", e);
				}
			}
		}

		((JavascriptExecutor) wdriver).executeScript("window.open('" + url + "')");
		do{
			checkAttempts++;
			if(checkAttempts > 10){
				//TODO we need to log file download interactions accordingly
				//instead of simply dismissing them.
				if(log.isWarnEnabled()){
					log.warn("Could not detect the newly opened window.  This " +
							"could be due to a file download.");
				}
				return null;
				//throw new RuntimeException("Unable to open new window.");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			newhandles = wdriver.getWindowHandles();
			newhandles.removeAll(oldhandles);
		} while(newhandles.size() < 1);
		wdriver.switchTo().window(newhandles.iterator().next());
		return wdriver.getWindowHandle();
	}
	
	/**
	 * <p>getNextPossibleRequest.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public MitmHttpRequest getNextPossibleRequest() throws ProxyErrorException{
		MitmHttpRequest req = pclient.getNextPossibleRequest();
		// there are no more possible requests, we can
		// do nothing else
		if (req == null) {
			if (log.isInfoEnabled()) {
				log.info("No more possible requests.");
			}
		} else {
			if (log.isInfoEnabled()) {
				log.info("Received next request from proxy.\n"
						+ req + "\n");
			}
		}
		return req;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	/**
	 * <p>run.</p>
	 */
	public void run() {
		try {
			if (log.isInfoEnabled()) {
				log.info("Starting BrowserEngine.");
			}
			
			boolean firstRequest = true;
			MitmHttpRequest req;

			main_run: while ((req = pclient.getPossibleRequest()) != null) {
				if (log.isInfoEnabled()) {
					log.info("Received request from proxy.\n" + req + "\n");
				}

				// begin interaction detection
				boolean notactivated = true;
				while (notactivated) {
					ElementActivationResult actresult = activateTargetElement(req);
					if (!actresult.elementSuccessfullyActivated()) {
						if (log.isInfoEnabled()) {
							log.info("Unable to activate an element matching the request.");
						}
						
						//If any of the following conditions are true we skip the current request and get the next one
						//Has referer and we found a frame path and the number of requests refered is under the threshold
						if ( (req.containsHeader("Referer") && actresult.foundFramePath() 
								&& req.getNumRequestsRefered() < this.numRequestsReferedThreshold)  
								//Has a positive delay from referer less than the threshold
								|| (req.getDelayFromReferer() > 0 
										&& req.getDelayFromReferer() < delayFromRefererThreshold)
								//Response is not html
								|| (!this.responseIsHTML(req))){
							if (log.isInfoEnabled()) {
								log.info("The request received does not corrispond to a "
										+ "possible user interaction.  Getting next possible "
										+ "request.");
							}
							/*
							* We assume the request resulted from a
							* non-clickable element and because a frame path was found
							* but no element was activated.
							* 
							* or the request was dynamically generated by
							* a javascript function called by an keyboard
							* or mouse event
							* we can't handle this case yet.
							* 
							* if a request has a referer and a frame path matching
							* that referer is found but the request's delayFromReferer
							* property is greater than the threshold then most likely
							* the request was not generated automatically due to page
							* rendering.  The most likely explanation is that the request
							* was cause by some sort of javascript.
							* 
							* If a request has a significant number of refering requests
							* then it behooves us to open the request anyway to consume a
							* lot of the automatically generated requests that will result
							* from opening the page.
							 */
							
							req = getNextPossibleRequest();
							// there are no more possible requests, we can
							// do nothing else
							if (req == null) {
								break main_run;
							}

						} else {
							if (log.isInfoEnabled()) {
								log.info("Opening url in new window.");
							}
							// if no referer, assume user typed the url in
							// the address bar.

							// If a referer exists but no frame path is
							// present in any window
							// then we assume that the source window was
							// cached.
							String windowHandle = null;
							if(firstRequest){
								try{
									wdriver.get(req.getUrl());
								} catch (TimeoutException e) {
									if(log.isErrorEnabled()){
										log.error("", e);
									}
								}
								windowHandle = wdriver.getWindowHandles().iterator().next();
							} else {
								try{
									windowHandle = openUrlInNewWindow(req
											.getUrl());
								} catch (Exception e){
									if(log.isErrorEnabled()){
										log.error("Unable to open " + req.getUrl() + " in new window.", 
												e);
									}
									windowHandle = null;
								}
							}
							
							// TODO might want to change this from a sleep
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							if(windowHandle == null || !this.validResultPage()){
								if(log.isInfoEnabled()){
									log.info("Request " + req.getUrl() + " does not result in a non-blank page. " +
											"Retrieving next possible interaction request.");
								}
								
								//only close the window if we know we have a handle to it
								//otherwise we could close the wrong window.
								//do not close the first window opened, it will kill WebDriver in the process
								if(windowHandle != null && !firstRequest){
									wdriver.close();
								}
								
								req = this.getNextPossibleRequest();
								// there are no more possible requests, we can do nothing else
								if (req == null) {
									break main_run;
								}
							} else {
								if(firstRequest){
									firstRequest = false;
								}
								
								PageEntry destpage = PageEntry.getPageEntry(
										wdriver, windowIMap, windowHandle);
								interactionLog.add(new InteractionRecord(
										ResultLocation.NEW_WINDOW, req,
										destpage));
								Set<String> newWindows = windowIMap.updateNewWindows();
								for(String window : newWindows){
									framePathIndex.updateIndex(window, 
											windowIMap.getWindowTimestamp(window));
								}
								notactivated = false;
							}
						}

					} else {
						notactivated = false;
						if (log.isInfoEnabled()) {
							log.info("Element activation successful.");
						}
					}
				}
				// end interaction detection

				// begin resting state detection
				while (!bstate.isResting()) {
					try {
						if (log.isInfoEnabled()) {
							log.info("Browser not resting, attempt: \n"
									+ bstate);
						}
						Thread.sleep(statePollInterval);
					} catch (InterruptedException e) {
						log.error(e);
					}
				}
				bstate.reset();
				// end resting state detection
				
				//cleans up entries for closed windows in the index
				windowIMap.checkForDeadWindows();
			}

		} catch (ProxyErrorException e) {
			log.fatal("Unable to determine browser resting state.", e);
		}
	}
	
	private boolean responseIsHTML(MitmHttpRequest req){
		String contentType = req.getResponseContentType();
		for(String htmlType : htmlMimeTypes){
			if(contentType.contains(htmlType)){
				return true;
			}
		}
		return false;
	}
	
	// returns true if the result page is valid
	private boolean validResultPage(){
		String wintitle = wdriver.getTitle();
		String winurl = wdriver.getCurrentUrl();
		if(log.isInfoEnabled()){
			log.info("Result page has url " + winurl + " and title " + wintitle);
		}
		
		//NOTE: this test is Firefox specific
		boolean retval =  !(wintitle.equals("Problem loading page") 
				|| winurl.equals("about:blank") || !containsClickableElements());
		
		if(log.isInfoEnabled()){
			if (retval){
				log.info("Valid result page with url " + winurl + " and title " + wintitle);
			} else {
				log.info("Invalid result page with url " + winurl + " and title " + wintitle);
			}
		}
		
		return retval;
	}
	
	//returns true if a page contains at least one clickable element
	/**
	 * <p>containsClickableElements.</p>
	 */
	public boolean containsClickableElements(){
		boolean retval = false;
		String tag = null;
		List<FramePath> allpaths = FrameUtils.findFramePaths(wdriver, null);
		outer:
		for(FramePath path : allpaths){
			FrameUtils.traverseFramePath(wdriver, path);
			List<WebElement> elements = wdriver.findElements(By.xpath("//*"));
			for(WebElement elem : elements){
				try{
					tag = elem.getTagName();
					if(!nonClickableHTMLElements.contains(tag)){
						if (eventClickableOnlyHTMLElements.contains(tag) 
								&& !containsMouseKBEventAttribute(wdriver, elem)){
							continue;
						}
						retval = true;
						break outer;
					}
				}catch (Exception e){
					if(log.isErrorEnabled()){
						log.error("Error processing element. Skipping.", e);
					}
				}
			}
		}
		wdriver.switchTo().defaultContent();
		if(log.isInfoEnabled()){
			if(retval){
				log.info("Page with url " + wdriver.getCurrentUrl() + " and title " 
						+ wdriver.getTitle() + " contains a clickable '" + tag + "' element");
			} else {
				log.info("Page with url " + wdriver.getCurrentUrl() + " and title " 
						+ wdriver.getTitle() + " contains a no clickable elements.");
			}
		}
		return retval;
	}


	/**
	 * <p>submitFormElement.</p>
	 *
	 * @param form a {@link org.openqa.selenium.WebElement} object.
	 * @param formvalues a {@link java.lang.String} object.
	 */
	public void submitFormElement(WebElement form, String formvalues){
		// TODO must be able to handle multipart form data

		if (log.isInfoEnabled()) {
			log.info("Filling out tag: " + form.getTagName() + " " + "id: "
					+ form.getAttribute("id") + " with values:\n" + formvalues);
		}

		// parse the request body
		Map<String, String> formdict = new HashMap<String, String>();
		String[] parts = formvalues.split("&");
		for (String part : parts) {
			String[] nameandval = part.split("=");
			// we decode the values because we want to enter exactly what was
			// supplied by the user
			try {
				formdict.put(nameandval[0],
						URLDecoder.decode(nameandval[1], "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				if(log.isErrorEnabled()){
					log.error("Unable to decode form value " + nameandval[1] 
							+ " . Skipping value.", e);
				}
			}
		}

		// fill out the form
		for (String key : formdict.keySet()) {
			WebElement formfield = null;
			try {
				formfield = form.findElement(By.id(key));
				if (log.isDebugEnabled()) {
					log.debug("Form field " + key + " found by id");
				}
			} catch (NoSuchElementException e) {
				try {
					formfield = form.findElement(By.name(key));
					if (log.isDebugEnabled()) {
						log.debug("Form field " + key + " found by name");
					}
				} catch (NoSuchElementException q) {
				}
			}

			if (formfield != null) {
				if (log.isInfoEnabled()) {
					log.info("Setting form field " + key + " to value "
							+ formdict.get(key));
				}
				formfield.sendKeys(formdict.get(key));
			} else {
				if (log.isInfoEnabled()) {
					log.info("Form field " + key + " was not found");
				}
			}
		}
		form.submit();
	}
	
	private class ElementActivationResult{

		private boolean elemActivated, foundFramePathVal;
		
		public ElementActivationResult(Boolean elemActivated, Boolean foundFramePathVal) {
			this.elemActivated = elemActivated;
			this.foundFramePathVal = foundFramePathVal;
		}
		
		public boolean elementSuccessfullyActivated(){
			return elemActivated;
		}
		
		public boolean foundFramePath(){
			return foundFramePathVal;
		}
	}
	
	private class PageMatchPair{

		private PageEntry pageEntry;
		private List<ElementSearchResult> searchResults;
		
		public PageMatchPair(PageEntry pageEntry, List<ElementSearchResult> searchResults) {
			this.pageEntry = pageEntry;
			this.searchResults = searchResults;
		}
		
		public PageEntry getPageEntry(){
			return pageEntry;
		}
		
		public List<ElementSearchResult> getElementSearchResults(){
			return searchResults;
		}
		
	}
}
