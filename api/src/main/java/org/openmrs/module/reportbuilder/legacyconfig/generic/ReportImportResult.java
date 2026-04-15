package org.openmrs.module.reportbuilder.legacyconfig.generic;

/**
 * Result of importing a single generic report from external directory. Tracks success/failure
 * status and provides details about the import process.
 */
public class ReportImportResult {
	
	private String fileName;
	
	private String reportName;
	
	private String reportUuid;
	
	private String category;
	
	private String subcategory;
	
	private boolean success;
	
	private String errorMessage;
	
	private long importTimestamp;
	
	// Additional metadata for our report structure
	private String reportType;
	
	private String reportYear;
	
	private String reportScope;
	
	public ReportImportResult() {
		this.importTimestamp = System.currentTimeMillis();
	}
	
	public ReportImportResult(String fileName, boolean success, String errorMessage) {
		this();
		this.fileName = fileName;
		this.success = success;
		this.errorMessage = errorMessage;
	}
	
	// Getters and setters
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getReportName() {
		return reportName;
	}
	
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	public String getReportUuid() {
		return reportUuid;
	}
	
	public void setReportUuid(String reportUuid) {
		this.reportUuid = reportUuid;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getSubcategory() {
		return subcategory;
	}
	
	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public long getImportTimestamp() {
		return importTimestamp;
	}
	
	public void setImportTimestamp(long importTimestamp) {
		this.importTimestamp = importTimestamp;
	}
	
	public String getReportType() {
		return reportType;
	}
	
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	
	public String getReportYear() {
		return reportYear;
	}
	
	public void setReportYear(String reportYear) {
		this.reportYear = reportYear;
	}
	
	public String getReportScope() {
		return reportScope;
	}
	
	public void setReportScope(String reportScope) {
		this.reportScope = reportScope;
	}
	
	@Override
	public String toString() {
		return "ReportImportResult{" + "fileName='" + fileName + '\'' + ", reportName='" + reportName + '\''
		        + ", category='" + category + '\'' + ", subcategory='" + subcategory + '\'' + ", success=" + success + '}';
	}
}
