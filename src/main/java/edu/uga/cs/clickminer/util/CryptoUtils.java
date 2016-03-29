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
package edu.uga.cs.clickminer.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//http://www.exampledepot.com/egs/java.security/ListServices.html
/**
 * <p>CryptoUtils class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: CryptoUtils.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class CryptoUtils {

	private static final transient Log log = LogFactory.getLog(CryptoUtils.class);
	
	/**
	 * <p>getHashValue.</p>
	 *
	 * @param src a {@link java.lang.String} object.
	 * @param hashAlgo a {@link java.lang.String} object.
	 */
	public static String getHashValue(String src, String hashAlgo) {
		StringBuffer sb = new StringBuffer();
		try {
			MessageDigest md = MessageDigest.getInstance(hashAlgo);
			md.update(src.getBytes());

			byte byteData[] = md.digest();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16)
						.substring(1));
			}
		} catch (NoSuchAlgorithmException e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
			return null;
		}
		return sb.toString();
	}
	
	// This method returns all available services types
	/**
	 * <p>getServiceTypes.</p>
	 */
	public static String[] getServiceTypes() {
		Set<String> result = new HashSet<String>();

		// All all providers
		Provider[] providers = Security.getProviders();
		for (int i = 0; i < providers.length; i++) {
			// Get services provided by each provider
			Set<?> keys = providers[i].keySet();
			for (Iterator<?> it = keys.iterator(); it.hasNext();) {
				String key = (String) it.next();
				key = key.split(" ")[0];

				if (key.startsWith("Alg.Alias.")) {
					// Strip the alias
					key = key.substring(10);
				}
				int ix = key.indexOf('.');
				result.add(key.substring(0, ix));
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	// This method returns the available implementations for a service type
	/**
	 * <p>getCryptoImpls.</p>
	 *
	 * @param serviceType a {@link java.lang.String} object.
	 */
	public static String[] getCryptoImpls(String serviceType) {
		Set<String> result = new HashSet<String>();

		// All all providers
		Provider[] providers = Security.getProviders();
		for (int i = 0; i < providers.length; i++) {
			// Get services provided by each provider
			Set<?> keys = providers[i].keySet();
			for (Iterator<?> it = keys.iterator(); it.hasNext();) {
				String key = (String) it.next();
				key = key.split(" ")[0];

				if (key.startsWith(serviceType + ".")) {
					result.add(key.substring(serviceType.length() + 1));
				} else if (key.startsWith("Alg.Alias." + serviceType + ".")) {
					// This is an alias
					result.add(key.substring(serviceType.length() + 11));
				}
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}
}
