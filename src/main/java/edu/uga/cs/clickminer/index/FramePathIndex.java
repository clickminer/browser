/*
* Copyright (C) 2012 Chris Neasbitt
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
package edu.uga.cs.clickminer.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;

import edu.uga.cs.clickminer.datamodel.FramePath;
import edu.uga.cs.clickminer.datamodel.MitmHttpRequest;
import edu.uga.cs.clickminer.util.FrameUtils;

/**
 * <p>FramePathIndex class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: FramePathIndex.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class FramePathIndex {

	private final Map<String, Long> time_index = new HashMap<String, Long>();	
	private final Map<String, List<FramePathIndexEntry>> path_index = 
			new HashMap<String, List<FramePathIndexEntry>>();
	private final WebDriver wdriver;
	//private final Set<String> windowNeedsUpdate = new HashSet<String>(); 
	
	private static Log log = LogFactory.getLog(FramePathIndex.class);
	
	/**
	 * <p>Constructor for FramePathIndex.</p>
	 *
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 */
	public FramePathIndex(WebDriver wdriver){
		this.wdriver = wdriver;
	}
	
	/**
	 * <p>updateIndex.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 * @param timestamp a long.
	 */
	public void updateIndex(String windowHandle, long timestamp){
		wdriver.switchTo().window(windowHandle);
		List<FramePathIndexEntry> entryval = 
				new ArrayList<FramePathIndexEntry>();
		
		List<FramePath> framepaths = FrameUtils.findFramePaths(wdriver, null);
		for(FramePath path : framepaths){
			entryval.add(new FramePathIndexEntry(path, windowHandle, this));
		}
		setLeafFramePathEntries(entryval);
		path_index.put(windowHandle, entryval);
		time_index.put(windowHandle, timestamp);
		if(log.isInfoEnabled()){
			wdriver.switchTo().defaultContent();
			log.info("Updated window " + windowHandle + " with url " + wdriver.getCurrentUrl() + " to timestamp " + timestamp);
			String logvalue = new String();
			for(FramePathIndexEntry entry : entryval){
				logvalue += entry.toString() + "\n";
			}
			log.info("Window " + windowHandle + " contains the following paths.\n" + logvalue);
		}
		//removeNeedsUpdate(windowHandle);
	}
	
	/**
	 * <p>getMatchingFramePathsFromReferer.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 * @param req a {@link edu.uga.cs.clickminer.datamodel.MitmHttpRequest} object.
	 */
	public List<FramePathIndexEntry> getMatchingFramePathsFromReferer(
			String windowHandle, MitmHttpRequest req) {
		if (req.containsHeader("Referer")) {
			String referer = req.getFirstHeader("Referer").getValue();
			List<FramePathIndexEntry> sresult = this.getMatchingFramePaths(windowHandle, referer);
			if (sresult != null) {
				return sresult;
			}
		}
		return null;
	}
	
	/**
	 * <p>getMatchingFramePaths.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 * @param matchval a {@link java.lang.String} object.
	 */
	public List<FramePathIndexEntry> getMatchingFramePaths(String windowHandle, String matchval){
		if(this.windowExists(windowHandle)){
			List<FramePathIndexEntry> retval = new ArrayList<FramePathIndexEntry>();
			List<FramePathIndexEntry> entryval = path_index.get(windowHandle);
			for(FramePathIndexEntry entry : entryval){
				String last_path = entry.getFramePath().getRefUrl();
				if(matchval == null || (last_path != null && last_path.equals(matchval))){
					retval.add(entry);
				}
			}
			if(retval.size() > 0){
				return retval;
			}
		}
		return null;
	}
	
	/**
	 * <p>removeWindow.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 */
	public void removeWindow(String windowHandle){
		if(time_index.containsKey(windowHandle)){
			time_index.remove(windowHandle);
		}
		if(path_index.containsKey(windowHandle)){
			path_index.remove(windowHandle);
		}
		//removeNeedsUpdate(windowHandle);
	}
	
	
	/**
	 * <p>removeEntry.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 * @param entry a {@link edu.uga.cs.clickminer.index.FramePathIndexEntry} object.
	 */
	protected void removeEntry(String windowHandle, FramePathIndexEntry entry){
		if(path_index.containsKey(windowHandle)){
			List<FramePathIndexEntry> entries = path_index.get(windowHandle);
			if(entries.contains(entry)){
				entries.remove(entry);
			}
		}
	}
	
	/**
	 * <p>windowExists.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 */
	public boolean windowExists(String windowHandle){
		return path_index.containsKey(windowHandle);
	}
	
	/**
	 * <p>getTimestamp.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 */
	public long getTimestamp(String windowHandle){
		if(time_index.containsKey(windowHandle)){
			return time_index.get(windowHandle);
		}
		return -1;		
	}
	
	
	/**
	 * <p>getAllFramePaths.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 */
	public List<FramePath> getAllFramePaths(String windowHandle){
		if(this.windowExists(windowHandle)){
			List<FramePath> retval = new ArrayList<FramePath>();
			List<FramePathIndexEntry> entryval = path_index.get(windowHandle);
			for(FramePathIndexEntry tup : entryval){
				retval.add(tup.getFramePath());
			}
			return retval;
		}
		return null;
	}
	
	private void sortSizeDesc(List<FramePathIndexEntry> list){
		//sorts in descending order
		Collections.sort(list, new Comparator<FramePathIndexEntry>(){
			@Override
			public int compare(FramePathIndexEntry o1, FramePathIndexEntry o2) {
				return Integer.compare(o2.getFramePath().length(), o1.getFramePath().length());
			}
		});
	}
	
	private void setLeafFramePathEntries(List<FramePathIndexEntry> entries){
		Hashtable<String, Integer> subPathTable = new Hashtable<String,Integer>();
		Set<Integer> deleted = new HashSet<Integer>();
		
		//sort decending order by length
		sortSizeDesc(entries);
		
		//hash the sub paths
		int maxsize = entries.get(0).getFramePath().length();
		for(int i = 0; i < entries.size(); i++){
			FramePathIndexEntry pair = entries.get(i);
			if(pair.getFramePath().length() < maxsize){
				subPathTable.put(pair.toString(), i);
			}
		}
		
		//find the indexes of the non-leaf paths from the hash table
		for(int i = 0; i < entries.size(); i++){
			FramePathIndexEntry pair = entries.get(i);
			int pairsize = pair.getFramePath().length();
			if(pairsize < maxsize && !deleted.contains(i)){
				for(int len = 1; len <= pairsize; len++){
					Integer match = subPathTable.get(pair.getFramePath().pathString(len));
					if(match != null){
						deleted.add(match);
					}
				}
			}
		}
		
		//return the indexes of remaining leaf paths
		for(int i = 0; i < entries.size(); i++){
			if(!deleted.contains(i)){
				entries.get(i).setLeafPath(true);
			}
		}
	}
	
	
	/**
	 * <p>getLeafFramePaths.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 */
	public List<FramePathIndexEntry> getLeafFramePaths(String windowHandle){
		List<FramePathIndexEntry> retval = new ArrayList<FramePathIndexEntry>();
		if(windowExists(windowHandle)){
			List<FramePathIndexEntry> entryval = path_index.get(windowHandle);
			for(FramePathIndexEntry entry : entryval){
				if(entry.isLeafPath()){
					retval.add(entry);
				}
			}
		}
		return retval;
	}
	
	/*public boolean needsUpdate(String windowHandle){
		return windowNeedsUpdate.contains(windowHandle);
	}
	
	protected void addNeedsUpdate(String windowHandle){
		windowNeedsUpdate.add(windowHandle);
	}
	
	private void removeNeedsUpdate(String windowHandle){
		windowNeedsUpdate.remove(windowHandle);
	}*/
	
	/**
	 * <p>isStale.</p>
	 *
	 * @param windowHandle a {@link java.lang.String} object.
	 */
	public boolean isStale(String windowHandle){
		boolean retval = false;
		List<FramePathIndexEntry> entries = getLeafFramePaths(windowHandle);
		try{
			long start = 0;
			if(log.isDebugEnabled()){
				start = System.currentTimeMillis();
			}
			for(FramePathIndexEntry entry : entries){
				FrameUtils.traverseFramePath(wdriver, entry.getFramePath());
			}
			if(log.isDebugEnabled()){
				log.debug("Traversing frame paths took " + (System.currentTimeMillis() - start) + "ms.");
			}
		} catch (StaleElementReferenceException e){
			if(log.isInfoEnabled()){
				log.info("FramePathIndex contains stale elements.");
			}
			retval = true;
		}
		return retval;
	}
}
