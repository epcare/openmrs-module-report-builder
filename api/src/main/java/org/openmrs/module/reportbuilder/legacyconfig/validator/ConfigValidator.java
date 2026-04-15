package org.openmrs.module.reportbuilder.legacyconfig.validator;

import org.openmrs.module.reportbuilder.legacyconfig.model.*;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ConfigValidator {
	
	public void validateCatalog(LegacyReportCatalog catalog) {
		if (catalog == null)
			throw new IllegalArgumentException("Catalog is required");
		if (catalog.getReports() == null || catalog.getReports().isEmpty())
			throw new IllegalArgumentException("Catalog must contain at least one report");
		Set<String> keys = new HashSet<String>();
		for (LegacyReportCatalogEntry entry : catalog.getReports()) {
			validateCatalogEntry(entry);
			if (!keys.add(entry.getKey()))
				throw new IllegalArgumentException("Duplicate catalog entry key: " + entry.getKey());
		}
	}
	
	public void validateCatalogEntry(LegacyReportCatalogEntry entry) {
		if (entry == null)
			throw new IllegalArgumentException("Catalog entry is required");
		require(entry.getKey(), "Catalog entry key is required");
		require(entry.getReport(), "Catalog entry report path is required");
		if (entry.getCohorts() != null)
			for (String c : entry.getCohorts())
				require(c, "Catalog entry cohort path is required");
	}
	
	public void validateReportConfig(ReportConfig config) {
		if (config == null)
			throw new IllegalArgumentException("Report config is required");
		require(config.getUuid(), "Report uuid is required");
		require(config.getName(), "Report name is required");
		if (config.getDatasets() == null || config.getDatasets().isEmpty())
			throw new IllegalArgumentException("Report must define at least one dataset");
		Set<String> datasetKeys = new HashSet<String>();
		for (DatasetRefConfig ds : config.getDatasets()) {
			if (ds == null)
				throw new IllegalArgumentException("Dataset reference is required");
			require(ds.getKey(), "Dataset reference key is required");
			if (!ds.isInlineDefinition())
				require(ds.getFile(), "Dataset reference file is required");
			if (ds.isInlineDefinition())
				validateDatasetConfig(ds);
			if (!datasetKeys.add(ds.getKey()))
				throw new IllegalArgumentException("Duplicate dataset reference key: " + ds.getKey());
		}
	}
	
	public void validateDatasetConfig(DatasetConfig config) {
		if (config == null)
			throw new IllegalArgumentException("Dataset config is required");
		require(config.getName(), "Dataset name is required");
		if (config.getColumns() == null || config.getColumns().isEmpty())
			throw new IllegalArgumentException("Dataset must define at least one column");
		Set<String> keys = new HashSet<String>();
		for (ColumnConfig column : config.getColumns()) {
			validateColumnConfig(column);
			if (!keys.add(column.getKey()))
				throw new IllegalArgumentException("Duplicate dataset column key: " + column.getKey());
		}
		Set<String> dimensionKeys = new HashSet<String>();
		if (config.getDimensions() != null)
			for (DimensionRefConfig dimension : config.getDimensions()) {
				if (dimension == null)
					throw new IllegalArgumentException("Dataset dimension reference is required");
				require(dimension.getKey(), "Dataset dimension key is required");
				require(dimension.getRef(), "Dataset dimension ref is required for key: " + dimension.getKey());
				if (!dimensionKeys.add(dimension.getKey()))
					throw new IllegalArgumentException("Duplicate dataset dimension key: " + dimension.getKey());
			}
		if (config.getRowFilter() != null && !config.getRowFilter().isEmpty()
		        && !hasText(asString(config.getRowFilter().get("type")))
		        && !hasText(asString(config.getRowFilter().get("method")))) {
			throw new IllegalArgumentException("rowFilter type or method is required");
		}
	}
	
	public void validateColumnConfig(ColumnConfig config) {
		if (config == null)
			throw new IllegalArgumentException("Column config is required");
		require(config.getKey(), "Column key is required");
		boolean hasIndicator = config.getIndicator() != null;
		boolean hasSource = config.getSource() != null
		        && (!(config.getSource() instanceof String) || hasText((String) config.getSource()));
		if (!hasIndicator && !hasSource)
			throw new IllegalArgumentException("Column '" + config.getKey()
			        + "' must define either an indicator or a source");
		if (hasIndicator)
			validateIndicatorConfig(config.getIndicator(), "column '" + config.getKey() + "'");
		if (hasSource && !hasText(config.getType()))
			config.setType("patient-data");
	}
	
	public void validateIndicatorConfig(IndicatorConfig config, String contextLabel) {
		if (config == null)
			throw new IllegalArgumentException("Indicator config is required for " + contextLabel);
		if (!hasText(config.getType()))
			config.setType("cohort");
		if ("cohort".equalsIgnoreCase(config.getType()))
			require(config.getCohortRef(), "Indicator cohortRef is required for " + contextLabel);
	}
	
	public void validateDesignConfig(DesignConfig config) {
		if (config == null)
			throw new IllegalArgumentException("Design config is required");
		require(config.getType(), "Design type is required");
		require(config.getName(), "Design name is required");
		require(config.getTemplate(), "Design template is required");
	}
	
	public void validateParameterConfig(ParameterConfig config) {
		if (config == null)
			throw new IllegalArgumentException("Parameter config is required");
		require(config.getName(), "Parameter name is required");
		require(config.getType(), "Parameter type is required");
	}
	
	public void validateCohortConfig(CohortConfig config, String key) {
		if (config == null)
			throw new IllegalArgumentException("Cohort config is required for key: " + key);
		require(config.getType(), "Cohort type is required for key: " + key);
	}
	
	public void require(String value, String message) {
		if (!hasText(value))
			throw new IllegalArgumentException(message);
	}
	
	private String asString(Object value) {
		return value == null ? null : String.valueOf(value).trim();
	}
	
	private boolean hasText(String value) {
		return value != null && value.trim().length() > 0;
	}
}
