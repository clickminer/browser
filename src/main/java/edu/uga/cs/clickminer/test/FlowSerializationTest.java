package edu.uga.cs.clickminer.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import pcap.reconst.http.datamodel.RecordedHttpFlow;

import edu.uga.cs.clickminer.util.HttpFlowSerializer;

public class FlowSerializationTest {

	private static String readJSON(String in) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(in)));
		StringBuffer buf = new StringBuffer();
		String line = null;
		while ((line = br.readLine()) != null) {
			buf.append(line);
		}
		br.close();
		return buf.toString();
	}
	
	public static void main(String[] args) throws JSONException, IOException{
		flowSerializationTest_1();
	}
	
	public static void flowSerializationTest_1() throws JSONException, IOException{
		List<RecordedHttpFlow> flows = new ArrayList<RecordedHttpFlow>();
		JSONArray json = new JSONArray(readJSON(
				"/home/cjneasbi/workspace/clickminer-webeventsextract/test/data/test.json"));
		System.out.println("Parsing " + json.length() + " flows\n.");
		for (int i = 0; i < json.length(); i++) {
			RecordedHttpFlow flow = HttpFlowSerializer.parseFlow(json.optJSONObject(i));
			System.out.println(flow.getRequest());
			System.out.println(EntityUtils.toString(flow.getResponse().getEntity()));
			System.out.println();
			flows.add(flow);
		}
		System.out.println("Parsed " + flows.size() + " flows.\n");
		
	}

}
