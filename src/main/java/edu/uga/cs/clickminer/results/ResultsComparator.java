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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uga.cs.clickminer.datamodel.ElementLocator;
import edu.uga.cs.clickminer.datamodel.InferredInteraction;
import edu.uga.cs.clickminer.datamodel.Interaction;
import edu.uga.cs.clickminer.datamodel.ide.Click;
import edu.uga.cs.clickminer.util.MutableURL;

/**
 * <p>ResultsComparator class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ResultsComparator.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ResultsComparator {
	
	private static final transient Log log = LogFactory.getLog(ResultsComparator.class);
	
	/**
	 * <p>compare.</p>
	 *
	 * @param recordedClicks a {@link java.util.List} object.
	 * @param minedClicks a {@link java.util.List} object.
	 */
	public static ComparisonResult compare(List<Click> recordedClicks, List<Interaction> minedClicks){
		return compare(recordedClicks, minedClicks, -1);
	}
	
	/**
	 * <p>compare.</p>
	 *
	 * @param recordedClicks a {@link java.util.List} object.
	 * @param minedClicks a {@link java.util.List} object.
	 * @param refererDelayThreshold a double.
	 */
	public static ComparisonResult compare(List<Click> recordedClicks, List<Interaction> minedClicks, 
			double refererDelayThreshold){
		List<Interaction> mclicks = new ArrayList<Interaction>();
		if(refererDelayThreshold >= 0){
			for(int i = 0; i < minedClicks.size(); i++){
				mclicks.add(null);
			}
			Collections.copy(mclicks,minedClicks);
			ResultsLoader.filterInteractionsByRefererDelay(refererDelayThreshold, mclicks);
		} else {
			mclicks = minedClicks;
		}
		Map<Click, Pair<Interaction, Pair<Boolean, Boolean>>> mapping = 
				ResultsComparator.doComparison(recordedClicks, mclicks, refererDelayThreshold);
		return ResultsComparator.getResults(mapping, recordedClicks, mclicks);
	}
	
	private static ComparisonResult getResults(Map<Click, Pair<Interaction, Pair<Boolean, Boolean>>> mapping,
			List<Click> recordedClicks, List<Interaction> minedClicks){
		int totalConfirmedRecorded = 0;
		int totalMatchableRecorded = 0;
		int totalMatchingUrlConfirmed = 0;
		int totalMatchingUrlUnconfirmed = 0;
		int totalMatchingLocConfirmed = 0;
		int totalMatchingLocUnconfirmed = 0;
		
		List<ComparisonMatch> compMatches = new ArrayList<ComparisonMatch>();
		List<Integer> matchedRecordedClicks = new ArrayList<Integer>();
		
		for(Click c : recordedClicks){
			if(c.isMatchable()){
				totalMatchableRecorded++;
				if(c.isConfirmed()){
					totalConfirmedRecorded++;
				}
			}
			ComparisonMatch cmatch;
			if(mapping.containsKey(c)){
				Pair<Interaction, Pair<Boolean, Boolean>> match = mapping.get(c);
				boolean hasloc = !(match.getLeft().getLocators().isEmpty());
				
				int minedindex = minedClicks.indexOf(match.getLeft());
				matchedRecordedClicks.add(minedindex);
				
				cmatch = new ComparisonMatch(recordedClicks.indexOf(c),
						c.isMatchable(), c.isConfirmed(), minedindex, hasloc, 
						match.getRight().getLeft(), match.getRight().getRight());
				
					
				if(c.isConfirmed()){
					if(match.getRight().getLeft()){
						totalMatchingUrlConfirmed++;
					}
					if(match.getRight().getRight()){
						totalMatchingLocConfirmed++;
					}
				} else {
					if(match.getRight().getLeft()){
						totalMatchingUrlUnconfirmed++;
					}
					if(match.getRight().getRight()){
						totalMatchingLocUnconfirmed++;
					}
				}
				
			} else {
				cmatch = new ComparisonMatch(recordedClicks.indexOf(c),
						c.isMatchable(), c.isConfirmed());
			}
			compMatches.add(cmatch);
		}
		
		List<Integer> falsePositiveClicks = new ArrayList<Integer>();
		List<Integer> addressBarInteractions = new ArrayList<Integer>();
		List<Integer> confirmedClicks = new ArrayList<Integer>();
		List<Integer> unconfirmedClicks = new ArrayList<Integer>();
		List<Integer> augmentedClicks = new ArrayList<Integer>();
		
		for(int i = 0; i < minedClicks.size(); i++){
			Interaction minedClick = minedClicks.get(i);
			if(!matchedRecordedClicks.contains(i) && 
					!(minedClick.getLocators().isEmpty())){
					falsePositiveClicks.add(i);
			}
			if(minedClick.possibleAddressBarInteraction()){
				addressBarInteractions.add(i);
			}
			if(minedClick instanceof InferredInteraction){
				augmentedClicks.add(i);
			} else {
				if(minedClick.getLocators().isEmpty()){
					unconfirmedClicks.add(i);
				} else {
					confirmedClicks.add(i);
				}
			}
		}

		ComparisonSummary summary = new ComparisonSummary(recordedClicks.size(),
				totalMatchableRecorded, totalConfirmedRecorded, minedClicks.size(),
				addressBarInteractions.size(), falsePositiveClicks.size(), 
				totalMatchingUrlConfirmed, totalMatchingUrlUnconfirmed, 
				totalMatchingLocConfirmed, totalMatchingLocUnconfirmed,
				confirmedClicks.size(), unconfirmedClicks.size(),
				augmentedClicks.size());
		
		return new ComparisonResult(summary, compMatches, falsePositiveClicks, addressBarInteractions,
				confirmedClicks, unconfirmedClicks, augmentedClicks);
	}
		
	// key = recorded interaction, value = (matching mined interaction, (matchTargetURL, matchTargetLocator))
	private static Map<Click, Pair<Interaction, Pair<Boolean, Boolean>>> doComparison(
			List<Click> recordedClicks,  List<Interaction> minedClicks, double refererDelayThreshold){
		Map<Click, Pair<Interaction, Pair<Boolean, Boolean>>> mapping = 
				new HashMap<Click, Pair<Interaction, Pair<Boolean, Boolean>>>();
		
		List<Interaction> tempMinedClicks = new ArrayList<Interaction>(minedClicks);
		for(Click recordedClick : recordedClicks){
			
			if(!recordedClick.isMatchable()){
				if(log.isInfoEnabled()){
					if(log.isDebugEnabled()){
						log.debug("Skipping comparison for recorded click " + recordedClicks.indexOf(recordedClick) 
								+ " . Click is not matchable.");
					}
				}
				continue;
			}
			
			List<String> ctargetloc = recordedClick.getTargetLocator();	
			Map<Interaction, Pair<Boolean, Boolean>> posmatches =
					new HashMap<Interaction, Pair<Boolean, Boolean>>();
			
			for(Interaction minedClick : tempMinedClicks){
				boolean matchTargetURL = false;
				boolean matchTargetLocator = false;
				
				try{
					MutableURL minedClickUrl = new MutableURL(minedClick.getInteractionUrl());
					MutableURL recordedClickUrl = new MutableURL(recordedClick.getAbsoluteTargetUrl());
					if(minedClickUrl.equals(recordedClickUrl) || minedClickUrl.approxEquals(recordedClickUrl)){
						matchTargetURL = true;
						
						List<ElementLocator> locators = minedClick.getLocators();
						locloop:
						for(List<String> locator : locators){
							String ilastloc = locator.get(locator.size() - 1);
							for(int i = ctargetloc.size() - 1; i >= 0; i--){
								if(ilastloc.equals(ctargetloc.get(i))){
									matchTargetLocator = true;
									break locloop;
								}
							}
						}
					}
				} catch (MalformedURLException e) {
					if(log.isErrorEnabled()){
						log.error("Error performing comparison.", e);
					}
				}
				
				posmatches.put(minedClick, new MutablePair<Boolean, Boolean>(matchTargetURL, matchTargetLocator));
			}
			
			Interaction bestmatch = null;
			for(Interaction r : posmatches.keySet()){
				Pair<Boolean, Boolean> matchprops = posmatches.get(r);
				if(matchprops.getLeft()){
					bestmatch = r;
					if(matchprops.getRight()){
						break;
					}
				}
			}
			
			if(bestmatch != null){
				mapping.put(recordedClick, new MutablePair<Interaction, Pair<Boolean, Boolean>>(bestmatch, 
						posmatches.get(bestmatch)));
				tempMinedClicks.remove(bestmatch);	
			}
		}
		
		return mapping;
	}

}
