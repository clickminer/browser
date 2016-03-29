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
package edu.uga.cs.clickminer.results;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>ClassificationResultList class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ClassificationResultList.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ClassificationResultList extends ArrayList<ClassificationResult> {

	private static final long serialVersionUID = -4780081161698829353L;
	private final String setName;
	
	/**
	 * <p>Constructor for ClassificationResultList.</p>
	 *
	 * @param setName a {@link java.lang.String} object.
	 */
	public ClassificationResultList(String setName) {
		this.setName = setName;
	}

	/**
	 * <p>Constructor for ClassificationResultList.</p>
	 *
	 * @param initialCapacity a int.
	 * @param setName a {@link java.lang.String} object.
	 */
	public ClassificationResultList(int initialCapacity, String setName) {
		super(initialCapacity);
		this.setName = setName;
	}

	/**
	 * <p>Constructor for ClassificationResultList.</p>
	 *
	 * @param c a {@link java.util.Collection} object.
	 * @param setName a {@link java.lang.String} object.
	 */
	public ClassificationResultList(Collection<? extends ClassificationResult> c, String setName) {
		super(c);
		this.setName = setName;
	}
	
	/**
	 * <p>Constructor for ClassificationResultList.</p>
	 *
	 * @param c a {@link edu.uga.cs.clickminer.results.ClassificationResultList} object.
	 */
	public ClassificationResultList(ClassificationResultList c) {
		super(c);
		this.setName = c.getSetName();
	}
	
	/**
	 * <p>Getter for the field <code>setName</code>.</p>
	 */
	public String getSetName(){
		return setName;
	}

}
