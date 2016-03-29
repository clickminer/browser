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
package edu.uga.cs.clickminer;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import edu.uga.cs.clickminer.datamodel.log.InteractionRecord;
import edu.uga.cs.clickminer.exception.ProxyErrorException;
import edu.uga.cs.json.JSONWriter;

/**
 * <p>ClickminerCLI class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ClickminerCLI.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ClickminerCLI {
	
	private static final Log log = LogFactory.getLog(ClickminerCLI.class);

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
					.withDescription("Path to browser binary. (Optional)")
					.withLongOpt("firefox-binary")
					.create("b"))
				.addOption(
					OptionBuilder.hasArg()
					.isRequired(false)
					.withDescription("Path to browser profile. (Optional)")
					.withLongOpt("firefox-profile")
					.create("P"))
				.addOption(
					OptionBuilder.hasArg()
					.isRequired(true)
					.withDescription("The proxy host.")
					.withLongOpt("host")
					.create("h"))
				.addOption(
					OptionBuilder.hasArg()
					.isRequired(true)
					.withDescription("The proxy port.")
					.withLongOpt("proxy-port")
					.withType(Number.class)
					.create("p"))
				.addOption(
					OptionBuilder.hasArg()
					.isRequired(true)
					.withDescription("The instrument server port.")
					.withLongOpt("inst-server-port")
					.withType(Number.class)
					.create("q"))
				.addOption(
					OptionBuilder.hasArg()
					.isRequired(true)
					.withDescription("The path to write the interaction log.")
					.withLongOpt("clicklog")
					.create("o"))
				.addOption(
					OptionBuilder.hasArg()
					.isRequired(false)
					.withDescription("Sets a limit to the number of times a request " +
							"can be checked as a possible interaction request.  (Optional)")
					.withLongOpt("check-limit")
					.withType(Number.class)
					.create("l"))
				.addOption(
					OptionBuilder.hasArgs()
					.isRequired(false)
					.withDescription("Prevents clickminer from considering any request with " +
							"the supplied response MIME type as a possible interaction.  More than one " +
							"MIME type may be supplied. (Optional)")
					.withLongOpt("filter-mime")
					.create("f"))
				.addOption(
					OptionBuilder.hasArg()
					.isRequired(false)
					.withDescription("Path to browser stdout/stderr debug log. (Optional)")
					.withLongOpt("firefox-stdio-log")
					.create("Lo"))
				.addOption(
					OptionBuilder.hasArg()
					.isRequired(false)
					.withDescription("Path to browser webdriver debug log. (Optional)")
					.withLongOpt("firefox-webdriver-log")
					.create("Ld"))
				.addOption(
					OptionBuilder.hasArg(false)
					.isRequired(false)
					.withDescription("Use firefox unstable loading strategy. (Optional)")
					.withLongOpt("unstable-loading")
					.create("u"))
				.addOption(
					OptionBuilder.hasArg(false)
					.isRequired(false)
					.withDescription("Examine the flashvars of embedded flash objects. (Optional)")
					.withLongOpt("flash")
					.create("F"))
				.addOption(
					OptionBuilder.hasArg(false)
					.isRequired(false)
					.withDescription("Enable dynamic execution of javascript " +
							"Warning: this could dramatically increase execution time.  (Optional)")
					.withLongOpt("javascript-execution")
					.create("j"));
	
		   return retval; 
	}
	
	private static Pair<DesiredCapabilities, FirefoxProfile> createBrowserConfig(CommandLine cli) 
			throws ParseException{
		DesiredCapabilities cap = new DesiredCapabilities();
		FirefoxProfile profile = null;
		String profilepath = cli.getOptionValue('P');
		if(profilepath != null){
			profile = new FirefoxProfile(new File(profilepath));
		} else {
			profile = new FirefoxProfile();
		}
		
		setProxyConfig(cli.getOptionValue('h'),
				((Number)cli.getParsedOptionValue("p")).intValue(), cap);
		setBrowserLoggingConfig(cli.getOptionValue("Lo"), cli.getOptionValue("Ld"), 
				cap, profile);
		if(cli.hasOption("u")){
			setUnstableLoadingConfig(profile);
		}
		
		return new ImmutablePair<DesiredCapabilities, FirefoxProfile>(cap, profile);
	}
	
	private static void setProxyConfig(String host, int port, 
			DesiredCapabilities cap) {
		String PROXY = host + ":" + port;
		Proxy proxy = new Proxy();
		proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY);
		cap.setCapability(CapabilityType.PROXY, proxy);
	}
	
	private static void setUnstableLoadingConfig(FirefoxProfile profile){
		profile.setPreference("webdriver.load.strategy","unstable");
	}
	
	private static void setBrowserLoggingConfig(String outLog, String driverLog, 
			DesiredCapabilities cap, FirefoxProfile profile){
		if(outLog != null || driverLog != null){
			if(driverLog != null){
				LoggingPreferences logs = new LoggingPreferences();
				logs.enable(LogType.BROWSER, Level.ALL);
				logs.enable(LogType.DRIVER, Level.ALL);
				logs.enable(LogType.CLIENT, Level.ALL);
				logs.enable(LogType.SERVER, Level.ALL);
				logs.enable(LogType.PROFILER, Level.ALL);
				cap.setCapability(CapabilityType.LOGGING_PREFS, logs);
				profile.setPreference("webdriver.log.file", driverLog);
			}
			if(outLog != null){
				System.setProperty("webdriver.firefox.logfile", outLog);
			}
			 
		}

	}
	
	private static void run(String[] args){
		GnuParser parser = new GnuParser();
		Options opts = ClickminerCLI.initializeOptions();
		CommandLine cli;
		RemoteWebDriver wdriver = null;
		try {
			cli = parser.parse(opts, args);
			
			if(cli.hasOption('?')){
				throw new ParseException(null);
			}
			
			if(log.isInfoEnabled()){
				StringBuffer arginfo = new StringBuffer("\n");
				arginfo.append("firefox-binary: " + cli.getOptionValue('b') + "\n");
				arginfo.append("firefox-profile: " + cli.getOptionValue('P') + "\n");
				arginfo.append("host: " + cli.getOptionValue('h') + "\n");
				arginfo.append("proxy-port: " + cli.getOptionValue('p') + "\n");
				arginfo.append("inst-server-port: " + cli.getOptionValue('q') + "\n");
				arginfo.append("clicklog: " + cli.getOptionValue('o') + "\n");
				arginfo.append("check-limit: " + cli.getOptionValue('l') + "\n");
				
				String[] mimes = cli.getOptionValues('f');
				String mimesstr = null;
				if (mimes != null){
					mimesstr = new String();
					for(int i = 0; i < mimes.length; i++){
						if(i < mimes.length - 1){
							mimesstr += mimes[i] + ",";
						} else {
							mimesstr += mimes[i];
						}
					}
				}
				
				arginfo.append("filter-mime: " + mimesstr + "\n");
				arginfo.append("javascript-execution: " + cli.hasOption('j') + "\n");
				arginfo.append("flash: " + cli.hasOption('F') + "\n");
				arginfo.append("unstable-loading: " + cli.hasOption('u') + "\n");
				arginfo.append("firefox-stdio-log: " + cli.getOptionValue("Lo") + "\n");
				arginfo.append("firefox-webdriver-log: " + cli.getOptionValue("Ld") + "\n");
				
				log.info(arginfo.toString());
			}
			
			String binarypath = cli.getOptionValue('b');
			FirefoxBinary binary = null;
			if(binarypath != null){
				binary = new FirefoxBinary(new File(binarypath));
			}

			Pair<DesiredCapabilities, FirefoxProfile> config = createBrowserConfig(cli);
			wdriver = new FirefoxDriver(binary, config.getRight(), config.getLeft());
			if(cli.hasOption("u")){
				wdriver.manage().timeouts().pageLoadTimeout(1000, TimeUnit.MILLISECONDS);
			    wdriver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);
			}
			
			ProxyClient pc = new ProxyClient(cli.getOptionValue('h'),
					((Number)cli.getParsedOptionValue("q")).intValue());
			
			if(cli.hasOption('l')){
				pc.setRequestCheckLimit(((Number)cli.getParsedOptionValue("l")).intValue());
			}
			if(cli.hasOption('f')){
				List<String> types = Arrays.asList(cli.getOptionValues('f'));
				pc.setFilteredResponseType(types);
			}
			
			BrowserEngine bengine = new BrowserEngine(pc, wdriver, cli.hasOption('j'), 
					cli.hasOption('F'));			
			try{
				bengine.run();
			} catch (Exception e) {
				if(log.isFatalEnabled()){
					log.fatal("",e);
				}
			}
			List<InteractionRecord> ilog = bengine.getInteractionLog();
			JSONWriter<InteractionRecord> writer = new JSONWriter<InteractionRecord>();	
			try {
				writer.write(new File(cli.getOptionValue('o')), ilog);
			} catch (Exception e) {
				if (log.isErrorEnabled()) {
					log.error("", e);
				}
			}
			bengine.close();
		} catch (ParseException e1) {
			PrintWriter writer = new PrintWriter(System.out);  
			HelpFormatter usageFormatter = new HelpFormatter();
			usageFormatter.printHelp(writer,80,"clickminer", "", opts,0,2,"");
			writer.close();
		} catch (ProxyErrorException e2){
			String message = "Error communicating with proxy server. Aborting";
			if(log.isFatalEnabled()){
				log.fatal(message, e2);
			}
		} finally {
			if (wdriver != null){
				try{
					wdriver.close();
				} catch(Exception e){
					if(log.isDebugEnabled()){
						log.debug(e);
					}
					System.exit(-1);
				}
			}
		}
	}
	
	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(String[] args){
		run(args);
	}
	
	
}
