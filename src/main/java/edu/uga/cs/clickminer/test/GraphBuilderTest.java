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
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uga.cs.clickminer.graph.InteractionRefererGraphMapper;
import edu.uga.cs.clickminer.graph.builder.InteractionGraphBuilder;
import edu.uga.cs.clickminer.graph.builder.RefererGraphBuilder;
import edu.uga.cs.clickminer.graph.export.InteractionGraphExporter;
import edu.uga.cs.clickminer.graph.export.RefererGraphExporter;
import edu.uga.cs.clickminer.graph.model.InteractionGraph;
import edu.uga.cs.clickminer.graph.model.RefererGraph;
import edu.uga.cs.clickminer.graph.pruner.InteractionGraphAdPruner;
import edu.uga.cs.clickminer.graph.pruner.InteractionGraphSocialMediaPruner;
import edu.uga.cs.clickminer.graph.pruner.RefererGraphAdPruner;
import edu.uga.cs.clickminer.util.ResultsUtils.FileType;

/**
 * <p>GraphBuilderTest class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: GraphBuilderTest.java 901 2014-04-25 03:27:51Z cjneasbitt $Id
 */
public class GraphBuilderTest {
	
	private static Log log = LogFactory.getLog(GraphBuilderTest.class);

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) throws Exception{
		//String resultsdir = "/home/cjneasbi/clickminer/test_traces/user14_nocache";
		//String resultsdir = "/home/cjneasbi/clickminer/test_traces/session_1395955236";
		//graphBuilderTest_1(resultsdir);
		//graphBuilderTest_2(resultsdir);
		//graphBuilderTest_3(resultsdir);
		//graphBuilderTest_4();
		//graphBuilderTest_5(resultsdir);
		graphBuilderTest_6();
	}
	
	/**
	 * <p>graphBuilderTest_1.</p>
	 *
	 * @param resultsdir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public static void graphBuilderTest_1(String resultsdir) throws Exception{
		if(log.isInfoEnabled()){
			log.info("Creating graph builder.");
		}
		InteractionGraphBuilder builder = new InteractionGraphBuilder(resultsdir);
		if(log.isInfoEnabled()){
			log.info("Graph builder created.");
			log.info("Getting graph.");
		}
		InteractionGraph graph = builder.getGraph();
		InteractionGraphExporter exporter = new InteractionGraphExporter();
		exporter.graphToFile(graph, new File("/home/cjneasbi/Desktop"));
	}
	
	/**
	 * <p>graphBuilderTest_2.</p>
	 *
	 * @param resultsdir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public static void graphBuilderTest_2(String resultsdir) throws Exception{
		if(log.isInfoEnabled()){
			log.info("Creating graph builder.");
		}
		RefererGraphBuilder builder = new RefererGraphBuilder(resultsdir, FileType.PCAP);
		if(log.isInfoEnabled()){
			log.info("Graph builder created.");
			log.info("Getting graph.");
		}
		RefererGraph graph = builder.getGraph();
		RefererGraphExporter exporter = new RefererGraphExporter();
		exporter.graphToFile(graph, new File("/home/cjneasbi/Desktop"));
	}
	
	/**
	 * <p>graphBuilderTest_3.</p>
	 *
	 * @param resultsdir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public static void graphBuilderTest_3(String resultsdir) throws Exception{
		if(log.isInfoEnabled()){
			log.info("Creating interaction graph builder.");
		}
		InteractionGraphBuilder builder = new InteractionGraphBuilder(resultsdir);
		if(log.isInfoEnabled()){
			log.info("Builder created.");
			log.info("Getting interaction graph.");
		}
		InteractionGraph igraph = builder.getGraph();
		if(log.isInfoEnabled()){
			log.info("Graph created.");
			log.info("Creating referer graph builder.");
		}
		RefererGraphBuilder builder2 = new RefererGraphBuilder(resultsdir, FileType.PCAP);
		if(log.isInfoEnabled()){
			log.info("Builder created.");
			log.info("Getting referer graph.");
		}
		RefererGraph rgraph = builder2.getGraph();
		if(log.isInfoEnabled()){
			log.info("Graph created.");
			log.info("Augmenting interaction graph.");
		}
		InteractionRefererGraphMapper.augmentInteractionGraph(igraph, rgraph);
		InteractionGraphExporter exporter = new InteractionGraphExporter();
		exporter.graphToFile(igraph, "/home/cjneasbi/Desktop/before.dot");
		
		InteractionGraphAdPruner pruner = new InteractionGraphAdPruner();
		pruner.pruneGraph(igraph);
		exporter.graphToFile(igraph, "/home/cjneasbi/Desktop/after.dot");
		
		InteractionGraphSocialMediaPruner pruner2 = new InteractionGraphSocialMediaPruner();
		pruner2.pruneGraph(igraph);
		exporter.graphToFile(igraph, "/home/cjneasbi/Desktop/after2.dot");
		
	}
	
	/**
	 * <p>graphBuilderTest_4.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public static void graphBuilderTest_4() throws Exception{
		RefererGraphBuilder builder = new RefererGraphBuilder(
				new File("/home/cjneasbi/clickminer/test_traces/user17_cache/traffic_trace.pcap")
				, FileType.PCAP);
		Map<String, List<Integer>> redirections = builder.findRedirections();
		Map<String, List<Integer>> responses = builder.findRedirectionResponses(redirections.keySet());
		for(String url : redirections.keySet()){
			System.out.println("Url: " + url);
			System.out.println("Redirections: " + redirections.get(url));
			System.out.print("Responses: ");
			if(responses.containsKey(url)){
				System.out.println(responses.get(url));
			}
		}
		List<Pair<Integer, Integer>> links = builder.linkRedirections(redirections, responses);
		System.out.println("Links:");
		for(Pair<Integer, Integer> link : links){
			System.out.println(link);
		}
		List<List<Integer>> chains = builder.connectRedirectionLinks(links);
		System.out.println("Chains:");
		for(List<Integer> chain : chains){
			System.out.println(chain);
		}
	}
	
	/**
	 * <p>graphBuilderTest_5.</p>
	 *
	 * @param resultsdir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public static void graphBuilderTest_5(String resultsdir) throws Exception{
		RefererGraphBuilder builder = new RefererGraphBuilder(resultsdir, FileType.PCAP);
		RefererGraph graph = builder.getGraph();
		RefererGraphExporter exporter = new RefererGraphExporter();
		exporter.graphToFile(graph, "/home/cjneasbi/Desktop/refgraph.dot");
		RefererGraphAdPruner pruner = new RefererGraphAdPruner();
		pruner.pruneGraph(graph);
	}
	
	/**
	 * <p>graphBuilderTest_5.</p>
	 *
	 * @param resultsdir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public static void graphBuilderTest_6() throws Exception{
		RefererGraphBuilder builder = new RefererGraphBuilder(
				new File("/home/cjneasbi/workspace/clickminer-webeventsextract/test/data/test.json")
				, FileType.JSON);
		RefererGraph graph = builder.getGraph();
		RefererGraphExporter exporter = new RefererGraphExporter();
		exporter.graphToFile(graph, "/home/cjneasbi/Desktop/refgraph.dot");
		RefererGraphAdPruner pruner = new RefererGraphAdPruner();
		pruner.pruneGraph(graph);
	}
	
}
