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
package edu.uga.cs.clickminer.graph.model;

import java.util.ArrayList;
import java.util.List;

import pcap.reconst.http.datamodel.RecordedHttpFlow;
import pcap.reconst.http.datamodel.RecordedHttpRequestMessage;
import pcap.reconst.http.datamodel.RecordedHttpResponse;

/**
 * <p>RedirectionChainVertex class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RedirectionChainVertex.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class RedirectionChainVertex extends FlowVertex {

	private static int VERTCOUNTER = 0;
	private static String IDTEXT = "REDIRECTION";
	private String id;
	//should be in order from start of chain to end
	private List<RecordedHttpFlow> flows = new ArrayList<RecordedHttpFlow>();
	
	/**
	 * <p>Constructor for RedirectionChainVertex.</p>
	 *
	 * @param flows a {@link java.util.List} object.
	 */
	public RedirectionChainVertex(List<RecordedHttpFlow> flows) {
		if(flows.size() > 0){
			this.flows.addAll(flows);
			this.id = IDTEXT + "_" + VERTCOUNTER++;
		} else {
			throw new RuntimeException("Flows size must be greater than 0");
		}
	}
	
	/**
	 * <p>add.</p>
	 *
	 * @param flow a {@link pcap.reconst.http.datamodel.RecordedHttpFlow} object.
	 */
	public void add(RecordedHttpFlow flow){
		flows.add(flow);
	}
	
	/**
	 * <p>size.</p>
	 */
	public int size(){
		return flows.size();
	}
	
	/** {@inheritDoc} */
	@Override
	public RecordedHttpRequestMessage getRequest() {
		return getFlow().getRequest();
	}
	
	/** {@inheritDoc} */
	@Override
	public RecordedHttpResponse getResponse() {
		return getFlow().getResponse();
	}
	
	/** {@inheritDoc} */
	@Override
	public AbstractFlowVertex copy() {
		return new RedirectionChainVertex(this.flows);
	}

	/** {@inheritDoc} */
	@Override
	public String getId() {
		return id;
	}

	/** {@inheritDoc} */
	@Override
	public String getUrl() {
		if(flows.size() > 0){
			return getFlow().getRequest().getUrl();
		}
		return null;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj){
		if(obj instanceof RedirectionChainVertex){
			RedirectionChainVertex vert = (RedirectionChainVertex)obj;
			return id.equals(vert.id) && flows.equals(vert.flows);
		}
		return false;
	}
		
	/** {@inheritDoc} */
	@Override
	public RecordedHttpFlow getFlow(){
		return flows.get(flows.size() - 1);
	}

}
