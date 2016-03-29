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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uga.cs.clickminer.datamodel.MitmHttpRequest;
import edu.uga.cs.clickminer.exception.ProxyErrorException;

/**
 * <p>ProxyClient class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: ProxyClient.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class ProxyClient {

	public enum ProxyMode {CONTENT, NOCONTENT}
	private final String host;

	private final int port;
	private static final String CMD_POS_REQ = "POS_REQ";
	private static final String CMD_NEXT_POS_REQ = "NEXT_POS_REQ";
	private static final String CMD_MIN_REQ = "MIN_REQ";
	private static final String CMD_PRINT_REQS = "PRINT_REQS";
	private static final String CMD_RECV_REQ_COUNT = "RECV_REQ_COUNT";
	private static final String CMD_CMPLT_REQ_COUNT = "CMPLT_REQ_COUNT";
	private static final String CMD_SET_TS = "SET_TS";
	private static final String CMD_GET_MODE = "GET_MODE";
	private static final String CMD_SET_MODE_NOCONTENT = "SET_MODE_NOCONTENT";
	private static final String CMD_SET_MODE_CONTENT = "SET_MODE_CONTENT";
	private static final String CMD_SET_REQ_CHECK_LIMIT = "SET_REQ_CHECK_LIMIT";
	private static final String CMD_GET_REQ_CHECK_LIMIT = "GET_REQ_CHECK_LIMIT";
	private static final String CMD_GET_LAST_REQ_REFERER = "GET_LAST_REQ_REFERER";
	private static final String CMD_SET_FILTERED_RESP_TYPE = "SET_FILTERED_RESP_TYPE";
	private static final String CMD_REM_FILTERED_RESP_TYPE = "REM_FILTERED_RESP_TYPE";
	
	private static final String RESP_ERROR = "ERROR";
	private static final String RESP_OK = "OK";

	private static final Log log = LogFactory.getLog(ProxyClient.class);

	/**
	 * <p>Constructor for ProxyClient.</p>
	 *
	 * @param host a {@link java.lang.String} object.
	 * @param port a int.
	 */
	public ProxyClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	private Socket connect() throws UnknownHostException, IOException {
		return new Socket(host, port);
	}
	
	private JSONObject commandToJson(String command, List<String> args) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("cmd", command);
		obj.put("args", args);
		return obj;
	}
	
	private void executeVoidCommand(String command) throws ProxyErrorException {
		this.executeCommand(command);
	}
	
	private void executeVoidCommand(String command, List<String> args) throws ProxyErrorException {
		this.executeCommand(command, args);
	}
	
	private JSONArray executeCommand(String command) throws ProxyErrorException {
		return this.executeCommand(command, new ArrayList<String>());
	}

	private JSONArray executeCommand(String command, List<String> args) throws ProxyErrorException {
		String respstr = null;
		Socket sock = null;
		try {
			sock = connect();
			if (log.isDebugEnabled()) {
				log.debug("Connected to the server.");
			}
			// Append a newline to the line to denote the end of the command.
			String cmdstr = this.commandToJson(command, args).toString() + "\n\n";
			sendCommand(cmdstr, sock);
			if (log.isDebugEnabled()) {
				log.debug("Sent command " + cmdstr.trim());
			}
			respstr = readResponse(sock).trim();
			if(log.isDebugEnabled()){
				log.debug("Received response " + respstr);
			}
		} catch (IOException e) {
			if (log.isDebugEnabled()) {
				log.debug("", e);
			}
			throw new ProxyErrorException("Error communicating with proxy.");
		} catch (JSONException e2) {
			if (log.isDebugEnabled()) {
				log.debug("", e2);
			}
			throw new ProxyErrorException("Unable to create json command object.");
		} finally {
			try {
				if (sock != null) {
					sock.close();
				}
			} catch (IOException e) {
				if (log.isErrorEnabled()) {
					log.error("", e);
				}
			}
		}
		
		try {
			JSONObject cmdresp = new JSONObject(respstr);
			String status = cmdresp.getString("status");
			
			if (status.equals(RESP_ERROR)) {
				throw new ProxyErrorException("Error executing the command "
						+ command + ".\n" + cmdresp.getString("msg") + "\n");
			} else if (status.equals(RESP_OK)){
				if(log.isDebugEnabled()){
					if (cmdresp.isNull("msg")){
						log.debug("Successfully executed command " + command + ".");
					} else {
						log.debug("Successfully executed command " + command + ".\n" + 
								cmdresp.getString("msg"));
					}
				}
				return cmdresp.getJSONArray("data");	
			} else {
				throw new ProxyErrorException("Error executing the command "
						+ command + ".\n" + "Undefined status message." + "\n");
			}
		} catch (JSONException e) {
			if(log.isDebugEnabled()){
				log.debug("", e);
			}
			throw new ProxyErrorException("Error executing the command "
					+ command + ".\n" + "Malformed response." + "\n");
		}
	}
	
	private int executeIntValCommand(String command, String attr) 
			throws ProxyErrorException{
		return this.executeIntValCommand(command, new ArrayList<String>(), attr);
	}
	
	private int executeIntValCommand(String command, List<String> args, String attr) 
			throws ProxyErrorException{
		return Integer.parseInt(this.executeStringValCommand(command, args, attr));
	}
	
	private long executeLongValCommand(String command, String attr) 
			throws ProxyErrorException{
		return this.executeLongValCommand(command, new ArrayList<String>(), attr);
	}
	
	private long executeLongValCommand(String command, List<String> args, String attr) 
			throws ProxyErrorException{
		return Long.parseLong(this.executeStringValCommand(command, args, attr));
	}
	
	private MitmHttpRequest executeReqValCommand(String command)
			throws ProxyErrorException {
		return this.executeReqValCommand(command, new ArrayList<String>());
	}
		
	// returns null if the response from the proxy is empty
	private MitmHttpRequest executeReqValCommand(String command, List<String> args)
			throws ProxyErrorException {
		JSONArray val = executeCommand(command, args);
		if (val.length() == 0) {
			return null;
		}
		try {
			JSONObject jval = val.getJSONObject(0);
			if (log.isDebugEnabled()) {
				log.debug("Received in response to " + command + " command.");
				log.debug(jval.toString(4));
			}
			return new MitmHttpRequest(jval);
		} catch (JSONException e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
			throw new ProxyErrorException(
					"Proxy returned invalid response to command " + command
							+ "\n" + val + "\n");
		}
	}
	
	private String executeStringValCommand(String command) 
			throws ProxyErrorException{
		return this.executeStringValCommand(command, new ArrayList<String>());
	}
	
	private String executeStringValCommand(String command, List<String> args) 
			throws ProxyErrorException{
		return this.executeStringValCommand(command, args, null);
	}
	
	private String executeStringValCommand(String command, String attr) 
			throws ProxyErrorException{
		return this.executeStringValCommand(command, new ArrayList<String>(), attr);
	}
	
	// returns null if the response from the proxy is empty
	private String executeStringValCommand(String command, List<String> args, String attr) 
			throws ProxyErrorException{
		JSONArray val = executeCommand(command, args);
		if (val.length() == 0) {
			return null;
		}
		try {	
			if(attr != null){
				JSONObject jval = val.getJSONObject(0);
				if (log.isDebugEnabled()) {
					log.debug("Received in response to " + command + " command.\n" + 
							jval.toString(4));
				}
				if(jval.has(attr)){
					return jval.getString(attr);
				} else {
					throw new ProxyErrorException(
							"Response to command " + command + " does not have an " +
									"attribute named " + attr);
				}
			} else {
				String retval = val.getString(0);
				if (log.isDebugEnabled()) {
					log.debug("Received in response to " + command + " command.\n" + 
							retval);
				}
				return retval;
			}
		} catch (JSONException e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
			throw new ProxyErrorException(
					"Proxy returned invalid response to command " + command
							+ "\n" + val + "\n");
		}
	}
	
	// returns null if the response from the proxy is empty
	/**
	 * <p>getMinRequest.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public MitmHttpRequest getMinRequest() throws ProxyErrorException {
		return this.executeReqValCommand(CMD_MIN_REQ);
	}
	
	/**
	 * <p>getMode.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public ProxyMode getMode() throws ProxyErrorException{
		String val = this.executeStringValCommand(CMD_GET_MODE, "mode");
		if(val.equals("content")){
			return ProxyMode.CONTENT;
		} else if(val.equals("nocontent")){
			return ProxyMode.NOCONTENT;
		} else {
			throw new ProxyErrorException("Unknown proxy mode: " + val);
		}
	}

	/**
	 * <p>getNextPossibleRequest.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public MitmHttpRequest getNextPossibleRequest() throws ProxyErrorException {
		return this.executeReqValCommand(CMD_NEXT_POS_REQ);
	}

	/**
	 * <p>getNumCompletedRequest.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public long getNumCompletedRequest() throws ProxyErrorException {
		return this.executeLongValCommand(CMD_CMPLT_REQ_COUNT, "count");
	}

	/**
	 * <p>getNumReceivedRequest.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public long getNumReceivedRequest() throws ProxyErrorException {
		return this.executeLongValCommand(CMD_RECV_REQ_COUNT, "count");
	}

	/**
	 * <p>getPossibleRequest.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public MitmHttpRequest getPossibleRequest() throws ProxyErrorException {
		return this.executeReqValCommand(CMD_POS_REQ);
	}
	
	/**
	 * <p>getRequestCheckLimit.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public int getRequestCheckLimit() throws ProxyErrorException {
		return this.executeIntValCommand(CMD_GET_REQ_CHECK_LIMIT, "limit");
	}

	/**
	 * <p>printRequest.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public String printRequest() throws ProxyErrorException {
		return executeStringValCommand(CMD_PRINT_REQS);
	}
	
	//use for requests with no referer
	/**
	 * <p>getLastRequestByReferer.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public String getLastRequestByReferer() throws ProxyErrorException {
		return this.getLastRequestByReferer(null);
	}
	
	/**
	 * <p>getLastRequestByReferer.</p>
	 *
	 * @param referer a {@link java.lang.String} object.
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public String getLastRequestByReferer(String referer) 
			throws ProxyErrorException {
		if(referer == null){
			return this.executeStringValCommand(CMD_GET_LAST_REQ_REFERER, "url");
		} else {
			ArrayList<String> args = new ArrayList<String>();
			args.add(referer);
			return this.executeStringValCommand(CMD_GET_LAST_REQ_REFERER, args, "url");
		}
	}
	
	private String readResponse(Socket sock) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				sock.getInputStream()));
		StringBuffer buf = new StringBuffer();
		String line = null;
		while ((line = in.readLine()) != null) {
			buf.append(line + "\n");
		}
		return buf.toString();
	}

	private void sendCommand(String command, Socket sock) throws IOException {
		PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
		out.print(command);
		out.flush();
	}

	/**
	 * <p>setModeContent.</p>
	 *
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public void setModeContent()  throws ProxyErrorException {
		this.executeVoidCommand(CMD_SET_MODE_CONTENT);
	}

	/**
	 * <p>setModeNoContent.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public void setModeNoContent(String url) throws ProxyErrorException {
		ArrayList<String> args = new ArrayList<String>();
		args.add(url);
		this.executeVoidCommand(CMD_SET_MODE_NOCONTENT, args);
	}

	/**
	 * <p>setProxyTimestamp.</p>
	 *
	 * @param ts a double.
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public void setProxyTimestamp(double ts) throws ProxyErrorException {
		ArrayList<String> args = new ArrayList<String>();
		args.add(Double.toString(ts));
		this.executeVoidCommand(CMD_SET_TS, args);
	}

	/**
	 * <p>setRequestCheckLimit.</p>
	 *
	 * @param limit a int.
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public void setRequestCheckLimit(int limit) throws ProxyErrorException {
		ArrayList<String> args = new ArrayList<String>();
		if(limit < 0){
			limit = 0;
		}
		args.add(Integer.toString(limit));
		this.executeVoidCommand(CMD_SET_REQ_CHECK_LIMIT, args);
	}
	
	/**
	 * <p>setFilteredResponseType.</p>
	 *
	 * @param types a {@link java.util.List} object.
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public void setFilteredResponseType(List<String> types) throws ProxyErrorException{
		this.executeVoidCommand(CMD_SET_FILTERED_RESP_TYPE, types);
	}
	
	/**
	 * <p>removeFilteredResponseType.</p>
	 *
	 * @param types a {@link java.util.List} object.
	 * @throws edu.uga.cs.clickminer.exception.ProxyErrorException if any.
	 */
	public void removeFilteredResponseType(List<String> types) throws ProxyErrorException {
		this.executeVoidCommand(CMD_REM_FILTERED_RESP_TYPE, types);
	}

}
