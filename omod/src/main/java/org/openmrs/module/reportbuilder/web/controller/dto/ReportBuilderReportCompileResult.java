package org.openmrs.module.reportbuilder.web.controller.dto;

public class ReportBuilderReportCompileResult {
	
	private String reportUuid;
	
	private String reportDefinitionUuid;
	
	private String reportDefinitionName;
	
	private String reportDesignPath;
	
	private Boolean compiled;
	
	private String category;
	
	private Boolean addedToLibrary;
	
	private String reportLibraryUuid;
	
	private String compiledJson;
	
	public String getReportUuid() {
		return reportUuid;
	}
	
	public void setReportUuid(String reportUuid) {
		this.reportUuid = reportUuid;
	}
	
	public String getReportDefinitionUuid() {
		return reportDefinitionUuid;
	}
	
	public void setReportDefinitionUuid(String reportDefinitionUuid) {
		this.reportDefinitionUuid = reportDefinitionUuid;
	}
	
	public String getReportDefinitionName() {
		return reportDefinitionName;
	}
	
	public void setReportDefinitionName(String reportDefinitionName) {
		this.reportDefinitionName = reportDefinitionName;
	}
	
	public String getReportDesignPath() {
		return reportDesignPath;
	}
	
	public void setReportDesignPath(String reportDesignPath) {
		this.reportDesignPath = reportDesignPath;
	}
	
	public Boolean getCompiled() {
		return compiled;
	}
	
	public void setCompiled(Boolean compiled) {
		this.compiled = compiled;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public Boolean getAddedToLibrary() {
		return addedToLibrary;
	}
	
	public void setAddedToLibrary(Boolean addedToLibrary) {
		this.addedToLibrary = addedToLibrary;
	}
	
	public String getReportLibraryUuid() {
		return reportLibraryUuid;
	}
	
	public void setReportLibraryUuid(String reportLibraryUuid) {
		this.reportLibraryUuid = reportLibraryUuid;
	}
	
	public String getCompiledJson() {
		return compiledJson;
	}
	
	public void setCompiledJson(String compiledJson) {
		this.compiledJson = compiledJson;
	}
}
