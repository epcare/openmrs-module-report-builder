/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reportbuilder.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * Legacy generic report schema that matches the actual JSON structure used in the 114 migrated
 * reports. This schema maintains compatibility with the existing UgandaEMRReports migration format.
 * Key differences from newer schemas: - Uses dataSetDefinitions array instead of single dataSet -
 * Flat categorization fields (category, subcategory, reportType, reportYear, reportScope) - Simpler
 * parameter structure without nested Parameters class - Version format like "1.4-generic"
 */
public class LegacyGenericReportSchema {
	
	/**
	 * Root JSON structure for a report definition - matches actual migrated report structure
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ReportDefinition {
		
		private String version;
		
		private String uuid;
		
		private String name;
		
		private String description;
		
		private Parameter[] parameters;
		
		private BaseCohortDefinition baseCohortDefinition;
		
		private DataSetDefinition[] dataSetDefinitions;
		
		// Categorization fields (flat structure)
		private String category;
		
		private String subcategory;
		
		private String reportType;
		
		private String reportYear;
		
		private String reportScope;
		
		// Getters and setters
		public String getVersion() {
			return version;
		}
		
		public void setVersion(String version) {
			this.version = version;
		}
		
		public String getUuid() {
			return uuid;
		}
		
		public void setUuid(String uuid) {
			this.uuid = uuid;
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
		
		public Parameter[] getParameters() {
			return parameters;
		}
		
		public void setParameters(Parameter[] parameters) {
			this.parameters = parameters;
		}
		
		public BaseCohortDefinition getBaseCohortDefinition() {
			return baseCohortDefinition;
		}
		
		public void setBaseCohortDefinition(BaseCohortDefinition baseCohortDefinition) {
			this.baseCohortDefinition = baseCohortDefinition;
		}
		
		public DataSetDefinition[] getDataSetDefinitions() {
			return dataSetDefinitions;
		}
		
		public void setDataSetDefinitions(DataSetDefinition[] dataSetDefinitions) {
			this.dataSetDefinitions = dataSetDefinitions;
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
	}
	
	/**
	 * Parameter definition
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Parameter {
		
		private String name;
		
		private String label;
		
		private String type;
		
		private boolean required;
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
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
		
		public boolean isRequired() {
			return required;
		}
		
		public void setRequired(boolean required) {
			this.required = required;
		}
	}
	
	/**
	 * Base cohort definition
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class BaseCohortDefinition {
		
		private String type;
		
		private String name;
		
		private Map<String, Object> config;
		
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
		
		public Map<String, Object> getConfig() {
			return config;
		}
		
		public void setConfig(Map<String, Object> config) {
			this.config = config;
		}
	}
	
	/**
	 * Dataset definition
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class DataSetDefinition {
		
		private String name;
		
		private String type; // "PATIENT_DATA_SET", "SQL_DATA_SET", "INDICATOR_DATA_SET"
		
		private RowFilter rowFilter;
		
		private Column[] columns;
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public RowFilter getRowFilter() {
			return rowFilter;
		}
		
		public void setRowFilter(RowFilter rowFilter) {
			this.rowFilter = rowFilter;
		}
		
		public Column[] getColumns() {
			return columns;
		}
		
		public void setColumns(Column[] columns) {
			this.columns = columns;
		}
	}
	
	/**
	 * Row filter
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class RowFilter {
		
		private String type;
		
		private String name;
		
		private Map<String, Object> config;
		
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
		
		public Map<String, Object> getConfig() {
			return config;
		}
		
		public void setConfig(Map<String, Object> config) {
			this.config = config;
		}
	}
	
	/**
	 * Column definition
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Column {
		
		private String name;
		
		private String key;
		
		private DataDefinition dataDefinition;
		
		private Converter converter;
		
		private Map<String, Object> _metadata;
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getKey() {
			return key;
		}
		
		public void setKey(String key) {
			this.key = key;
		}
		
		public DataDefinition getDataDefinition() {
			return dataDefinition;
		}
		
		public void setDataDefinition(DataDefinition dataDefinition) {
			this.dataDefinition = dataDefinition;
		}
		
		public Converter getConverter() {
			return converter;
		}
		
		public void setConverter(Converter converter) {
			this.converter = converter;
		}
		
		public Map<String, Object> get_metadata() {
			return _metadata;
		}
		
		public void set_metadata(Map<String, Object> _metadata) {
			this._metadata = _metadata;
		}
		
		/**
		 * Get the position from metadata for column ordering.
		 * 
		 * @return the position value, or null if not set
		 */
		public Integer getPosition() {
			if (_metadata != null && _metadata.containsKey("position")) {
				Object pos = _metadata.get("position");
				if (pos instanceof Number) {
					return ((Number) pos).intValue();
				}
				if (pos instanceof String) {
					try {
						return Integer.parseInt((String) pos);
					}
					catch (NumberFormatException e) {
						// ignore
					}
				}
			}
			return null;
		}
	}
	
	/**
	 * Data definition
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class DataDefinition {
		
		private String type;
		
		private Map<String, Object> config;
		
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public Map<String, Object> getConfig() {
			return config;
		}
		
		public void setConfig(Map<String, Object> config) {
			this.config = config;
		}
	}
	
	/**
	 * Converter definition
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Converter {
		
		private String type;
		
		private Map<String, Object> config;
		
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public Map<String, Object> getConfig() {
			return config;
		}
		
		public void setConfig(Map<String, Object> config) {
			this.config = config;
		}
	}
}
