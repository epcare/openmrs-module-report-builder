package org.openmrs.module.reportbuilder.legacyconfig.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportConfig {
	
	private String uuid;
	
	private String key;
	
	private String name;
	
	private String description;
	
	private String status = "LEGACY";
	
	private String parametersRef;
	
	private List<ParameterConfig> parameters = new ArrayList<ParameterConfig>();
	
	private List<DatasetRefConfig> datasets = new ArrayList<DatasetRefConfig>();
	
	/**
	 * Legacy fallback only. Prefer embedded design fields below.
	 */
	private List<DesignRefConfig> designs = new ArrayList<DesignRefConfig>();
	
	/**
	 * Legacy generic fallback fields.
	 */
	private String template;
	
	private String designUuid;
	
	private String designName;
	
	/**
	 * Embedded Excel design fields.
	 */
	private String excelTemplate;
	
	private String excelDesignUuid;
	
	private String excelDesignName;
	
	/**
	 * Embedded JSON design fields. jsonTemplate is legacy file-path style. jsonTemplateConfig is
	 * the new embedded style.
	 */
	private String jsonTemplate;
	
	private String jsonDesignUuid;
	
	private String jsonDesignName;
	
	private Map<String, Object> jsonTemplateConfig = new LinkedHashMap<String, Object>();
	
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getParametersRef() {
		return parametersRef;
	}
	
	public void setParametersRef(String parametersRef) {
		this.parametersRef = parametersRef;
	}
	
	public List<ParameterConfig> getParameters() {
		return parameters;
	}
	
	public void setParameters(List<ParameterConfig> parameters) {
		this.parameters = parameters;
	}
	
	public List<DatasetRefConfig> getDatasets() {
		return datasets;
	}
	
	public void setDatasets(List<DatasetRefConfig> datasets) {
		this.datasets = datasets;
	}
	
	public List<DesignRefConfig> getDesigns() {
		return designs;
	}
	
	public void setDesigns(List<DesignRefConfig> designs) {
		this.designs = designs;
	}
	
	public String getTemplate() {
		return template;
	}
	
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public String getDesignUuid() {
		return designUuid;
	}
	
	public void setDesignUuid(String designUuid) {
		this.designUuid = designUuid;
	}
	
	public String getDesignName() {
		return designName;
	}
	
	public void setDesignName(String designName) {
		this.designName = designName;
	}
	
	public String getExcelTemplate() {
		return excelTemplate;
	}
	
	public void setExcelTemplate(String excelTemplate) {
		this.excelTemplate = excelTemplate;
	}
	
	public String getExcelDesignUuid() {
		return excelDesignUuid;
	}
	
	public void setExcelDesignUuid(String excelDesignUuid) {
		this.excelDesignUuid = excelDesignUuid;
	}
	
	public String getExcelDesignName() {
		return excelDesignName;
	}
	
	public void setExcelDesignName(String excelDesignName) {
		this.excelDesignName = excelDesignName;
	}
	
	public String getJsonTemplate() {
		return jsonTemplate;
	}
	
	public void setJsonTemplate(String jsonTemplate) {
		this.jsonTemplate = jsonTemplate;
	}
	
	public String getJsonDesignUuid() {
		return jsonDesignUuid;
	}
	
	public void setJsonDesignUuid(String jsonDesignUuid) {
		this.jsonDesignUuid = jsonDesignUuid;
	}
	
	public String getJsonDesignName() {
		return jsonDesignName;
	}
	
	public void setJsonDesignName(String jsonDesignName) {
		this.jsonDesignName = jsonDesignName;
	}
	
	public Map<String, Object> getJsonTemplateConfig() {
		return jsonTemplateConfig;
	}
	
	public void setJsonTemplateConfig(Map<String, Object> jsonTemplateConfig) {
		this.jsonTemplateConfig = jsonTemplateConfig;
	}
}
