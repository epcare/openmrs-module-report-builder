package org.openmrs.module.reportbuilder.model;

import org.openmrs.BaseOpenmrsMetadata;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Hibernate entity for legacy reports stored in the database. Maps to the legacy_report table and
 * stores the complete configuration as JSON.
 */
@Entity
@Table(name = "legacy_report")
@AttributeOverrides({ @AttributeOverride(name = "description", column = @Column(name = "description", length = 2000)) })
public class LegacyReport extends BaseOpenmrsMetadata {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "legacy_report_id")
	private Integer id;
	
	@Column(name = "version")
	private String version;
	
	@Column(name = "category")
	private String category;
	
	@Column(name = "subcategory")
	private String subcategory;
	
	@Column(name = "report_type")
	private String reportType;
	
	@Column(name = "report_year")
	private String reportYear;
	
	@Column(name = "report_scope")
	private String reportScope;
	
	@Column(name = "status", length = 50)
	private String status = "ACTIVE";
	
	@Column(name = "config_json", nullable = false, columnDefinition = "TEXT")
	private String configJson;
	
	public LegacyReport() {
	}
	
	public LegacyReport(String uuid) {
		setUuid(uuid);
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
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
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getConfigJson() {
		return configJson;
	}
	
	public void setConfigJson(String configJson) {
		this.configJson = configJson;
	}
	
	@Override
	public Integer getId() {
		return id;
	}
	
	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "LegacyReport{" + "uuid='" + getUuid() + '\'' + ", name='" + getName() + '\'' + ", version='" + version
		        + '\'' + ", status='" + status + '\'' + '}';
	}
}
