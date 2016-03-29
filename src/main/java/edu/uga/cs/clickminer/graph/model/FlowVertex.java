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

import pcap.reconst.http.datamodel.RecordedHttpFlow;
import pcap.reconst.http.datamodel.RecordedHttpRequestMessage;
import pcap.reconst.http.datamodel.RecordedHttpResponse;

/**
 * <p>FlowVertex class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: FlowVertex.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class FlowVertex extends AbstractFlowVertex {

	private static int VERTCOUNTER = 0;
	private static String IDTEXT = "FLOW";
	private RecordedHttpFlow flow;
	private String id;

	/**
	 * <p>Constructor for FlowVertex.</p>
	 */
	protected FlowVertex(){}
	
	/**
	 * <p>Constructor for FlowVertex.</p>
	 *
	 * @param flow a {@link pcap.reconst.http.datamodel.RecordedHttpFlow} object.
	 */
	public FlowVertex(RecordedHttpFlow flow) {
		this.flow = flow;
		this.id = IDTEXT + "_" + VERTCOUNTER++;
	}
	
	/**
	 * <p>Constructor for FlowVertex.</p>
	 *
	 * @param vert a {@link edu.uga.cs.clickminer.graph.model.FlowVertex} object.
	 */
	public FlowVertex(FlowVertex vert){
		this(vert.getFlow());
	}
	
	/**
	 * <p>Getter for the field <code>flow</code>.</p>
	 */
	public RecordedHttpFlow getFlow(){
		return flow;
	}

	/**
	 * <p>getRequest.</p>
	 */
	public RecordedHttpRequestMessage getRequest() {
		return flow.getRequest();
	}
	
	/**
	 * <p>getResponse.</p>
	 */
	public RecordedHttpResponse getResponse() {
		return flow.getResponse();
	}

	/** {@inheritDoc} */
	@Override
	public String getId() {
		return id;
	}

	/** {@inheritDoc} */
	@Override
	public AbstractFlowVertex copy() {
		return new FlowVertex(this);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj){
		if(obj instanceof FlowVertex){
			FlowVertex vert = (FlowVertex)obj;
			return flow.equals(vert.flow) && id.equals(vert.id);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String getUrl() {
		return flow.getRequest().getUrl();
	}

}
