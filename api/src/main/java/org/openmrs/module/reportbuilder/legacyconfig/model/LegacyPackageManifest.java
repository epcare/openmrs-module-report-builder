package org.openmrs.module.reportbuilder.legacyconfig.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated Replaced by LegacyReportCatalog and LegacyReportCatalogEntry for flat catalog-based
 *             legacy report loading.
 */
public class LegacyPackageManifest {
	
	private Integer version;
	
	private String kind;
	
	private String status;
	
	private String report;
	
	private List<String> cohorts = new ArrayList<String>();
	
	private List<String> datasets = new ArrayList<String>();
	
	private List<String> designs = new ArrayList<String>();
	
	private String templatesRoot;
	
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
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
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
	
	public String getTemplatesRoot() {
		return templatesRoot;
	}
	
	public void setTemplatesRoot(String templatesRoot) {
		this.templatesRoot = templatesRoot;
	}
}
