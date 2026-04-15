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

import java.util.Map;

/**
 * Generic report schema that eliminates all custom Java dependencies. Uses only standard OpenMRS
 * metadata and SQL-based definitions. Schema versions: - 2.0: Generic, metadata-driven approach (no
 * custom classes)
 */
public class GenericReportSchema {
	
	/**
	 * Root JSON structure for a report definition
	 */
	public static class ReportDefinition {
		
		private String version = "2.0";
		
		private String uuid;
		
		private String name;
		
		private String description;
		
		private String reportType; // "LINE_LIST" or "AGGREGATE"
		
		private ReportMetadata metadata;
		
		private Parameters parameters;
		
		private DataSetDefinition dataSet;
		
		private ReportDesign design;
		
		private AdvancedFeatures advancedFeatures;
		
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
		
		public GenericReportSchema.DataSetDefinition getDataSet() {
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
		
		public AdvancedFeatures getAdvancedFeatures() {
			return advancedFeatures;
		}
		
		public void setAdvancedFeatures(AdvancedFeatures advancedFeatures) {
			this.advancedFeatures = advancedFeatures;
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
		
		private String type; // "DATE", "LOCATION", "PATIENT", "ENCOUNTER", "CONCEPT"
		
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
	 * Generic dataset definition - metadata-driven
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
	 * Generic row filter - metadata-based cohort definitions
	 */
	public static class RowFilter {
		
		private String type; // "SQL", "OBS", "ENCOUNTER", "PROGRAM", "PATIENT_SEARCH"
		
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
	 * Generic column definition - no custom classes
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
	 * Advanced features configuration for complex reports Supports dynamic column generation,
	 * multi-dimensional indicators, and query optimization
	 */
	public static class AdvancedFeatures {
		
		private DynamicColumnGeneration dynamicColumnGeneration;
		
		private ComplexQueryOptimization complexQueryOptimization;
		
		private IndicatorDataSet indicatorDataSet;
		
		private ResultMapping resultMapping;
		
		public DynamicColumnGeneration getDynamicColumnGeneration() {
			return dynamicColumnGeneration;
		}
		
		public void setDynamicColumnGeneration(DynamicColumnGeneration dynamicColumnGeneration) {
			this.dynamicColumnGeneration = dynamicColumnGeneration;
		}
		
		public ComplexQueryOptimization getComplexQueryOptimization() {
			return complexQueryOptimization;
		}
		
		public void setComplexQueryOptimization(ComplexQueryOptimization complexQueryOptimization) {
			this.complexQueryOptimization = complexQueryOptimization;
		}
		
		public IndicatorDataSet getIndicatorDataSet() {
			return indicatorDataSet;
		}
		
		public void setIndicatorDataSet(IndicatorDataSet indicatorDataSet) {
			this.indicatorDataSet = indicatorDataSet;
		}
		
		public ResultMapping getResultMapping() {
			return resultMapping;
		}
		
		public void setResultMapping(ResultMapping resultMapping) {
			this.resultMapping = resultMapping;
		}
	}
	
	/**
	 * Dynamic column generation configuration for time-series data Used for reports like ART
	 * Register with 72+ monthly columns
	 */
	public static class DynamicColumnGeneration {
		
		private boolean enabled;
		
		private String type; // "MONTHLY_TIME_SERIES", "WEEKLY_TIME_SERIES", "CUSTOM"
		
		private DynamicColumnConfig config;
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
		
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public DynamicColumnConfig getConfig() {
			return config;
		}
		
		public void setConfig(DynamicColumnConfig config) {
			this.config = config;
		}
	}
	
	public static class DynamicColumnConfig {
		
		private String startDate; // Parameter reference like "${startDate}"
		
		private String endDate; // Parameter reference like "${endDate}"
		
		private int maxHistoryMonths;
		
		private DynamicMetric[] metrics;
		
		public String getStartDate() {
			return startDate;
		}
		
		public void setStartDate(String startDate) {
			this.startDate = startDate;
		}
		
		public String getEndDate() {
			return endDate;
		}
		
		public void setEndDate(String endDate) {
			this.endDate = endDate;
		}
		
		public int getMaxHistoryMonths() {
			return maxHistoryMonths;
		}
		
		public void setMaxHistoryMonths(int maxHistoryMonths) {
			this.maxHistoryMonths = maxHistoryMonths;
		}
		
		public DynamicMetric[] getMetrics() {
			return metrics;
		}
		
		public void setMetrics(DynamicMetric[] metrics) {
			this.metrics = metrics;
		}
	}
	
	public static class DynamicMetric {
		
		private String name; // "regimen", "viral_load", "visit_date"
		
		private String query; // SQL query with placeholders like "{target_month}"
		
		private String resultType; // "CONCEPT_NAME", "NUMERIC", "DATE", "COUNT"
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getQuery() {
			return query;
		}
		
		public void setQuery(String query) {
			this.query = query;
		}
		
		public String getResultType() {
			return resultType;
		}
		
		public void setResultType(String resultType) {
			this.resultType = resultType;
		}
	}
	
	/**
	 * Complex query optimization configuration Used for reports like PMTCT Audit Tool with massive
	 * SQL queries
	 */
	public static class ComplexQueryOptimization {
		
		private boolean enabled;
		
		private String queryDecomposition; // "HIERARCHICAL", "PARALLEL", "MATERIALIZED"
		
		private String cachingStrategy; // "MATERIALIZED_VIEWS", "RESULT_CACHE", "NONE"
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
		
		public String getQueryDecomposition() {
			return queryDecomposition;
		}
		
		public void setQueryDecomposition(String queryDecomposition) {
			this.queryDecomposition = queryDecomposition;
		}
		
		public String getCachingStrategy() {
			return cachingStrategy;
		}
		
		public void setCachingStrategy(String cachingStrategy) {
			this.cachingStrategy = cachingStrategy;
		}
	}
	
	/**
	 * Indicator data set configuration for aggregate reports Used for HMIS reports with complex
	 * indicator calculations
	 */
	public static class IndicatorDataSet {
		
		private boolean enabled;
		
		private Indicator[] indicators;
		
		private DimensionDefinition[] dimensionDefinitions;
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
		
		public Indicator[] getIndicators() {
			return indicators;
		}
		
		public void setIndicators(Indicator[] indicators) {
			this.indicators = indicators;
		}
		
		public DimensionDefinition[] getDimensionDefinitions() {
			return dimensionDefinitions;
		}
		
		public void setDimensionDefinitions(DimensionDefinition[] dimensionDefinitions) {
			this.dimensionDefinitions = dimensionDefinitions;
		}
	}
	
	public static class Indicator {
		
		private String key; // "HMIS105_MALARIA_001"
		
		private String type; // "BASE", "COMPOSITE", "TEMPORAL"
		
		private String formula; // For COMPOSITE: "HMIS105_MALARIA_001 / HMIS105_OPD_001 * 100"
		
		private String baseIndicator; // For TEMPORAL: base indicator key
		
		private String[] disaggregation; // Dimension keys like ["age", "gender"]
		
		private String sqlQuery; // For BASE indicators
		
		public String getKey() {
			return key;
		}
		
		public void setKey(String key) {
			this.key = key;
		}
		
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public String getFormula() {
			return formula;
		}
		
		public void setFormula(String formula) {
			this.formula = formula;
		}
		
		public String getBaseIndicator() {
			return baseIndicator;
		}
		
		public void setBaseIndicator(String baseIndicator) {
			this.baseIndicator = baseIndicator;
		}
		
		public String[] getDisaggregation() {
			return disaggregation;
		}
		
		public void setDisaggregation(String[] disaggregation) {
			this.disaggregation = disaggregation;
		}
		
		public String getSqlQuery() {
			return sqlQuery;
		}
		
		public void setSqlQuery(String sqlQuery) {
			this.sqlQuery = sqlQuery;
		}
	}
	
	public static class DimensionDefinition {
		
		private String name; // "age", "gender", "location"
		
		private String type; // "AGE_GROUPS", "CONCEPT", "LOCATION"
		
		private DimensionGroup[] groups;
		
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
		
		public DimensionGroup[] getGroups() {
			return groups;
		}
		
		public void setGroups(DimensionGroup[] groups) {
			this.groups = groups;
		}
	}
	
	public static class DimensionGroup {
		
		private String key; // "0_28_days", "adult", "male"
		
		private String label; // "0-28 Days", "Adult", "Male"
		
		private double minAge;
		
		private double maxAge;
		
		private String ageUnit; // "YEARS", "MONTHS"
		
		private String conceptUuid;
		
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
		
		public double getMinAge() {
			return minAge;
		}
		
		public void setMinAge(double minAge) {
			this.minAge = minAge;
		}
		
		public double getMaxAge() {
			return maxAge;
		}
		
		public void setMaxAge(double maxAge) {
			this.maxAge = maxAge;
		}
		
		public String getAgeUnit() {
			return ageUnit;
		}
		
		public void setAgeUnit(String ageUnit) {
			this.ageUnit = ageUnit;
		}
		
		public String getConceptUuid() {
			return conceptUuid;
		}
		
		public void setConceptUuid(String conceptUuid) {
			this.conceptUuid = conceptUuid;
		}
	}
	
	/**
	 * Result mapping configuration for complex data transformations Used for mother-infant pairing
	 * and concept lookups
	 */
	public static class ResultMapping {
		
		private String[] motherColumns;
		
		private String[] infantColumns;
		
		private DataTransformation[] dataTransformations;
		
		public String[] getMotherColumns() {
			return motherColumns;
		}
		
		public void setMotherColumns(String[] motherColumns) {
			this.motherColumns = motherColumns;
		}
		
		public String[] getInfantColumns() {
			return infantColumns;
		}
		
		public void setInfantColumns(String[] infantColumns) {
			this.infantColumns = infantColumns;
		}
		
		public DataTransformation[] getDataTransformations() {
			return dataTransformations;
		}
		
		public void setDataTransformations(DataTransformation[] dataTransformations) {
			this.dataTransformations = dataTransformations;
		}
	}
	
	public static class DataTransformation {
		
		private String target; // Target column name
		
		private String transformation; // "CONCEPT_NAME_LOOKUP", "AGE_CALCULATION", "DATE_FORMAT"
		
		private String sourceColumn; // Source column for transformation
		
		private String conceptIdColumn; // For CONCEPT_NAME_LOOKUP
		
		public String getTarget() {
			return target;
		}
		
		public void setTarget(String target) {
			this.target = target;
		}
		
		public String getTransformation() {
			return transformation;
		}
		
		public void setTransformation(String transformation) {
			this.transformation = transformation;
		}
		
		public String getSourceColumn() {
			return sourceColumn;
		}
		
		public void setSourceColumn(String sourceColumn) {
			this.sourceColumn = sourceColumn;
		}
		
		public String getConceptIdColumn() {
			return conceptIdColumn;
		}
		
		public void setConceptIdColumn(String conceptIdColumn) {
			this.conceptIdColumn = conceptIdColumn;
		}
	}
	
	/**
	 * Generic data definition - uses only OpenMRS metadata and SQL
	 */
	public static class DataDefinition {
		
		private String type; // "PERSON_ATTRIBUTE", "IDENTIFIER", "OBS", "ENCOUNTER", "PROGRAM", "SQL"
		
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
	 * Generic converter - only built-in OpenMRS converters
	 */
	public static class Converter {
		
		private String type; // "BIRTHDATE_AGE", "CONCEPT_NAME", "OBJECT_FORMATTER", "NULL_VALUE"
		
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
	 * Report design configuration
	 */
	public static class ReportDesign {
		
		private String type; // "excel", "json", "csv"
		
		private String template; // Template filename or JSON structure
		
		private Map<String, String> properties;
		
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
		
		public Map<String, String> getProperties() {
			return properties;
		}
		
		public void setProperties(Map<String, String> properties) {
			this.properties = properties;
		}
	}
}
