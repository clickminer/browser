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

/**
 * <p>ExperimentUtils class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ExperimentUtils.java 843 2013-10-03 16:26:11Z cjneasbitt $Id
 */
public class ExperimentUtils {
	
	/**
	 * <p>getLgRefererDelayThresholds.</p>
	 */
	public static double[] getLgRefererDelayThresholds(){
		double[] retval = {0.142, 0.2, 0.283, 0.4, 0.566, 0.8, 1.132, 1.6, 2.263, 
			3.2, 4.525, 6.4, 9.051, 12.8, 18.101, 25.6, 36.204, 51.2};
		return retval;
	}
		
}
