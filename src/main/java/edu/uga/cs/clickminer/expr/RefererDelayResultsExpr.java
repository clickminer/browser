package edu.uga.cs.clickminer.expr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pcap.reconst.http.HttpFlowParser;
import pcap.reconst.http.datamodel.RecordedHttpFlow;
import pcap.reconst.tcp.JpcapReconstructor;
import pcap.reconst.tcp.PacketReassembler;
import pcap.reconst.tcp.Reconstructor;
import pcap.reconst.tcp.TcpConnection;

import au.com.bytecode.opencsv.CSVReader;

import edu.uga.cs.clickminer.results.ClassificationResult;
import edu.uga.cs.clickminer.results.ClassificationResultList;
import edu.uga.cs.clickminer.results.ResultsComparison;
import edu.uga.cs.clickminer.util.ExperimentUtils;
import edu.uga.cs.clickminer.util.OctaveUtils;
import edu.uga.cs.clickminer.util.ResultsUtils;
import edu.uga.cs.clickminer.util.ResultsUtils.FileType;

/**
 * <p>RefererDelayResultsExpr class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: RefererDelayResultsExpr.java 901 2014-04-25 03:27:51Z cjneasbitt $Id
 */
public class RefererDelayResultsExpr {

	private static transient final Log log = LogFactory.getLog(RefererDelayResultsExpr.class);
	
	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) throws Exception {
		List<String> output = new ArrayList<String>();
		//output.add(experiment1(args[0], args[1]));
		output.addAll(experiment2(args[0], args[1]));
		for(String out : output){
			System.out.println(out + "\n");
		}
	}
	
	/**
	 * <p>experiment1.</p>
	 *
	 * @param resultsdir a {@link java.lang.String} object.
	 * @param outdir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public static String experiment1(String resultsdir, String outdir) throws Exception{
		List<ClassificationResult> results = runExperiment(resultsdir, 
				outdir, ExperimentUtils.getLgRefererDelayThresholds(),
				getTotalFlows(ResultsUtils.getTraceFile(resultsdir, FileType.PCAP)));
		
		Collection<ClassificationResultList> resultsets = new HashSet<ClassificationResultList>();
		resultsets.add(new ClassificationResultList(results, "clickminer"));
		
		
		return OctaveUtils.sensSpecOctavePlot(resultsets, 
			"Referer Delay Filtered Clickminer Results.");
	}
	
	/**
	 * <p>experiment2.</p>
	 *
	 * @param resultsdir a {@link java.lang.String} object.
	 * @param outdir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public static List<String> experiment2(String resultsdir, String outdir) throws Exception{
		List<String> retval = new ArrayList<String>();
		List<ClassificationResult> results = runExperiment(resultsdir, outdir, ExperimentUtils.getLgRefererDelayThresholds(),
				getTotalFlows(ResultsUtils.getTraceFile(resultsdir, FileType.PCAP)), true);
		
		for(ClassificationResult result : results){
			System.out.println(result);
		}
		
		Collection<ClassificationResultList> resultsets = new HashSet<ClassificationResultList>();
		resultsets.add(new ClassificationResultList(results, "clickminer"));
		
		
		retval.add(OctaveUtils.sensSpecOctavePlot(resultsets, 
				"Referer Delay Filtered Referer Graph Augmented Clickminer Results."));
		retval.add(OctaveUtils.rocOctavePlot(resultsets, 
				"Referer Delay Filtered Referer Graph Augmented Clickminer Results."));
		retval.add(OctaveUtils.fMeasureOctavePlot(resultsets, 
				"Referer Delay Filtered Referer Graph Augmented Clickminer Results."));
		retval.add(OctaveUtils.truePosFalsePosOctavePlot(resultsets, 
				"Referer Delay Filtered Referer Graph Augmented Clickminer Results."));
		
		return retval;
	}

	private static int getTotalFlows(File pcapfile) throws Exception {
		int retval = 0;
		Reconstructor reconstructor = new JpcapReconstructor(
				new PacketReassembler());
		HttpFlowParser http = new HttpFlowParser(reconstructor.reconstruct(pcapfile
				.getAbsolutePath()));
		Map<TcpConnection, List<RecordedHttpFlow>> flowMap = http.parse();
		for (TcpConnection conn : flowMap.keySet()) {
			retval += flowMap.get(conn).size();
		}
		return retval;
	}

	private static List<ClassificationResult> runExperiment(
			String resultsdir, String outdir, double[] thresholds,
			int totalFlows) throws Exception{
		return runExperiment(resultsdir, outdir, thresholds, totalFlows, false);
	}
	
	
	private static List<ClassificationResult> runExperiment(
			String resultsdir, String outdir, double[] thresholds,
			int totalFlows, boolean augmentInteractionGraph) throws Exception {
		List<ClassificationResult> retval = new ArrayList<ClassificationResult>();
		for (double thresh : thresholds) {
			if(log.isInfoEnabled()){
				log.info("Testing threshold: " + thresh);
			}
			String outputdir = new File(outdir).getCanonicalPath()
					+ File.separatorChar + "threshold_"
					+ String.format("%.3f", thresh);
			File outf = new File(outputdir);
			outf.mkdir();
			ResultsComparison rc = new ResultsComparison(
					ResultsUtils.getMinedClicks(resultsdir),
					ResultsUtils.getRecordedClicks(resultsdir), 
					ResultsUtils.getTraceFile(resultsdir, FileType.PCAP),
					FileType.PCAP,
					thresh,
					augmentInteractionGraph,
					true);
			rc.outputResults(outputdir);
			retval.add(getIterationResult(thresh, outputdir, totalFlows));
			if(log.isInfoEnabled()){
				log.info("Testing threshold: " + thresh + " complete.\n" + retval.get(retval.size() - 1));
			}
			removeDirectory(outf);
		}
		return retval;
	}

	private static ClassificationResult getIterationResult(double thresh,
			String outdir, int totalFlows) throws FileNotFoundException,
			IOException {
		CSVReader reader = new CSVReader(new FileReader(
				ResultsUtils.getResultsSummary(outdir)), ',', '"', 1);
		String[] line = reader.readNext();
		reader.close();
		int truePos = Integer.parseInt(line[6]) + Integer.parseInt(line[7]);
		int falsePos = Integer.parseInt(line[3]) - truePos;
		int matchableRecordedInteractions = Integer.parseInt(line[1]);
		//int trueNeg = totalFlows - matchableRecordedInteractions;
		int falseNeg = matchableRecordedInteractions - truePos;
		int trueNeg = totalFlows - truePos - falsePos - falseNeg;
		
		return new ClassificationResult(thresh, truePos, falsePos,
				trueNeg, falseNeg);

	}

	private static boolean removeDirectory(File directory) {

		if (directory == null)
			return false;
		if (!directory.exists())
			return true;
		if (!directory.isDirectory())
			return false;

		String[] list = directory.list();
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				File entry = new File(directory, list[i]);
				if (entry.isDirectory()) {
					if (!removeDirectory(entry))
						return false;
				} else {
					if (!entry.delete())
						return false;
				}
			}
		}
		return directory.delete();
	}
}
