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

import edu.uga.cs.clickminer.util.ResultsUtils;
import edu.uga.cs.clickminer.util.ResultsUtils.FileType;

/**
 * <p>ClickminerResultsCLI class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ClickminerResultsCLI.java 901 2014-04-25 03:27:51Z cjneasbitt $Id
 */
public class ClickminerResultsCLI {
	
	private static final transient Log log = LogFactory.getLog(ClickminerResultsCLI.class);

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) throws Exception{
		run(args);
	}
	
	@SuppressWarnings("static-access")
	private static Options initializeOptions(){
		Options retval = new Options();
		
		retval.addOption(
				OptionBuilder.isRequired(false)
				.withDescription("Print help message.")
				.withLongOpt("help")
				.create("?"))
			.addOption(
				OptionBuilder.hasArg()
				.isRequired(false)
				.withDescription("Referer delay threshold in seconds. (Optional)")
				.withLongOpt("referer-delay-threshold")
				.withType(Number.class)
				.create("t"))
			.addOption(
				OptionBuilder.hasArg(false)
				.isRequired(false)
				.withDescription("Filter out ad related mined clicks at the end of an " +
						"interaction path. (Optional)")
				.withLongOpt("filter-ads")
				.create("f"))
			.addOption(
				OptionBuilder.hasArg(false)
				.isRequired(false)
				.withDescription("Augment the mined clicks with the flows found " +
						"in the supplied in the 'validate' argument. (Optional)")
				.withLongOpt("augment")
				.create("a"))
			.addOption(
				OptionBuilder.hasArg()
				.isRequired(true)
				.withDescription("Validate the recorded clicks against the supplied " +
						"source trace.  Only '.pcap' and '.json' files supported.")
				.withLongOpt("validate")
				.create("v"))
			.addOption(
				OptionBuilder.hasArg()
				.isRequired(true)
				.withDescription("The path to the directory in which to write " +
						"the results.")
				.withLongOpt("output-dir")
				.create("o"))
			.addOption(
				OptionBuilder.hasArg()
				.isRequired(true)
				.withDescription("The path to the mined clicks log.")
				.withLongOpt("mined-clicks")
				.create("m"))
			.addOption(
				OptionBuilder.hasArg()
				.isRequired(true)
				.withDescription("The path to the recorded clicks log.")
				.withLongOpt("recorded-clicks")
				.create("r"));
	
		return retval;
	}
	
	private static File loadFile(char opt, CommandLine cli){
		File retval = null;
		if(cli.hasOption(opt)){
			retval = new File(cli.getOptionValue(opt));
		}
		return retval;
	}
	
	private static void run(String[] args){
		GnuParser parser = new GnuParser();
		Options opts = ClickminerResultsCLI.initializeOptions();
		CommandLine cli;
		try{
			
			cli = parser.parse(opts, args);		
			if(cli.hasOption('?')){
				throw new ParseException(null);
			}
			
			if(log.isInfoEnabled()){
				StringBuffer arginfo = new StringBuffer("\n");
				arginfo.append("mined-clicks: " + cli.getOptionValue('m') + "\n");
				arginfo.append("recorded-clicks: " + cli.getOptionValue('r') + "\n");
				arginfo.append("output-dir: " + cli.getOptionValue('o') + "\n");
				arginfo.append("augment: " + cli.hasOption('a') + "\n");
				arginfo.append("validate: " + cli.getOptionValue('v') + "\n");
				arginfo.append("filter-ads: " + cli.hasOption('f') + "\n");
				arginfo.append("referer-delay-threshold: " + cli.getOptionValue('t') + "\n");
				log.info(arginfo.toString());
			}
			
			ResultsComparison compare;
			File sourcePath = ClickminerResultsCLI.loadFile('v', cli);
			Number thresh = (Number)cli.getParsedOptionValue("t");
			File minedpath = ClickminerResultsCLI.loadFile('m', cli);
			File recordedpath = ClickminerResultsCLI.loadFile('r', cli);
			String outputpath = cli.getOptionValue('o');
			boolean adFilter = cli.hasOption('f');
			boolean augment = cli.hasOption('a');
			
			FileType type = ResultsUtils.getFileType(cli.getOptionValue('v'));
			
			if(thresh == null){
				compare = new ResultsComparison(minedpath, recordedpath, sourcePath, type, augment, adFilter);
			} else {
				compare = new ResultsComparison(minedpath, recordedpath, sourcePath, type, thresh.doubleValue(), augment, adFilter);
			}
			compare.outputResults(outputpath);
			
		} catch (ParseException e1) {
			PrintWriter writer = new PrintWriter(System.out);  
			HelpFormatter usageFormatter = new HelpFormatter();
			usageFormatter.printHelp(writer,80,"clickminer-results", "", opts,0,2,"");
			writer.close();
		} catch (Exception e) {
			if(log.isFatalEnabled()){
				log.fatal("", e);
			}
		}
	}
}
