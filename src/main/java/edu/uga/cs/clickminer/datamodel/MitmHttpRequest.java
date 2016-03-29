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
package edu.uga.cs.clickminer.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHeaderIterator;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pcap.reconst.http.datamodel.HttpRequestUrl;
import pcap.reconst.http.datamodel.TimestampedHttpMessage;

import edu.uga.cs.json.JSONSerializable;

/**
 * <p>MitmHttpRequest class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: MitmHttpRequest.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class MitmHttpRequest extends JSONSerializable implements TimestampedHttpMessage, HttpRequestUrl  {

	private static final long serialVersionUID = -3609262032178089854L;
	// delayFromReferer is measured in seconds
	private double timestamp_start, timestamp_end, delayFromReferer;
	private int port, majorVersion, minorVersion, numRequestsRefered;
	private String host, scheme, method, path, url, content, responseContentType;
	private List<Header> heads = new ArrayList<Header>();
	private HttpParams params;
	private static transient final Log log = LogFactory.getLog(MitmHttpRequest.class);

	/**
	 * <p>Constructor for MitmHttpRequest.</p>
	 *
	 * @param jval a {@link org.json.JSONObject} object.
	 */
	public MitmHttpRequest(JSONObject jval) {
		super(jval);
		fromJSONObject(jval);
	}

	/** {@inheritDoc} */
	public void fromJSONObject(JSONObject jval) {
		
		timestamp_start = jval.optDouble("timestamp_start");
		timestamp_end = jval.optDouble("timestamp_end");
		delayFromReferer = jval.optDouble("delay_from_referer");
		port = jval.optInt("port");
		host = jval.optString("host");
		scheme = jval.optString("scheme");
		method = jval.optString("method");
		path = jval.optString("path");
		url = jval.optString("url");
		content = jval.optString("content");
		responseContentType = jval.optString("response_content_type");
		numRequestsRefered = jval.optInt("num_requests_refered");
		JSONArray versionval = jval.optJSONArray("httpversion");
		if (versionval != null) {
			majorVersion = versionval.optInt(0);
			minorVersion = versionval.optInt(1);
		}
		JSONArray jvalh = jval.optJSONArray("headers");
		if (jvalh != null) {
			for (int i = 0; i < jvalh.length(); i++) {
				JSONArray htup = jvalh.optJSONArray(i);
				if (htup != null) {
					heads.add(new BasicHeader(htup.optString(0), htup.optString(1)));
				}
			}
		}
	}

	/**
	 * <p>Getter for the field <code>content</code>.</p>
	 */
	public String getContent() {
		return content;
	}

	/**
	 * <p>Getter for the field <code>delayFromReferer</code>.</p>
	 */
	public double getDelayFromReferer() {
		return delayFromReferer;
	}

	/**
	 * <p>Getter for the field <code>host</code>.</p>
	 */
	public String getHost() {
		return host;
	}

	/**
	 * <p>Getter for the field <code>majorVersion</code>.</p>
	 */
	public int getMajorVersion() {
		return majorVersion;
	}

	/**
	 * <p>Getter for the field <code>method</code>.</p>
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * <p>Getter for the field <code>minorVersion</code>.</p>
	 */
	public int getMinorVersion() {
		return minorVersion;
	}

	/**
	 * <p>Getter for the field <code>path</code>.</p>
	 */
	public String getPath() {
		return path;
	}

	/**
	 * <p>Getter for the field <code>port</code>.</p>
	 */
	public int getPort() {
		return port;
	}

	/**
	 * <p>Getter for the field <code>scheme</code>.</p>
	 */
	public String getScheme() {
		return scheme;
	}

	/** {@inheritDoc} */
	@Override
	public double getStartTS() {
		return timestamp_start;
	}
	
	/** {@inheritDoc} */
	@Override
	public double getEndTS(){
		return timestamp_end;
	}

	/** {@inheritDoc} */
	@Override
	public String getUrl() {
		return url;
	}
	
	/**
	 * <p>Getter for the field <code>numRequestsRefered</code>.</p>
	 */
	public int getNumRequestsRefered() {
		return numRequestsRefered;
	}

	/**
	 * <p>Getter for the field <code>responseContentType</code>.</p>
	 */
	public String getResponseContentType() {
		return responseContentType;
	}

	/**
	 * <p>toJSONObject.</p>
	 */
	public JSONObject toJSONObject() {
		JSONObject retval = new JSONObject();
		try {
			retval.put("timestamp_start", timestamp_start);
			retval.put("timestamp_end", timestamp_end);
			retval.put("delay_from_referer", delayFromReferer);
			retval.put("port", port);
			retval.put("host", host);
			retval.put("scheme", scheme);
			retval.put("method", method);
			retval.put("path", path);
			retval.put("url", url);
			retval.put("content", content);
			retval.put("response_content_type", responseContentType);
			retval.put("num_requests_refered", numRequestsRefered);

			JSONArray versionval = new JSONArray();
			versionval.put(majorVersion);
			versionval.put(minorVersion);
			retval.put("httpversion", versionval);

			JSONArray jvalh = new JSONArray();
			for(Header header : heads){
				JSONArray headertup = new JSONArray();
				headertup.put(header.getName());
				headertup.put(header.getValue());
				jvalh.put(headertup);
			}
			retval.put("headers", jvalh);
		} catch (JSONException e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
		}

		return retval;
	}

	/**
	 * <p>toString.</p>
	 */
	public String toString() {
		try {
			return this.toJSONObject().toString(4);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void addHeader(Header header) {
		heads.add(header);
	}

	/** {@inheritDoc} */
	@Override
	public void addHeader(String name, String value) {
		heads.add(new BasicHeader(name, value));
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsHeader(String name) {
		for(Header header : heads){
			if(header.getName().equals(name)){
				return true;
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Header[] getAllHeaders() {
		return heads.toArray(new Header[heads.size()]);
	}

	/** {@inheritDoc} */
	@Override
	public Header getFirstHeader(String name) {
		for(Header header : heads){
			if(header.getName().equals(name)){
				return header;
			}
		}
		return null;
	}
	
	/** {@inheritDoc} */
	@Override
	public Header[] getHeaders(String name) {
		List<Header> matches = new ArrayList<Header>();
		for(Header header : heads){
			if(header.getName().equals(name)){
				matches.add(header);
			}
		}
		if(matches.size() > 0){
			return matches.toArray(new Header[matches.size()]);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Header getLastHeader(String name) {
		for(int i = heads.size() - 1; i >= 0; i--){
			if(heads.get(i).getName().equals(name)){
				return heads.get(i);
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public HttpParams getParams() {
		return params;
	}

	/** {@inheritDoc} */
	@Override
	public ProtocolVersion getProtocolVersion() {
		return new HttpVersion(majorVersion, minorVersion);
	}

	/** {@inheritDoc} */
	@Override
	public HeaderIterator headerIterator() {
		return this.headerIterator(null);
	}

	/** {@inheritDoc} */
	@Override
	public HeaderIterator headerIterator(String name) {
		return new BasicHeaderIterator(heads.toArray(new Header[heads.size()]), name);
	}

	/** {@inheritDoc} */
	@Override
	public void removeHeader(Header header) {
		heads.remove(header);
	}

	/** {@inheritDoc} */
	@Override
	public void removeHeaders(String name) {
		List<Header> remove = new ArrayList<Header>();
		for(Header header : heads){
			if(header.getName().equals(name)){
				remove.add(header);
			}
		}
		heads.removeAll(remove);
	}

	/** {@inheritDoc} */
	@Override
	public void setHeader(Header header) {
		for(int i = 0; i < heads.size(); i++){
			if(heads.get(i).getName().equals(header.getName())){
				heads.set(i, header);
				break;
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setHeader(String name, String value) {
		this.setHeader(new BasicHeader(name, value));
	}

	/** {@inheritDoc} */
	@Override
	public void setHeaders(Header[] headers) {
		heads.clear();
		for(Header header : headers){
			heads.add(header);
		}	
	}

	/** {@inheritDoc} */
	@Override
	public void setParams(HttpParams params) {
		this.params = params;
	}

	/** {@inheritDoc} */
	@Override
	public RequestLine getRequestLine() {
		return new BasicRequestLine(method, path, getProtocolVersion());
	}
}
