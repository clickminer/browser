package edu.uga.cs.clickminer.expr;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pcap.reconst.http.datamodel.RecordedHttpRequestMessage;


import edu.uga.cs.clickminer.datamodel.ide.Click;
import edu.uga.cs.clickminer.graph.builder.RefererGraphBuilder;
import edu.uga.cs.clickminer.graph.model.RefererGraph;
import edu.uga.cs.clickminer.graph.pruner.GraphPruner;
import edu.uga.cs.clickminer.graph.pruner.RefererGraphDelayPruner;
import edu.uga.cs.clickminer.graph.pruner.RefererGraphHeaderPruner;
import edu.uga.cs.clickminer.graph.pruner.RefererGraphMissingPruner;
import edu.uga.cs.clickminer.results.ClassificationResult;
import edu.uga.cs.clickminer.results.ClassificationResultList;
import edu.uga.cs.clickminer.util.ExperimentUtils;
import edu.uga.cs.clickminer.util.MutableURL;
import edu.uga.cs.clickminer.util.OctaveUtils;
import edu.uga.cs.clickminer.util.ResultsUtils;
import edu.uga.cs.json.JSONReader;

/**
 * <p>RefererGraphThresholdExpr class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RefererGraphThresholdExpr.java 901 2014-04-25 03:27:51Z cjneasbitt $Id
 */
public class RefererGraphThresholdExpr {

	private static transient final Log log = LogFactory.getLog(RefererGraphThresholdExpr.class);
	
	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) throws Exception{
		List<String> output = new ArrayList<String>();
		output.add(experiment1(args[0]));
		output.add(experiment2(args[0]));
		output.add(experiment3(args[0]));
		output.add(experiment4(args[0]));
		output.add(experiment5(args[0]));
		
		for(String out : output){
			System.out.println(out + "\n");
		}
	}
	
	private static List<Click> getMatchableRecordedClicks(String resultsdir) throws Exception{
		List<Click> clicks = new JSONReader<Click>().read(
				ResultsUtils.getRecordedClicks(resultsdir), Click.class);
		List<Click> temp = new ArrayList<Click>();
		for(Click c : clicks){
			if(!c.isMatchable()){
				temp.add(c);
			}
		}
		clicks.removeAll(temp);
		return clicks;	
	}
	
	private static List<Pair<RecordedHttpRequestMessage, Click>> getMatchingRequests(List<Click> recordedClicks, List<RecordedHttpRequestMessage> requests){
		List<Pair<RecordedHttpRequestMessage, Click>> retval = new ArrayList<Pair<RecordedHttpRequestMessage, Click>>();
		List<RecordedHttpRequestMessage> temp = new ArrayList<RecordedHttpRequestMessage>(requests);
		for(Click c : recordedClicks){
			RecordedHttpRequestMessage match = null;
			for(RecordedHttpRequestMessage posmatch : temp){
				try {
					MutableURL posmatchurl = new MutableURL(posmatch.getUrl());
					MutableURL curl = new MutableURL(c.getAbsoluteTargetUrl());
					if(posmatchurl.equals(curl) || posmatchurl.approxEquals(curl)){
						match = posmatch;
						break;
					}
				} catch (MalformedURLException e) {
					if(log.isErrorEnabled()){
						log.error("Error getting matching requests.", e);
					}
				}
			}
			if(match != null){
				retval.add(new MutablePair<RecordedHttpRequestMessage, Click>(match,c));
				temp.remove(match);
			}
		}
		return retval;
	}
	
	private static Pair<Map<String, List<String>>, Map<String, List<String>>> exclusiveHeaderFilters(){
		Map<String, List<String>> reqHeaders = new HashMap<String, List<String>>();
		List<String> temp = new ArrayList<String>();
		temp.add("XMLHttpRequest");
		reqHeaders.put("X-Requested-With", temp);
		temp = new ArrayList<String>();
		temp.add("application/ocsp-request");
		reqHeaders.put("Content-Type", temp);
		
		Map<String, List<String>> respHeaders = new HashMap<String, List<String>>();
		temp = new ArrayList<String>();
		temp.add("text/css");
		temp.add("text/javascript");
		temp.add("application/x-javascript");
		temp.add("application/javascript");
		temp.add("application/x-shockwave-flash");
		temp.add("image/x-icon");
		respHeaders.put("Content-Type", temp);
		
		return new MutablePair<Map<String, List<String>>, Map<String, List<String>>>(reqHeaders, respHeaders);
	}
	
	//filter everything but html responses
	private static Pair<Map<String, List<String>>, Map<String, List<String>>> inclusiveHeaderFilters(){
		Map<String, List<String>> respHeaders = new HashMap<String, List<String>>();
		List<String>temp = new ArrayList<String>();
		temp.add("text/html");
		temp.add("application/xhtml+xml");
		respHeaders.put("Content-Type", temp);
		
		return new MutablePair<Map<String, List<String>>, Map<String, List<String>>>(null, respHeaders);
	}
	
	private static RefererGraphHeaderPruner prunerExperiment1() throws Exception{
		Pair<Map<String, List<String>>, Map<String, List<String>>> filters = exclusiveHeaderFilters();
		return new RefererGraphHeaderPruner(filters.getLeft(), filters.getRight());
	}
	
	private static RefererGraphHeaderPruner prunerExperiment2() throws Exception{
		Pair<Map<String, List<String>>, Map<String, List<String>>> filters = inclusiveHeaderFilters();
		return new RefererGraphHeaderPruner(filters.getLeft(), false, filters.getRight(), false);
	}
	
	private static RefererGraphHeaderPruner prunerExperiment3() throws Exception{
		Pair<Map<String, List<String>>, Map<String, List<String>>> filters = exclusiveHeaderFilters();
		return new RefererGraphHeaderPruner(true, filters.getLeft(), filters.getRight());
	}
	
	private static RefererGraphHeaderPruner prunerExperiment4() throws Exception{
		Pair<Map<String, List<String>>, Map<String, List<String>>> filters = inclusiveHeaderFilters();
		return new RefererGraphHeaderPruner(true, filters.getLeft(), false, filters.getRight(), false);
	}
	
	private static RefererGraphHeaderPruner prunerExperiment5() throws Exception{
		return new RefererGraphHeaderPruner();
	}
	
	private static List<ClassificationResult> runExperiment(RefererGraphHeaderPruner pruner, 
			double[] thresholds, String resultsdir) throws Exception{
		// http://www.gwumc.edu/library/tutorials/studydesign101/formulas.html
		RefererGraphDelayPruner delayPruner = new RefererGraphDelayPruner();
		List<GraphPruner<RefererGraph>> pruners = new ArrayList<GraphPruner<RefererGraph>>();
		pruners.add(delayPruner);
		pruners.add(new RefererGraphMissingPruner());
		pruners.add(pruner);
		
		
		List<ClassificationResult> retval = new ArrayList<ClassificationResult>();
		if(log.isInfoEnabled()){
			log.info("Creating graph builder.");
		}
		RefererGraphBuilder builder = new RefererGraphBuilder(resultsdir, ResultsUtils.FileType.PCAP);
		if(log.isInfoEnabled()){
			log.info("Getting matchable recorded clicks.");
		}
		List<Click> recordedClicks = getMatchableRecordedClicks(resultsdir);
		for(double thresh : thresholds){
			if(log.isInfoEnabled()){
				log.info("Testing threshold: " + thresh);
			}
			RefererGraph graph = builder.getGraph();			
			List<RecordedHttpRequestMessage> prunedRequests = graph.getRequests();

			delayPruner.setTimeDeltaThresh(thresh);
			for(GraphPruner<RefererGraph> p : pruners){
				p.pruneGraph(graph);
			}
			
			List<RecordedHttpRequestMessage> remainingRequests = graph.getRequests(); //a + c
			prunedRequests.removeAll(remainingRequests); //b + d
			
			
			List<Click> leftoverRecordedClicks = new ArrayList<Click>(recordedClicks);
			List<Pair<RecordedHttpRequestMessage, Click>> reqMatches = getMatchingRequests(recordedClicks, remainingRequests);
			List<RecordedHttpRequestMessage> matchingRequests = new ArrayList<RecordedHttpRequestMessage>(); // a
			for(Pair<RecordedHttpRequestMessage, Click> reqMatch : reqMatches){
				matchingRequests.add(reqMatch.getLeft());
				//only match against the recorded clicks that weren't already matched from remainingRequests
				leftoverRecordedClicks.remove(reqMatch.getRight());
			}
			
			List<Pair<RecordedHttpRequestMessage, Click>> misreqMatches = getMatchingRequests(leftoverRecordedClicks, prunedRequests);
			List<RecordedHttpRequestMessage> mismatchingRequests = new ArrayList<RecordedHttpRequestMessage>(); // b
			for(Pair<RecordedHttpRequestMessage, Click> misreqMatch : misreqMatches){
				mismatchingRequests.add(misreqMatch.getLeft());
			}
			
			/*
			long truePos = matchingRequests.size();
			long falseNeg = mismatchingRequests.size();
			long falsePos = remainingRequests.size() - truePos;
			long trueNeg = prunedRequests.size() - falseNeg;
			*/
			
			retval.add(new ClassificationResult(thresh, matchingRequests.size(), remainingRequests.size() - matchingRequests.size(),
					prunedRequests.size() - mismatchingRequests.size(), mismatchingRequests.size()));
			if(log.isInfoEnabled()){
				log.info("Testing threshold: " + thresh + " complete.\n" + retval.get(retval.size() - 1));
			}
		}
		return retval;
	}
	
	/**
	 * <p>experiment1.</p>
	 *
	 * @param resultsdir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public static String experiment1(String resultsdir) throws Exception{
		if(log.isInfoEnabled()){
			log.info("Starting experiment 1.");
		}
		
		List<ClassificationResult> result = runExperiment(prunerExperiment1(), 
				ExperimentUtils.getLgRefererDelayThresholds(), resultsdir);
		Collection<ClassificationResultList> resultsets = new HashSet<ClassificationResultList>();
		resultsets.add(new ClassificationResultList(result, "clickminer"));
		
		return OctaveUtils.sensSpecOctavePlot(resultsets,
				"Referer Delay Classification: Exclusive Header Filtering");
	}
	
	/**
	 * <p>experiment2.</p>
	 *
	 * @param resultsdir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public static String experiment2(String resultsdir) throws Exception{
		if(log.isInfoEnabled()){
			log.info("Starting experiment 2.");
		}
		
		List<ClassificationResult> result = runExperiment(prunerExperiment2(), 
				ExperimentUtils.getLgRefererDelayThresholds(), resultsdir);
		Collection<ClassificationResultList> resultsets = new HashSet<ClassificationResultList>();
		resultsets.add(new ClassificationResultList(result, "clickminer"));
		
		return OctaveUtils.sensSpecOctavePlot(resultsets,
				"Referer Delay Classification: Inclusive Header Filtering");
	}
	
	/**
	 * <p>experiment3.</p>
	 *
	 * @param resultsdir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public static String experiment3(String resultsdir) throws Exception{
		if(log.isInfoEnabled()){
			log.info("Starting experiment 3.");
		}
		
		List<ClassificationResult> result = runExperiment(prunerExperiment3(), 
				ExperimentUtils.getLgRefererDelayThresholds(), resultsdir);
		Collection<ClassificationResultList> resultsets = new HashSet<ClassificationResultList>();
		resultsets.add(new ClassificationResultList(result, "clickminer"));
		
		return OctaveUtils.sensSpecOctavePlot(resultsets,
				"Referer Delay Classification: Exclusive Header and Missing Referer Filtering");
	}
	
	/**
	 * <p>experiment4.</p>
	 *
	 * @param resultsdir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public static String experiment4(String resultsdir) throws Exception{
		if(log.isInfoEnabled()){
			log.info("Starting experiment 4.");
		}
		
		List<ClassificationResult> result = runExperiment(prunerExperiment4(), 
				ExperimentUtils.getLgRefererDelayThresholds(), resultsdir);
		Collection<ClassificationResultList> resultsets = new HashSet<ClassificationResultList>();
		resultsets.add(new ClassificationResultList(result, "clickminer"));
		
		return OctaveUtils.sensSpecOctavePlot(resultsets,
				"Referer Delay Classification: Inclusive Header and Missing Referer Filtering");
	}
	
	/**
	 * <p>experiment5.</p>
	 *
	 * @param resultsdir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public static String experiment5(String resultsdir) throws Exception{
		if(log.isInfoEnabled()){
			log.info("Starting experiment 5.");
		}
		
		List<ClassificationResult> result = runExperiment(prunerExperiment5(), 
				ExperimentUtils.getLgRefererDelayThresholds(), resultsdir);
		Collection<ClassificationResultList> resultsets = new HashSet<ClassificationResultList>();
		resultsets.add(new ClassificationResultList(result, "clickminer"));
		
		return OctaveUtils.sensSpecOctavePlot(resultsets,
				"Referer Delay Classification: No Filtering");
	}
}
