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
package edu.uga.cs.adblock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.google.common.net.InternetDomainName;

import edu.uga.cs.json.JSONReader;

/**
 * <p>Used to match URL's against AdBlock rules.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: AdBlockRuleMatcher.java 846 2013-10-03 17:51:49Z cjneasbitt $Id
 */
public class AdBlockRuleMatcher {

	private static final transient Log log = LogFactory.getLog(AdBlockRuleMatcher.class);
	
	private final List<AdBlockRule> rules;
	private final Map<String, Boolean> isBlockedIndex = 
			new HashMap<String, Boolean>();
	
	/**
	 * <p>Constructs an {@link edu.uga.cs.adblock.AdBlockRuleMatcher} object from
	 * the file "easylist.txt" in the current classpath.</p>
	 * @throws java.io.IOException if unable to load or parse the default rules file.
	 */
	public AdBlockRuleMatcher() throws IOException {
		rules = parseDefaultRules();
	}
	
	/**
	 * <p>Constructs an {@link edu.uga.cs.adblock.AdBlockRuleMatcher} object from
	 * a file containing a set of parsed AdBlock rules serialized in JSON.</p>
	 *
	 * @param parsedRulesFile a {@link java.lang.String} object representing the
	 * path to the rules file.
	 * @throws java.io.IOException if unable to find or load the rules file.
	 * @throws org.json.JSONException if the rules are not in the correct JSON
	 * format.
	 */
	public AdBlockRuleMatcher(String parsedRulesFile) throws IOException,
			JSONException {
		rules = loadRules(parsedRulesFile);
	}

	/**
	 * <p>Constructs an {@link edu.uga.cs.adblock.AdBlockRuleMatcher} object from 
	 * a list of {@link edu.uga.cs.adblock.AdBlockRule} objects.</p>
	 *
	 * @param rules a {@link java.util.List} of 
	 * {@link edu.uga.cs.adblock.AdBlockRule} objects.
	 */
	public AdBlockRuleMatcher(List<AdBlockRule> rules) {
		this.rules = rules;
	}
	
	private List<AdBlockRule> parseDefaultRules() throws IOException{
		InputStream list = AdBlockRuleMatcher.class
				.getClassLoader().getResourceAsStream("easylist.txt");
		return AdBlockRuleParser.parseRules(list);
	}

	private List<AdBlockRule> loadRules(String parsedRulesFile) throws IOException,
			JSONException {
		JSONReader<AdBlockRule> reader = new JSONReader<AdBlockRule>();
		return reader.read(new File(parsedRulesFile), AdBlockRule.class);
	}

	/**
	 * <p>Checks to see if the effective second level domain URL of the supplied URL 
	 * is blocked by any rule in the rule set.  The effective second level domain is 
	 * defined as [protocol]://[2LD].  For example, the 2LD URL of http://www.example.com/index.html 
	 * is http://example.com.  For https://www.blah.co.uk the 2LD URL is https://blah.co.uk.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 * @return true, if the URL is blocked by a rule, false otherwise.
	 */
	public boolean isBlocked2LD(String url){
		try {
			URL urlObj = new URL(url);
			InternetDomainName dname = InternetDomainName.from(urlObj.getHost());
			String shortUrl = urlObj.getProtocol() + "://" + dname.topPrivateDomain().name() + "/";
			return isBlocked(shortUrl);
		} catch(Exception e){
			if(log.isErrorEnabled()){
				log.error("Error matching url.", e);
			}
			return false;
		}
	}
	
	/**
	 * <p>Checks to see if the full effective second level domain URL of the supplied URL 
	 * is blocked by any rule in the rule set.   The full effective second level domain is 
	 * defined as [protocol]://[2LD]/[Path]?[Query]. For example, the 2LD URL of 
	 * http://www.example.co.uk/index.html is http://example.co.uk/index.html.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 * @return true, if the URL is blocked by a rule, false otherwise.
	 */
	public boolean isBlockedFull2LD(String url){
		try{
			URL urlObj = new URL(url);
			InternetDomainName dname = InternetDomainName.from(urlObj.getHost());
			String pathStr = urlObj.getPath();
			String queryStr = urlObj.getQuery();
			String urlEnd = "";
			if(pathStr != null){
				urlEnd += pathStr;
			}
			if(queryStr != null){
				urlEnd += "?" + queryStr;
			}
			String twoLDUrl = urlObj.getProtocol() + "://" + dname.topPrivateDomain().name() +
					urlEnd;
			
			return isBlocked(twoLDUrl);
		} catch(Exception e){
			if(log.isErrorEnabled()){
				log.error("Error matching url.", e);
			}
			return false;
		}
	}
	
	/**
	 * <p>Checks to see if the supplied URL is blocked by any rule in the rule set.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 * @return true, if the URL is blocked by a rule, false otherwise.
	 */
	public boolean isBlocked(String url){
		try{
			if(isBlockedIndex.containsKey(url)){
				return isBlockedIndex.get(url);
			}
			List<AdBlockRule> rules = this.getMatchedRules(url, true);
			filterExceptionRules(rules);
			boolean retval = rules.size() > 0;
			isBlockedIndex.put(url, retval);
			return retval;
		} catch (Exception e){
			if(log.isErrorEnabled()){
				log.error("Error matching url.", e);
			}
			return false;
		}
	}

	private void filterExceptionRules(List<AdBlockRule> rules) {
		List<AdBlockRule> remove = new ArrayList<AdBlockRule>();
		for (AdBlockRule rule : rules) {
			if (rule.isException()) {
				remove.add(rule);
			}
		}
		rules.removeAll(remove);
	}

	/**
	 * <p>Returns the list of rules that pertain the the supplied URL.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 * @param applyExceptions if true this method will return the first matching
	 * exception rule, otherwise it will return the list of all matching exception 
	 * and blocking rules.
	 * @throws java.net.MalformedURLException if supplied URL is malformed.
	 * @return a {@link java.util.List} of matching AdBlock rules.
	 */
	public List<AdBlockRule> getMatchedRules(String url, boolean applyExceptions)
			throws MalformedURLException {
		List<AdBlockRule> retval = new ArrayList<AdBlockRule>();
		for (AdBlockRule rule : rules) {
			boolean applyrule = false;
			Matcher matcher = Pattern.compile(rule.getRegex()).matcher(url);
			if (!matcher.find()) {
				continue;
			} else {
				applyrule = true;
				for (String opt : rule.getOptions()) {
					if (opt.startsWith("domain=")) {
						applyrule = this.evalDomainOption(url,
								opt.replaceFirst("domain=", ""));
					}
				}
			}
			if (applyrule) {
				retval.add(rule);
			}
		}

		// exception rules negate all other matching rules
		if (applyExceptions) {
			for (AdBlockRule rule : retval) {
				if (rule.isException()) {
					retval.clear();
					retval.add(rule);
					break;
				}
			}
		}
		return retval;
	}

	private boolean evalDomainOption(String url, String optval)
			throws MalformedURLException {
		boolean retval = false;
		String hostname = new URL(url).getHost();
		String[] optdomains = optval.split("\\|");
		for (String optdomain : optdomains) {
			boolean newval = true;
			if (optdomain.startsWith("~")) {
				newval = false;
				optdomain = optdomain.replaceFirst("~", "");
			}
			if (hostname.contains(optdomain)) {
				retval = newval;
			}
		}
		return retval;
	}
}
