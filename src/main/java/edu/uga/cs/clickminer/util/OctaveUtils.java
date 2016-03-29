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
package edu.uga.cs.clickminer.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import edu.uga.cs.clickminer.results.ClassificationResult;
import edu.uga.cs.clickminer.results.ClassificationResultList;

/**
 * <p>OctaveUtils class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: OctaveUtils.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class OctaveUtils {
	
	private OctaveUtils(){}
	
	private static final transient Log log = LogFactory
			.getLog(OctaveUtils.class);
	
	/**
	 * <p>execute.</p>
	 *
	 * @param octscript a {@link java.lang.String} object.
	 */
	public static void execute(String octscript){
		OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
		octave.eval(octscript);
		octave.close();
	}
	
	/**
	 * <p>printPlot.</p>
	 *
	 * @param plotscript a {@link java.lang.String} object.
	 * @param outpath a {@link java.lang.String} object.
	 * @param device a {@link java.lang.String} object.
	 */
	public static void printPlot(String plotscript, String outpath, String device){
		if(log.isDebugEnabled()){
			log.debug("Printing the following plot.\n" + plotscript);
		}
		String printcmd = "print('" + outpath + "','-d" + device + "');\n";
		if(!plotscript.trim().endsWith(";")){
			plotscript = plotscript.trim() + ";\n";
		} else {
			plotscript = plotscript.trim() + "\n";
		}
		plotscript += printcmd;
		execute(plotscript);
	}
	
	private static String generateROCData(String dataname, List<ClassificationResult> results){
		String retval = dataname + " = [";
		for(ClassificationResult result : results){
			retval += result.getFalsePositiveRate() + ", " + 
					result.getTruePositiveRate() + ";";
		}
		return retval + "];\n";
	}
	
	private static String generateROCPoints(List<ClassificationResult> results){
		String retval = "";
		for(ClassificationResult result: results){
			retval += "text(" + result.getFalsePositiveRate() + "," + 
					result.getTruePositiveRate() + ",\"" + result.getThreshold() + 
					"\",\"rotation\",-45);\n";
		}
		return retval;
	}
	
	private static AxisBounds2D getROCAxisBounds(Collection<ClassificationResultList> resultset){
		AxisBounds2D bounds = new OctaveUtils().new AxisBounds2D();
		for(List<ClassificationResult> results : resultset){
			for(ClassificationResult result: results){
				double fpr = result.getFalsePositiveRate();
				double tpr = result.getTruePositiveRate();
				
				if(fpr < bounds.xmin){
					bounds.xmin = fpr;
				}
				if(fpr > bounds.xmax){
					bounds.xmax = fpr;
				}
				
				if(tpr < bounds.ymin){
					bounds.ymin = tpr;
				}
				if(tpr > bounds.ymax){
					bounds.ymax = tpr;
				}
			}
		}
		return bounds;
	}
	
	
	/**
	 * <p>rocOctavePlot.</p>
	 *
	 * @param resultset a {@link java.util.Collection} object.
	 * @param title a {@link java.lang.String} object.
	 */
	public static String rocOctavePlot(Collection<ClassificationResultList> resultset, String title){
		
		//generate the data arrays and the plot command and the point text commands
		String datastr = "", plotstr = "plot(", pointsstr = "";
		
		int size = resultset.size(), counter = 0;
		Iterator<ClassificationResultList> iter = resultset.iterator();
		while(iter.hasNext()){
			ClassificationResultList reslist = OctaveUtils.sort(iter.next());
			String arrname = "data" + counter;
			String setname = reslist.getSetName();
			
			datastr += OctaveUtils.generateROCData(arrname, reslist);
			plotstr += arrname + "(:,1), " + arrname + "(:,2), \"-x;" + setname + "-ROC;\"";
			if(counter++ < size - 1){
				plotstr += ",";
			}
			
			pointsstr += OctaveUtils.generateROCPoints(reslist);
		}
		plotstr = datastr + plotstr + ");\n" + pointsstr;
						
		//find the plot axis bounds
		AxisBounds2D bounds = OctaveUtils.getROCAxisBounds(resultset);
		
		double xint = (bounds.xmax - bounds.xmin) / 10;
		double yint = (bounds.ymax - bounds.ymin) / 10;
		
		bounds.xmin = bounds.xmin - (2*xint) > 0 ? bounds.xmin - (2*xint) : 0;
		bounds.xmax = bounds.xmax + (2*xint) < 1 ? bounds.xmax + (2*xint) : 1;
		bounds.ymin = bounds.ymin - (2*yint) > 0 ? bounds.ymin - (2*yint) : 0;
		bounds.ymax = bounds.ymax + (2*yint) < 1 ? bounds.ymax + (2*yint) : 1;
		
		String xminstr = String.format("%.4f", bounds.xmin);
		String xmaxstr = String.format("%.4f", bounds.xmax);
		String yminstr = String.format("%.4f", bounds.ymin);
		String ymaxstr = String.format("%.4f", bounds.ymax);
		
		//add the rest of the plot parameters
		plotstr += "grid on;\n" +
			"ax=gca();\n" +
			"set (ax, \"xtick\", ["+ xminstr +":" + 
			String.format("%.4f", xint) + ":" + xmaxstr +"]);\n" +
			"set (ax, \"ytick\", ["+ yminstr +":" + 
			String.format("%.4f", yint) + ":" + ymaxstr +"]);\n" +
			"xlabel(\"False Positive Rate.\");\n" +
			"ylabel(\"True Positive Rate\");\n" +
			"axis ([" + xminstr + "," + xmaxstr + "," + yminstr + "," + ymaxstr + "]);\n" +
			"legend(\"location\", \"southeast\");\n" +
			"title(\"" + title + "\");";
		
		return plotstr;
	}
	
	private static String generateTruePosFalsePosData(String dataname, List<ClassificationResult> results){
		String retval = dataname + " = [";
		for(ClassificationResult result : results){
			retval += result.getThreshold() + ", " + result.getTruePositiveRate() + 
					", " + result.getFalsePositiveRate() + ";";
		}
		return retval + "];\n";
	}
	
	
	/**
	 * <p>truePosFalsePosOctavePlot.</p>
	 *
	 * @param resultset a {@link java.util.Collection} object.
	 * @param title a {@link java.lang.String} object.
	 */
	public static String truePosFalsePosOctavePlot(Collection<ClassificationResultList> resultset, 
			String title){
		
		String datastr = "", plotstr = "semilogx(";
		int size = resultset.size(), counter = 0;
		Iterator<ClassificationResultList> iter = resultset.iterator();
		while(iter.hasNext()){
			ClassificationResultList reslist = OctaveUtils.sort(iter.next());
			String arrname = "data" + counter;
			String setname = reslist.getSetName();
			
			datastr += OctaveUtils.generateTruePosFalsePosData(arrname, reslist);
			plotstr += arrname + "(:,1), " + arrname + "(:,2), \";" + setname + "-TPR;\", " + 
				arrname + "(:,1), " + arrname + "(:,3),\";" + setname + "-FPR;\"";
			if(counter++ < size - 1){
				plotstr += ",";
			}	
		}
		plotstr = datastr + plotstr + ");\n" +
				"xlabel(\"Threshold in sec.\");\n" +
				"ylabel(\"True/False Positive Rate\");\n" +
				"axis(\"tight\");\n" +
				"title(\"" + title + "\");";
		return plotstr;
	}
	
	
	
	private static String generateSensSpecData(String dataname, List<ClassificationResult> results){
		String retval = dataname + " = [";
		for(ClassificationResult result : results){
			retval += result.getThreshold() + ", " + result.getSensitivity() + 
					", " + result.getSpecificity() + ";";
		}
		return retval + "];\n";
	}
	
	/**
	 * <p>sensSpecOctavePlot.</p>
	 *
	 * @param resultset a {@link java.util.Collection} object.
	 * @param title a {@link java.lang.String} object.
	 */
	public static String sensSpecOctavePlot(Collection<ClassificationResultList> resultset, 
			String title){
		
		String datastr = "", plotstr = "semilogx(";
		int size = resultset.size(), counter = 0;
		Iterator<ClassificationResultList> iter = resultset.iterator();
		while(iter.hasNext()){
			ClassificationResultList reslist = OctaveUtils.sort(iter.next());
			String arrname = "data" + counter;
			String setname = reslist.getSetName();
			
			datastr += OctaveUtils.generateSensSpecData(arrname, reslist);
			plotstr += arrname + "(:,1), " + arrname + "(:,2), \";" + setname + "-Sensitivity;\", " + 
				arrname + "(:,1), " + arrname + "(:,3),\";" + setname + "-Specificity;\"";
			if(counter++ < size - 1){
				plotstr += ",";
			}
			
		}
		plotstr = datastr + plotstr + ");\n" +
				"xlabel(\"Threshold in sec.\");\n" +
				"ylabel(\"Sensitivity/Specificity\");\n" +
				"axis(\"tight\");\n" +
				"title(\"" + title + "\");";
		return plotstr;
	}
	
	
	/**
	 * <p>histOctavePlot.</p>
	 *
	 * @param values a {@link java.util.List} object.
	 */
	public static String histOctavePlot(List<Double> values){
		Collections.sort(values);
		double maxvalue = values.get(values.size() - 1);
		long maxvalueint = (long)maxvalue;
		if(maxvalue > (maxvalueint + 0.5)){
			maxvalue = maxvalueint + 1;
		} else {
			maxvalue = maxvalueint + 0.5;
		}
		String data = "data = [";
		for(double value: values){
			data += String.format("%.3f", value) + ",";
		}
		data += "];\nbins = (0.5:0.5:" + String.format("%.1f", maxvalue) + ");\n" + 
				"[nn, xx] = hist(data,bins);\n" +
				"ax = plotyy(bins, kernel_density(bins', data', 0.5), xx, nn, @plot, @bar);\n" +
				"xlabel(\"Referer Delay (in sec.)\");\n" +
				"ylabel (ax(1), \"Density\");\n" +
				"ylabel (ax(2), \"Number of Requests\");";
		return data;
	}
	
	
	private static String generateFMeasureData(String dataname, List<ClassificationResult> results){
		String retval = dataname + " = [";
		for(ClassificationResult result : results){
			retval += result.getThreshold() + ", " + result.getF05Measure() + 
					", " + result.getF1Measure() + ", " + result.getF2Measure() + ";";
		}
		return retval + "];\n";
	}
	
	
	/**
	 * <p>fMeasureOctavePlot.</p>
	 *
	 * @param resultset a {@link java.util.Collection} object.
	 * @param title a {@link java.lang.String} object.
	 */
	public static String fMeasureOctavePlot(Collection<ClassificationResultList> resultset, String title){
		
		String datastr = "", plotstr = "semilogx(";
		int size = resultset.size(), counter = 0;
		Iterator<ClassificationResultList> iter = resultset.iterator();
		while(iter.hasNext()){
			ClassificationResultList reslist = OctaveUtils.sort(iter.next());
			String arrname = "data" + counter;
			String setname = reslist.getSetName();
			
			datastr += OctaveUtils.generateFMeasureData(arrname, reslist);
			plotstr += arrname + "(:,1), " + arrname + "(:,2), \";" + setname + 
					"-F0.5;\", " + arrname + "(:,1), " + arrname + "(:,3),\";" + 
					setname + "-F1;\", " + arrname + "(:,1), " + arrname + 
					"(:,4), \";" + setname + "-F2;\"";
			if(counter++ < size - 1){
				plotstr += ",";
			}
		}
		plotstr = datastr + plotstr + ");\n" +
				"xlabel(\"Threshold in sec.\");\n" +
				"ylabel(\"F-Measure\");\n" +
				"axis(\"tight\");\n" +
				"title(\"" + title + "\");";
		return plotstr;
	}
	
	
	private static ClassificationResultList sort(ClassificationResultList results){
		ClassificationResultList resultscpy = new ClassificationResultList(results);
		Collections.sort(resultscpy, new Comparator<ClassificationResult>(){
			@Override
			public int compare(ClassificationResult o1,
					ClassificationResult o2) {
				return Double.compare(o1.getThreshold(), o2.getThreshold());
			}
		});
		return resultscpy;
	}
	
	
	private class AxisBounds2D {
		public double xmin = Double.MAX_VALUE, xmax = Double.MIN_VALUE, 
				ymin = Double.MAX_VALUE, ymax = Double.MIN_VALUE;
	}

}
