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
package edu.uga.cs.clickminer.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.uga.cs.clickminer.datamodel.Interaction;
import edu.uga.cs.clickminer.datamodel.ide.Click;
import edu.uga.cs.clickminer.util.ResultsUtils.FileType;

/**
 * <p>ResultsComparison class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ResultsComparison.java 901 2014-04-25 03:27:51Z cjneasbitt $Id
 */
public class ResultsComparison {

	private final List<Click> recordedClicks;
	private final List<Interaction> minedClicks;

	/**
	 * <p>Constructor for ResultsComparison.</p>
	 *
	 * @param ilog a {@link java.io.File} object.
	 * @param clog a {@link java.io.File} object.
	 * @param pcap a {@link java.io.File} object.
	 * @param augmentMined a boolean.
	 * @param filterAds a boolean.
	 * @throws java.lang.Exception if any.
	 */
	public ResultsComparison(File ilog, File clog, File tracefile, 
			FileType type, boolean augmentMined, 
			boolean filterAds) throws Exception {
		this(ilog, clog, tracefile, type, -1, augmentMined, filterAds);
	}

	/**
	 * <p>Constructor for ResultsComparison.</p>
	 *
	 * @param ilog a {@link java.io.File} object.
	 * @param clog a {@link java.io.File} object.
	 * @param file a {@link java.io.File} object.
	 * @param refererDelayThreshold a double.
	 * @param augmentMined a boolean.
	 * @param filterAds a boolean.
	 * @throws java.lang.Exception if any.
	 */
	public ResultsComparison(File ilog, File clog, File tracefile, FileType type,
			double refererDelayThreshold, boolean augmentMined, boolean filterAds) 
					throws Exception {
		if (augmentMined) {
			minedClicks = ResultsLoader.loadMinedClicks(ilog, tracefile, type, filterAds,
					refererDelayThreshold);
		} else {
			minedClicks = ResultsLoader.loadMinedClicks(ilog, filterAds,
					refererDelayThreshold);
		}
		recordedClicks = ResultsLoader.loadRecordedClicks(clog, tracefile);
	}

	private void outputResultsClicks(List<Click> rClicks,
			List<Interaction> mClicks, String outputdir) throws IOException {
		StringBuffer retval = new StringBuffer();
		retval.append("Recorded Clicks:\n");
		int confirmedcount = 0;
		for (Click c : rClicks) {
			if (c.isConfirmed()) {
				confirmedcount++;
			}
			retval.append("Click: " + rClicks.indexOf(c) + "\n");
			retval.append(c.toString() + "\n");
		}
		retval.append("Total Recorded Clicks: " + rClicks.size() + "\n");
		retval.append("Total Confirmed Recorded Clicks: " + confirmedcount
				+ "\n\n");

		retval.append("Mined Clicks:\n");
		for (Interaction r : mClicks) {
			retval.append("Click: " + mClicks.indexOf(r) + "\n");
			retval.append(r.toString() + "\n");
		}
		retval.append("Total Mined Clicks: " + mClicks.size() + "\n\n");

		FileWriter writer = new FileWriter(
				new File(outputdir).getCanonicalPath() + File.separatorChar
						+ new File("results_comparison_clicks.txt"));
		writer.write(retval.toString());
		writer.close();
	}

	/**
	 * <p>outputResults.</p>
	 *
	 * @param outputdir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public void outputResults(String outputdir) throws Exception {
		if (new File(outputdir).isDirectory()) {
			ComparisonResult result = ResultsComparator.compare(recordedClicks,
					minedClicks);
			outputResultsClicks(recordedClicks, minedClicks, outputdir);
			ComparisonResultWriter.write(result, outputdir);
		} else {
			throw new RuntimeException(outputdir + " is not a directory.");
		}
	}
}
