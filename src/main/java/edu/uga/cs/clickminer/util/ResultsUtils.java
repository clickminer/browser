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
package edu.uga.cs.clickminer.util;

import java.io.File;
import java.io.IOException;

/**
 * <p>ResultsUtils class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ResultsUtils.java 901 2014-04-25 03:27:51Z cjneasbitt $Id
 */
public class ResultsUtils {
	
	public enum FileType {
		PCAP("traffic_trace.pcap"),
		JSON("traffic_trace.json");
		
		private final String filename;
		FileType(String filename){
			this.filename = filename;
		}
		
		public String getFilename(){
			return filename;
		}
	}

	private static File getFile(File resultsDir, String filename) throws IOException{
		return new File(resultsDir.getCanonicalPath() + File.separatorChar + 
				filename);
	}
	
	private static File getResultsDir(String outputdir) throws IOException{
		File resultsDir = new File(outputdir);
		if(!resultsDir.isDirectory()){
			throw new IOException(outputdir + " is not a directory.");
		}
		return resultsDir;
	}
	
	public static File getTraceFile(String outputdir, FileType type) throws IOException{
		return ResultsUtils.getFile(ResultsUtils.getResultsDir(outputdir), 
				type.getFilename());
	}
	
	/**
	 * <p>getPcap.</p>
	 *
	 * @param outputdir a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	/*private static File getPcap(String outputdir) throws IOException{
		return ResultsUtils.getFile(ResultsUtils.getResultsDir(outputdir), 
				"traffic_trace.pcap");
	}
	
	private static File getJSON(String outputdir) throws IOException {
		return ResultsUtils.getFile(ResultsUtils.getResultsDir(outputdir), 
				"traffic_trace.json");
	}*/
	
	/**
	 * Get the supported file type from a file path.
	 * 
	 * @param path, the source file path
	 * @return a FileType or null if unsupported
	 */
	public static FileType getFileType(String suffix){
		FileType type = null;
		if(suffix != null){
			if(suffix.endsWith(".pcap")){
				type = FileType.PCAP;
			} else if (suffix.endsWith(".json")) {
				type = FileType.JSON;
			}
		}
		return type;
	}
	
	
	
	/**
	 * <p>getMinedClicks.</p>
	 *
	 * @param outputdir a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public static File getMinedClicks(String outputdir) throws IOException{
		return ResultsUtils.getFile(ResultsUtils.getResultsDir(outputdir), 
				"mined_clicks.json");
	}
	
	/**
	 * <p>getRecordedClicks.</p>
	 *
	 * @param outputdir a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public static File getRecordedClicks(String outputdir) throws IOException{
		return ResultsUtils.getFile(ResultsUtils.getResultsDir(outputdir), 
				"recorded_clicks.json");
	}
	
	/**
	 * <p>getResultsFalsePositives.</p>
	 *
	 * @param outputdir a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public static File getResultsFalsePositives(String outputdir) throws IOException{
		return ResultsUtils.getFile(ResultsUtils.getResultsDir(outputdir), 
				"results_comparison_falsepositives.csv");
	}
	
	/**
	 * <p>getResultsAddressBar.</p>
	 *
	 * @param outputdir a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public static File getResultsAddressBar(String outputdir) throws IOException{
		return ResultsUtils.getFile(ResultsUtils.getResultsDir(outputdir), 
				"results_comparison_addressbar.csv");
	}
	
	/**
	 * <p>getResultsMatches.</p>
	 *
	 * @param outputdir a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public static File getResultsMatches(String outputdir) throws IOException{
		return ResultsUtils.getFile(ResultsUtils.getResultsDir(outputdir), 
				"results_comparison_matches.csv");
	}
	
	/**
	 * <p>getResultsSummary.</p>
	 *
	 * @param outputdir a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public static File getResultsSummary(String outputdir) throws IOException{
		return ResultsUtils.getFile(ResultsUtils.getResultsDir(outputdir), 
				"results_comparison_summary.csv");
	}
}
