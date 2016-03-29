package edu.uga.cs.adblock.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.util.List;

import org.json.JSONException;

import edu.uga.cs.adblock.AdBlockRule;
import edu.uga.cs.adblock.AdBlockRuleMatcher;
import edu.uga.cs.adblock.AdBlockRuleParser;

/**
 * <p>AdBlockRuleTester class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: AdBlockRuleTester.java 854 2014-04-10 19:51:02Z cjneasbitt $Id
 */
public class AdBlockRuleTester {

	// parse rules
	/**
	 * <p>adBlockRuleTest_1.</p>
	 *
	 * @throws java.io.IOException if any.
	 */
	public static void adBlockRuleTest_1() throws IOException {
		InputStream stream = new FileInputStream(new File("/home/cjneasbi/Desktop/easylist.txt"));
		AdBlockRuleParser.writeRules("/home/cjneasbi/Desktop/easylist_rules.json",
				AdBlockRuleParser.parseRules(stream));
	}

	// match rules
	/**
	 * <p>adBlockRuleTest_2.</p>
	 *
	 * @throws java.io.IOException if any.
	 * @throws org.json.JSONException if any.
	 */
	public static void adBlockRuleTest_2() throws IOException, JSONException {
		AdBlockRuleMatcher matcher = new AdBlockRuleMatcher(
				"/home/cjneasbi/Desktop/easylist_rules.json");
		List<AdBlockRule> rules = matcher
				.getMatchedRules(
						"http://ad.extreme-board.com/adi/N3340.SD148013N3340SN0/B6301858.34;sz=300x250;pc=cbs590254;click0=http://adlog.com.com/adlog/e/r=8801&sg=590254&o=1%253a&h=cn&p=2&b=1&l=en_US&site=1&pt=2000&nd=1&pid=&cid=0&pp=100&e=3&rqid=01phx1-ad-e18:4FA35126734F7E&orh=&oepartner=&epartner=&ppartner=&pdom=&cpnmodule=&count=&ra=97.81.99.137&pg=T6QDxgoOYI8AACALU6kAAAGw&t=2012.05.04.16.28.54&event=58/;ord=2012.05.04.16.28.54?",
						false);
		System.out.println("Matching rules");
		for (AdBlockRule rule : rules) {
			System.out.println(rule);
		}
	}

	/**
	 * <p>adBlockRuleTest_3.</p>
	 *
	 * @throws java.io.IOException if any.
	 * @throws org.json.JSONException if any.
	 * @throws java.net.URISyntaxException if any.
	 */
	public static void adBlockRuleTest_3() throws IOException, JSONException, URISyntaxException {;
		InputStream list = AdBlockRuleTester.class
				.getClassLoader().getResourceAsStream("easylist.txt");
		AdBlockRuleMatcher matcher = new AdBlockRuleMatcher(AdBlockRuleParser.parseRules(list));
		System.out
				.println(matcher
						.isBlocked("http://ad.doubleclick.net/adi/N3880.turnermediagroup.c/B6306508.14;dcove=o;sz=160x600;click0=http://ads.cnn.com/event.ng/Type=click&FlightID=466652&AdID=635278&TargetID=158027&Values=57003&Redirect=;ord=cgRKzgK,bikhuhydKbRAA"));
	}
	
	/**
	 * <p>adBlockRuleTest_4.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public static void adBlockRuleTest_4() throws Exception {
		AdBlockRuleMatcher matcher = new AdBlockRuleMatcher();
		System.out
		.println(matcher
				.isBlocked("http://ad.doubleclick.net/adi/N3880.turnermediagroup.c/B6306508.14;dcove=o;sz=160x600;click0=http://ads.cnn.com/event.ng/Type=click&FlightID=466652&AdID=635278&TargetID=158027&Values=57003&Redirect=;ord=cgRKzgK,bikhuhydKbRAA"));
	}
	
	//tests for social media buttons
	/**
	 * <p>adBlockRuleTest_5.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public static void adBlockRuleTest_5() throws Exception {
		InputStream list = AdBlockRuleTester.class
				.getClassLoader().getResourceAsStream("fanboy-social.txt");
		List<AdBlockRule> rules = AdBlockRuleParser.parseRules(list);
		for(AdBlockRule rule : rules){
			System.out.println(rule);
		}
		AdBlockRuleMatcher matcher = new AdBlockRuleMatcher(rules);
		System.out.println(matcher.isBlocked("http://www.facebook.com/plugins/like.php?api_key=164914533589201&locale=en_US&sdk=joey&channel_url=http%3A%2F%2Fstatic.ak.facebook.com%2Fconnect%2Fxd_arbiter.php%3Fversion%3D23%23cb%3Df290924a89e69%26origin%3Dhttp%253A%252F%252Fwww.nba.com%252Fffe1d6bf3ba776%26domain%3Dwww.nba.com%26relation%3Dparent.parent&href=http%3A%2F%2Fwww.nba.com%2Fgames%2F20130421%2FLALSAS%2Fgameinfo.html%3Fls%3Dpot&node_type=link&width=90&font=arial&layout=button_count&colorscheme=light&show_faces=false&extended_social_context=false"));
		System.out.println(matcher.isBlockedFull2LD("http://www.facebook.com/plugins/like.php?api_key=164914533589201&locale=en_US&sdk=joey&channel_url=http%3A%2F%2Fstatic.ak.facebook.com%2Fconnect%2Fxd_arbiter.php%3Fversion%3D23%23cb%3Df290924a89e69%26origin%3Dhttp%253A%252F%252Fwww.nba.com%252Fffe1d6bf3ba776%26domain%3Dwww.nba.com%26relation%3Dparent.parent&href=http%3A%2F%2Fwww.nba.com%2Fgames%2F20130421%2FLALSAS%2Fgameinfo.html%3Fls%3Dpot&node_type=link&width=90&font=arial&layout=button_count&colorscheme=light&show_faces=false&extended_social_context=false"));
	}

	public static void adBlockRuleTest_6() throws Exception {
		String url = "http://www.google.com/imgres?q=beatles&num=10&hl=en&tbo=d&biw=1440&bih=842&tbm=isch&tbnid=qLSwKCJOFpktSM:&imgrefurl=http://www.iamthebeatles.com/&docid=MKG-0ob7cON5aM&imgurl=http://www.apple.com/itunesaffiliates/beatles/beatles_300x250.jpg&w=300&h=250&ei=4CalUNPPHISi9QSG3oCgBw&zoom=1&iact=hc&vpx=339&vpy=343&dur=2081&hovh=200&hovw=240&tx=99&ty=78&sig=115410919865416117339&page=1&tbnh=147&tbnw=176&start=0&ndsp=29&ved=1t:429,r:9,s:0,i:166";
		InputStream list = AdBlockRuleTester.class
				.getClassLoader().getResourceAsStream("easylist.txt");
		List<AdBlockRule> rules = AdBlockRuleParser.parseRules(list);
		AdBlockRuleMatcher matcher = new AdBlockRuleMatcher(rules);
		System.out.println(matcher.isBlocked(url) + " " + matcher.isBlockedFull2LD(url) + 
				" " + matcher.isBlocked2LD(url));
		List<AdBlockRule> matchrules = matcher.getMatchedRules(url, false);
		for(AdBlockRule rule : matchrules){
			System.out.println(rule);
		}
		
	}
	
	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) throws Exception {
		//adBlockRuleTest_1();
		//adBlockRuleTest_2();
		//adBlockRuleTest_3();
		//adBlockRuleTest_4();
		//adBlockRuleTest_5();
		adBlockRuleTest_6();
	}
}
