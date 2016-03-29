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
package edu.uga.cs.clickminer.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import edu.uga.cs.clickminer.datamodel.ide.Click;
import edu.uga.cs.clickminer.datamodel.log.InteractionRecord;
import edu.uga.cs.json.JSONReader;
import edu.uga.cs.json.JSONWriter;

/**
 * <p>JSONIOTest class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: JSONIOTest.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class JSONIOTest {

	/**
	 * <p>jsonIOTest_1.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public static void jsonIOTest_1() throws Exception{
		JSONReader<Click> reader = new JSONReader<Click>();
		List<Click> clicks = reader.read(
				new File("/home/cjneasbi/workspace/clickminer-SeIDE/example-trace2.txt.json.log"), 
				Click.class);
		for(Click c : clicks){
			System.out.println(c.toJSONString());
		}
	}
	
	/**
	 * <p>jsonIOTest_2.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public static void jsonIOTest_2() throws Exception{
		JSONReader<Click> reader = new JSONReader<Click>();
		JSONWriter<Click> writer = new JSONWriter<Click>();
		
		List<Click> clicks = reader.read(
				new File("/home/cjneasbi/workspace/clickminer-SeIDE/example-trace2.txt.json.log"), 
				Click.class);		
		writer.write(new File("/tmp/clicks2.txt.json.log"), clicks);
		List<Click> clicks2 = reader.read(
				new File("/tmp/clicks2.txt.json.log"), 
				Click.class);
		
		if(clicks.size() == clicks2.size()){
			for(int i = 0; i < clicks.size(); i++){
				if(clicks.get(i).toJSONString().equals(clicks2.get(i).toJSONString())){
					System.out.println("Click objects at index " + i +  " match.");
				} else {
					System.out.println("Click objects at index " + i +  " do not match.");
				}
			}
		} else {
			System.out.println("Wrong number of deserialized clicks.");
		}
	}
	
    /**
     * <p>jsonIOTest_3.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.json.JSONException if any.
     */
    public static void jsonIOTest_3(String path) throws IOException, JSONException{
        File objfile = new File(path);
        JSONReader<InteractionRecord> reader = new JSONReader<InteractionRecord>();
        List<InteractionRecord> records = reader.read(objfile, InteractionRecord.class);
        JSONWriter<InteractionRecord> writer = new JSONWriter<InteractionRecord>();
        writer.write(objfile, records);
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public static void main(String[] args) throws Exception{
        //jsonIOTest_1();
        //jsonIOTest_2();
        //jsonIOTest_3(args[0]);
    }
}
