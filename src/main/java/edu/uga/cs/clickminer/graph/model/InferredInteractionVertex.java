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

import edu.uga.cs.clickminer.datamodel.InferredInteraction;
import edu.uga.cs.clickminer.datamodel.Interaction;
import pcap.reconst.http.datamodel.RecordedHttpFlow;
import pcap.reconst.http.datamodel.RecordedHttpRequestMessage;
import pcap.reconst.http.datamodel.RecordedHttpResponse;

/**
 * <p>InferredInteractionVertex class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: InferredInteractionVertex.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class InferredInteractionVertex extends SingleInteractionVertex {

	private static int RECORDCOUNTER = 0;
	private static String IDTEXT = "INFERRED";
	
	private final String id;
	private final RecordedHttpFlow flow;
	private final double refererDelay;
	
	/**
	 * <p>Constructor for InferredInteractionVertex.</p>
	 *
	 * @param flow a {@link pcap.reconst.http.datamodel.RecordedHttpFlow} object.
	 * @param refererDelay a double.
	 */
	public InferredInteractionVertex(RecordedHttpFlow flow, double refererDelay) {
		this.id = IDTEXT + "_" + RECORDCOUNTER++;
		this.flow = flow;
		this.refererDelay = refererDelay;
	}
	
	/**
	 * <p>Getter for the field <code>refererDelay</code>.</p>
	 */
	public double getRefererDelay(){
		return refererDelay;
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
		return this.id;
	}

	/** {@inheritDoc} */
	@Override
	public String getUrl() {
		return flow.getRequest().getUrl();
	}

	/** {@inheritDoc} */
	@Override
	public double getStartTS() {
		return flow.getRequest().getStartTS();
	}

	/** {@inheritDoc} */
	@Override
	public double getEndTS() {
		return flow.getRequest().getEndTS();
	}

	/** {@inheritDoc} */
	@Override
	public Interaction getInteraction() {
		return new InferredInteraction(getRequest(), getRefererDelay());
	}

}
