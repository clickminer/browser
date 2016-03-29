package edu.uga.cs.clickminer.expr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import edu.uga.cs.clickminer.datamodel.ide.Click;
import edu.uga.cs.clickminer.util.OctaveUtils;
import edu.uga.cs.clickminer.util.ResultsUtils;
import edu.uga.cs.json.JSONReader;

/**
 * <p>ClickTimeIntervalPlotter class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ClickTimeIntervalPlotter.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ClickTimeIntervalPlotter {
	
	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.io.IOException if any.
	 * @throws org.json.JSONException if any.
	 */
	public static void main(String[] args) throws IOException, JSONException{
		System.out.println(OctaveUtils.histOctavePlot(new ArrayList<Double>(
				getClickIntervals(getClicks(args[0])).values())));
	}
	
	
	private static Map<Click, Double> getClickIntervals(List<Click> clicks){
		Map<Click, Double> retval = new HashMap<Click, Double>();
		Collections.sort(clicks, new Comparator<Click>(){
			@Override
			public int compare(Click o1, Click o2) {
				return Double.compare(o1.getTimestamp(), o2.getTimestamp());
			}
		});
		
		for(int i = 1; i < clicks.size(); i++){
			retval.put(clicks.get(i), 
					clicks.get(i).getTimestampSec() - clicks.get(i-1).getTimestampSec());
		}
		return retval;
	}

	
	private static List<Click> getClicks(String resultsdir) throws IOException, JSONException{
		JSONReader<Click> reader = new JSONReader<Click>();
		return reader.read(ResultsUtils.getRecordedClicks(resultsdir), 
				Click.class);
	}
}
