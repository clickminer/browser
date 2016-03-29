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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pcap.reconst.http.datamodel.RecordedHttpRequestMessage;
import edu.uga.cs.clickminer.RCI;
import edu.uga.cs.clickminer.datamodel.Interaction;
import edu.uga.cs.clickminer.datamodel.ide.Click;
import edu.uga.cs.clickminer.graph.builder.RefererGraphBuilder;
import edu.uga.cs.clickminer.graph.export.InteractionGraphExporter;
import edu.uga.cs.clickminer.graph.model.InteractionGraph;
import edu.uga.cs.clickminer.graph.model.RefererGraph;
//import edu.uga.cs.clickminer.graph.pruner.GraphPruner;
//import edu.uga.cs.clickminer.graph.pruner.RefererGraphAdPruner;
//import edu.uga.cs.clickminer.graph.pruner.RefererGraphDelayPruner;
//import edu.uga.cs.clickminer.graph.pruner.RefererGraphHeaderPruner;
//import edu.uga.cs.clickminer.graph.pruner.RefererGraphMissingPruner;
//import edu.uga.cs.clickminer.graph.pruner.RefererGraphSocialMediaPruner;
import edu.uga.cs.clickminer.util.MutableURL;
import edu.uga.cs.clickminer.util.OctaveUtils;
import edu.uga.cs.clickminer.util.RUtils;
import edu.uga.cs.clickminer.util.ResultsUtils.FileType;

/**
 * <p>ResultsPlotter class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ResultsPlotter.java 912 2014-05-07 22:02:54Z cjneasbitt $Id
 */
public class ResultsPlotter {

	private static final double[] refererDelayThresh = { 0.142, 0.2, 0.283,
			0.4, 0.566, 0.8, 1.132, 1.6, 2.263, 3.2, 4.525, 6.4, 9.051, 12.8,
			18.101, 25.6, 36.204, 51.2 };

	private final List<Click> recordedClicks;
	private final List<Interaction> minedClicks;
	private final InteractionGraph igraph;
	private final File sourceTrace;
	private final FileType type;
	private final boolean pruneAds;

	private static final transient Log log = LogFactory
			.getLog(ResultsPlotter.class);

	/**
	 * <p>Constructor for ResultsPlotter.</p>
	 *
	 * @param ilogPath a {@link java.lang.String} object.
	 * @param clogPath a {@link java.lang.String} object.
	 * @param filePath a {@link java.lang.String} object.
	 * @param augmentMined a boolean.
	 * @param pruneAds a boolean.
	 * @throws java.lang.Exception if any.
	 */
	public ResultsPlotter(String ilogPath, String clogPath, String filePath,
			FileType type, boolean augmentMined, boolean pruneAds) throws Exception {
		this(new File(ilogPath), new File(clogPath), new File(filePath), type,
				augmentMined, pruneAds);
	}

	/**
	 * <p>Constructor for ResultsPlotter.</p>
	 *
	 * @param ilog a {@link java.io.File} object.
	 * @param clog a {@link java.io.File} object.
	 * @param sourceTrace a {@link java.io.File} object.
	 * @param augmentMined a boolean.
	 * @param pruneAds a boolean.
	 * @throws java.lang.Exception if any.
	 */
	public ResultsPlotter(File ilog, File clog, File file, FileType type,
			boolean augmentMined, boolean pruneAds) throws Exception {
		if (augmentMined) {
			igraph = ResultsLoader.loadMinedClicksGraph(ilog, file, type, pruneAds);
		} else {
			igraph = ResultsLoader.loadMinedClicksGraph(ilog, pruneAds);	
		}
		minedClicks = igraph.extractInteractions();
		recordedClicks = ResultsLoader.loadRecordedClicks(clog, file);
		this.sourceTrace = file;
		this.pruneAds = pruneAds;
		this.type = type;
	}

	private List<ClassificationResult> generateClickminerPlotData(int totalFlows)
			throws Exception {
		List<ClassificationResult> retval = new ArrayList<ClassificationResult>();
		if (log.isInfoEnabled()) {
			log.info("Generating clickminer plot data.");
		}
		for (double thresh : refererDelayThresh) {
			ComparisonResult result = ResultsComparator.compare(recordedClicks,
					minedClicks, thresh);
			retval.add(getClickminerIterationResult(thresh, result, totalFlows));
			if (log.isInfoEnabled()) {
				log.info("Testing threshold: " + thresh + " complete.\n"
						+ retval.get(retval.size() - 1));
			}
		}
		if (log.isInfoEnabled()) {
			log.info("Clickminer plot data generated.");
		}
		return retval;
	}

	// http://www.gwumc.edu/library/tutorials/studydesign101/formulas.html
	private List<ClassificationResult> generateNaivePlotData(
			RefererGraphBuilder builder) throws Exception {
		List<ClassificationResult> retval = new ArrayList<ClassificationResult>();
		if (log.isInfoEnabled()) {
			log.info("Generating naive plot data.");
		}

		/*
		RefererGraphDelayPruner delayPruner = new RefererGraphDelayPruner();
		List<GraphPruner<RefererGraph>> pruners = new ArrayList<GraphPruner<RefererGraph>>();
		pruners.add(delayPruner);
		pruners.add(new RefererGraphMissingPruner());
		pruners.add(new RefererGraphHeaderPruner());
		if (pruneAds) {
			pruners.add(new RefererGraphAdPruner());
			pruners.add(new RefererGraphSocialMediaPruner());
		}

		List<ClassificationResult> retval = new ArrayList<ClassificationResult>();
		if (log.isDebugEnabled()) {
			log.debug("Creating graph builder.");
			log.debug("Getting matchable recorded clicks.");
		}
		List<Click> recordedClicks = getMatchableRecordedClicks();
		*/
		RCI rci = new RCI(builder);
		for (double thresh : refererDelayThresh) {
			//RefererGraph graph = builder.getGraph();
			Pair<RefererGraph, RefererGraph> graphs = rci.applyRCI(thresh, pruneAds);
			List<RecordedHttpRequestMessage> prunedRequests = graphs.getLeft()
					.getRequests();

			/*delayPruner.setTimeDeltaThresh(thresh);
			for (GraphPruner<RefererGraph> p : pruners) {
				p.pruneGraph(graph);
			}*/

			List<RecordedHttpRequestMessage> remainingRequests = graphs.getRight()
					.getRequests(); // TP + FP
			prunedRequests.removeAll(remainingRequests); // TN + FN

			List<Click> leftoverRecordedClicks = new ArrayList<Click>(
					recordedClicks);
			List<Pair<RecordedHttpRequestMessage, Click>> reqMatches = getMatchingRequests(
					recordedClicks, remainingRequests);
			List<RecordedHttpRequestMessage> matchingRequests = new ArrayList<RecordedHttpRequestMessage>(); // TP
			for (Pair<RecordedHttpRequestMessage, Click> reqMatch : reqMatches) {
				matchingRequests.add(reqMatch.getLeft());
				// only match against the recorded clicks that weren't already
				// matched from remainingRequests
				leftoverRecordedClicks.remove(reqMatch.getRight());
			}

			if (log.isInfoEnabled()) {
				String buf = "";
				for (Click c : leftoverRecordedClicks) {
					buf += c.getAbsoluteTargetUrl() + "\n";
				}
				log.info("Remaining clicks\n" + buf);
			}

			/*
			 * //FIXME getMatchingRequests is missing several matches
			 * List<Pair<RecordedHttpRequestMessage, Click>> misreqMatches =
			 * getMatchingRequests(leftoverRecordedClicks, prunedRequests);
			 * List<RecordedHttpRequestMessage> mismatchingRequests = new
			 * ArrayList<RecordedHttpRequestMessage>(); // FN
			 * for(Pair<RecordedHttpRequestMessage, Click> misreqMatch :
			 * misreqMatches){ mismatchingRequests.add(misreqMatch.getLeft()); }
			 */

			retval.add(getNaiveIterationResult(thresh, matchingRequests.size(),
					leftoverRecordedClicks.size(), remainingRequests.size(),
					prunedRequests.size()));
			if (log.isInfoEnabled()) {
				log.info("Testing threshold: " + thresh + " complete.\n"
						+ retval.get(retval.size() - 1));
			}
		}
		if (log.isInfoEnabled()) {
			log.info("Naive plot data generated.");
		}
		return retval;
	}

	private List<Click> getMatchableRecordedClicks() throws Exception {
		List<Click> retval = new ArrayList<Click>();
		for (Click c : recordedClicks) {
			if (c.isMatchable()) {
				retval.add(c);
			}
		}
		return retval;
	}

	private List<Pair<RecordedHttpRequestMessage, Click>> getMatchingRequests(
			List<Click> recordedClicks,
			List<RecordedHttpRequestMessage> requests) {
		List<Pair<RecordedHttpRequestMessage, Click>> retval = new ArrayList<Pair<RecordedHttpRequestMessage, Click>>();
		List<RecordedHttpRequestMessage> temp = new ArrayList<RecordedHttpRequestMessage>(
				requests);
		for (Click c : recordedClicks) {
			RecordedHttpRequestMessage match = null;
			for (RecordedHttpRequestMessage posmatch : temp) {
				try {
					MutableURL posmatchurl = new MutableURL(posmatch.getUrl());
					MutableURL curl = new MutableURL(c.getAbsoluteTargetUrl());
					if(posmatchurl.equals(curl) || posmatchurl.approxEquals(curl)){
						match = posmatch;
						break;
					}
				} catch (MalformedURLException e) {
					if(log.isDebugEnabled()){
						log.debug("Error getting matching requests.", e);
					}
				}
			}
			if (match != null) {
				retval.add(new MutablePair<RecordedHttpRequestMessage, Click>(
						match, c));
				temp.remove(match);
			}
		}
		return retval;
	}

	private ClassificationResult getNaiveIterationResult(double thresh,
			int numMatchingRequests, int numMismatchingRequests,
			int numRemainingRequests, int numPrunedRequests) {
		return new ClassificationResult(thresh, numMatchingRequests,
				numRemainingRequests - numMatchingRequests, numPrunedRequests
						- numMismatchingRequests, numMismatchingRequests);
	}

	private ClassificationResult getClickminerIterationResult(double thresh,
			ComparisonResult result, int totalFlows) {
		ComparisonSummary summary = result.getSummary();
		int truePos = summary.getMinedMatchingPossibleClicks()
				+ summary.getMinedMatchingPossibleNonClick();
		int falsePos = summary.getTotalMinedInteractions() - truePos;
		int falseNeg = summary.getMatchableRecordedInteractions() - truePos;
		int trueNeg = totalFlows - truePos - falsePos - falseNeg;

		return new ClassificationResult(thresh, truePos, falsePos, trueNeg,
				falseNeg);
	}

	/**
	 * <p>outputResultsPlots.</p>
	 *
	 * @param outputdir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public void outputResultsPlots(String outputdir) throws Exception {
		Collection<ClassificationResultList> resultsets = generatePlotData(type);
	
		String plottitle = "Comparative Clickminer Results.";
		String prepath = new File(outputdir).getCanonicalPath()
				+ File.separatorChar;
		String device = "pdf";
		
		InteractionGraphExporter exporter = new InteractionGraphExporter();
		exporter.graphToFile(igraph, prepath + "click_graph.dot");

		OctaveUtils.printPlot(OctaveUtils.rocOctavePlot(resultsets, plottitle),
				prepath + "roc_plot.pdf", device);
		OctaveUtils.printPlot(
				OctaveUtils.sensSpecOctavePlot(resultsets, plottitle), prepath
						+ "sen_spec_plot.pdf", device);
		OctaveUtils.printPlot(
				OctaveUtils.fMeasureOctavePlot(resultsets, plottitle), prepath
						+ "fmeasure_plot.pdf", device);
		OctaveUtils.printPlot(
				OctaveUtils.truePosFalsePosOctavePlot(resultsets, plottitle),
				prepath + "tpr_fpr_plot.pdf", device);
		
		List<ClassificationResultList> resultlist = 
				new ArrayList<ClassificationResultList>(resultsets);
		ClassificationResultList temp = resultlist.get(0);
		RUtils.printRData(RUtils.prepRData(temp), prepath + temp.getSetName() + "_roc_rdata.dat");
		temp = resultlist.get(1);
		RUtils.printRData(RUtils.prepRData(temp), prepath + temp.getSetName() + "_roc_rdata.dat");
	}

	private Collection<ClassificationResultList> generatePlotData(FileType type)
			throws Exception {
		Set<ClassificationResultList> retval = new HashSet<ClassificationResultList>();
		RefererGraphBuilder builder = new RefererGraphBuilder(sourceTrace, type);
		retval.add(new ClassificationResultList(this
				.generateNaivePlotData(builder), "RCI"));
		retval.add(new ClassificationResultList(this
				.generateClickminerPlotData(builder.getGraph().totalFlows()),
				"ClickMiner"));
		return retval;
	}

}
