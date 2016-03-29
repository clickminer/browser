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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;

import edu.uga.cs.clickminer.datamodel.MitmHttpRequest;
import edu.uga.cs.clickminer.util.UsesCrypto;

/**
 * <p>RequestSearchIndex class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RequestSearchIndex.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class RequestSearchIndex extends UsesCrypto {

	private final Map<String, Map<String, TimestampFoundFramePathPair>> index = 
			new HashMap<String, Map<String, TimestampFoundFramePathPair>>();
	
	/**
	 * <p>Constructor for RequestSearchIndex.</p>
	 */
	public RequestSearchIndex(){
		super(LogFactory.getLog(RequestSearchIndex.class), "SHA-256");
	}
	
	/**
	 * <p>setEntry.</p>
	 *
	 * @param req a {@link edu.uga.cs.clickminer.datamodel.MitmHttpRequest} object.
	 * @param windowHandle a {@link java.lang.String} object.
	 * @param timestamp a long.
	 * @param foundFramePath a boolean.
	 */
	public void setEntry(MitmHttpRequest req, String windowHandle, 
			long timestamp, boolean foundFramePath){
		Map<String, TimestampFoundFramePathPair> entries;
		if(this.requestExists(req)){
			entries = this.getEntry(req);
		} else {
			entries = new HashMap<String, TimestampFoundFramePathPair>();
			index.put(this.hashRequest(req), entries);
		}
		entries.put(windowHandle, new TimestampFoundFramePathPair(timestamp, foundFramePath));
	}
	
	/**
	 * <p>entryExists.</p>
	 *
	 * @param req a {@link edu.uga.cs.clickminer.datamodel.MitmHttpRequest} object.
	 * @param windowHandle a {@link java.lang.String} object.
	 */
	public boolean entryExists(MitmHttpRequest req, String windowHandle){
		if(this.requestExists(req)){
			Map<String, TimestampFoundFramePathPair> entries = this.getEntry(req);
			return entries.containsKey(windowHandle);
		}
		return false;
	}
	
	private boolean requestExists(MitmHttpRequest req){
		return index.containsKey(this.hashRequest(req));
	}
	
	private Map<String, TimestampFoundFramePathPair> getEntry(MitmHttpRequest req){
		return index.get(this.hashRequest(req));
	}
	
	/**
	 * <p>getTimestamp.</p>
	 *
	 * @param req a {@link edu.uga.cs.clickminer.datamodel.MitmHttpRequest} object.
	 * @param windowHandle a {@link java.lang.String} object.
	 */
	public long getTimestamp(MitmHttpRequest req, String windowHandle){
		if(this.entryExists(req, windowHandle)){
			return this.getEntry(req).get(windowHandle).getTimestamp();
		}
		return -1L;
	}
	
	/**
	 * <p>foundFramePaths.</p>
	 *
	 * @param req a {@link edu.uga.cs.clickminer.datamodel.MitmHttpRequest} object.
	 * @param windowHandle a {@link java.lang.String} object.
	 */
	public boolean foundFramePaths(MitmHttpRequest req, String windowHandle){
		if(this.entryExists(req, windowHandle)){
			return this.getEntry(req).get(windowHandle).foundFramePath();
		}
		return false;
	}
	
	private String hashRequest(MitmHttpRequest req){
		StringBuffer buf = new StringBuffer();
		buf.append(req.getHost());
		buf.append(req.getPort());
		buf.append(req.getScheme());
		buf.append(req.getMethod());
		buf.append(req.getPath());
		buf.append(req.getContent());
		
		Header[] headers = req.getAllHeaders();
		Arrays.sort(headers, new Comparator<Header>(){
			@Override
			public int compare(Header o1, Header o2) {
				int val = o1.getName().compareTo(o2.getName());
				if(val == 0){
					val = o1.getValue().compareTo(o2.getValue());
				}
				return val;
			}
		});
		for(Header header : headers){
			buf.append(header.getName());
			buf.append(header.getValue());
		}
		
		return this.getHashValue(buf.toString());
	}
	
	private class TimestampFoundFramePathPair{

		private long timestamp;
		private boolean foundFramePathVal;
		
		public TimestampFoundFramePathPair(long timestamp, boolean foundFramePathVal) {
			this.timestamp = timestamp;
			this.foundFramePathVal = foundFramePathVal;
		}
		
		public long getTimestamp(){
			return timestamp;
		}
		
		public boolean foundFramePath(){
			return foundFramePathVal;
		}
		
	}
	

	
	
	
}
