package org.openmrs.module.reportbuilder.legacyconfig.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ColumnConfig {
	
	private String key;
	
	@JsonAlias({ "name" })
	private String label;
	
	private String type;
	
	private Object source;
	
	private Object converter;
	
	private IndicatorConfig indicator;
	
	private Map<String, String> dimensionOptions = new HashMap<String, String>();
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Object getSource() {
		return source;
	}
	
	public void setSource(Object source) {
		this.source = source;
	}
	
	public Object getConverter() {
		return converter;
	}
	
	public void setConverter(Object converter) {
		this.converter = converter;
	}
	
	public IndicatorConfig getIndicator() {
		return indicator;
	}
	
	public void setIndicator(IndicatorConfig indicator) {
		this.indicator = indicator;
	}
	
	public Map<String, String> getDimensionOptions() {
		return dimensionOptions;
	}
	
	public void setDimensionOptions(Map<String, String> dimensionOptions) {
		this.dimensionOptions = dimensionOptions;
	}
	
	public String getSourceKey() {
		return source instanceof String ? ((String) source) : null;
	}
	
	public String getConverterKey() {
		return converter instanceof String ? ((String) converter) : null;
	}
}
