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
package edu.uga.cs.clickminer.datamodel.log;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uga.cs.clickminer.datamodel.ElementLocator;
import edu.uga.cs.clickminer.datamodel.Interaction;
import edu.uga.cs.clickminer.datamodel.MitmHttpRequest;
import edu.uga.cs.json.JSONSerializable;

/**
 * <p>InteractionRecord class.</p>
 *
 * @author Chris Neasbitt
 * @version $Id: InteractionRecord.java 844 2013-10-03 16:53:41Z cjneasbitt $Id
 */
public class InteractionRecord extends JSONSerializable implements Interaction {

	public enum ResultLocation {
		CURRENT_WINDOW, NEW_WINDOW
	}

	private static final long serialVersionUID = -7140694698737200252L;;

	private ResultLocation resultLocation;
	// empty list of window entries when opened in address bar
	private List<PageEntry> srcpageentries = new ArrayList<PageEntry>();
	private PageEntry destpageentry;
	private MitmHttpRequest request;
	private static transient final Log log = LogFactory
			.getLog(InteractionRecord.class);

	/**
	 * <p>Constructor for InteractionRecord.</p>
	 *
	 * @param jval a {@link org.json.JSONObject} object.
	 */
	public InteractionRecord(JSONObject jval) {
		super(jval);
		this.fromJSONObject(jval);
	}
	
	/**
	 * <p>possibleAddressBarInteraction.</p>
	 */
	public boolean possibleAddressBarInteraction(){
		return this.srcpageentries.size() == 0 && request != null &&
				!request.containsHeader("Referer");
	}

	/**
	 * <p>Constructor for InteractionRecord.</p>
	 *
	 * @param resultLocation a {@link edu.uga.cs.clickminer.datamodel.log.InteractionRecord.ResultLocation} object.
	 * @param request a {@link edu.uga.cs.clickminer.datamodel.MitmHttpRequest} object.
	 * @param destpage a {@link edu.uga.cs.clickminer.datamodel.log.PageEntry} object.
	 */
	public InteractionRecord(ResultLocation resultLocation,
			MitmHttpRequest request, PageEntry destpage) {
		super(null);
		setResultLocation(resultLocation);
		setRequest(request);
		setDestPageEntry(destpage);
	}

	/**
	 * <p>addPageEntry.</p>
	 *
	 * @param entry a {@link edu.uga.cs.clickminer.datamodel.log.PageEntry} object.
	 */
	public void addPageEntry(PageEntry entry) {
		srcpageentries.add(entry);
	}

	/**
	 * <p>setDestPageEntry.</p>
	 *
	 * @param entry a {@link edu.uga.cs.clickminer.datamodel.log.PageEntry} object.
	 */
	public void setDestPageEntry(PageEntry entry) {
		destpageentry = entry;
	}

	/** {@inheritDoc} */
	public void fromJSONObject(JSONObject jval) {
		resultLocation = ResultLocation.valueOf(ResultLocation.class,
				jval.optString("result_location"));
		request = new MitmHttpRequest(jval.optJSONObject("request"));

		srcpageentries = new ArrayList<PageEntry>();
		JSONArray pageArray = jval.optJSONArray("pages");
		for (int i = 0; i < pageArray.length(); i++) {
			srcpageentries.add(new PageEntry(pageArray.optJSONObject(i)));
		}

		destpageentry = new PageEntry(jval.optJSONObject("destination_page"));
	}

	/**
	 * <p>Getter for the field <code>request</code>.</p>
	 */
	public MitmHttpRequest getRequest() {
		return request;
	}

	/**
	 * <p>Getter for the field <code>resultLocation</code>.</p>
	 */
	public ResultLocation getResultLocation() {
		return resultLocation;
	}

	/**
	 * <p>getDestPageEntry.</p>
	 */
	public PageEntry getDestPageEntry() {
		return destpageentry;
	}

	/**
	 * <p>getSrcPageEntries.</p>
	 */
	public List<PageEntry> getSrcPageEntries() {
		return srcpageentries;
	}

	/**
	 * <p>Setter for the field <code>request</code>.</p>
	 *
	 * @param request a {@link edu.uga.cs.clickminer.datamodel.MitmHttpRequest} object.
	 */
	public void setRequest(MitmHttpRequest request) {
		this.request = request;
	}

	/**
	 * <p>Setter for the field <code>resultLocation</code>.</p>
	 *
	 * @param resultLocation a {@link edu.uga.cs.clickminer.datamodel.log.InteractionRecord.ResultLocation} object.
	 */
	public void setResultLocation(ResultLocation resultLocation) {
		this.resultLocation = resultLocation;
	}

	/**
	 * <p>toJSONObject.</p>
	 */
	public JSONObject toJSONObject() {
		JSONObject retval = new JSONObject();
		try {
			retval.put("result_location", resultLocation.name());
			retval.put("request", request.toJSONObject());

			JSONArray pagearray = new JSONArray();
			for (PageEntry pageent : srcpageentries) {
				pagearray.put(pageent.toJSONObject());
			}
			retval.put("pages", pagearray);
			
			retval.put("destination_page", destpageentry.toJSONObject());

		} catch (JSONException e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
		}
		return retval;
	}

	/**
	 * <p>toString.</p>
	 */
	public String toString() {
		String retval =  "Target URL: " + this.request.getUrl() +  "\n"
				+ "Target Locators:" + "\n";
		for(PageEntry page : this.srcpageentries){
			for(FrameEntry frame : page.getFrames()){
				for(ElementEntry element : frame.getEntries()){
					retval += "\t" + element.getLocatorString() + "\n";
				}
			}
		}
		return retval;
	}

	/** {@inheritDoc} */
	@Override
	public String getInteractionUrl() {
		return request.getUrl();
	}

	/** {@inheritDoc} */
	@Override
	public List<ElementLocator> getLocators() {
		List<ElementLocator> retval = new ArrayList<ElementLocator>();
		for(PageEntry page : this.getSrcPageEntries()){
			for(FrameEntry frame : page.getFrames()){
				for(ElementEntry element : frame.getEntries()){
					ElementLocator itargetloc = element.getLocator();
					if(itargetloc.size() > 0){
						retval.add(itargetloc);
					}
				}
			}
		}
		return retval;
	}

	/** {@inheritDoc} */
	@Override
	public double getDelayFromReferer() {
		return request.getDelayFromReferer();
	}
}
