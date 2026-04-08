package org.openmrs.module.reportbuilder.legacyconfig.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LegacyReportCatalogEntry {
	
	private String key;
	
	private String report;
	
	private List<String> cohorts = new ArrayList<String>();
	
	private List<String> datasets = new ArrayList<String>();
	
	private List<String> designs = new ArrayList<String>();
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getReport() {
		return report;
	}
	
	public void setReport(String report) {
		this.report = report;
	}
	
	public List<String> getCohorts() {
		return cohorts;
	}
	
	public void setCohorts(List<String> cohorts) {
		this.cohorts = cohorts;
	}
	
	public List<String> getDatasets() {
		return datasets;
	}
	
	public void setDatasets(List<String> datasets) {
		this.datasets = datasets;
	}
	
	public List<String> getDesigns() {
		return designs;
	}
	
	public void setDesigns(List<String> designs) {
		this.designs = designs;
	}
}
