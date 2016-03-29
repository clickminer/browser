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
package edu.uga.cs.clickminer.graph.provider;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.ext.ComponentAttributeProvider;

import edu.uga.cs.clickminer.graph.model.AbstractInteractionVertex;
import edu.uga.cs.clickminer.graph.model.AdChainInteractionVertex;
import edu.uga.cs.clickminer.graph.model.InferredInteractionVertex;
import edu.uga.cs.clickminer.graph.model.MinedInteractionVertex;
import edu.uga.cs.clickminer.graph.model.MissingInteractionVertex;
import edu.uga.cs.clickminer.graph.pruner.InteractionGraphAdBlockBasedPruner;
import edu.uga.cs.clickminer.graph.pruner.InteractionGraphAdPruner;
import edu.uga.cs.clickminer.graph.pruner.InteractionGraphSocialMediaPruner;

/**
 * <p>InteractionRecordVertexAttributeProvider class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: InteractionRecordVertexAttributeProvider.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class InteractionRecordVertexAttributeProvider implements
		ComponentAttributeProvider<AbstractInteractionVertex> {

	//private final List<Integer> addressBars;
	//private final List<Integer> falsePositives;
	//private final List<Integer> matches;
	private final InteractionGraphAdBlockBasedPruner adMatcher, socialMatcher;
	private final long refererDelayThreshold;
	
	private transient static Log log = LogFactory.getLog(InteractionRecordVertexAttributeProvider.class);

	/**
	 * <p>Constructor for InteractionRecordVertexAttributeProvider.</p>
	 *
	 * @param refererDelayThreshold a long.
	 * @throws java.lang.Exception if any.
	 */
	public InteractionRecordVertexAttributeProvider(long refererDelayThreshold) throws Exception{
		this.refererDelayThreshold = refererDelayThreshold;
		adMatcher = new InteractionGraphAdPruner();
		socialMatcher = new InteractionGraphSocialMediaPruner();
	}
	
	/**
	 * <p>Constructor for InteractionRecordVertexAttributeProvider.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public InteractionRecordVertexAttributeProvider()
			throws Exception {
		this(-1);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getComponentAttributes(
			AbstractInteractionVertex arg0) {
		Map<String, String> retval = new HashMap<String, String>();
		
		if(arg0 instanceof MinedInteractionVertex){
			retval.put("color", "green");
		} else if(arg0 instanceof InferredInteractionVertex){
			retval.put("color", "orange");
		} else if(arg0 instanceof AdChainInteractionVertex){
			retval.put("color", "red");
		} else {
			retval.put("color", "yellow");
		}
		
		try {
			if(!(arg0 instanceof MissingInteractionVertex) && 
					(adMatcher.matches(arg0.getUrl()) || socialMatcher.matches(arg0.getUrl()))){
				retval.put("color", "red");
			}
		} catch (Exception e) {
			if(log.isErrorEnabled()){
				log.error("", e);
			}
		}

		return retval;
	}


	/**
	 * <p>underRefererDelayThresh.</p>
	 *
	 * @param vert a {@link edu.uga.cs.clickminer.graph.model.MinedInteractionVertex} object.
	 */
	public boolean underRefererDelayThresh(MinedInteractionVertex vert) {
		return vert.getInteractionRecord().getRequest().getDelayFromReferer() < this.refererDelayThreshold;

	}

}
