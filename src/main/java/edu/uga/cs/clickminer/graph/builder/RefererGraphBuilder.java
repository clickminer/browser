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
package edu.uga.cs.clickminer.graph.builder;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;

import pcap.reconst.http.HttpFlowParser;
import pcap.reconst.http.datamodel.RecordedHttpFlow;
import pcap.reconst.http.datamodel.RecordedHttpRequestMessage;
import pcap.reconst.http.datamodel.RecordedHttpResponse;
import pcap.reconst.tcp.JpcapReconstructor;
import pcap.reconst.tcp.PacketReassembler;
import pcap.reconst.tcp.Reconstructor;
import pcap.reconst.tcp.TcpConnection;
import edu.uga.cs.clickminer.graph.model.AbstractFlowVertex;
import edu.uga.cs.clickminer.graph.model.AbstractRefererEdge;
import edu.uga.cs.clickminer.graph.model.MissingFlowEdge;
import edu.uga.cs.clickminer.graph.model.MissingFlowVertex;
import edu.uga.cs.clickminer.graph.model.RedirectionChainVertex;
import edu.uga.cs.clickminer.graph.model.RefererEdge;
import edu.uga.cs.clickminer.graph.model.RefererGraph;
import edu.uga.cs.clickminer.graph.model.FlowVertex;
import edu.uga.cs.clickminer.util.HttpFlowSerializer;
import edu.uga.cs.clickminer.util.MutableURL;
import edu.uga.cs.clickminer.util.ResultsUtils;
import edu.uga.cs.clickminer.util.ResultsUtils.FileType;

/**
 * <p>RefererGraphBuilder class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RefererGraphBuilder.java 901 2014-04-25 03:27:51Z cjneasbitt $Id
 */
public class RefererGraphBuilder implements DirectedGraphBuilder<RefererGraph> {

	private List<RecordedHttpFlow> flows;
	private RefererGraph orig = null;
	
	private static final Set<Integer> redirectStatusCodes;
	static {
		redirectStatusCodes = new HashSet<Integer>();
		redirectStatusCodes.add(300);
		redirectStatusCodes.add(301);
		redirectStatusCodes.add(302);
		redirectStatusCodes.add(303);
		redirectStatusCodes.add(307);
		redirectStatusCodes.add(308);
	}
	
	private static transient final Log log = LogFactory.getLog(RefererGraphBuilder.class);

	/**
	 * <p>Constructor for RefererGraphBuilder.</p>
	 *
	 * @param resultsdir a {@link java.lang.String} object.
	 * @param filetype a {@link ResultsUtils.FileType} object.
	 * @throws java.lang.Exception if any.
	 */
	public RefererGraphBuilder(String resultsdir, ResultsUtils.FileType filetype) throws Exception {
		this.flows = getFlows(ResultsUtils.getTraceFile(resultsdir, filetype), filetype);
	}
	
	
	/**
	 * <p>Constructor for RefererGraphBuilder.</p>
	 *
	 * @param resultsdir a {@link java.io.File} object.
	 * @param filetype a {@link ResultsUtils.FileType} object.
	 * @throws java.lang.Exception if any.
	 */
	public RefererGraphBuilder(File tracefile, ResultsUtils.FileType filetype) throws Exception {
		this.flows = getFlows(tracefile, filetype);
	}

	/** {@inheritDoc} */
	@Override
	public RefererGraph getGraph() {
		if(orig == null){
			orig = new RefererGraph(AbstractRefererEdge.class);
			if(log.isDebugEnabled()){
				log.debug("Creating graph nodes.");
			}
			createGraphRequestNodes(orig);
			if(log.isDebugEnabled()){
				log.debug("Graph nodes created.");
				log.debug("Creating graph edges.");
			}
			
			createGraphRequestEdges(orig);
			if(log.isDebugEnabled()){
				log.debug("Graph edges created.");
				log.debug("Compressing redirection chains.");
			}
			compressRedirectionChains(orig);
			if(log.isDebugEnabled()){
				log.debug("Redirection chains compressed.");
			}
		}
		return orig.copy();
	}
	
	private List<RecordedHttpFlow> getFlows(File file, FileType type) throws Exception {
		List<RecordedHttpFlow> flows = null;
		switch(type){
		case JSON:
			flows = getJSONFlows(file);
			break;
		case PCAP:
			flows = getPcapFlows(file);
			break;
		default:
			throw new RuntimeException("Unknown file type");
		}
		sortFlows(flows);
		return flows;
	}
	
	private List<RecordedHttpFlow> getJSONFlows(File file) throws Exception{		
		List<RecordedHttpFlow> retval = new ArrayList<RecordedHttpFlow>();
		JSONArray json = new JSONArray(HttpFlowSerializer.read(file));
		for (int i = 0; i < json.length(); i++) {
			RecordedHttpFlow flow = HttpFlowSerializer.parseFlow(json.optJSONObject(i));
			retval.add(flow);
		}
		return retval;
	}
	
	private void sortFlows(List<RecordedHttpFlow> flows){
		Collections.sort(flows, new Comparator<RecordedHttpFlow>() {
			@Override
			public int compare(RecordedHttpFlow o1, RecordedHttpFlow o2) {
				// Sorts in decending order of end timestamp
				if (o1.getRequest().getEndTS() < o2.getRequest().getEndTS()) {
				//if (o1.getRequest().getStartTS() < o2.getRequest().getStartTS()) {
					return 1;
				} else if (o1.getRequest().getEndTS() == o2.getRequest().getEndTS()) {
				//} else if (o1.getRequest().getStartTS() == o2.getRequest().getStartTS()) {
					return 0;
				} else {
					return -1;
				}
			}
		});
	}
	
	private List<RecordedHttpFlow> getPcapFlows(File pcapfile) throws Exception{
		Reconstructor reconstructor = new JpcapReconstructor(
				new PacketReassembler());
		HttpFlowParser http = new HttpFlowParser(reconstructor.reconstruct(pcapfile
				.getAbsolutePath()));
		Map<TcpConnection, List<RecordedHttpFlow>> flowMap = http.parse();
		List<RecordedHttpFlow> retval = new ArrayList<RecordedHttpFlow>();
		for (TcpConnection conn : flowMap.keySet()) {
			for (RecordedHttpFlow output : flowMap.get(conn)) {
				retval.add(output);
			}
		}
		return retval;
	}

	private void createGraphRequestNodes(RefererGraph graph) {
		for (RecordedHttpFlow flow : flows) {
			graph.addVertex(new FlowVertex(flow));
		}
	}
	
	
	private void compressRedirectionChains(RefererGraph graph){
		Map<String, List<Integer>> redirects = findRedirections();
		List<List<Integer>> chains = connectRedirectionLinks(
				linkRedirections(redirects, findRedirectionResponses(redirects.keySet())));
		for(List<Integer> chain : chains){
			List<FlowVertex> vertChain = new ArrayList<FlowVertex>();
			List<RecordedHttpFlow> flowChain = new ArrayList<RecordedHttpFlow>();
			for(int index : chain){
				if(index >= 0){
					RecordedHttpFlow flow = flows.get(index);
					flowChain.add(flow);
					FlowVertex vert = graph.findMatchingVertex(flow);
					if(log.isErrorEnabled() && vert == null){
						log.error("Could not find vertex for flow number " + index);
					}
					vertChain.add(graph.findMatchingVertex(flow));
				}
			}
						
			RedirectionChainVertex newVert = new RedirectionChainVertex(flowChain);
			graph.addVertex(newVert);
			for(int i = 0; i < vertChain.size(); i++){
				FlowVertex vert = vertChain.get(i);
				if(!graph.containsVertex(vert)){
					if(log.isErrorEnabled()){
						log.error("Could not find vertex for flow " + chain.get(i) + " in graph.");
					}
				}
				if(i == 0){
					Set<AbstractRefererEdge> inEdges = graph.incomingEdgesOf(vert);
					for(AbstractRefererEdge inEdge : inEdges){
						graph.addEdge(graph.getEdgeSource(inEdge), newVert, inEdge.copy());
					}
				}
				
				Set<AbstractRefererEdge> outEdges = graph.outgoingEdgesOf(vert);
				for(AbstractRefererEdge outEdge : outEdges){
					graph.addEdge(newVert, graph.getEdgeTarget(outEdge), outEdge.copy());
				}
				graph.removeVertex(vert);
			}
			
			Set<AbstractRefererEdge> inedges = graph.incomingEdgesOf(newVert);
			if(inedges.size() > 0){
				AbstractFlowVertex replace = graph.getEdgeSource(inedges.iterator().next());
				Set<AbstractRefererEdge> outedges = graph.outgoingEdgesOf(newVert);
				for(AbstractRefererEdge outedge : outedges){
					graph.addEdge(replace, graph.getEdgeTarget(outedge), outedge.copy());
				}
			}
			graph.removeVertex(newVert);
		}
	}
	
	/**
	 * <p>connectRedirectionLinks.</p>
	 *
	 * @param links a {@link java.util.List} object.
	 */
	public List<List<Integer>> connectRedirectionLinks(
			List<Pair<Integer, Integer>> links){
		List<List<Integer>> retval = new ArrayList<List<Integer>>();
		Collections.sort(links, new Comparator<Pair<Integer, Integer>>(){
			@Override
			public int compare(Pair<Integer, Integer> o1,
					Pair<Integer, Integer> o2) {
				// Sorts in decending of link source
				if (o1.getLeft() < o2.getLeft()) {
					return 1;
				} else if (o1.getLeft() == o2.getLeft()) {
					return 0;
				} else {
					return -1;
				}
			}
		});
		for(Pair<Integer, Integer> link : links){
			boolean added = false;
			for(List<Integer> chain : retval){
				Integer lastChainLink = chain.get(chain.size() - 1);
				if(lastChainLink != -1 && lastChainLink.equals(link.getLeft())){
					if(!chain.contains(link.getRight())){
						chain.add(link.getRight());
					}
					added = true;
					break;
				}
			}
			if(!added){
				List<Integer> newChain = new ArrayList<Integer>();
				newChain.add(link.getLeft());
				newChain.add(link.getRight());
				retval.add(newChain);
			}
		}
		return retval;
	}
	
	
	/**
	 * <p>linkRedirections.</p>
	 *
	 * @param redirects a {@link java.util.Map} object.
	 * @param responses a {@link java.util.Map} object.
	 */
	public List<Pair<Integer, Integer>> linkRedirections(Map<String, List<Integer>> redirects,
			Map<String, List<Integer>> responses){
		List<Pair<Integer, Integer>> retval = new ArrayList<Pair<Integer, Integer>>();
		List<Double> delayValues = new ArrayList<Double>();
		Set<String> skipUrls = new HashSet<String>();
		for(String url : redirects.keySet()){
			if(responses.containsKey(url)){
				List<Integer> redirectsIndexes = redirects.get(url);
				List<Integer> responsesIndexes = responses.get(url);
				if(log.isDebugEnabled()){
					log.debug("\nUrl : " + url + "\nRedirects : " + redirectsIndexes + "\nResponses : " + responsesIndexes);
				}
				
				if(redirectsIndexes.size() == 1 && 
						redirectsIndexes.size() == responsesIndexes.size()){
					Pair<Double, Pair<Integer, Integer>> result = linkSingleRedirection(redirectsIndexes.get(0), 
							responsesIndexes.get(0));
					if(result != null){
						delayValues.add(result.getLeft());
						retval.add(result.getRight());
						skipUrls.add(url);
					}
				}
			} else {
				List<Integer> redList = redirects.get(url);
				for(int index : redList){
					retval.add(new ImmutablePair<Integer, Integer>(index, -1));
				}
				skipUrls.add(url);
			}
		}
		
		double avgDelta = 0;
		double maxDeltaDiff = Integer.MIN_VALUE;
		for(double delay : delayValues){
			double absdelay = Math.abs(delay);
			avgDelta += absdelay;
			if(absdelay > maxDeltaDiff){
				maxDeltaDiff = absdelay;
			}
		}
		avgDelta = avgDelta/delayValues.size();
		maxDeltaDiff = 2 * maxDeltaDiff - avgDelta;
		if(log.isDebugEnabled()){
			log.debug("Average delay: " + avgDelta  + " Max delay difference: " + maxDeltaDiff);
		}
		
		
		for(String url : redirects.keySet()){
			if(responses.containsKey(url) && !skipUrls.contains(url)){
				retval.addAll(linkRedirections(redirects.get(url), responses.get(url), avgDelta, maxDeltaDiff));
			}
		}
		
		return retval;
	}

	/**
	 * <p>linkSingleRedirection.</p>
	 *
	 * @param redirectIndex a int.
	 * @param responseIndex a int.
	 */
	public Pair<Double, Pair<Integer, Integer>> linkSingleRedirection(int redirectIndex, 
			int responseIndex){
		if(redirectIndex > responseIndex){
			RecordedHttpFlow redirect = flows.get(redirectIndex);
			RecordedHttpFlow response = flows.get(responseIndex);
			double delta = response.getRequest().getStartTS() - redirect.getResponse().getEndTS();
			return new ImmutablePair<Double, Pair<Integer, Integer>>(delta, new ImmutablePair<Integer, Integer>(
					redirectIndex, responseIndex));
		}		
		return null;
	}
	
	/**
	 * <p>linkRedirections.</p>
	 *
	 * @param redirects a {@link java.util.List} object.
	 * @param responses a {@link java.util.List} object.
	 */
	public List<Pair<Integer, Integer>> linkRedirections(List<Integer> redirects, 
			List<Integer> responses){
		return linkRedirections(redirects, responses, 0.5, 2.5);
	}
	
	
	/**
	 * <p>linkRedirections.</p>
	 *
	 * @param redirects a {@link java.util.List} object.
	 * @param responses a {@link java.util.List} object.
	 * @param delta a double.
	 */
	public List<Pair<Integer, Integer>> linkRedirections(List<Integer> redirects, 
			List<Integer> responses, double delta){
		return linkRedirections(redirects, responses, delta, 2.5);
	}
	
	
	/**
	 * <p>linkRedirections.</p>
	 *
	 * @param redirects a {@link java.util.List} object.
	 * @param responses a {@link java.util.List} object.
	 * @param delta a double.
	 * @param deltaDiffLimit a double.
	 */
	public List<Pair<Integer, Integer>> linkRedirections(List<Integer> redirects, 
			List<Integer> responses, double delta, double deltaDiffLimit){
		if(delta < 0 || deltaDiffLimit < 0){
			throw new RuntimeException("delta and deltalDiffLimit must be positive, " +
					"delta: " + delta + " deltaDiffLimit " + deltaDiffLimit);
		}
		List<Pair<Integer, Integer>> retval = new ArrayList<Pair<Integer, Integer>>();
		//minDiff, list (redirect, minDiff)
		List<Pair<Double, Pair<Integer, Integer>>> links = 
				new ArrayList<Pair<Double, Pair<Integer, Integer>>>();
		for(int req : redirects){
			double reqEnd = flows.get(req).getResponse().getEndTS();
			double minDiff = Double.MAX_VALUE;
			for(int resp : responses){
				if(req != resp){
					double respStart = flows.get(resp).getRequest().getStartTS();
					if(log.isDebugEnabled()){
						log.info("Redirect : " + req + " Response : " + resp + " RespStart : " + respStart + " ReqEnd: " + reqEnd);
					}
					if(log.isDebugEnabled()){
						log.info("Redirect : " + req + " Response : " + resp + " Delta: " + (respStart - reqEnd));
					}
					
					if(reqEnd < respStart){
						double diff = Math.abs(delta - Math.abs(respStart - reqEnd));
						if(diff < deltaDiffLimit){
							links.add(new ImmutablePair<Double, Pair<Integer, Integer>>(minDiff, 
									new ImmutablePair<Integer, Integer>(req, resp)));
						}
					}
				}
			}
		}

		Collections.sort(links, new Comparator<Pair<Double, Pair<Integer, Integer>>>(){
			@Override
			public int compare(Pair<Double, Pair<Integer, Integer>> arg0,
					Pair<Double, Pair<Integer, Integer>> arg1) {
				return arg0.getLeft().compareTo(arg1.getLeft());
			}
		});
		
		
		//Select the links by best deltaDiff
		List<Integer> takenRedirects = new ArrayList<Integer>();
		List<Integer> takenResponses = new ArrayList<Integer>();
		for(Pair<Double, Pair<Integer, Integer>> entry : links){
			Pair<Integer, Integer> posPair = entry.getRight();
			if(log.isDebugEnabled()){
				log.debug("Considering pos pair Redirect : " + posPair.getLeft() + " Response : " + posPair.getRight());
			}
			if(!takenRedirects.contains(posPair.getLeft()) && !takenResponses.contains(posPair.getRight())){
				retval.add(posPair);
				takenRedirects.add(posPair.getLeft());
				takenResponses.add(posPair.getRight());
			}
		}
		
		
		for(int redirect : redirects){
			if(!takenRedirects.contains(redirect)){
				retval.add(new ImmutablePair<Integer, Integer>(redirect, -1));
			}
		}
		return retval;
	}
	
	/**
	 * <p>findRedirectionResponses.</p>
	 *
	 * @param urls a {@link java.util.Set} object.
	 */
	public Map<String, List<Integer>> findRedirectionResponses(Set<String> urls){
		Map<String, List<Integer>> retval = new HashMap<String, List<Integer>>();
		for(int i = 0; i < flows.size(); i++){
			RecordedHttpRequestMessage req = flows.get(i).getRequest();
			String reqUrl = req.getUrl();
			if(urls.contains(reqUrl)){
				if(retval.containsKey(req.getUrl())){
					retval.get(reqUrl).add(i);
				} else {
					List<Integer> vals = new ArrayList<Integer>();
					vals.add(i);
					retval.put(reqUrl, vals);
				}
			}
		}
		return retval;
	}
	
	/**
	 * <p>findRedirections.</p>
	 */
	public Map<String, List<Integer>> findRedirections(){
		Map<String, List<Integer>> retval = new HashMap<String, List<Integer>>();
		for(int i = 0; i < flows.size(); i++){
			RecordedHttpResponse resp = flows.get(i).getResponse();
			if(resp != null && redirectStatusCodes.contains(
					resp.getStatusLine().getStatusCode())){
				String locurl = getLocationURL(flows.get(i));
				if(locurl != null){
					if(retval.containsKey(locurl)){
						retval.get(locurl).add(i);
					} else {
						List<Integer> vals = new ArrayList<Integer>();
						vals.add(i);
						retval.put(locurl, vals);
					}
				}
			}
		}
		return retval;
	}
	
	private String getLocationURL(RecordedHttpFlow flow){
		String retval = null;
		if(flow.getResponse().containsHeader("Location")){
			String locval = flow.getResponse()
					.getFirstHeader("Location").getValue();
			MutableURL url = null;
			try{
				url = new MutableURL(locval);
				retval = url.toString();
			} catch(MalformedURLException e){
				//The url is relative
				if(log.isDebugEnabled()){
					log.debug("Found possibly relative url " + locval, e);
				}
				if(flow.getRequest().containsHeader("Host")){
					String host = flow.getRequest().getFirstHeader("Host").getValue();
					try {
						url = new MutableURL("http://" + host + locval);
						retval = url.toString();
					} catch (MalformedURLException e1) {
						if(log.isErrorEnabled()){
							log.error("Can not reconstruct the absolute location url.", e);
						}
					}
					
				}
			}
		}
		return retval;
	}

	private void createGraphRequestEdges(RefererGraph graph) {		
		for (int i = 0; i < flows.size(); i++) {
			RecordedHttpRequestMessage childreq = flows.get(i).getRequest();
			if (childreq.containsHeader("Referer")) {
				String referer = childreq.getFirstHeader("Referer").getValue();
				FlowVertex childvert = graph.findMatchingVertex(childreq);
				boolean addedEdge = false;
				if(childvert != null){
					for (int j = i + 1; j < flows.size(); j++) {
						RecordedHttpRequestMessage posparentreq = flows.get(j).getRequest();
						if (childreq.getStartTS() > posparentreq.getEndTS()
								&& posparentreq.getUrl().equals(referer)) {
							
							FlowVertex parentvert = graph.findMatchingVertex(posparentreq);
							if(parentvert != null){
								double timeDelta = childreq.getStartTS()
										- posparentreq.getEndTS();
								graph.addEdge(parentvert, childvert, new RefererEdge(
										timeDelta));
								addedEdge = true;
								break;
							}
						}
					}
				}
				if (!addedEdge) {
					MissingFlowVertex missvert = graph.findMatchingVertex(referer);
					if (missvert == null) {
						missvert = new MissingFlowVertex(referer);
						graph.addVertex(missvert);
					}
					graph.addEdge(missvert, childvert, new MissingFlowEdge());
				}
			}
		}
	}
}
