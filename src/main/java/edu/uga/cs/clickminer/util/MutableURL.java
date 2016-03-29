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
package edu.uga.cs.clickminer.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

/**
 * <p>MutableURL class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: MutableURL.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class MutableURL{

	private URL url;
	private enum PART {PROTOCOL, USERINFO, HOST, PORT, PATH, QUERY, REF};
	
	private static final transient Log log = LogFactory.getLog(MutableURL.class);
	
	/**
	 * <p>Constructor for MutableURL.</p>
	 *
	 * @param url a {@link edu.uga.cs.clickminer.util.MutableURL} object.
	 * @throws java.net.MalformedURLException if any.
	 */
	public MutableURL(MutableURL url) throws MalformedURLException{
		this.url = new URL(url.toString());
	}
	
	/**
	 * <p>Constructor for MutableURL.</p>
	 *
	 * @param url a {@link java.net.URL} object.
	 * @throws java.net.MalformedURLException if any.
	 */
	public MutableURL(URL url) throws MalformedURLException {
		this.url = new URL(url.toString());
	}
	
	/**
	 * <p>Constructor for MutableURL.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 * @throws java.net.MalformedURLException if any.
	 */
	public MutableURL(String url) throws MalformedURLException{
		this.url = new URL(url);
	}
	
	private Map<MutableURL.PART, String> getParts(){
		Map<MutableURL.PART, String> retval = new HashMap<MutableURL.PART, String>();
		retval.put(PART.PROTOCOL, url.getProtocol());
		retval.put(PART.USERINFO, url.getUserInfo());
		retval.put(PART.HOST, url.getHost());
		retval.put(PART.PORT, url.getPort() == -1 ? null : Integer.toString(url.getPort()));
		retval.put(PART.PATH, url.getPath());
		retval.put(PART.QUERY, url.getQuery());
		retval.put(PART.REF, url.getRef());
		return retval;
	}
	
	private void recreate(Map<MutableURL.PART, String> parts) throws MalformedURLException{
		String urlstr = new String();
		if(parts.get(PART.PROTOCOL) != null){
			urlstr += parts.get(PART.PROTOCOL) + "://";
		}
		if(parts.get(PART.USERINFO) != null){
			urlstr += parts.get(PART.USERINFO) + "@";
		}
		if(parts.get(PART.HOST) != null){
			urlstr += parts.get(PART.HOST);
		}
		if(parts.get(PART.PORT) != null){
			urlstr +=  ":" + parts.get(PART.PORT);
		}
		if(parts.get(PART.PATH) != null){
			urlstr += parts.get(PART.PATH);
		}
		if(parts.get(PART.QUERY) != null){
			urlstr += "?" + parts.get(PART.QUERY);
		}
		if(parts.get(PART.REF) != null){
			urlstr += "#" + parts.get(PART.REF);
		}
		url = new URL(urlstr);
	}
	
	/**
	 * <p>getAuthority.</p>
	 */
	public String getAuthority(){
		return url.getAuthority();
	}
	
	/**
	 * <p>getContent.</p>
	 *
	 * @throws java.io.IOException if any.
	 */
	public Object getContent() throws IOException{
		return url.getContent();
	}
	
	/**
	 * <p>getContent.</p>
	 *
	 * @param classes an array of {@link java.lang.Class} objects.
	 * @throws java.io.IOException if any.
	 */
	@SuppressWarnings("rawtypes")
	public Object getContent(Class[] classes) throws IOException{
		return url.getContent(classes);
	}
	
	/**
	 * <p>getDefaultPort.</p>
	 */
	public int getDefaultPort(){
		return url.getDefaultPort();
	}
	
	/**
	 * <p>getFile.</p>
	 */
	public String getFile(){
		return url.getFile();
	}
	
	/**
	 * <p>getHost.</p>
	 */
	public String getHost(){
		return url.getHost();
	}
	
	/**
	 * <p>getPath.</p>
	 */
	public String getPath(){
		return url.getPath();
	}
	
	/**
	 * <p>getPort.</p>
	 */
	public int getPort(){
		return url.getPort();
	}
	
	/**
	 * <p>getProtocol.</p>
	 */
	public String getProtocol(){
		return url.getProtocol();
	}
	
	/**
	 * <p>getQuery.</p>
	 */
	public String getQuery(){
		return url.getQuery();
	}
	
	/**
	 * <p>getRef.</p>
	 */
	public String getRef(){
		return url.getRef();
	}
	
	/**
	 * <p>getUserInfo.</p>
	 */
	public String getUserInfo(){
		return url.getUserInfo();
	}
	
	/**
	 * <p>hashCode.</p>
	 */
	public int hashCode(){
		return url.hashCode();
	}
	
	/**
	 * <p>openConnection.</p>
	 *
	 * @throws java.io.IOException if any.
	 */
	public URLConnection openConnection() throws IOException{
		return url.openConnection();
	}
	
	/**
	 * <p>openConnection.</p>
	 *
	 * @param proxy a {@link java.net.Proxy} object.
	 * @throws java.io.IOException if any.
	 */
	public URLConnection openConnection(Proxy proxy) throws IOException{
		return url.openConnection(proxy);
	}
	
	/**
	 * <p>openStream.</p>
	 *
	 * @throws java.io.IOException if any.
	 */
	public InputStream openStream() throws IOException{
		return url.openStream();
	}
	
	/**
	 * <p>setURLStreamHandlerFactory.</p>
	 *
	 * @param fac a {@link java.net.URLStreamHandlerFactory} object.
	 */
	public static void setURLStreamHandlerFactory(URLStreamHandlerFactory fac){
		URL.setURLStreamHandlerFactory(fac);
	}
	
	/**
	 * <p>toExternalForm.</p>
	 */
	public String toExternalForm(){
		return url.toExternalForm();
	}
	
	/**
	 * <p>toURI.</p>
	 *
	 * @throws java.net.URISyntaxException if any.
	 */
	public URI toURI() throws URISyntaxException{
		return url.toURI();
	}
	
	/**
	 * <p>setProtocol.</p>
	 *
	 * @param protocol a {@link java.lang.String} object.
	 * @throws java.net.MalformedURLException if any.
	 */
	public void setProtocol(String protocol) throws MalformedURLException{
		Map<MutableURL.PART, String> parts = getParts();
		parts.put(PART.PROTOCOL, protocol);
		recreate(parts);
	}
	
	/**
	 * <p>setUserInfo.</p>
	 *
	 * @param userinfo a {@link java.lang.String} object.
	 * @throws java.net.MalformedURLException if any.
	 */
	public void setUserInfo(String userinfo) throws MalformedURLException{
		Map<MutableURL.PART, String> parts = getParts();
		parts.put(PART.USERINFO, userinfo);
		recreate(parts);
	}
	
	/**
	 * <p>setHost.</p>
	 *
	 * @param host a {@link java.lang.String} object.
	 * @throws java.net.MalformedURLException if any.
	 */
	public void setHost(String host) throws MalformedURLException{
		Map<MutableURL.PART, String> parts = getParts();
		parts.put(PART.HOST, host);
		recreate(parts);
	}
	
	/**
	 * <p>setPort.</p>
	 *
	 * @param port a {@link java.lang.Integer} object.
	 * @throws java.net.MalformedURLException if any.
	 */
	public void setPort(Integer port) throws MalformedURLException{
		Map<MutableURL.PART, String> parts = getParts();
		if(port == null){
			parts.put(PART.PORT, null);
		} else {
			parts.put(PART.PORT, port.toString());
		}
		recreate(parts);
	}
	
	/**
	 * <p>setPath.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 * @throws java.net.MalformedURLException if any.
	 */
	public void setPath(String path) throws MalformedURLException{
		Map<MutableURL.PART, String> parts = getParts();
		parts.put(PART.PATH, path);
		recreate(parts);
	}
	
	/**
	 * <p>setQuery.</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 * @throws java.net.MalformedURLException if any.
	 */
	public void setQuery(String query) throws MalformedURLException{
		Map<MutableURL.PART, String> parts = getParts();
		parts.put(PART.QUERY, query);
		recreate(parts);
	}
	
	/**
	 * <p>setRef.</p>
	 *
	 * @param ref a {@link java.lang.String} object.
	 * @throws java.net.MalformedURLException if any.
	 */
	public void setRef(String ref) throws MalformedURLException{
		Map<MutableURL.PART, String> parts = getParts();
		parts.put(PART.REF, ref);
		recreate(parts);
	}
	
	/**
	 * <p>stripQuery.</p>
	 */
	public void stripQuery(){
		try {
			setQuery(null);
		} catch (MalformedURLException e) {
			if(log.isErrorEnabled()){
				log.error("Removing the query section made url malformed.  " +
						"This shouldn't ever happen.", e);
			}
		}
	}
	
	/**
	 * <p>stripRef.</p>
	 */
	public void stripRef(){
		try {
			setRef(null);
		} catch (MalformedURLException e) {
			if(log.isErrorEnabled()){
				log.error("Removing the reference section made url malformed.  " +
						"This shouldn't ever happen.", e);
			}
		}
	}
	
	/**
	 * <p>stripPort.</p>
	 */
	public void stripPort(){
		try {
			setPort(null);
		} catch (MalformedURLException e) {
			if(log.isErrorEnabled()){
				log.error("Removing the port section made url malformed.  " +
						"This shouldn't ever happen.", e);
			}
		}
	}
	
	
	/**
	 * <p>normalizePath.</p>
	 *
	 * @throws java.net.MalformedURLException if any.
	 * @throws java.net.URISyntaxException if any.
	 */
	public void normalizePath() throws MalformedURLException, URISyntaxException{
		String[] parts = getPath().split(";", 2);
		if(parts.length > 0){
			String newpath;
			String pathpart = parts[0];
			if(pathpart.endsWith("/")){
				pathpart = pathpart.substring(0, pathpart.length() - 1);
			}
			newpath = pathpart;
			if(parts.length > 1){
				newpath += ";" + parts[1];
			}
			setPath(newpath);
		}
		url = url.toURI().normalize().toURL();
	}
	
	private void normalizeEncoding() throws MalformedURLException, UnsupportedEncodingException{
		url = new URL(URLDecoder.decode(url.toString(), "UTF-8"));
	}
	
	/**
	 * <p>normalizeUrl.</p>
	 *
	 * @throws java.net.MalformedURLException if any.
	 * @throws java.net.URISyntaxException if any.
	 * @throws java.io.UnsupportedEncodingException if any.
	 */
	public void normalizeUrl() throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		normalizePath();
		normalizeEncoding();
	}
	
	/**
	 * <p>getSimilarity.</p>
	 *
	 * @param url a {@link edu.uga.cs.clickminer.util.MutableURL} object.
	 */
	public float getSimilarity(MutableURL url){
		return (new Levenshtein().getSimilarity(url.toString(), this.url.toString()));
	}
	
	/**
	 * <p>getSimilarity.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 */
	public float getSimilarity(String url){
		return (new Levenshtein().getSimilarity(url, this.url.toString()));
	}
	
	/**
	 * <p>approxEquals.</p>
	 *
	 * @param url a {@link edu.uga.cs.clickminer.util.MutableURL} object.
	 */
	public boolean approxEquals(MutableURL url){
		return approxEquals(url.toString());
	}
	
	/**
	 * <p>approxEquals.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 */
	public boolean approxEquals(String url){
		return getSimilarity(url) > 0.95;
	}
	
	/**
	 * <p>getQueryParams.</p>
	 *
	 * @throws java.net.MalformedURLException if any.
	 * @throws java.net.URISyntaxException if any.
	 */
	public Set<NameValuePair> getQueryParams()
			throws MalformedURLException, URISyntaxException {
		return new HashSet<NameValuePair>(URLEncodedUtils.parse(toURI(), "UTF-8"));
	}
	
	private void equalsNormalizer(MutableURL url) throws MalformedURLException, UnsupportedEncodingException, URISyntaxException{
		url.normalizeUrl();
		url.stripRef();
		url.stripQuery();
		
		if(url.getPort() == url.getDefaultPort()){
			url.stripPort();
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		try{
			if(obj instanceof MutableURL){
				MutableURL other = new MutableURL((MutableURL)obj);
				MutableURL cur = new MutableURL(this);
				
				Set<NameValuePair> otherParams = other.getQueryParams();
				Set<NameValuePair> curParams = cur.getQueryParams();
				
				equalsNormalizer(other);
				equalsNormalizer(cur);
								
				return other.toString().equals(cur.toString()) && otherParams.equals(curParams);
			}
		} catch (URISyntaxException e) {
			if(log.isDebugEnabled()){
				log.debug("", e);
			}
		} catch (Exception e) {
			if(log.isErrorEnabled()){
				log.error("", e);
			}
		}
		return false;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public String toString(){
		return url.toString();
	}
	
}
