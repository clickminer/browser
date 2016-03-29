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
package edu.uga.cs.clickminer.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * <p>ComparisonResultWriter class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ComparisonResultWriter.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ComparisonResultWriter {

	/**
	 * <p>write.</p>
	 *
	 * @param result a {@link edu.uga.cs.clickminer.results.ComparisonResult} object.
	 * @param outputdir a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public static void write(ComparisonResult result, String outputdir) throws IOException{
		List<String[]> matchesOutput = new ArrayList<String[]>();
		CSVWriter writer = new CSVWriter(
				new FileWriter(new File(outputdir).getCanonicalPath() 
						+ File.separatorChar 
						+"results_comparison_matches.csv"), ',');
		
		String[] header = {"Recorded Click","Click Matchable","Click Confirmed"
				,"Matching Mined Click", "Match Has Locator", "URL Match","Locator Match"};
		matchesOutput.add(header);
		for(ComparisonMatch match : result.getMatches()){
			matchesOutput.add(match.toCSVArray());
		}
		writer.writeAll(matchesOutput);
		writer.close();
		
		writeListFile(outputdir, "results_comparison_falsepositives.csv", 
				"Falsely Mined Clicks", result.getFalsePositiveClicks());
		writeListFile(outputdir, "results_comparison_addressbar.csv", 
				"Address Bar Interactions", result.getAddressBarInteractions());
		writeListFile(outputdir, "results_comparison_confirmed.csv", 
				"Confirmed Clicks", result.getConfirmedClicks());
		writeListFile(outputdir, "results_comparison_unconfirmed.csv", 
				"Unconfirmed Clicks", result.getUnconfirmedClicks());
		writeListFile(outputdir, "results_comparison_augmented.csv", 
				"Augmented Clicks", result.getAugmentedClicks());

		
		writer = new CSVWriter(
				new FileWriter(new File(outputdir).getCanonicalPath() 
						+ File.separatorChar 
						+"results_comparison_summary.csv"), ',');
		writer.writeAll(result.getSummary().toCSVData());
		writer.close();
	}
	
	private static void writeListFile(String outputdir, String filename, String listHeader,
			List<Integer> list) throws IOException{
		List<String[]> buf = new ArrayList<String[]>();
		CSVWriter writer = new CSVWriter(
				new FileWriter(new File(outputdir).getCanonicalPath() 
						+ File.separatorChar 
						+ filename), ',');
		String[] header2 = {listHeader};
		buf.clear();
		buf.add(header2);
		for(Integer i : list){
			String[] row = new String[1];
			row[0] = i.toString();
			buf.add(row);
		}
		writer.writeAll(buf);
		writer.close();	
	}
}
