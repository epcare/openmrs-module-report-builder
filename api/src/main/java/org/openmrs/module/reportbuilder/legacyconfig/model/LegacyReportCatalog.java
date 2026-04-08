package org.openmrs.module.reportbuilder.legacyconfig.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LegacyReportCatalog {
	
	private Integer version;
	
	private String kind;
	
	private List<LegacyReportCatalogEntry> reports = new ArrayList<LegacyReportCatalogEntry>();
	
	public Integer getVersion() {
		return version;
	}
	
	public void setVersion(Integer version) {
		this.version = version;
	}
	
	public String getKind() {
		return kind;
	}
	
	public void setKind(String kind) {
		this.kind = kind;
	}
	
	public List<LegacyReportCatalogEntry> getReports() {
		return reports;
	}
	
	public void setReports(List<LegacyReportCatalogEntry> reports) {
		this.reports = reports;
	}
}
