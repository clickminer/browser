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

import edu.uga.cs.clickminer.results.ResultsComparison;
import edu.uga.cs.clickminer.util.ResultsUtils.FileType;

/**
 * <p>ResultsComparisonTest class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ResultsComparisonTest.java 901 2014-04-25 03:27:51Z cjneasbitt $Id
 */
public class ResultsComparisonTest {

	/**
	 * <p>resultsComparisonTest_1.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public static void resultsComparisonTest_1() throws Exception{
		ResultsComparison rc = new ResultsComparison(
				new File("/home/cjneasbi/clickminer/test_traces/user3_nocache/mined_clicks.json"), 
				new File("/home/cjneasbi/clickminer/test_traces/user3_nocache/recorded_clicks.json"),
				null,
				FileType.PCAP, false,
				false);
		rc.outputResults("/home/cjneasbi/clickminer/test_traces/user3_nocache");
	}
	
	/**
	 * <p>resultsComparisonTest_2.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public static void resultsComparisonTest_2() throws Exception{
		ResultsComparison rc = new ResultsComparison(
				new File("/home/cjneasbi/clickminer/test_traces/user1_nocache/mined_clicks.json"), 
				new File("/home/cjneasbi/clickminer/test_traces/user1_nocache/recorded_clicks.json"),
				new File("/home/cjneasbi/clickminer/test_traces/user1_nocache/traffic_trace.pcap"),
				FileType.PCAP,
				2.0,
				true,
				true);
		rc.outputResults("/home/cjneasbi/Desktop/temp");
	}
	
	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) throws Exception{
		//resultsComparisonTest_1();
		resultsComparisonTest_2();
	}
	
}
