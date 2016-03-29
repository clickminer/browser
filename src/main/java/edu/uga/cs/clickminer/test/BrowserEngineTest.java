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
package edu.uga.cs.clickminer.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
//import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.RemoteWebDriver;

import edu.uga.cs.clickminer.BrowserEngine;
import edu.uga.cs.clickminer.ProxyClient;
//import edu.uga.cs.clickminer.datamodel.ElementSearchResult;
import edu.uga.cs.clickminer.datamodel.FramePath;
import edu.uga.cs.clickminer.datamodel.log.InteractionRecord;
import edu.uga.cs.clickminer.exception.ProxyErrorException;
import edu.uga.cs.clickminer.util.FrameUtils;
import edu.uga.cs.clickminer.util.JSUtils;
import edu.uga.cs.clickminer.util.TestUtils;
import edu.uga.cs.json.JSONWriter;

/**
 * <p>BrowserEngineTest class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: BrowserEngineTest.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class BrowserEngineTest {

	private static Log log = LogFactory.getLog(BrowserEngineTest.class);
	
	private static List<String> filteredTypes;
	static {
		filteredTypes = new ArrayList<String>();
		filteredTypes.add("text/css");
		filteredTypes.add("text/javascript");
		filteredTypes.add("application/x-javascript");
		filteredTypes.add("application/javascript");
		filteredTypes.add("application/x-shockwave-flash");
	}

	/*private static void activateElement(RemoteWebDriver wdriver,
			ElementSearchResult sresult) throws IOException {

		wdriver.switchTo().defaultContent();
		for (WebElement elem : sresult.getFramePath().getPath()) {
			wdriver.switchTo().frame(elem);
		}

		WebElement target = sresult.getMatchingElements().get(0);
		if (log.isInfoEnabled()) {
			log.info("Target element is located at " + target.getLocation());
		}
		target.click();

		if (!sresult.matchesInDefaultFrame()) {
			wdriver.switchTo().defaultContent();
		}
	}*/

	/**
	 * <p>browserEngineTest_1.</p>
	 */
	public static void browserEngineTest_1() {
		if (log.isErrorEnabled()) {
			log.error("Debug enabled.");
		}
		if (log.isWarnEnabled()) {
			log.warn("Warn enabled.");
		}
		if (log.isInfoEnabled()) {
			log.info("Info enabled.");
		}
		if (log.isDebugEnabled()) {
			log.debug("Debug enabled.");
		}

	}

	// run with default binary, profile and without native events
	/**
	 * <p>browserEngineTest_2.</p>
	 */
	public static void browserEngineTest_2() {
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		WebDriver wdriver = new FirefoxDriver(TestUtils.createProxyConfig());
		BrowserEngine bengine = new BrowserEngine(pc, wdriver);
		bengine.run();
		List<InteractionRecord> ilog = bengine.getInteractionLog();
		JSONWriter<InteractionRecord> writer = new JSONWriter<InteractionRecord>();	
		try {
			writer.write(new File(
					"/home/cjneasbi/Desktop/ilog.json"), ilog);
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
		}
		bengine.close();
	}

	// use with test_trace_5
	/*public static void browserEngineTest_3() throws ProxyErrorException,
			InterruptedException {
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		WebDriver wdriver = new FirefoxDriver(TestUtils.createProxyConfig());
		BrowserEngine bengine = new BrowserEngine(pc, wdriver);
		wdriver.get("http://www.cnet.com/");
		Thread.sleep(20000);
		pc.setProxyTimestamp(1.343943586072432E9);
		List<FramePath> framepaths = FrameUtils
				.findFramePathsFromReferer(wdriver, pc.getMinRequest());
		if (log.isInfoEnabled()) {
			if (framepaths != null) {
				log.info("Found frame paths.");
			} else {
				log.info("Unable to find and frame paths.");
			}
		}
		bengine.close();
	}*/

	// use with test_trace_5
	// need to change the profile and binary location on each machine
	/*public static void browserEngineTest_4() throws ProxyErrorException,
			InterruptedException, IOException {
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);

		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		profile.setEnableNativeEvents(true);
		RemoteWebDriver wdriver = new FirefoxDriver(new FirefoxBinary(new File(
				"/home/cjneasbi/Desktop/old_firefox/firefox-12/firefox")),
				profile, TestUtils.createProxyConfig());

		// WebDriver wdriver = new FirefoxDriver(TestUtils.createProxyConfig());

		BrowserEngine bengine = new BrowserEngine(pc, wdriver);
		wdriver.get("http://www.cnet.com/");
		Thread.sleep(20000);
		// should return an object tag
		ElementSearchResult sresult = bengine
				.searchForClickableElements("http://clk.atdmt.com/go/408653039/direct;wi.970;hi.66;pc.606617;ai.285952243");
		if (log.isInfoEnabled()) {
			if (sresult != null) {
				log.info("Found elements");
				if (sresult.matchesInDefaultFrame()) {
					List<String> psrcs = sresult.getFramePathUrls();
					log.info("Source Frame: " + psrcs.get(psrcs.size() - 1));
				} else {
					log.info("Source Frame: <default>");
				}

				FrameUtils.traverseFramePath(wdriver, sresult.getFramePath());
				String framep = "Frame Path: //";

				for (String pathfrag : sresult.getFramePathUrls()) {
					framep += pathfrag + "/";
				}
				log.info(framep);
				log.info("Matching Elements: ");
				for (WebElement elem : sresult.getMatchingElements()) {
					log.info("\t" + elem.getTagName());
				}
				wdriver.switchTo().defaultContent();

				activateElement(wdriver, sresult);
				Thread.sleep(20000);
			} else {
				log.info("Unable to locate elements.");
			}
		}
		bengine.close();
	}*/

	// run with specified binary, profile and native events activated
	/**
	 * <p>browserEngineTest_5.</p>
	 */
	public static void browserEngineTest_5() {
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);

		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		profile.setEnableNativeEvents(true);
		RemoteWebDriver wdriver = new FirefoxDriver(new FirefoxBinary(new File(
				"/home/cjneasbi/Desktop/old_firefox/firefox-12/firefox")),
				profile, TestUtils.createProxyConfig());

		BrowserEngine bengine = new BrowserEngine(pc, wdriver);
		bengine.run();
		List<InteractionRecord> ilog = bengine.getInteractionLog();
		JSONWriter<InteractionRecord> writer = new JSONWriter<InteractionRecord>();	
		try {
			writer.write(new File(
					"/home/cjneasbi/Desktop/ilog.json"), ilog);
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
		}
		bengine.close();
	}

	// test WebDriver.getCurrentUrl() for subframes
	/**
	 * <p>browserEngineTest_6.</p>
	 */
	public static void browserEngineTest_6() {
		RemoteWebDriver wdriver = new FirefoxDriver();

		wdriver.get("http://docs.oracle.com/javase/6/docs/api/");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<FramePath> framepaths = FrameUtils.findFramePaths(wdriver,
				"http://docs.oracle.com/javase/6/docs/api/overview-frame.html");
		if (log.isInfoEnabled()) {
			if (framepaths == null) {
				log.info("Did not find any frame path.");
			} else {
				log.info("Found frame paths.");
			}
		}
	}

	// tests openUrlInNewWindow
	/**
	 * <p>browserEngineTest_7.</p>
	 */
	public static void browserEngineTest_7() {
		RemoteWebDriver wdriver = new FirefoxDriver();
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		BrowserEngine bengine = new BrowserEngine(pc, wdriver);

		wdriver.get("http://docs.oracle.com/javase/6/docs/api/");
		bengine.openUrlInNewWindow("http://www.google.com");
	}
	
	/*public static void browserEngineTest_8() throws IOException,
		InterruptedException, ProxyErrorException {
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		
		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		WebDriver wdriver = new FirefoxDriver(
				new FirefoxBinary(
						new File("/home/cjneasbi/Desktop/old_firefox/firefox-17.0/firefox-bin")),
						profile,
						TestUtils.createProxyConfig());
		
		BrowserEngine bengine = new BrowserEngine(pc, wdriver);
		pc.setFilteredResponseType(filteredTypes);
		
		wdriver.get("http://us.battle.net/d3/");
		Thread.sleep(5000);
		
		ElementSearchResult sresult = bengine
				.searchForClickableElements("http://us.blizzard.com/en-us/games/hots/");
		if(sresult != null){
			System.out.println("Found Element.");
			for(WebElement elem : sresult.getMatchingElements()){
				System.out.println(JSUtils.getElementLocator(wdriver, elem));
			}
		} else {
			System.out.println("Element not found.");
		}
	}*/
	
	
	// run with specified binary, specified profile and without native events
	// need selenium ide installed
	/**
	 * <p>browserEngineTest_9.</p>
	 */
	public static void browserEngineTest_9() {
		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		WebDriver wdriver = new FirefoxDriver(
				new FirefoxBinary(new File("/home/cjneasbi/Desktop/old_firefox/firefox-14.0.1/firefox-bin")), 
				profile,
				TestUtils.createProxyConfig());
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		BrowserEngine bengine = new BrowserEngine(pc, wdriver);
		try{
			bengine.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<InteractionRecord> ilog = bengine.getInteractionLog();
		JSONWriter<InteractionRecord> writer = new JSONWriter<InteractionRecord>();	
		try {
			writer.write(new File(
					"/home/cjneasbi/Desktop/mined_clicks.json"), ilog);
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
		}
		bengine.close();
	}
	
	//Determines what url is displayed on page load error
	/**
	 * <p>browserEngineTest_10.</p>
	 */
	public static void browserEngineTest_10(){
		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		WebDriver wdriver = new FirefoxDriver(null, profile);
		
		wdriver.get("http://www.dkfjaojfko.com");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(wdriver.getCurrentUrl());
		System.out.println(wdriver.getTitle());
		System.out.println(wdriver.getPageSource());
		wdriver.quit();
	}
	
	// run with default binary, specified profile and without native events
	// need selenium ide installed
	/**
	 * <p>browserEngineTest_11.</p>
	 */
	public static void browserEngineTest_11() {
		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		WebDriver wdriver = new FirefoxDriver(null, profile,
				TestUtils.createProxyConfig());
		//ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		//BrowserEngine bengine = new BrowserEngine(pc, wdriver);
		
		wdriver.get("http://cnn-f.akamaihd.net/crossdomain.xml");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
		List<WebElement> sresult = wdriver.findElements(By.xpath("//*"));
		for(WebElement elem : sresult){
			System.out.println(elem.getTagName());
		}
	}
	
	
	// run with specified binary, specified profile and without native events, javascript searching
	// need selenium ide installed
	/**
	 * <p>browserEngineTest_12.</p>
	 */
	public static void browserEngineTest_12() {
		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		WebDriver wdriver = new FirefoxDriver(
				new FirefoxBinary(new File("/home/cjneasbi/Desktop/old_firefox/firefox-14.0.1/firefox-bin")), 
				profile,
				TestUtils.createProxyConfig());
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		BrowserEngine bengine = new BrowserEngine(pc, wdriver, true, true);
		try{
			bengine.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<InteractionRecord> ilog = bengine.getInteractionLog();
		JSONWriter<InteractionRecord> writer = new JSONWriter<InteractionRecord>();	
		try {
			writer.write(new File(
					"/home/cjneasbi/Desktop/mined_clicks_js.json"), ilog);
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error(e);
			}
		}
		bengine.close();
	}
	
	// run with specified binary, specified profile and without native events
	// request search limit set to 3
	// need selenium ide installed
	/**
	 * <p>browserEngineTest_13.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public static void browserEngineTest_13() throws ProxyErrorException {
		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		WebDriver wdriver = new FirefoxDriver(
				new FirefoxBinary(new File("/home/cjneasbi/Desktop/old_firefox/firefox-14.0.1/firefox-bin")), 
				profile,
				TestUtils.createProxyConfig());
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		pc.setRequestCheckLimit(3);
		BrowserEngine bengine = new BrowserEngine(pc, wdriver);
		try{
			bengine.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<InteractionRecord> ilog = bengine.getInteractionLog();
		JSONWriter<InteractionRecord> writer = new JSONWriter<InteractionRecord>();	
		try {
			writer.write(new File(
					"/home/cjneasbi/Desktop/mined_clicks.json"), ilog);
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
		}
		bengine.close();
	}
	
	// run with specified binary, specified profile and without native events, javascript searching
	// request search limit set to 3
	// need selenium ide installed
	/**
	 * <p>browserEngineTest_14.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public static void browserEngineTest_14() throws ProxyErrorException {
		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		WebDriver wdriver = new FirefoxDriver(
				new FirefoxBinary(new File("/home/cjneasbi/Desktop/old_firefox/firefox-14.0.1/firefox-bin")), 
				profile,
				TestUtils.createProxyConfig());
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		pc.setRequestCheckLimit(3);
		BrowserEngine bengine = new BrowserEngine(pc, wdriver, true, true);
		try{
			bengine.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<InteractionRecord> ilog = bengine.getInteractionLog();
		JSONWriter<InteractionRecord> writer = new JSONWriter<InteractionRecord>();	
		try {
			writer.write(new File(
					"/home/cjneasbi/Desktop/mined_clicks_js.json"), ilog);
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
		}
		bengine.close();
	}
	
	//Tests JSUtils.setElementAttribute
	/**
	 * <p>browserEngineTest_15.</p>
	 */
	public static void browserEngineTest_15(){
		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		WebDriver wdriver = new FirefoxDriver(
				new FirefoxBinary(
						new File("/home/cjneasbi/Desktop/old_firefox/firefox-14.0.1/firefox-bin")), 
				profile);
		wdriver.get("http://www.cs.uga.edu/~neasbitt/");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		WebElement elem = wdriver.findElement(By.id("logo"));
		JSUtils.setElementAttribute(wdriver, elem, "id", "logologologo1");
		
		try{
			elem = wdriver.findElement(By.id("logologologo1"));
			System.out.println("Pass");
		} catch(NoSuchElementException e) {
			System.out.println("Fail");
		}
		wdriver.close();
		
	}
	
	// run with specified profile, test selenium ide locators plugin
	/**
	 * <p>browserEngineTest_16.</p>
	 *
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	public static void browserEngineTest_16() throws IOException,
			InterruptedException {
		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		WebDriver wdriver = new FirefoxDriver(
				new FirefoxBinary(
						new File("/home/cjneasbi/Desktop/old_firefox/firefox-17.0/firefox-bin")), 
				profile);
		// WebDriver wdriver = new FirefoxDriver();
		wdriver.get("http://www.cs.uga.edu/~neasbitt/");
		wdriver.switchTo().activeElement();

		Thread.sleep(10000);

		WebElement elem = wdriver.findElement(By.id("logo"));
		List<String> locator = JSUtils.getElementLocator(wdriver, elem);

		for(String s : locator){
			System.out.println(s);
		}
		wdriver.close();
	}
	
	//Tests browser engine findJSClickableElements
	/**
	 * <p>browserEngineTest_17.</p>
	 *
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public static void browserEngineTest_17() throws IOException,
			InterruptedException, ProxyErrorException {
		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		WebDriver wdriver = new FirefoxDriver(
				new FirefoxBinary(
						new File("/home/cjneasbi/Desktop/old_firefox/firefox-17.0/firefox-bin")), 
				profile,
				TestUtils.createProxyConfig());
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		BrowserEngine bengine = new BrowserEngine(pc, wdriver, true, true);
		
		wdriver.get("file:///home/cjneasbi/workspace/clickminer-browser/resource/test/js_test.html");
		wdriver.switchTo().activeElement();
		Thread.sleep(5000);
		
		List<WebElement> elements = bengine.findJSClickableElements();
		for(WebElement elem : elements){
			System.out.println(elem.getAttribute("id"));
		}
		bengine.close();
	}
	
	// tests getting all attributes 
	/**
	 * <p>browserEngineTest_18.</p>
	 *
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	public static void browserEngineTest_18() throws IOException,
			InterruptedException {
		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		WebDriver wdriver = new FirefoxDriver(
				new FirefoxBinary(
						new File("/home/cjneasbi/Desktop/old_firefox/firefox-17.0/firefox-bin")), 
				profile);
		// WebDriver wdriver = new FirefoxDriver();
		wdriver.get("http://www.cs.uga.edu/~neasbitt/");
		wdriver.switchTo().activeElement();

		Thread.sleep(5000);

		WebElement elem = wdriver.findElement(By.id("profile"));
		Map<String, String> attrs = JSUtils.getElementAttributes(wdriver, elem);
		System.out.println(attrs);
		
		wdriver.close();
	}
	
	//used to test the responses generated by the proxy for requests not in the saved flows
	/**
	 * <p>browserEngineTest_19.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	public static void browserEngineTest_19() throws ProxyErrorException, InterruptedException {
		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		WebDriver wdriver = new FirefoxDriver(
				new FirefoxBinary(new File("/home/cjneasbi/Desktop/old_firefox/firefox-17.0/firefox-bin")), 
				profile,
				TestUtils.createProxyConfig());

		for(int i = 0; i < 10; i++){
			//wdriver.get("http://choices.truste.com/get?name=admarker-full-tr.png");
			wdriver.get("http://blah.blah.com");
			Thread.sleep(5000);
		}

		wdriver.quit();
	}
	
	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 * @throws java.lang.InterruptedException if any.
	 * @throws java.io.IOException if any.
	 */
	public static void main(String[] args) throws ProxyErrorException,
			InterruptedException, IOException {
		// browserEngineTest_1();
		// browserEngineTest_2();
		// browserEngineTest_3();
		// browserEngineTest_4();
		// browserEngineTest_5();
		// browserEngineTest_6();
		// browserEngineTest_7();
		// browserEngineTest_8();
		// browserEngineTest_9();
		// browserEngineTest_10();
		// browserEngineTest_11();
		// browserEngineTest_12();
		// browserEngineTest_13();
		// browserEngineTest_14();
		// browserEngineTest_15();
		// browserEngineTest_16();
		// browserEngineTest_17();
		// browserEngineTest_18();
		browserEngineTest_19();
	}

}
