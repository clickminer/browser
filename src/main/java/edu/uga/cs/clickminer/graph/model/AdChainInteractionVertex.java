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

import edu.uga.cs.clickminer.datamodel.Interaction;

/**
 * <p>AdChainInteractionVertex class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: AdChainInteractionVertex.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class AdChainInteractionVertex extends AbstractInteractionVertex implements HasInteraction {

	private static int VERTCOUNTER = 0;
	private static String IDTEXT = "ADCHAIN";
	private String id;
	private List<Interaction> requests = 
			new ArrayList<Interaction>();
	
	/**
	 * <p>Constructor for AdChainInteractionVertex.</p>
	 *
	 * @param req a {@link edu.uga.cs.clickminer.datamodel.Interaction} object.
	 */
	public AdChainInteractionVertex(Interaction req) {
		this.requests.add(req);
		this.id = IDTEXT + "_" + VERTCOUNTER++;
	}
	
	/**
	 * <p>Constructor for AdChainInteractionVertex.</p>
	 *
	 * @param requests a {@link java.util.List} object.
	 */
	public AdChainInteractionVertex(List<Interaction> requests) {
		if(requests.size() > 0){
			this.requests.addAll(requests);
			this.id = IDTEXT + "_" + VERTCOUNTER++;
		} else {
			throw new RuntimeException("Request size must be greater than 0");
		}
	}
	
	/**
	 * <p>add.</p>
	 *
	 * @param req a {@link edu.uga.cs.clickminer.datamodel.Interaction} object.
	 */
	public void add(Interaction req){
		requests.add(req);
	}

	/** {@inheritDoc} */
	@Override
	public String getId() {
		return id;
	}

	/** {@inheritDoc} */
	@Override
	public String getUrl() {
		return requests.get(0).getInteractionUrl();
	}

	/** {@inheritDoc} */
	@Override
	public Interaction getInteraction() {
		return requests.get(0);
	}

}
