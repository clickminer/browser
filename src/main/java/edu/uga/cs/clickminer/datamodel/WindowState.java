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

import org.openqa.selenium.WebDriver;
import org.apache.commons.logging.LogFactory;

import edu.uga.cs.clickminer.ProxyClient;
import edu.uga.cs.clickminer.exception.BrowserStateException;
import edu.uga.cs.clickminer.exception.ProxyErrorException;
import edu.uga.cs.clickminer.util.UsesCrypto;

/**
 * <p>WindowState class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: WindowState.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class WindowState extends UsesCrypto {

	private String lastPageHash = null, title = null, url = null;
	private long lastCompleteRequestNum = -1;
	private int restingCheckAttempts = 0;
	private int sameLastPageHash = 0;
	private int sameLastRequestNum = 0;

	private ProxyClient pclient = null;
	private WebDriver wdriver = null;
	private String windowHandle = null;

	/*
	 * The minimum number of consecutive resting check attempts where no new
	 * requests have been sent and the page has not changed before we consider
	 * the browser to be in a resting state.
	 */
	private int minSameRequestAndPage = -1;

	/*
	 * The minimum number of consecutive resting check attempts where no new
	 * completed requests have been sent but the page has changed before we
	 * consider the browser to be in a resting state. Should be greater than
	 * minSameRequestAndPage .
	 */
	private int minNoNewCompletedRequests = -1;

	/*
	 * The maximum number of check attempts to perform before we consider the
	 * window to be in a resting state.
	 */
	private int checkLimit = -1;

	/**
	 * <p>Constructor for WindowState.</p>
	 *
	 * @param pclient a {@link edu.uga.cs.clickminer.ProxyClient} object.
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 * @param windowHandle a {@link java.lang.String} object.
	 */
	public WindowState(ProxyClient pclient, WebDriver wdriver,
			String windowHandle) {
		this(pclient, wdriver, windowHandle, "SHA-256", 2, 4, 25);
	}

	/**
	 * <p>Constructor for WindowState.</p>
	 *
	 * @param pclient a {@link edu.uga.cs.clickminer.ProxyClient} object.
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 * @param windowHandle a {@link java.lang.String} object.
	 * @param hashAlgo a {@link java.lang.String} object.
	 */
	public WindowState(ProxyClient pclient, WebDriver wdriver,
			String windowHandle, String hashAlgo) {
		this(pclient, wdriver, windowHandle, hashAlgo, 2, 4, 25);
	}

	/**
	 * <p>Constructor for WindowState.</p>
	 *
	 * @param pclient a {@link edu.uga.cs.clickminer.ProxyClient} object.
	 * @param wdriver a {@link org.openqa.selenium.WebDriver} object.
	 * @param windowHandle a {@link java.lang.String} object.
	 * @param hashAlgo a {@link java.lang.String} object.
	 * @param minSameRequestAndPage a int.
	 * @param minNoNewRequests a int.
	 * @param checkLimit a int.
	 */
	public WindowState(ProxyClient pclient, WebDriver wdriver,
			String windowHandle, String hashAlgo, int minSameRequestAndPage,
			int minNoNewRequests, int checkLimit) {
		super(LogFactory.getLog(WindowState.class), hashAlgo);
		this.pclient = pclient;
		this.wdriver = wdriver;
		this.windowHandle = windowHandle;

		if (minSameRequestAndPage < 1 || minNoNewRequests < 1
				|| checkLimit < 1) {
			throw new BrowserStateException(
					"minSameRequestAndPage, minNoNewRequests, and "
							+ "checkLimit must have positive values");
		}
		this.minSameRequestAndPage = minSameRequestAndPage;
		this.minNoNewCompletedRequests = minNoNewRequests;
		this.checkLimit = checkLimit;
		
		wdriver.switchTo().window(this.windowHandle);
		this.title = wdriver.getTitle();
		this.url = wdriver.getCurrentUrl();

	}

	// http://www.mkyong.com/java/java-sha-hashing-example/
	// TODO implement shingles method
	// TODO implement some way to incorporate the page source of frames
	/**
	 * <p>getPageHash.</p>
	 */
	public String getPageHash() {
		return this.getHashValue(wdriver.getPageSource());
	}

	/**
	 * <p>toString.</p>
	 */
	public String toString() {
		return "Window: " + title + "\n" + "URL: " 
				+ url + "\n" +  "Last Page Hash: "
				+ lastPageHash + "\n" + "Last Total Completed Requests: "
				+ lastCompleteRequestNum + "\n"
				+ "Same Last Page Hash Count: " + sameLastPageHash + "\n"
				+ "Same Last Total Completed Request Count: "
				+ sameLastRequestNum + "\n" + "Check Attempts: "
				+ restingCheckAttempts + "\n" + "Seen Page Hashes:\n";
	}

	/**
	 * <p>isResting.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public boolean isResting() throws ProxyErrorException {
		wdriver.switchTo().window(this.windowHandle);
		boolean retval = false;
		if (restingCheckAttempts == 0) {
			lastPageHash = getPageHash();
			lastCompleteRequestNum = pclient.getNumCompletedRequest();
			restingCheckAttempts++;
		} else {
			String tempPageHash = getPageHash();
			long tempCompleteRequestNum = pclient.getNumCompletedRequest();

			if (tempPageHash.equals(lastPageHash)) {
				sameLastPageHash++;
			} else {
				lastPageHash = tempPageHash;
				sameLastPageHash = 0;
			}

			if (lastCompleteRequestNum == tempCompleteRequestNum) {
				sameLastRequestNum++;
			} else {
				lastCompleteRequestNum = tempCompleteRequestNum;
				sameLastRequestNum = 0;
			}

			// no new requests and same page for minSameRequestAndPage attempts
			if ((sameLastPageHash >= minSameRequestAndPage && sameLastRequestNum >= minSameRequestAndPage)
					// no new completed requests for minNoNewRequests attempts
					|| sameLastRequestNum >= minNoNewCompletedRequests
					// reached max check limit
					|| restingCheckAttempts >= checkLimit ) {
				retval = true;
			} else {
				restingCheckAttempts++;
			}
		}
		return retval;
	}

	/**
	 * <p>reset.</p>
	 */
	public void reset() {
		lastPageHash = null;
		lastCompleteRequestNum = -1;
		restingCheckAttempts = 0;
		sameLastPageHash = 0;
		sameLastRequestNum = 0;
	}

}
