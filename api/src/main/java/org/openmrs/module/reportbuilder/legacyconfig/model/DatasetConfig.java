package org.openmrs.module.reportbuilder.legacyconfig.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetConfig {
	
	private String type;
	
	private String name;
	
	private String parametersRef;
	
	private List<ParameterConfig> parameters = new ArrayList<ParameterConfig>();
	
	private List<DimensionRefConfig> dimensions = new ArrayList<DimensionRefConfig>();
	
	private List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
	
	/**
	 * Used by line-list / patient-data style datasets. Kept generic for now because filter
	 * structures vary.
	 */
	private Map<String, Object> rowFilter = new LinkedHashMap<String, Object>();
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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
	
	public List<DimensionRefConfig> getDimensions() {
		return dimensions;
	}
	
	public void setDimensions(List<DimensionRefConfig> dimensions) {
		this.dimensions = dimensions;
	}
	
	public List<ColumnConfig> getColumns() {
		return columns;
	}
	
	public void setColumns(List<ColumnConfig> columns) {
		this.columns = columns;
	}
	
	public Map<String, Object> getRowFilter() {
		return rowFilter;
	}
	
	public void setRowFilter(Map<String, Object> rowFilter) {
		this.rowFilter = rowFilter;
	}
}
