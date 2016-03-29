/*
* Copyright (C) 2013 Chris Neasbitt
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

import java.net.MalformedURLException;

import edu.uga.cs.clickminer.util.MutableURL;

/**
 * <p>MutableURLTest class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: MutableURLTest.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class MutableURLTest {

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.net.MalformedURLException if any.
	 */
	public static void main(String[] args) throws MalformedURLException {
		//mutableURLTest_1();
		//mutableURLTest_2();
		//mutableURLTest_3();
		mutableURLTest_4();
	}
	
	/**
	 * <p>mutableURLTest_1.</p>
	 *
	 * @throws java.net.MalformedURLException if any.
	 */
	public static void mutableURLTest_1() throws MalformedURLException{
		MutableURL url = new MutableURL("http://ken:secret@example.com:8080/res/./categories/;name=foo/objects;name=green/?page=1&name=2#12345");
		MutableURL url2 = new MutableURL("http://ken:secret@example.com:8080/res/categories/;name=foo/objects;name=green/?name=2&page=1");
		System.out.println(url.equals(url2));
	}
	
	/**
	 * <p>mutableURLTest_2.</p>
	 *
	 * @throws java.net.MalformedURLException if any.
	 */
	public static void mutableURLTest_2() throws MalformedURLException{
		MutableURL url = new MutableURL("http://ken:secret@example.com:80/res/./categories/;name=foo/objects;name=green/?page=1&name=2#12345");
		MutableURL url2 = new MutableURL("http://ken:secret@example.com/res/categories/;name=foo/objects;name=green/?name=2&page=1");
		System.out.println(url.equals(url2));
	}
	
	/**
	 * <p>mutableURLTest_3.</p>
	 *
	 * @throws java.net.MalformedURLException if any.
	 */
	public static void mutableURLTest_3() throws MalformedURLException{
		MutableURL url = new MutableURL("http://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&ved=0CDAQFjAA&url=http%3A%2F%2Fwww.python.org%2F&ei=fNSjUMvmJIuc9QSuyIGIAQ&usg=AFQjCNG7frXlIQC6rpM3VV6f5i7nq5VeIg");
		MutableURL url2 = new MutableURL(" http://www.google.com/");
		System.out.println(url.getSimilarity(url2));
		System.out.println(url.approxEquals(url2));
	}
	
	/**
	 * <p>mutableURLTest_4.</p>
	 *
	 * @throws java.net.MalformedURLException if any.
	 */
	public static void mutableURLTest_4() throws MalformedURLException{
		MutableURL url = new MutableURL("/pixel.php?partneruuid=0969a7a0-94e1-450d-9c8e-98b091df9fb0");
		System.out.println(url.getProtocol());
	}

}
