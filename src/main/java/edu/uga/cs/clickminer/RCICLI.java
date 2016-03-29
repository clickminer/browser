package edu.uga.cs.clickminer;

import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uga.cs.clickminer.graph.builder.RefererGraphBuilder;
import edu.uga.cs.clickminer.graph.export.RefererGraphExporter;
import edu.uga.cs.clickminer.graph.model.RefererGraph;
import edu.uga.cs.clickminer.util.ResultsUtils;
import edu.uga.cs.clickminer.util.ResultsUtils.FileType;

public class RCICLI {
	
	private static final transient Log log = LogFactory.getLog(RCICLI.class);

	@SuppressWarnings("static-access")
	private static Options initializeOptions(){
		Options retval = new Options();
		
		retval.addOption(
				OptionBuilder.isRequired(false)
				.withDescription("Print help message.")
				.withLongOpt("help")
				.create("?"))
			.addOption(
				OptionBuilder.hasArg(false)
				.isRequired(false)
				.withDescription("Filter out ad related requests at the end of an " +
						"path. (Optional)")
				.withLongOpt("filter-ads")
				.create("f"))
			.addOption(
				OptionBuilder.hasArg(true)
				.isRequired(true)
				.withDescription("The path to the source trace file.")
				.withLongOpt("source-trace")
				.create("s"))
			.addOption(
				OptionBuilder.hasArg(true)
				.isRequired(true)
				.withDescription("The value of the referrer delay threshold. (in seconds)")
				.withLongOpt("delay-thresh")
				.create("t"))
			.addOption(
				OptionBuilder.hasArg()
				.isRequired(true)
				.withDescription("The path to the directory in which to write " +
						"the results.")
				.withLongOpt("output-dir")
				.create("o"));
		
		return retval;
	}
	
	private static void run(String[] args){
		GnuParser parser = new GnuParser();
		Options opts = RCICLI.initializeOptions();
		CommandLine cli;
		try{
			
			cli = parser.parse(opts, args);		
			if(cli.hasOption('?')){
				throw new ParseException(null);
			}
			
			if(log.isInfoEnabled()){
				StringBuffer arginfo = new StringBuffer("\n");
				arginfo.append("delay-thresh: " + cli.getOptionValue('t') + "\n");
				arginfo.append("output-dir: " + cli.getOptionValue('o') + "\n");
				arginfo.append("source-path: " + cli.getOptionValue('s') + "\n");
				arginfo.append("filter-ads: " + cli.hasOption('f') + "\n");
				log.info(arginfo.toString());
			}
			
			double refererDelayThresh = Double.parseDouble(cli.getOptionValue("t"));
			if(refererDelayThresh < 0){
				throw new IllegalArgumentException("");
			}
			
			String outputpath = cli.getOptionValue('o');
			boolean adFilter = cli.hasOption('f');
			String sourcePath = cli.getOptionValue('s');
			
			String prepath = new File(outputpath).getCanonicalPath()
					+ File.separatorChar;
			FileType type = ResultsUtils.getFileType(sourcePath);
			
			if(type == null){
				throw new RuntimeException("Unsupported source file type, only '.pcap' and '.json' are allowed.");
			}
			RCI rci = new RCI(new RefererGraphBuilder(new File(sourcePath), type));
			Pair<RefererGraph, RefererGraph> graphs = rci.applyRCI(refererDelayThresh, adFilter);
			RefererGraphExporter exporter = new RefererGraphExporter();
			
			exporter.graphToFile(graphs.getLeft(), prepath + "referrer_graph.dot");
			exporter.graphToFile(graphs.getRight(), prepath + "rci_referrer_graph.dot");
					
		} catch (ParseException e1) {
			PrintWriter writer = new PrintWriter(System.out);  
			HelpFormatter usageFormatter = new HelpFormatter();
			usageFormatter.printHelp(writer,80,"rci-cli", "", opts,0,2,"");
			writer.close();
		} catch (Exception e) {
			if(log.isFatalEnabled()){
				log.fatal("", e);
			}
		}
	}
		
	public static void main(String[] args) {
		run(args);
	}
}
