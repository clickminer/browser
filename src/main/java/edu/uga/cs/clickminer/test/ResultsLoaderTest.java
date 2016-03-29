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

import java.io.File;
import java.util.List;

import edu.uga.cs.clickminer.datamodel.ide.Click;
import edu.uga.cs.clickminer.results.ResultsLoader;

/**
 * <p>ResultsLoaderTest class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ResultsLoaderTest.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ResultsLoaderTest {

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) throws Exception {
		//resultsLoaderTest_1();
		resultsLoaderTest_2();
	}
	
	
	/**
	 * <p>resultsLoaderTest_1.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public static void resultsLoaderTest_1() throws Exception{
		File clog = new File("/home/cjneasbi/clickminer/test_traces/user1_nocache/recorded_clicks.json");
		File pcap = new File("/home/cjneasbi/clickminer/test_traces/user1_nocache/traffic_trace.pcap");
		
		List<Click> allclicks = ResultsLoader.loadRecordedClicks(clog);
		List<Click> capclicks = ResultsLoader.loadRecordedClicks(clog, pcap);
		
		System.out.println("All clicks size: " + allclicks.size() + 
				" Captured clicks size: " + capclicks.size());
	}
	
	/**
	 * <p>resultsLoaderTest_2.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public static void resultsLoaderTest_2() throws Exception{
		File clog = new File("/home/cjneasbi/clickminer/test_traces/user1_nocache/recorded_clicks.json");
		File pcap = new File("/home/cjneasbi/clickminer/test_traces/user1_nocache/traffic_trace.pcap");
		
		List<Click> allclicks = ResultsLoader.loadRecordedClicks(clog);
		List<Click> capclicks = ResultsLoader.loadRecordedClicks(clog, pcap);
		
		allclicks.removeAll(capclicks);
		System.out.println("Non-recorded clicks:");
		for(Click c : allclicks){
			System.out.println(c);
		}
	}
}
