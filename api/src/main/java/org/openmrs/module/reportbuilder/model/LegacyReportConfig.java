package org.openmrs.module.reportbuilder.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete legacy report configuration matching the frontend specification. This model supports the
 * Legacy Report Editor frontend with multi-type indicators, complex disaggregation, and SQL
 * datasets.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegacyReportConfig {
	
	private String uuid;
	
	private String key; // Report key/identifier for frontend compatibility
	
	private String name;
	
	private String description;
	
	private String version;
	
	private String status = "ACTIVE";
	
	// Parameters
	private List<Parameter> parameters = new ArrayList<Parameter>();
	
	// Advanced Features
	private AdvancedFeatures advancedFeatures = new AdvancedFeatures();
	
	// Dataset Definitions
	private List<DataSetDefinition> dataSetDefinitions = new ArrayList<DataSetDefinition>();
	
	// Classification
	private String category;
	
	private String subcategory;
	
	private String reportType;
	
	private String reportYear;
	
	private String reportScope;
	
	// Timestamps
	private String dateCreated;
	
	private String dateChanged;
	
	// Design configuration (for frontend compatibility)
	private Map<String, Object> jsonTemplateConfig = new HashMap<String, Object>();
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Parameter {
		
		private String name;
		
		private String label;
		
		private String type; // DATE, LOCATION, STRING, NUMBER, BOOLEAN
		
		public Parameter() {
		}
		
		public Parameter(String name, String label, String type) {
			this.name = name;
			this.label = label;
			this.type = type;
		}
		
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
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AdvancedFeatures {
		
		private IndicatorDataSet indicatorDataSet = new IndicatorDataSet();
		
		public IndicatorDataSet getIndicatorDataSet() {
			return indicatorDataSet;
		}
		
		public void setIndicatorDataSet(IndicatorDataSet indicatorDataSet) {
			this.indicatorDataSet = indicatorDataSet;
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class IndicatorDataSet {
		
		private boolean enabled = false;
		
		private List<Indicator> indicators = new ArrayList<Indicator>();
		
		private List<DimensionDefinition> dimensionDefinitions = new ArrayList<DimensionDefinition>();
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
		
		public List<Indicator> getIndicators() {
			return indicators;
		}
		
		public void setIndicators(List<Indicator> indicators) {
			this.indicators = indicators;
		}
		
		public List<DimensionDefinition> getDimensionDefinitions() {
			return dimensionDefinitions;
		}
		
		public void setDimensionDefinitions(List<DimensionDefinition> dimensionDefinitions) {
			this.dimensionDefinitions = dimensionDefinitions;
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Indicator {
		
		private String key; // Unique identifier
		
		private String type; // BASE, COMPOSITE, TEMPORAL
		
		private String name;
		
		private String description;
		
		// BASE indicator fields
		private String sqlQuery;
		
		private List<String> disaggregation = new ArrayList<String>();
		
		// COMPOSITE indicator fields
		private String formula;
		
		// TEMPORAL indicator fields
		private String baseIndicator;
		
		private List<String> timePeriods = new ArrayList<String>();
		
		// Additional properties
		private Map<String, Object> properties = new HashMap<String, Object>();
		
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
		
		public String getSqlQuery() {
			return sqlQuery;
		}
		
		public void setSqlQuery(String sqlQuery) {
			this.sqlQuery = sqlQuery;
		}
		
		public List<String> getDisaggregation() {
			return disaggregation;
		}
		
		public void setDisaggregation(List<String> disaggregation) {
			this.disaggregation = disaggregation;
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
		
		public List<String> getTimePeriods() {
			return timePeriods;
		}
		
		public void setTimePeriods(List<String> timePeriods) {
			this.timePeriods = timePeriods;
		}
		
		public Map<String, Object> getProperties() {
			return properties;
		}
		
		public void setProperties(Map<String, Object> properties) {
			this.properties = properties;
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class DimensionDefinition {
		
		private String name; // Dimension name (e.g., "age", "gender")
		
		private String type; // AGE_GROUPS, CONCEPT
		
		private List<DimensionGroup> groups = new ArrayList<DimensionGroup>();
		
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
		
		public List<DimensionGroup> getGroups() {
			return groups;
		}
		
		public void setGroups(List<DimensionGroup> groups) {
			this.groups = groups;
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class DimensionGroup {
		
		private String key; // Group key (e.g., "under_5", "male")
		
		private String label; // Display label
		
		// AGE_GROUPS specific fields
		private Double minAge;
		
		private Double maxAge;
		
		private String ageUnit; // YEARS, MONTHS, DAYS
		
		// CONCEPT specific fields
		private String conceptUuid; // OpenMRS concept UUID
		
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
		
		public Double getMinAge() {
			return minAge;
		}
		
		public void setMinAge(Double minAge) {
			this.minAge = minAge;
		}
		
		public Double getMaxAge() {
			return maxAge;
		}
		
		public void setMaxAge(Double maxAge) {
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
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class DataSetDefinition {
		
		private String name; // Dataset name
		
		private String type; // SQL_DATA_SET, INDICATOR_DATA_SET, COHORT_DATA_SET
		
		private DataSetConfig config = new DataSetConfig();
		
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
		
		public DataSetConfig getConfig() {
			return config;
		}
		
		public void setConfig(DataSetConfig config) {
			this.config = config;
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class DataSetConfig {
		
		private String sql; // SQL query for SQL_DATA_SET type
		
		private Map<String, Object> parameters = new HashMap<String, Object>();
		
		public String getSql() {
			return sql;
		}
		
		public void setSql(String sql) {
			this.sql = sql;
		}
		
		public Map<String, Object> getParameters() {
			return parameters;
		}
		
		public void setParameters(Map<String, Object> parameters) {
			this.parameters = parameters;
		}
	}
	
	// Getters and Setters for main class
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
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public List<Parameter> getParameters() {
		return parameters;
	}
	
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
	
	public AdvancedFeatures getAdvancedFeatures() {
		return advancedFeatures;
	}
	
	public void setAdvancedFeatures(AdvancedFeatures advancedFeatures) {
		this.advancedFeatures = advancedFeatures;
	}
	
	public List<DataSetDefinition> getDataSetDefinitions() {
		return dataSetDefinitions;
	}
	
	public void setDataSetDefinitions(List<DataSetDefinition> dataSetDefinitions) {
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
	
	public String getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public String getDateChanged() {
		return dateChanged;
	}
	
	public void setDateChanged(String dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	public Map<String, Object> getJsonTemplateConfig() {
		return jsonTemplateConfig;
	}
	
	public void setJsonTemplateConfig(Map<String, Object> jsonTemplateConfig) {
		this.jsonTemplateConfig = jsonTemplateConfig;
	}
}
