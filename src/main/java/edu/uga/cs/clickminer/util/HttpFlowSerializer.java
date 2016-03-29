package edu.uga.cs.clickminer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.message.BasicStatusLine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pcap.reconst.http.datamodel.RecordedHttpEntityEnclosingRequest;
import pcap.reconst.http.datamodel.RecordedHttpFlow;
import pcap.reconst.http.datamodel.RecordedHttpRequest;
import pcap.reconst.http.datamodel.RecordedHttpRequestMessage;
import pcap.reconst.http.datamodel.RecordedHttpResponse;
import pcap.reconst.tcp.MessageMetadata;
import pcap.reconst.tcp.TcpConnection;
import pcap.reconst.tcp.TimestampPair;

public class HttpFlowSerializer {
	
	private static final transient Log log = LogFactory.getLog(HttpFlowSerializer.class);

	public static ContentType genContentType(HttpMessage mess){
		ContentType contype = null;
		Header contentHead = mess.getFirstHeader("Content-Type");
		if(contentHead != null){
			try{
				String[] typestr = contentHead.getValue().split(";");
				String mime = typestr[0];
				String charset  = null;
				if(typestr.length > 1){
					for(int i = 1; i < typestr.length; i++){
						if(typestr[i].contains("charset=")){
							charset = typestr[i].replace("charset=", "").trim();
						}
					}	
				}
				if(log.isDebugEnabled()){
					log.debug("Parse Content-Type: mime = " + mime + " charset = " + charset);
				}
				
				if(charset != null){
					contype = ContentType.create(mime, charset);
				} else {
					contype = ContentType.create(mime, StandardCharsets.UTF_8);
				}
			} catch (IllegalArgumentException e) {
				if(log.isFatalEnabled()){
					log.fatal(contentHead.getValue(), e);
					System.exit(1);
				}
			}
		} else {
			contype = ContentType.create(ContentType.DEFAULT_TEXT.getMimeType(), 
					StandardCharsets.UTF_8);
		}
		return contype;
	}
	
	public static HttpEntity genEntity(JSONObject obj, HttpMessage resp) 
			throws JSONException{
		HttpEntity retval;
		ContentType contype = genContentType(resp);
		if(obj.has("content")){
			retval = new StringEntity(obj.getString("content"), contype);
		} else {
			retval = new StringEntity(new String(), contype);
		}
		return retval;
	}
	
	public static RecordedHttpFlow parseFlow(JSONObject jval) 
			throws JSONException {		
		RecordedHttpRequestMessage req = null;
		RecordedHttpResponse resp = null;
		
		JSONObject obj = jval.optJSONObject("request");
		if(obj != null){
			req = parseRequest(obj);
		}
		
		obj = jval.optJSONObject("response");
		if(obj != null){
			resp = parseResponse(obj);
		}
				
		return new RecordedHttpFlow(new byte[0], req, resp);
	}
	
	public static Header[] parseHeaders(JSONArray jvalh){
		List<Header> buf = new ArrayList<Header>();
		if (jvalh != null) {
			for (int i = 0; i < jvalh.length(); i++) {
				JSONArray htup = jvalh.optJSONArray(i);
				if (htup != null) {
					buf.add(new BasicHeader(htup.optString(0), htup.optString(1)));
				}
			}
		}
		return buf.toArray(new Header[buf.size()]);
	}
	
	public static MessageMetadata parseMetadata(JSONObject obj) 
			throws JSONException {
		return new MessageMetadata( 
				new TimestampPair(obj.getDouble("timestamp_start"), obj.getDouble("timestamp_end"))
				, new TcpConnection());
	}
	
	public static ProtocolVersion parseProtocolVersion(JSONObject obj) throws JSONException{
		JSONArray arr = obj.getJSONArray("httpversion");
		return new HttpVersion(arr.getInt(0), arr.getInt(1));
	}
	
	public static RecordedHttpRequestMessage parseRequest(JSONObject obj) 
			throws JSONException {
		RecordedHttpRequestMessage req = null;
		
		MessageMetadata md = parseMetadata(obj);
		RequestLine reqLine = new BasicRequestLine(obj.getString("method")
				, obj.getString("path")
				, parseProtocolVersion(obj));
		
		Header[] headers = new Header[0];
		JSONArray jvalh = obj.optJSONArray("headers");
		if (jvalh != null) {
			headers = parseHeaders(jvalh);
		}
		
		String content = obj.optString("content");
		if(content != null && !content.equals("")){
			RecordedHttpEntityEnclosingRequest temp = 
					new RecordedHttpEntityEnclosingRequest(reqLine, md);
			temp.setHeaders(headers);
			temp.setEntity(genEntity(obj, temp));
			req = temp;
		} else {
			req = new RecordedHttpRequest(reqLine, md);
			req.setHeaders(headers);
		}
		return req;
	}
	
	public static RecordedHttpResponse parseResponse(JSONObject obj) 
			throws JSONException{
		RecordedHttpResponse resp = null;
		
		try{
		MessageMetadata md = parseMetadata(obj);
		StatusLine statLine = new BasicStatusLine(parseProtocolVersion(obj)
				, obj.getInt("code")
				, obj.getString("msg"));
		
		resp = new RecordedHttpResponse(statLine, md);
		
		Header[] headers = new Header[0];
		JSONArray jvalh = obj.optJSONArray("headers");
		if (jvalh != null) {
			headers = parseHeaders(jvalh);
		}
		
		resp.setHeaders(headers);
		resp.setEntity(genEntity(obj, resp));
		
		
		} catch (JSONException e) {
			e.printStackTrace();
			System.err.println(obj.toString(4));
			System.exit(1);
		}
		return resp;
	}
	
	private HttpFlowSerializer() {}
	
	public JSONObject serializeFlow(RecordedHttpFlow flow){
		//TODO implement, reverse parseFlow, parseRequest, parseResponse
		throw new UnsupportedOperationException();
		//JSONObject retval = new JSONObject();
		//return retval;
	}
	
	public static String read(File in) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(in));
		StringBuffer buf = new StringBuffer();
		String line = null;
		while ((line = br.readLine()) != null) {
			buf.append(line);
		}
		br.close();
		return buf.toString();
	}

}
