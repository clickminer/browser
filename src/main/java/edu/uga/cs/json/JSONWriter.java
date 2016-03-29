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
package edu.uga.cs.json;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * <p>JSONWriter class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: JSONWriter.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class JSONWriter<T extends JSONSerializable> {

	/**
	 * <p>write.</p>
	 *
	 * @param in a {@link java.io.File} object.
	 * @param recs a {@link java.util.List} object.
	 * @throws java.io.IOException if any.
	 * @throws org.json.JSONException if any.
	 */
	public void write(File in,
			List<T> recs) throws IOException, JSONException {
		FileWriter writer = new FileWriter(in);
		JSONArray json = new JSONArray();
		for (T record : recs) {
			json.put(record.toJSONObject());
		}
		writer.write(json.toString(4));
		writer.close();
	}
}
