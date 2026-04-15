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

/**
 * Defines the JSON schema structure for reports. This serves as the contract between Java code and
 * JSON configuration. Schema versions: - 1.0: Initial version supporting line-list and aggregate
 * reports
 */
public class ReportSchema {
	
	/**
	 * Root JSON structure for a report definition
	 */
	public static class ReportDefinition {
		
		private String version = "1.0";
		
		private String uuid;
		
		private String name;
		
		private String description;
		
		private String reportType; // "LINE_LIST" or "AGGREGATE"
		
		private ReportMetadata metadata;
		
		private Parameters parameters;
		
		private DataSetDefinition dataSet;
		
		private ReportDesign design;
		
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
		
		public String getReportType() {
			return reportType;
		}
		
		public void setReportType(String reportType) {
			this.reportType = reportType;
		}
		
		public ReportMetadata getMetadata() {
			return metadata;
		}
		
		public void setMetadata(ReportMetadata metadata) {
			this.metadata = metadata;
		}
		
		public Parameters getParameters() {
			return parameters;
		}
		
		public void setParameters(Parameters parameters) {
			this.parameters = parameters;
		}
		
		public DataSetDefinition getDataSet() {
			return dataSet;
		}
		
		public void setDataSet(DataSetDefinition dataSet) {
			this.dataSet = dataSet;
		}
		
		public ReportDesign getDesign() {
			return design;
		}
		
		public void setDesign(ReportDesign design) {
			this.design = design;
		}
	}
	
	/**
	 * Report metadata
	 */
	public static class ReportMetadata {
		
		private String category;
		
		private String[] tags;
		
		private boolean retired = false;
		
		public String getCategory() {
			return category;
		}
		
		public void setCategory(String category) {
			this.category = category;
		}
		
		public String[] getTags() {
			return tags;
		}
		
		public void setTags(String[] tags) {
			this.tags = tags;
		}
		
		public boolean isRetired() {
			return retired;
		}
		
		public void setRetired(boolean retired) {
			this.retired = retired;
		}
	}
	
	/**
	 * Report parameters
	 */
	public static class Parameters {
		
		private Parameter[] parameters;
		
		public Parameter[] getParameters() {
			return parameters;
		}
		
		public void setParameters(Parameter[] parameters) {
			this.parameters = parameters;
		}
	}
	
	public static class Parameter {
		
		private String name;
		
		private String label;
		
		private String type; // "java.util.Date", "java.lang.Integer", etc.
		
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
	}
	
	/**
	 * Dataset definition - supports both line-list and aggregate
	 */
	public static class DataSetDefinition {
		
		private String name;
		
		private RowFilter rowFilter;
		
		private Column[] columns;
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
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
	 * Row filter for line-list reports
	 */
	public static class RowFilter {
		
		private String type; // "cohort", "sql", "custom"
		
		private String className; // Fully qualified Java class for custom implementations
		
		private java.util.Map<String, Object> parameters;
		
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public String getClassName() {
			return className;
		}
		
		public void setClassName(String className) {
			this.className = className;
		}
		
		public java.util.Map<String, Object> getParameters() {
			return parameters;
		}
		
		public void setParameters(java.util.Map<String, Object> parameters) {
			this.parameters = parameters;
		}
	}
	
	/**
	 * Column definition
	 */
	public static class Column {
		
		private String name;
		
		private String key;
		
		private DataDefinition dataDefinition;
		
		private Converter converter;
		
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
	}
	
	/**
	 * Data definition
	 */
	public static class DataDefinition {
		
		private String type; // "builtin", "custom", "person", "patient", "obs"
		
		private String className; // Fully qualified Java class for custom implementations
		
		private String propertyName; // For builtin types like "preferredName"
		
		private java.util.Map<String, Object> config;
		
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public String getClassName() {
			return className;
		}
		
		public void setClassName(String className) {
			this.className = className;
		}
		
		public String getPropertyName() {
			return propertyName;
		}
		
		public void setPropertyName(String propertyName) {
			this.propertyName = propertyName;
		}
		
		public java.util.Map<String, Object> getConfig() {
			return config;
		}
		
		public void setConfig(java.util.Map<String, Object> config) {
			this.config = config;
		}
	}
	
	/**
	 * Data converter
	 */
	public static class Converter {
		
		private String type; // "builtin", "custom", "chained", "collection"
		
		private String className; // Fully qualified Java class for custom implementations
		
		private String propertyName; // For builtin property converters
		
		private Converter[] converters; // For chained converters
		
		private java.util.Map<String, Object> config;
		
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public String getClassName() {
			return className;
		}
		
		public void setClassName(String className) {
			this.className = className;
		}
		
		public String getPropertyName() {
			return propertyName;
		}
		
		public void setPropertyName(String propertyName) {
			this.propertyName = propertyName;
		}
		
		public Converter[] getConverters() {
			return converters;
		}
		
		public void setConverters(Converter[] converters) {
			this.converters = converters;
		}
		
		public java.util.Map<String, Object> getConfig() {
			return config;
		}
		
		public void setConfig(java.util.Map<String, Object> config) {
			this.config = config;
		}
	}
	
	/**
	 * Report design configuration
	 */
	public static class ReportDesign {
		
		private String type; // "excel", "json", "csv"
		
		private String template; // Template filename or JSON structure
		
		private java.util.Map<String, String> properties;
		
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public String getTemplate() {
			return template;
		}
		
		public void setTemplate(String template) {
			this.template = template;
		}
		
		public java.util.Map<String, String> getProperties() {
			return properties;
		}
		
		public void setProperties(java.util.Map<String, String> properties) {
			this.properties = properties;
		}
	}
}
