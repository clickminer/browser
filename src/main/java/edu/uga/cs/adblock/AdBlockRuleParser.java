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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uga.cs.json.JSONWriter;

/**
 * <p>Parses all AdBlock rules pertaining to URLs to a set of 
 * {@link edu.uga.cs.adblock.AdBlockRule} objects.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: AdBlockRuleParser.java 846 2013-10-03 17:51:49Z cjneasbitt $Id
 */
public class AdBlockRuleParser {

	private static transient final Log log = LogFactory.getLog(AdBlockRuleParser.class);

	private AdBlockRuleParser(){}
	
	/**
	 * <p>Parses the URL AdBlock rules from the rules file into 
	 * {@link edu.uga.cs.adblock.AdBlockRule} objects.</p>
	 *
	 * @param rulesFile a {@link java.io.InputStream} of the AdBlock rules file.
	 * @return a {@link java.util.List} of parsed {@link edu.uga.cs.adblock.AdBlockRule}
	 * objects.
	 * @throws java.io.IOException if the rules file cannot be opened or read.
	 */
	public static List<AdBlockRule> parseRules(InputStream rulesFile) throws IOException {		
		List<AdBlockRule> retval = new ArrayList<AdBlockRule>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(rulesFile));
		int count = 0;
		String line;
		while ((line = reader.readLine()) != null) {
			AdBlockRule rule = parseRule(line, count);
			if (rule != null) {
				retval.add(rule);
			}
			count++;
		}
		reader.close();
		return retval;
	}

	private static AdBlockRule parseRule(String line, int count) {

		String rule = null;
		List<String> options = new ArrayList<String>();
		boolean exception = false;

		// Skip the first line which is Adblock version number
		// Skip comment lines
		// Skip all filter rules that refer to HTML elements
		if (count == 0 || line.charAt(0) == '!' || line.contains("##")) {
			return null;
		}

		// Strip the options
		if (line.contains("$")) {
			String[] parts = line.split("\\$", 2);
			for (String opt : parts[1].split(",")) {
				options.add(opt);
			}
			line = parts[0];
		}

		rule = line;

		// Make note of exception rules
		if (line.startsWith("@@")) {
			exception = true;
			line = line.replaceFirst("@@", "");
		}
		
		//Escape '+' to protect from RE compiler, do this before adding any + chars
		if (line.contains("+")) {
			line = line.replace("+", "\\+");
		}
		
		// Separator character '^' , do this before adding any ^ chars
		if (rule.contains("^")) {
			/*line = line.replaceAll("\\^",
					"[^a-zA-Z0-9_\\\\\\\\\\\\%\\\\.\\\\-]");*/
			line = line.replace("^", "\\S+");
		}

		// Deal with '||' and '|' characters at the beginning
		if (line.startsWith("||")) {
			line = line.replaceFirst("\\|\\|", "^http[s]{0,1}://");
		} else if (line.startsWith("|")) {
			line = line.replaceFirst("\\|", "^");
		}

		// Deal with '|' that might appear at the end
		if (line.endsWith("|")) {
			line = line.substring(0, line.length() - 1) + "$";
		}

		// Escape '.','|' and '?' to protect from RE compiler

		if (line.contains("|")) {
			line = line.replace("|", "\\|");
		}
		if (line.contains("?")) {
			line = line.replace("?", "\\?");
		}
		if (line.contains(".")) {
			line = line.replace(".", "\\.");
		}
		
		// Wildcard character '*'
		if (rule.contains("*")) {
			line = line.replace("*", ".*");
		}



		return new AdBlockRule(line, options, exception);
	}

	/**
	 * <p>Serializes a {@link java.util.List} of {@link edu.uga.cs.adblock.AdBlockRule}
	 * objects to a file in JSON representation.</p>
	 *
	 * @param outputPath a {@link java.lang.String} representing the path of the 
	 * output file.
	 * @param rules a {@link java.util.List} of {@link edu.uga.cs.adblock.AdBlockRule} 
	 * objects to serialize.
	 */
	public static void writeRules(String outputPath, List<AdBlockRule> rules) {
		JSONWriter<AdBlockRule> writer = new JSONWriter<AdBlockRule>();
		try {
			writer.write(new File(outputPath), rules);
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("Unable to write rules to file.", e);
			}
		}
	}

}
