package org.openmrs.module.reportbuilder.legacyconfig.builder;

import org.openmrs.module.reportbuilder.legacyconfig.model.*;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.LegacyAliasResolver;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.ReferenceResolver;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatasetDefinitionFactory {
	
	private final ReferenceResolver referenceResolver;
	
	private final LegacyAliasResolver legacyAliasResolver;
	
	private final IndicatorBuilder indicatorBuilder;
	
	private final PatientDataDefinitionResolver patientDataDefinitionResolver;
	
	private final PatientDataConverterResolver patientDataConverterResolver;
	
	private final PatientDataRowFilterResolver patientDataRowFilterResolver;
	
	public DatasetDefinitionFactory(ReferenceResolver referenceResolver, LegacyAliasResolver legacyAliasResolver,
	    IndicatorBuilder indicatorBuilder, PatientDataDefinitionResolver patientDataDefinitionResolver,
	    PatientDataConverterResolver patientDataConverterResolver, PatientDataRowFilterResolver patientDataRowFilterResolver) {
		this.referenceResolver = referenceResolver;
		this.legacyAliasResolver = legacyAliasResolver;
		this.indicatorBuilder = indicatorBuilder;
		this.patientDataDefinitionResolver = patientDataDefinitionResolver;
		this.patientDataConverterResolver = patientDataConverterResolver;
		this.patientDataRowFilterResolver = patientDataRowFilterResolver;
	}
	
	public DataSetDefinition build(DatasetConfig config, List<Parameter> parameters,
	        Map<String, CohortDefinition> cohortDefinitions) {
		if (config == null)
			throw new IllegalArgumentException("Dataset config is required");
		String type = config.getType() == null ? "" : config.getType().trim();
		if (isAggregateDataset(type, config))
			return buildAggregateDataset(config, parameters, cohortDefinitions);
		if (isPatientDataDataset(type, config))
			return buildPatientDataDataset(config, parameters);
		throw new IllegalArgumentException("Unsupported dataset type: '" + config.getType() + "' for dataset '"
		        + config.getName() + "'");
	}
	
	private boolean isAggregateDataset(String type, DatasetConfig config) {
		if ("cohort-indicator".equalsIgnoreCase(type) || "indicator".equalsIgnoreCase(type)
		        || "aggregate".equalsIgnoreCase(type) || "aggregate-indicator".equalsIgnoreCase(type)
		        || "cohortindicator".equalsIgnoreCase(type))
			return true;
		if (type.length() == 0 && config.getColumns() != null)
			for (ColumnConfig c : config.getColumns())
				if (c != null && c.getIndicator() != null)
					return true;
		return false;
	}
	
	private boolean isPatientDataDataset(String type, DatasetConfig config) {
		if ("patient-data".equalsIgnoreCase(type) || "patientdata".equalsIgnoreCase(type)
		        || "line-list".equalsIgnoreCase(type) || "linelist".equalsIgnoreCase(type))
			return true;
		if (config.getColumns() != null && !config.getColumns().isEmpty())
			for (ColumnConfig c : config.getColumns())
				if (c != null && c.getSource() != null)
					return true;
		return false;
	}
	
	public CohortIndicatorDataSetDefinition buildAggregateDataset(DatasetConfig config, List<Parameter> parameters,
	        Map<String, CohortDefinition> cohortDefinitions) {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName(config.getName());
		dsd.setParameters(parameters);
		if (config.getDimensions() != null)
			for (DimensionRefConfig dim : config.getDimensions()) {
				if (dim == null)
					throw new IllegalArgumentException("Dataset dimension reference is required");
				CohortDefinitionDimension dimension = resolveDimension(dim.getRef());
				dsd.addDimension(dim.getKey(), dimension, null);
			}
		if (config.getColumns() == null || config.getColumns().isEmpty())
			throw new IllegalArgumentException("Dataset must define at least one column");
		for (ColumnConfig column : config.getColumns()) {
			IndicatorConfig indicatorConfig = column.getIndicator();
			if (indicatorConfig == null)
				throw new IllegalArgumentException("Indicator is required for aggregate column '" + column.getKey() + "'");
			String cohortRef = indicatorConfig.getCohortRef();
			if (!hasText(cohortRef))
				throw new IllegalArgumentException("Indicator cohortRef is required for column '" + column.getKey() + "'");
			CohortDefinition cd = cohortDefinitions.get(cohortRef);
			if (cd == null)
				throw new IllegalArgumentException("No cohort definition found for ref '" + cohortRef + "' in column '"
				        + column.getKey() + "'");
			CohortIndicator indicator = indicatorBuilder.build(indicatorConfig, cd);
			StringBuilder dimensionMapping = new StringBuilder();
			boolean first = true;
			if (column.getDimensionOptions() != null)
				for (Map.Entry<String, String> entry : column.getDimensionOptions().entrySet()) {
					if (!first)
						dimensionMapping.append(",");
					dimensionMapping.append(entry.getKey()).append("=").append(entry.getValue());
					first = false;
				}
			dsd.addColumn(column.getKey(), getColumnLabel(column), Mapped.mapStraightThrough(indicator),
			    dimensionMapping.toString());
		}
		return dsd;
	}
	
	private DataSetDefinition buildPatientDataDataset(DatasetConfig config, List<Parameter> parameters) {
		PatientDataSetDefinition patientDataSetDefinition = new PatientDataSetDefinition();
		patientDataSetDefinition.setName(config.getName());
		patientDataSetDefinition.setParameters(parameters);
		applyPatientDataRowFilter(patientDataSetDefinition, config, parameters);
		if (config.getColumns() == null || config.getColumns().isEmpty())
			throw new IllegalArgumentException("Dataset must define at least one column");
		for (ColumnConfig column : config.getColumns()) {
			if (!hasText(column.getKey()))
				throw new IllegalArgumentException("Patient-data column key is required");
			if (column.getSource() == null)
				throw new IllegalArgumentException("Patient-data column source is required for column '" + column.getKey()
				        + "'");
			DataDefinition dataDefinition = patientDataDefinitionResolver.resolve(column.getSource());
			DataConverter converter = column.getConverter() == null ? null : patientDataConverterResolver.resolve(column
			        .getConverter());
			if (converter != null)
				patientDataSetDefinition.addColumn(column.getKey(), dataDefinition, (String) null, converter);
			else
				patientDataSetDefinition.addColumn(column.getKey(), dataDefinition, (String) null);
		}
		return patientDataSetDefinition;
	}
	
	private void applyPatientDataRowFilter(PatientDataSetDefinition patientDataSetDefinition, DatasetConfig config,
	        List<Parameter> parameters) {
		if (config.getRowFilter() == null || config.getRowFilter().isEmpty())
			return;
		CohortDefinition cohortDefinition = patientDataRowFilterResolver.resolve(config.getRowFilter(), parameters);
		if (cohortDefinition == null)
			throw new IllegalArgumentException("Unable to resolve rowFilter for patient-data dataset '" + config.getName()
			        + "'");
		patientDataSetDefinition.addRowFilter(Mapped.mapStraightThrough(cohortDefinition));
	}
	
	private CohortDefinitionDimension resolveDimension(String ref) {
		if (!hasText(ref))
			throw new IllegalArgumentException("Dataset dimension ref is required");
		if (ref.startsWith("java:"))
			return (CohortDefinitionDimension) referenceResolver.resolveJavaReference(ref);
		return legacyAliasResolver.resolveDimension(ref);
	}
	
	private String getColumnLabel(ColumnConfig column) {
		return hasText(column.getLabel()) ? column.getLabel() : column.getKey();
	}
	
	private boolean hasText(String value) {
		return value != null && value.trim().length() > 0;
	}
}
