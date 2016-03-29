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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.Header;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import edu.uga.cs.clickminer.ProxyClient;
import edu.uga.cs.clickminer.ProxyClient.ProxyMode;
import edu.uga.cs.clickminer.datamodel.MitmHttpRequest;
import edu.uga.cs.clickminer.exception.ProxyErrorException;
import edu.uga.cs.clickminer.util.TestUtils;

/**
 * <p>ProxyClientTest class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ProxyClientTest.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ProxyClientTest {

	/**
	 * <p>proxyClientTest_1.</p>
	 */
	public static void proxyClientTest_1() {
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		System.out.println("Sending request");
		try {
			String req_info = pc.printRequest();
			System.out.println("Received response:");
			System.out.println(req_info);
		} catch (ProxyErrorException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>proxyClientTest_2.</p>
	 */
	public static void proxyClientTest_2() {
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		System.out.println("Sending request");
		try {
			MitmHttpRequest req = pc.getMinRequest();
			System.out.println("Received response:");
			System.out.println(req);
			System.out.println("version: " + req.getMajorVersion() + "."
					+ req.getMinorVersion());
			System.out.println("headers:");
			Header[] headers = req.getAllHeaders();
			for (Header header : headers) {
				System.out.println("\t" + header.getName() + ": " + header.getValue());
			}
		} catch (ProxyErrorException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>proxyClientTest_3.</p>
	 */
	public static void proxyClientTest_3() {
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);

		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "8080");

		try {
			URL url = new URL("http://www.cnet.com/");
			url.openStream();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Sending request");
		try {
			long count = pc.getNumReceivedRequest();
			System.out.println("Received response:");
			System.out.println(count);
		} catch (ProxyErrorException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <p>proxyClientTest_4.</p>
	 */
	public static void proxyClientTest_4() {
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		try {
			System.out.println(pc.getMode());
		} catch (ProxyErrorException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <p>proxyClientTest_5.</p>
	 */
	public static void proxyClientTest_5() {
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		try {
			pc.setModeNoContent("http://www.google.com");
			System.out.println(pc.getMode());
			pc.setModeContent();
			System.out.println(pc.getMode());
		} catch (ProxyErrorException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * <p>proxyClientTest_6.</p>
	 */
	public static void proxyClientTest_6() {
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		try {
			pc.setModeNoContent("blah");
			System.out.println(pc.getMode());
			pc.setModeContent();
			System.out.println(pc.getMode());
		} catch (ProxyErrorException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * <p>proxyClientTest_7.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public static void proxyClientTest_7() throws Exception{
		FirefoxProfile profile = new FirefoxProfile(
				TestUtils.getWebdriverProfile("/home/cjneasbi/.mozilla/firefox", 
				"webdriver"));
		WebDriver wdriver = new FirefoxDriver(
				new FirefoxBinary(new File("/home/cjneasbi/Desktop/old_firefox/firefox-14.0.1/firefox-bin")), 
				profile,
				TestUtils.createProxyConfig());
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		
		pc.setModeNoContent("http://www.newegg.com");
		if(pc.getMode() == ProxyMode.NOCONTENT){
			System.out.println("Pass");
		} else {
			System.out.println("Fail");
		}
		
		wdriver.get("http://www.yahoo.com");
		Thread.sleep(2000);
		if(pc.getMode() == ProxyMode.NOCONTENT){
			System.out.println("Pass");
		} else {
			System.out.println("Fail");
		}
		
		wdriver.get("http://www.newegg.com");
		Thread.sleep(2000);
		if(pc.getMode() == ProxyMode.CONTENT){
			System.out.println("Pass");
		} else {
			System.out.println("Fail");
		}
		
		wdriver.close();
	}
	
	/**
	 * <p>proxyClientTest_8.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public static void proxyClientTest_8() throws Exception{
		ProxyClient pc = new ProxyClient("127.0.0.1", 8888);
		int retval = pc.getRequestCheckLimit();
		if(retval == 0){
			System.out.println("Pass");
		} else {
			System.out.println("Fail");
		}
		
		pc.setRequestCheckLimit(-5);
		retval = pc.getRequestCheckLimit();
		if(retval == 0){
			System.out.println("Pass");
		} else {
			System.out.println("Fail");
		}
		
		pc.setRequestCheckLimit(3);
		retval = pc.getRequestCheckLimit();
		if(retval == 3){
			System.out.println("Pass");
		} else {
			System.out.println("Fail");
		}
	}

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) throws Exception {
		// proxyClientTest_1();
		// proxyClientTest_2();
		// proxyClientTest_3();
		// proxyClientTest_4();
		// proxyClientTest_5();
		// proxyClientTest_6();
		// proxyClientTest_7();
		proxyClientTest_8();
	}

}
