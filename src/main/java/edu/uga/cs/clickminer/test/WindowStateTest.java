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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import edu.uga.cs.clickminer.ProxyClient;
import edu.uga.cs.clickminer.datamodel.WindowState;
import edu.uga.cs.clickminer.util.TestUtils;

/**
 * <p>WindowStateTest class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: WindowStateTest.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class WindowStateTest {

	// tests the pageHash method, pclient not needed
	/**
	 * <p>windowStateTest_1.</p>
	 *
	 * @throws java.lang.InterruptedException if any.
	 */
	public static void windowStateTest_1() throws InterruptedException {
		WebDriver wdriver = new FirefoxDriver();
		wdriver.get("http://www.google.com");
		WindowState bstate = new WindowState(null, wdriver,
				wdriver.getWindowHandle());

		Thread.sleep(2000); // not the best way to do this, but quick and dirty
		System.out.println(bstate.getPageHash());
	}

	// tests the isResting method
	/**
	 * <p>windowStateTest_2.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public static void windowStateTest_2() throws Exception {
		long poll_interval = 10000;
		WebDriver wdriver = new FirefoxDriver(TestUtils.createProxyConfig());
		ProxyClient pclient = new ProxyClient("127.0.0.1", 8888);
		wdriver.get("http://www.google.com");

		WindowState bstate = new WindowState(pclient, wdriver,
				wdriver.getWindowHandle());
		Thread.sleep(2000); // not the best way to do this, but quick and dirty

		while (!bstate.isResting()) {
			try {
				System.out.println(bstate);
				Thread.sleep(poll_interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("The browser is in a resting state.");
		System.out.println(bstate);
		bstate.reset();
		wdriver.quit();
	}

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) throws Exception {
		windowStateTest_2();
	}
}
