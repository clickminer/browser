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

import org.apache.commons.logging.Log;

/**
 * <p>Abstract UsesCrypto class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: UsesCrypto.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public abstract class UsesCrypto {
	
	protected String hashAlgo = null;
	protected transient Log log = null;
	
	/**
	 * <p>Constructor for UsesCrypto.</p>
	 *
	 * @param log a {@link org.apache.commons.logging.Log} object.
	 * @param hashAlgo a {@link java.lang.String} object.
	 */
	protected UsesCrypto(Log log, String hashAlgo){
		this.log = log;
		this.setHashAlgo(hashAlgo);
	}
	
	/**
	 * <p>Constructor for UsesCrypto.</p>
	 *
	 * @param log a {@link org.apache.commons.logging.Log} object.
	 */
	protected UsesCrypto(Log log){
		this(log, CryptoUtils.getCryptoImpls("MessageDigest")[0]);
	}
	
	/**
	 * <p>getHashValue.</p>
	 *
	 * @param src a {@link java.lang.String} object.
	 */
	protected String getHashValue(String src){
		return CryptoUtils.getHashValue(src, this.hashAlgo);
	}
	
	/**
	 * <p>Setter for the field <code>hashAlgo</code>.</p>
	 *
	 * @param hashAlgo a {@link java.lang.String} object.
	 */
	protected void setHashAlgo(String hashAlgo) {
		try {
			MessageDigest.getInstance(hashAlgo);
			this.hashAlgo = hashAlgo;
		} catch (NoSuchAlgorithmException e) {
			String[] impls = CryptoUtils.getCryptoImpls("MessageDigest");
			if (impls.length > 0) {
				if (log.isInfoEnabled()) {
					log.info("Unable to get instance of " + hashAlgo
							+ " . Falling back on " + impls[0], e);
				}
				try {
					MessageDigest.getInstance(impls[0]);
					this.hashAlgo = impls[0];
				} catch (NoSuchAlgorithmException e1) {
					if (log.isErrorEnabled()) {
						log.error(e1);
					}
				}
			} else {
				if (log.isErrorEnabled()) {
					log.error("No MessageDigest algorithms supported");
				}
				throw new RuntimeException("No MessageDigest algorithms supported");
			}
		}
	}
}
