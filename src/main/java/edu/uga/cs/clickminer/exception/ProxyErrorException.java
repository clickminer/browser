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
 * <p>ProxyErrorException class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ProxyErrorException.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ProxyErrorException extends Exception {

	private static final long serialVersionUID = 2774349110109388839L;

	/**
	 * <p>Constructor for ProxyErrorException.</p>
	 */
	public ProxyErrorException() {
	}

	/**
	 * <p>Constructor for ProxyErrorException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 */
	public ProxyErrorException(String message) {
		super(message);
	}

	/**
	 * <p>Constructor for ProxyErrorException.</p>
	 *
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public ProxyErrorException(Throwable cause) {
		super(cause);
	}

	/**
	 * <p>Constructor for ProxyErrorException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public ProxyErrorException(String message, Throwable cause) {
		super(message, cause);
	}

}
