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
 * <p>AdChainFlowVertex class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: AdChainFlowVertex.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class AdChainFlowVertex extends FlowVertex {

	private static int VERTCOUNTER = 0;
	private static String IDTEXT = "ADCHAIN";
	private String id;
	private List<RecordedHttpFlow> flows = new ArrayList<RecordedHttpFlow>();
	
	/**
	 * <p>Constructor for AdChainFlowVertex.</p>
	 *
	 * @param flow a {@link pcap.reconst.http.datamodel.RecordedHttpFlow} object.
	 */
	public AdChainFlowVertex(RecordedHttpFlow flow) {
		this.flows.add(flow);
		this.id = IDTEXT + "_" + VERTCOUNTER++;
	}
	
	/**
	 * <p>Constructor for AdChainFlowVertex.</p>
	 *
	 * @param flows a {@link java.util.List} object.
	 */
	public AdChainFlowVertex(List<RecordedHttpFlow> flows){
		if(flows.size() > 0){
			this.flows.addAll(flows);
			this.id = IDTEXT + "_" + VERTCOUNTER++;
		} else {
			throw new RuntimeException("Flows size must be greater than 0");
		}
	}
	
	/**
	 * <p>Constructor for AdChainFlowVertex.</p>
	 *
	 * @param vert a {@link edu.uga.cs.clickminer.graph.model.AdChainFlowVertex} object.
	 */
	public AdChainFlowVertex(AdChainFlowVertex vert){
		this(vert.flows);
	}
	
	/**
	 * <p>add.</p>
	 *
	 * @param flow a {@link pcap.reconst.http.datamodel.RecordedHttpFlow} object.
	 */
	public void add(RecordedHttpFlow flow){
		flows.add(flow);
	}
	
	/** {@inheritDoc} */
	@Override
	public RecordedHttpFlow getFlow(){
		return flows.get(0);
	}

	/** {@inheritDoc} */
	@Override
	public RecordedHttpRequestMessage getRequest() {
		return flows.get(0).getRequest();
	}
	
	/** {@inheritDoc} */
	@Override
	public RecordedHttpResponse getResponse() {
		return flows.get(0).getResponse();
	}
	
	
	/** {@inheritDoc} */
	@Override
	public String getId() {
		return id;
	}

	/** {@inheritDoc} */
	@Override
	public AbstractFlowVertex copy() {
		return new AdChainFlowVertex(this);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj){
		if(obj instanceof AdChainFlowVertex){
			AdChainFlowVertex vert = (AdChainFlowVertex)obj;
			return flows.equals(vert.flows) && id.equals(vert.id);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String getUrl() {
		return flows.get(0).getRequest().getUrl();
	}

}
