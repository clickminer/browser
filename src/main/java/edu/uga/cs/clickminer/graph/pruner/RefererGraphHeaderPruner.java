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
package edu.uga.cs.clickminer.graph.pruner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;

import edu.uga.cs.clickminer.graph.model.AbstractFlowVertex;
import edu.uga.cs.clickminer.graph.model.RefererGraph;
import edu.uga.cs.clickminer.graph.model.FlowVertex;

/**
 * <p>RefererGraphHeaderPruner class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RefererGraphHeaderPruner.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class RefererGraphHeaderPruner implements GraphPruner<RefererGraph> {

	private static Log log = LogFactory.getLog(RefererGraphHeaderPruner.class);
	
	private final Map<String, List<String>> filteredRequestHeaders, filteredResponseHeaders;
	private final boolean exclusiveReqHeaderFilter, exclusiveRespHeaderFilter;
	private final boolean filterNoReferer;
	
	/**
	 * <p>Constructor for RefererGraphHeaderPruner.</p>
	 */
	public RefererGraphHeaderPruner(){
		filteredRequestHeaders = null;
		filteredResponseHeaders = null;
		exclusiveReqHeaderFilter = true;
		exclusiveRespHeaderFilter = true;
		filterNoReferer = false;
	}
	
	/**
	 * <p>Constructor for RefererGraphHeaderPruner.</p>
	 *
	 * @param filterNoReferer a boolean.
	 * @param filteredRequestHeaders a {@link java.util.Map} object.
	 * @param filteredResponseHeaders a {@link java.util.Map} object.
	 */
	public RefererGraphHeaderPruner(boolean filterNoReferer, Map<String, List<String>> filteredRequestHeaders, 
			Map<String, List<String>> filteredResponseHeaders){
		this(filterNoReferer, filteredRequestHeaders,true,filteredResponseHeaders,true);
	}
	
	/**
	 * <p>Constructor for RefererGraphHeaderPruner.</p>
	 *
	 * @param filteredRequestHeaders a {@link java.util.Map} object.
	 * @param filteredResponseHeaders a {@link java.util.Map} object.
	 */
	public RefererGraphHeaderPruner(Map<String, List<String>> filteredRequestHeaders, 
			Map<String, List<String>> filteredResponseHeaders){
		this(filteredRequestHeaders,true,filteredResponseHeaders,true);
	}
	
	/**
	 * <p>Constructor for RefererGraphHeaderPruner.</p>
	 *
	 * @param filteredRequestHeaders a {@link java.util.Map} object.
	 * @param exclusiveReqHeaderFilter a boolean.
	 * @param filteredResponseHeaders a {@link java.util.Map} object.
	 * @param exclusiveRespHeaderFilter a boolean.
	 */
	public RefererGraphHeaderPruner(Map<String, List<String>> filteredRequestHeaders, boolean exclusiveReqHeaderFilter,
			Map<String, List<String>> filteredResponseHeaders, boolean exclusiveRespHeaderFilter){
		this(false, filteredRequestHeaders, exclusiveReqHeaderFilter, filteredResponseHeaders, exclusiveRespHeaderFilter);
	}
	
	/**
	 * <p>Constructor for RefererGraphHeaderPruner.</p>
	 *
	 * @param filterNoReferer a boolean.
	 * @param filteredRequestHeaders a {@link java.util.Map} object.
	 * @param exclusiveReqHeaderFilter a boolean.
	 * @param filteredResponseHeaders a {@link java.util.Map} object.
	 * @param exclusiveRespHeaderFilter a boolean.
	 */
	public RefererGraphHeaderPruner(boolean filterNoReferer, Map<String, List<String>> filteredRequestHeaders, boolean exclusiveReqHeaderFilter,
			Map<String, List<String>> filteredResponseHeaders, boolean exclusiveRespHeaderFilter){
		this.filterNoReferer = filterNoReferer;
		this.filteredRequestHeaders = filteredRequestHeaders;
		this.filteredResponseHeaders = filteredResponseHeaders;
		this.exclusiveReqHeaderFilter = exclusiveReqHeaderFilter;
		this.exclusiveRespHeaderFilter = exclusiveRespHeaderFilter;
	}
	
	/** {@inheritDoc} */
	@Override
	public void pruneGraph(RefererGraph refgraph) {
		List<AbstractFlowVertex> flows;
		if(filterNoReferer){
			flows = this.noRefererFlows(refgraph);
			if(log.isDebugEnabled()){
				log.debug("Removing " + flows.size() + " no referer flows.");
			}
			refgraph.removeAllVertices(flows);
		}
		flows = this.headerFilteredFlows(refgraph);
		if(log.isDebugEnabled()){
			log.debug("Removing " + flows.size() + " header filtered flows.");
		}
		refgraph.removeAllVertices(flows);
		flows = this.statusFilteredFlows(refgraph);
		if(log.isDebugEnabled()){
			log.debug("Removing " + flows.size() + " status filtered flows.");
		}
		refgraph.removeAllVertices(flows);
	}
	
	private List<AbstractFlowVertex> noRefererFlows(RefererGraph refgraph){
		List<AbstractFlowVertex> retval = new ArrayList<AbstractFlowVertex>();
		for (AbstractFlowVertex vert : refgraph.vertexSet()) {
			if (vert instanceof FlowVertex) {
				FlowVertex reqvert = (FlowVertex)vert;
				if(!reqvert.getRequest().containsHeader("Referer")){
					retval.add(reqvert);
				}
			}
		}
		return retval;
	}
	
	private List<AbstractFlowVertex> statusFilteredFlows(RefererGraph refgraph){
		List<AbstractFlowVertex> retval = new ArrayList<AbstractFlowVertex>();
		for(AbstractFlowVertex abvert : refgraph.vertexSet()){
			if(abvert instanceof FlowVertex){
				FlowVertex vert = (FlowVertex)abvert;
				if(vert.getResponse() != null){
					int status = vert.getResponse().getStatusLine().getStatusCode();
					if(status == 204 || (status >= 400 && status <= 599) ){
						retval.add(abvert);
					}
				}
			}
		}
		return retval;
	}
	
	private List<AbstractFlowVertex> headerFilteredFlows(RefererGraph refgraph){
		List<AbstractFlowVertex> retval = new ArrayList<AbstractFlowVertex>();
		outer:
		for(AbstractFlowVertex abvert : refgraph.vertexSet()){
			if(abvert instanceof FlowVertex){
				FlowVertex vert = (FlowVertex)abvert;
				if(this.filteredRequestHeaders != null){
					for(String header : this.filteredRequestHeaders.keySet()){
						if(vert.getRequest() != null){
							List<String> headervals = this.filteredRequestHeaders.get(header);
							Header messageHeaderVal = vert.getRequest().getFirstHeader(header);
							boolean filter = false;
							if(this.exclusiveReqHeaderFilter && messageHeaderVal != null && 
									(headervals == null || headervals.contains(messageHeaderVal.getValue()))){
								filter = true;
							}
							if(!this.exclusiveReqHeaderFilter && messageHeaderVal != null && 
									(headervals != null && !headervals.contains(messageHeaderVal.getValue()))){
								filter = true;
							}
							if(filter){
								retval.add(abvert);
								continue outer;
							}
						}
					}
				}
				
				if(this.filteredResponseHeaders != null){
					for(String header : this.filteredResponseHeaders.keySet()){
						if(vert.getResponse() != null){
							List<String> headervals = this.filteredResponseHeaders.get(header);
							String messageHeaderVal = vert.getResponse().getFirstHeader(header).getValue();
							
							boolean filter = false;
							if(this.exclusiveRespHeaderFilter && messageHeaderVal != null && 
									(headervals == null || headervals.contains(messageHeaderVal))){
								filter = true;
							}
							if(!this.exclusiveRespHeaderFilter && messageHeaderVal == null &&
									(headervals != null && !headervals.contains(messageHeaderVal))){
								filter = true;
							}
							if(filter){
								retval.add(abvert);
								continue outer;
							}
						}
					}
				}
			}
		}
		return retval;
	}
}
