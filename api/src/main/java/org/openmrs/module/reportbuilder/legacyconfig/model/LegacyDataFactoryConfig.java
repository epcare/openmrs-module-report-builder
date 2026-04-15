package org.openmrs.module.reportbuilder.legacyconfig.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LegacyDataFactoryConfig {
	
	private Map<String, AliasMethodConfig> concepts = new LinkedHashMap<String, AliasMethodConfig>();
	
	private Map<String, AliasMethodConfig> encounterTypes = new LinkedHashMap<String, AliasMethodConfig>();
	
	private Map<String, AliasMethodConfig> patientDataSources = new LinkedHashMap<String, AliasMethodConfig>();
	
	private Map<String, AliasMethodConfig> converters = new LinkedHashMap<String, AliasMethodConfig>();
	
	private Map<String, AliasMethodConfig> dimensions = new LinkedHashMap<String, AliasMethodConfig>();
	
	private Map<String, FactoryMethodConfig> rowFilters = new LinkedHashMap<String, FactoryMethodConfig>();
	
	public Map<String, AliasMethodConfig> getConcepts() {
		return concepts;
	}
	
	public void setConcepts(Map<String, AliasMethodConfig> concepts) {
		this.concepts = concepts;
	}
	
	public Map<String, AliasMethodConfig> getEncounterTypes() {
		return encounterTypes;
	}
	
	public void setEncounterTypes(Map<String, AliasMethodConfig> encounterTypes) {
		this.encounterTypes = encounterTypes;
	}
	
	public Map<String, AliasMethodConfig> getPatientDataSources() {
		return patientDataSources;
	}
	
	public void setPatientDataSources(Map<String, AliasMethodConfig> patientDataSources) {
		this.patientDataSources = patientDataSources;
	}
	
	public Map<String, AliasMethodConfig> getConverters() {
		return converters;
	}
	
	public void setConverters(Map<String, AliasMethodConfig> converters) {
		this.converters = converters;
	}
	
	public Map<String, AliasMethodConfig> getDimensions() {
		return dimensions;
	}
	
	public void setDimensions(Map<String, AliasMethodConfig> dimensions) {
		this.dimensions = dimensions;
	}
	
	public Map<String, FactoryMethodConfig> getRowFilters() {
		return rowFilters;
	}
	
	public void setRowFilters(Map<String, FactoryMethodConfig> rowFilters) {
		this.rowFilters = rowFilters;
	}
}
