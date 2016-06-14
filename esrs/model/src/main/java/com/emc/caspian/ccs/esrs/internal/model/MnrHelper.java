package com.emc.caspian.ccs.esrs.internal.model;

import net.sf.json.JSONObject;


public class MnrHelper {
	private String reportName;
	private JSONObject reportString;

	public String getReportName() {
		return reportName;
	}
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	public JSONObject getReportString() {
		return reportString;
	}
	public void setReportString(JSONObject reportString) {
		this.reportString = reportString;
	}
}
