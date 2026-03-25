package org.openmrs.module.reportbuilder.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsMetadata;

import javax.persistence.*;

@Entity
@Table(name = "report_builder_etl_source")
public class ETLSource extends BaseOpenmrsMetadata {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "etl_source_id")
	private Integer etlSourceId;
	
	@Column(name = "code", length = 100)
	private String code;
	
	@Column(name = "table_patterns", columnDefinition = "TEXT")
	private String tablePatterns;
	
	@Column(name = "schema_name", length = 255)
	private String schemaName;
	
	@Column(name = "source_type", length = 100)
	private String sourceType;
	
	@Column(name = "active", nullable = false)
	private Boolean active = true;
	
	// ========================
	// ID mapping (OpenMRS)
	// ========================
	
	@Override
	public Integer getId() {
		return etlSourceId;
	}
	
	@Override
	public void setId(Integer id) {
		this.etlSourceId = id;
	}
	
	// ========================
	// Getters & Setters
	// ========================
	
	public Integer getEtlSourceId() {
		return etlSourceId;
	}
	
	public void setEtlSourceId(Integer etlSourceId) {
		this.etlSourceId = etlSourceId;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getTablePatterns() {
		return tablePatterns;
	}
	
	public void setTablePatterns(String tablePatterns) {
		this.tablePatterns = tablePatterns;
	}
	
	public String getSchemaName() {
		return schemaName;
	}
	
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	
	public String getSourceType() {
		return sourceType;
	}
	
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
	
	public Boolean getActive() {
		return active;
	}
	
	public void setActive(Boolean active) {
		this.active = active;
	}
}
