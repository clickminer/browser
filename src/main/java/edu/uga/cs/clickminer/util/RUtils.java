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

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import edu.uga.cs.clickminer.results.ClassificationResult;
import edu.uga.cs.clickminer.results.ClassificationResultList;

/**
 * <p>RUtils class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RUtils.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class RUtils {

	/**
	 * <p>prepRData.</p>
	 *
	 * @param results a {@link edu.uga.cs.clickminer.results.ClassificationResultList} object.
	 */
	public static String prepRData(ClassificationResultList results){
		String xvals = new String(), yvals = new String(), avals = new String();
		for(int i = 0; i < results.size(); i++){
			ClassificationResult result = results.get(i);
			xvals += result.getFalsePositiveRate();
			yvals += result.getTruePositiveRate();
			avals += result.getThreshold();
			if(i < results.size() - 1){
				xvals += ",";
				yvals += ",";
				avals += ",";
			}
		}
		return xvals + "\n" + yvals + "\n" + avals;
	}
	
	/**
	 * <p>printRData.</p>
	 *
	 * @param rData a {@link java.lang.String} object.
	 * @param outpath a {@link java.lang.String} object.
	 * @throws java.io.FileNotFoundException if any.
	 */
	public static void printRData(String rData, String outpath) 
			throws FileNotFoundException{
		PrintWriter writer = new PrintWriter(outpath);
		writer.write(rData);
		writer.close();
	}
}
