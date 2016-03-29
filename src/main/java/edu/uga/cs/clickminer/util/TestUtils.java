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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * <p>TestUtils class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: TestUtils.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class TestUtils {

	/**
	 * <p>createProxyConfig.</p>
	 */
	public static DesiredCapabilities createProxyConfig() {
		String PROXY = "127.0.0.1:8080";
		Proxy proxy = new Proxy();
		proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY);
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setCapability(CapabilityType.PROXY, proxy);
		return cap;
	}
	
	/**
	 * <p>getWebdriverProfile.</p>
	 *
	 * @param profiledir a {@link java.lang.String} object.
	 * @param profilename a {@link java.lang.String} object.
	 */
	public static File getWebdriverProfile(String profiledir, final String profilename){
		File srcdir = new File(profiledir);
		File[] retval = srcdir.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.endsWith("." + profilename);
			}	
		});
		
		if(retval.length > 0){
			return retval[0];
		}
		return null;
	}
	
	/**
	 * <p>readJSScriptsAsJSString.</p>
	 *
	 * @param abpath a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public static String readJSScriptsAsJSString(String abpath)
			throws IOException {
		StringBuffer buf = new StringBuffer();
		BufferedReader br = new BufferedReader(new FileReader(abpath));
		String line = null;
		while ((line = br.readLine()) != null) {
			buf.append(line.replace("\"", "\\\""));
		}
		br.close();
		return buf.toString();
	}
	
	/**
	 * <p>loadJSScript.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 * @param script a {@link java.lang.String} object.
	 * @param id a {@link java.lang.String} object.
	 */
	public static void loadJSScript(WebDriver wdriver, String script, String id) {
		String loadjs = "var scriptElt = document.createElement('script');\n"
				+ "scriptElt.setAttribute('type', 'text/javascript');\n"
				+ "scriptElt.setAttribute('id', '"
				+ id
				+ "');\n"
				+ "scriptElt.text = \""
				+ script
				+ "\";\n"
				+ "document.getElementsByTagName('head')[0].appendChild(scriptElt);";

		((JavascriptExecutor) wdriver).executeAsyncScript(loadjs);
	}

}
