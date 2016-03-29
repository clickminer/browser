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

//http://www2.cs.uregina.ca/~dbd/cs831/notes/confusion_matrix/confusion_matrix.html
/**
 * <p>ClassificationResult class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ClassificationResult.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ClassificationResult {
	private final double threshold;
	private final long truePositive, falsePositive, trueNegative, falseNegative;
	
	/**
	 * <p>Constructor for ClassificationResult.</p>
	 *
	 * @param threshold a double.
	 * @param truePositive a long.
	 * @param falsePositive a long.
	 * @param trueNegative a long.
	 * @param falseNegative a long.
	 */
	public ClassificationResult(double threshold, long truePositive, 
			long falsePositive, long trueNegative, long falseNegative){
		this.threshold = threshold;
		this.truePositive = truePositive;
		this.falsePositive = falsePositive;
		this.trueNegative = trueNegative;
		this.falseNegative = falseNegative;
	}

	/**
	 * <p>Getter for the field <code>threshold</code>.</p>
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * <p>Getter for the field <code>truePositive</code>.</p>
	 */
	public long getTruePositive() {
		return truePositive;
	}

	/**
	 * <p>Getter for the field <code>falsePositive</code>.</p>
	 */
	public long getFalsePositive() {
		return falsePositive;
	}

	/**
	 * <p>Getter for the field <code>trueNegative</code>.</p>
	 */
	public long getTrueNegative() {
		return trueNegative;
	}

	/**
	 * <p>Getter for the field <code>falseNegative</code>.</p>
	 */
	public long getFalseNegative() {
		return falseNegative;
	}
	
	/**
	 * <p>getSensitivity.</p>
	 */
	public double getSensitivity() {
		return truePositive / (double)(truePositive + falseNegative);
	}
	
	/**
	 * <p>getTruePositiveRate.</p>
	 */
	public double getTruePositiveRate() {
		return getSensitivity();
	}
	
	/**
	 * <p>getRecall.</p>
	 */
	public double getRecall(){
		return getSensitivity();
	}
	
	/**
	 * <p>getFalsePositiveRate.</p>
	 */
	public double getFalsePositiveRate(){
		return falsePositive / (double)(falsePositive + trueNegative);
	}
	
	/**
	 * <p>getSpecificity.</p>
	 */
	public double getSpecificity() {
		return trueNegative / (double)(falsePositive + trueNegative);
	}
	
	/**
	 * <p>getTrueNegativeRate.</p>
	 */
	public double getTrueNegativeRate(){
		return getSpecificity();
	}
	
	/**
	 * <p>getFalseNegativeRate.</p>
	 */
	public double getFalseNegativeRate(){
		return falseNegative / (double)(truePositive + falseNegative);
	}
	
	/**
	 * <p>getPrecision.</p>
	 */
	public double getPrecision(){
		return truePositive / (double)(truePositive + falsePositive);
	}
	
	/**
	 * <p>getAccuracy.</p>
	 */
	public double getAccuracy() {
		return truePositive + trueNegative / (double)(truePositive + 
				trueNegative + falsePositive + falseNegative);
	}
	
	/**
	 * <p>getF1Measure.</p>
	 */
	public double getF1Measure(){
		return getFMeasure(1);
	}
	
	/**
	 * <p>getF2Measure.</p>
	 */
	public double getF2Measure(){
		return getFMeasure(2);
	}
	
	/**
	 * <p>getF05Measure.</p>
	 */
	public double getF05Measure(){
		return getFMeasure(0.5);
	}
	
	/**
	 * <p>getFMeasure.</p>
	 *
	 * @param beta a double.
	 */
	public double getFMeasure(double beta){
		double pre = this.getPrecision();
		double rec = this.getRecall();
		double beta2 = Math.pow(beta, 2);
		return (1 + beta2) * (pre * rec)/(beta2 * pre + rec);
	}
	
	/**
	 * <p>toString.</p>
	 */
	public String toString(){
		return "Threshold: " + this.getThreshold() +
				" TruePositives: " + this.getTruePositive() +
				" FalsePositives: " + this.getFalsePositive() +
				" TrueNegatives: " + this.getTrueNegative() +
				" FalseNegatives: " + this.getFalseNegative();
	}
}
