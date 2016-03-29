package edu.uga.cs.clickminer.results;

import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uga.cs.clickminer.graph.builder.RefererGraphBuilder;
import edu.uga.cs.clickminer.graph.export.InteractionGraphExporter;
import edu.uga.cs.clickminer.graph.export.RefererGraphExporter;
import edu.uga.cs.clickminer.graph.model.InteractionGraph;
import edu.uga.cs.clickminer.graph.model.RefererGraph;
import edu.uga.cs.clickminer.util.ResultsUtils;
import edu.uga.cs.clickminer.util.ResultsUtils.FileType;

public class ClickminerGrapherCLI {
	
	private static final transient Log log = LogFactory.getLog(ClickminerGrapherCLI.class);

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
				.withDescription("Filter out ad related mined clicks at the end of an " +
						"interaction path. (Optional)")
				.withLongOpt("filter-ads")
				.create("f"))
			.addOption(
				OptionBuilder.hasArg(true)
				.isRequired(true)
				.withDescription("Select the type of graph to make. " +
						"'i' for interaction graph, 'r' for referrer graph.")
				.withLongOpt("graph-type")
				.create("g"))
			.addOption(
				OptionBuilder.hasArg(true)
				.isRequired(false)
				.withDescription("The path to the source trace file. Required if graph-type is 'r'")
				.withLongOpt("source-trace")
				.create("s"))
			.addOption(
				OptionBuilder.hasArg()
				.isRequired(true)
				.withDescription("The path to the directory in which to write " +
						"the results.")
				.withLongOpt("output-dir")
				.create("o"))
			.addOption(
				OptionBuilder.hasArg()
				.isRequired(false)
				.withDescription("The path to the mined clicks log.")
				.withLongOpt("mined-clicks")
				.create("m"));
		
	
		return retval;
	}
	
	private static void run(String[] args){
		GnuParser parser = new GnuParser();
		Options opts = ClickminerGrapherCLI.initializeOptions();
		CommandLine cli;
		try{
			
			cli = parser.parse(opts, args);		
			if(cli.hasOption('?')){
				throw new ParseException(null);
			}
			
			if(log.isInfoEnabled()){
				StringBuffer arginfo = new StringBuffer("\n");
				arginfo.append("graph-type: " + cli.getOptionValue('g') + "\n");
				arginfo.append("mined-clicks: " + cli.getOptionValue('m') + "\n");
				arginfo.append("output-dir: " + cli.getOptionValue('o') + "\n");
				arginfo.append("source-path: " + cli.getOptionValue('s') + "\n");
				arginfo.append("filter-ads: " + cli.hasOption('f') + "\n");
				log.info(arginfo.toString());
			}
			
			String graphType = cli.getOptionValue('g');
			String sourcePath = cli.getOptionValue('s');
			String minedpath = cli.getOptionValue('m');
			String outputpath = cli.getOptionValue('o');
			boolean adFilter = cli.hasOption('f');
			
			String prepath = new File(outputpath).getCanonicalPath()
					+ File.separatorChar;
			FileType type = ResultsUtils.getFileType(sourcePath);
			
			if(graphType.equals("i")){
				InteractionGraph igraph;
				if (sourcePath != null) {
					if(type == null){
						throw new RuntimeException("Unsupported source file type, only '.pcap' and '.json' are allowed.");
					}
					igraph = ResultsLoader.loadMinedClicksGraph(new File(minedpath), new File(sourcePath), type, adFilter);
				} else {
					igraph = ResultsLoader.loadMinedClicksGraph(new File(minedpath), adFilter);
				}
				
				InteractionGraphExporter exporter = new InteractionGraphExporter();
				exporter.graphToFile(igraph, prepath + "click_graph.dot");
			} else if (graphType.equals("r")) {
				if(type == null){
					throw new RuntimeException("Unsupported source file type, only '.pcap' and '.json' are allowed.");
				}
				RefererGraph rgraph = new RefererGraphBuilder(new File(sourcePath), type).getGraph();
				RefererGraphExporter exporter = new RefererGraphExporter();
				exporter.graphToFile(rgraph, prepath + "referrer_graph.dot");
			} else {
				throw new RuntimeException("Unsupported graph type, only 'i' and 'r' are allowed.");
			}
					
		} catch (ParseException e1) {
			PrintWriter writer = new PrintWriter(System.out);  
			HelpFormatter usageFormatter = new HelpFormatter();
			usageFormatter.printHelp(writer,80,"clickminer-grapher", "", opts,0,2,"");
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
