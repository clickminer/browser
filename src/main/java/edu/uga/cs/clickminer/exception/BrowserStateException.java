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
package edu.uga.cs.clickminer.exception;

/**
 * <p>BrowserStateException class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: BrowserStateException.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class BrowserStateException extends RuntimeException {

	private static final long serialVersionUID = 1544767689359333049L;

	/**
	 * <p>Constructor for BrowserStateException.</p>
	 */
	public BrowserStateException() {
	}

	/**
	 * <p>Constructor for BrowserStateException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 */
	public BrowserStateException(String message) {
		super(message);
	}

	/**
	 * <p>Constructor for BrowserStateException.</p>
	 *
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public BrowserStateException(Throwable cause) {
		super(cause);
	}

	/**
	 * <p>Constructor for BrowserStateException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public BrowserStateException(String message, Throwable cause) {
		super(message, cause);
	}

}
