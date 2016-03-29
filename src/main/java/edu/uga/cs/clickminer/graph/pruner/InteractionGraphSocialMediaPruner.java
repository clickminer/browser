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

import java.io.InputStream;

import edu.uga.cs.adblock.AdBlockRuleMatcher;
import edu.uga.cs.adblock.AdBlockRuleParser;

/**
 * <p>InteractionGraphSocialMediaPruner class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: InteractionGraphSocialMediaPruner.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class InteractionGraphSocialMediaPruner extends
		InteractionGraphAdBlockBasedPruner {

	/**
	 * <p>Constructor for InteractionGraphSocialMediaPruner.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public InteractionGraphSocialMediaPruner() throws Exception {
		super();
	}

	/** {@inheritDoc} */
	@Override
	protected AdBlockRuleMatcher initMatcher() throws Exception {
		InputStream stream = InteractionGraphSocialMediaPruner.class
		.getClassLoader().getResourceAsStream("fanboy-social.txt");
		return new AdBlockRuleMatcher(AdBlockRuleParser.parseRules(stream));	
	}

	/** {@inheritDoc} */
	@Override
	public boolean matches(String url){
		AdBlockRuleMatcher matcher = getMatcher();
		return matcher.isBlocked(url) || matcher.isBlockedFull2LD(url);
	}

}
