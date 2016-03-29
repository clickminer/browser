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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * <p>JSONReader class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: JSONReader.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class JSONReader<T extends JSONSerializable> {
	private static final transient Log log = LogFactory.getLog(JSONReader.class);

	/**
	 * <p>read.</p>
	 *
	 * @param in a {@link java.io.File} object.
	 * @param clazz a {@link java.lang.Class} object.
	 * @throws java.io.IOException if any.
	 * @throws org.json.JSONException if any.
	 */
	public List<T> read(File in, Class<T> clazz)
			throws IOException, JSONException {
		List<T> retval = new ArrayList<T>();
		BufferedReader br = new BufferedReader(new FileReader(in));
		StringBuffer buf = new StringBuffer();
		String line = null;
		while ((line = br.readLine()) != null) {
			buf.append(line);
		}
		br.close();

		if (log.isDebugEnabled()) {
			log.debug("Read the following from file.");
			log.debug(buf.toString());
		}
		
		JSONArray json = new JSONArray(buf.toString());
		for (int i = 0; i < json.length(); i++) {
			JSONObject jval = json.optJSONObject(i);
			Constructor<T> obj;
			try {
				obj = clazz.getConstructor(jval.getClass());
				retval.add(obj.newInstance(jval));
			} catch (Exception e) {
				if (log.isErrorEnabled()) {
					log.error("", e);
				}
				e.printStackTrace();
			}	
		}
		return retval;
	}
}
