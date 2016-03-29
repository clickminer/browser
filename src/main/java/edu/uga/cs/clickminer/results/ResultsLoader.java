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
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pcap.reconst.http.HttpFlowParser;
import pcap.reconst.http.datamodel.RecordedHttpFlow;
import pcap.reconst.tcp.JpcapReconstructor;
import pcap.reconst.tcp.PacketReassembler;
import pcap.reconst.tcp.Reconstructor;
import pcap.reconst.tcp.TcpConnection;

import edu.uga.cs.clickminer.datamodel.Interaction;
import edu.uga.cs.clickminer.datamodel.ide.Click;
import edu.uga.cs.clickminer.graph.InteractionRefererGraphMapper;
import edu.uga.cs.clickminer.graph.builder.InteractionGraphBuilder;
import edu.uga.cs.clickminer.graph.builder.RefererGraphBuilder;
import edu.uga.cs.clickminer.graph.model.InteractionGraph;
import edu.uga.cs.clickminer.graph.model.RefererGraph;
import edu.uga.cs.clickminer.graph.pruner.InteractionGraphAdPruner;
import edu.uga.cs.clickminer.graph.pruner.InteractionGraphSocialMediaPruner;
import edu.uga.cs.clickminer.util.MutableURL;
import edu.uga.cs.clickminer.util.ResultsUtils.FileType;
import edu.uga.cs.json.JSONReader;

/**
 * <p>ResultsLoader class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ResultsLoader.java 901 2014-04-25 03:27:51Z cjneasbitt $Id
 */
public class ResultsLoader {

	private static final transient Log log = LogFactory.getLog(ResultsLoader.class);
	
	/**
	 * <p>loadMinedClicks.</p>
	 *
	 * @param ilog a {@link java.io.File} object.
	 * @param filterAds a boolean.
	 * @throws java.lang.Exception if any.
	 */
	public static List<Interaction> loadMinedClicks(File ilog, boolean filterAds) throws Exception{
		return ResultsLoader.loadMinedClicks(ilog, null, null, filterAds, -1);
	}
	
	/**
	 * <p>loadMinedClicks.</p>
	 *
	 * @param ilog a {@link java.io.File} object.
	 * @param file a {@link java.io.File} object.
	 * @param filterAds a boolean.
	 * @throws java.lang.Exception if any.
	 */
	public static List<Interaction> loadMinedClicks(File ilog, File file, FileType type, boolean filterAds) throws Exception{
		return ResultsLoader.loadMinedClicks(ilog, file, type, filterAds, -1);
	}
	
	/**
	 * <p>loadMinedClicks.</p>
	 *
	 * @param ilog a {@link java.io.File} object.
	 * @param filterAds a boolean.
	 * @param refererDelayThreshold a double.
	 * @throws java.lang.Exception if any.
	 */
	public static List<Interaction> loadMinedClicks(File ilog, boolean filterAds, double refererDelayThreshold) throws Exception {
		return ResultsLoader.loadMinedClicks(ilog, null, null, filterAds, refererDelayThreshold);
	}
	
	/**
	 * <p>loadMinedClicks.</p>
	 *
	 * @param ilog a {@link java.io.File} object.
	 * @param file a {@link java.io.File} object.
	 * @param filterAds a boolean.
	 * @param refererDelayThreshold a double.
	 * @throws java.lang.Exception if any.
	 */
	public static List<Interaction> loadMinedClicks(File ilog, File file, FileType type, boolean filterAds, double refererDelayThreshold) throws Exception{
		List<Interaction> interactions = loadMinedClicksGraph(ilog, file, type,  filterAds).extractInteractions();
		if(refererDelayThreshold >= 0){
			ResultsLoader.filterInteractionsByRefererDelay(refererDelayThreshold, interactions);
		}
		return interactions;
	}
	
	/**
	 * <p>loadMinedClicksGraph.</p>
	 * 
	 *
	 * @param ilog a {@link java.io.File} object.
	 * @param filterAds a boolean.
	 * @throws java.lang.Exception if any.
	 */
	public static InteractionGraph loadMinedClicksGraph(File ilog, boolean filterAds) throws Exception{
		return loadMinedClicksGraph(ilog, null, null, filterAds);
	}
	
	/**
	 * <p>loadMinedClicksGraph.</p>
	 *
	 * @param ilog a {@link java.io.File} object.
	 * @param file a {@link java.io.File} object.
	 * @param filterAds a boolean.
	 * @throws java.lang.Exception if any.
	 */
	public static InteractionGraph loadMinedClicksGraph(File ilog
			, File file
			, FileType type
			,  boolean filterAds) throws Exception{
		if(log.isDebugEnabled()){
			log.debug("Loading mined clicks.");
		}
		
		InteractionGraph igraph = new InteractionGraphBuilder(ilog).getGraph();		
		if(file != null){
			RefererGraph rgraph = new RefererGraphBuilder(file, type).getGraph();
			InteractionRefererGraphMapper.augmentInteractionGraph(igraph, rgraph);
		}
		
		if(filterAds){
			new InteractionGraphAdPruner().pruneGraph(igraph);
			new InteractionGraphSocialMediaPruner().pruneGraph(igraph);
		}
		
		if(log.isDebugEnabled()){
			log.debug("Mined clicks loaded.");
		}

		return igraph;
	}
	
	/**
	 * <p>filterInteractionsByRefererDelay.</p>
	 *
	 * @param thresh a double.
	 * @param mclicks a {@link java.util.List} object.
	 */
	public static void filterInteractionsByRefererDelay(double thresh, List<Interaction> mclicks){		
		List<Interaction> filtered = new ArrayList<Interaction>();
		for(Interaction record : mclicks){
			double delay = record.getDelayFromReferer();
			if(delay > 0 && delay < thresh){
				filtered.add(record);
				if(log.isDebugEnabled()){
					log.debug("Filtering with referer delay " + delay +  " " + record.getInteractionUrl());
				}
			}
		}
		mclicks.removeAll(filtered);
	}
	
	/**
	 * <p>loadRecordedClicks.</p>
	 *
	 * @param clog a {@link java.io.File} object.
	 * @throws java.lang.Exception if any.
	 */
	public static List<Click> loadRecordedClicks(File clog) throws Exception{
		return loadRecordedClicks(clog, null);
	}
	
	//loads only those clicks whose url appears in the pcap
	/**
	 * <p>loadRecordedClicks.</p>
	 *
	 * @param clog a {@link java.io.File} object.
	 * @param pcap a {@link java.io.File} object.
	 * @throws java.lang.Exception if any.
	 */
	public static List<Click> loadRecordedClicks(File clog, File pcap) throws Exception{
		List<Click> retval;
		JSONReader<Click> creader = new JSONReader<Click>();
		retval = creader.read(clog, Click.class);
		if(pcap != null){
			List<Click> remove = new ArrayList<Click>();
			List<UtilURL> urls = getUrls(pcap);
			for(Click c : retval){
				try{
					UtilURL temp = new ResultsLoader().new UtilURL(c.getAbsoluteTargetUrl());
					if(!urls.contains(temp) && !c.isLoadedFromCache()){
						if(log.isDebugEnabled()){
							log.debug("Removing " + temp);
						}
						remove.add(c);
					}
				} catch (MalformedURLException e){
					if(log.isDebugEnabled()){
						log.debug("Filtering recorded click with malformed url. " 
								+ c.getAbsoluteTargetUrl(), e);
					}
					remove.add(c);
				}
			}
			
			
			retval.removeAll(remove);
		}
		return retval;
	}
	
	private static List<UtilURL> getUrls(File pcapfile) throws Exception{
		List<UtilURL> retval = new ArrayList<UtilURL>();
		Reconstructor reconstructor = new JpcapReconstructor(
				new PacketReassembler());
		HttpFlowParser http = new HttpFlowParser(reconstructor.reconstruct(pcapfile
				.getAbsolutePath()));
		Map<TcpConnection, List<RecordedHttpFlow>> flowMap = http.parse();
		for (TcpConnection conn : flowMap.keySet()) {
			for (RecordedHttpFlow output : flowMap.get(conn)) {
				try {
					retval.add(new ResultsLoader().new UtilURL(output.getRequest().getUrl()));
				} catch (MalformedURLException e) {
					if(log.isDebugEnabled()){
						log.debug("Skipping flow due to malformed request url. " 
								+ output.getRequest().getUrl(), e);
					}
				}
			}
		}
		return retval;
	}
	
	private class UtilURL extends MutableURL {
				
		public UtilURL(String url) throws MalformedURLException {
			super(url);
		}

		@Override
		public boolean equals(Object obj){
			if(obj instanceof UtilURL){
				UtilURL temp = (UtilURL)obj;
				return super.equals(obj) || this.approxEquals(temp);
			}
			return false;
		}
	}
	
}
