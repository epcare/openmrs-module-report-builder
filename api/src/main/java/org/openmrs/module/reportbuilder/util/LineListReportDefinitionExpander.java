/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark of OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reportbuilder.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reportbuilder.contract.LegacyGenericReportSchema;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.GenericConverterResolver;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.GenericDataDefinitionResolver;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.GenericRowFilterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Expands a compiled line-list report configuration into a fully populated OpenMRS ReportDefinition
 * with proper cohort definitions, data definitions, and converters. This transforms the "lean"
 * compiled JSON format from LinelistConfigCompiler into the rich object graph expected by the
 * OpenMRS reporting module, enabling proper serialization and UI display.
 */
public class LineListReportDefinitionExpander {
	
	private static final Logger log = LoggerFactory.getLogger(LineListReportDefinitionExpander.class);
	
	private final GenericDataDefinitionResolver dataDefinitionResolver;
	
	private final GenericConverterResolver converterResolver;
	
	private final GenericRowFilterResolver rowFilterResolver;
	
	private final ObjectMapper objectMapper;
	
	public LineListReportDefinitionExpander() {
		this.dataDefinitionResolver = new GenericDataDefinitionResolver();
		this.converterResolver = new GenericConverterResolver();
		this.rowFilterResolver = new GenericRowFilterResolver();
		this.objectMapper = new ObjectMapper();
	}
	
	/**
	 * Expands a compiled JSON configuration into a fully populated ReportDefinition.
	 * 
	 * @param compiledJson The compiled JSON string from LinelistConfigCompiler
	 * @param reportDefinition The base ReportDefinition to populate (created by
	 *            findOrCreateReportDefinition)
	 * @return The fully populated ReportDefinition
	 */
	public ReportDefinition expand(String compiledJson, ReportDefinition reportDefinition) {
		try {
			LegacyGenericReportSchema.ReportDefinition reportConfig = objectMapper.readValue(compiledJson,
			    LegacyGenericReportSchema.ReportDefinition.class);
			
			// Clear existing data to ensure clean expansion
			reportDefinition.getDataSetDefinitions().clear();
			reportDefinition.getParameters().clear();
			
			// Expand parameters
			List<Parameter> parameters = expandParameters(reportConfig.getParameters());
			for (Parameter p : parameters) {
				reportDefinition.addParameter(p);
			}
			
			// Expand baseCohortDefinition
			LegacyGenericReportSchema.BaseCohortDefinition baseCohort = reportConfig.getBaseCohortDefinition();
			if (baseCohort != null) {
				CohortDefinition cohortDefinition = expandBaseCohortDefinition(baseCohort, parameters);
				if (cohortDefinition != null) {
					reportDefinition.setBaseCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
				}
			}
			
			// Expand dataSetDefinitions - we only support one PATIENT_DATA_SET for line-lists
			LegacyGenericReportSchema.DataSetDefinition[] dataSetDefs = reportConfig.getDataSetDefinitions();
			if (dataSetDefs != null && dataSetDefs.length > 0) {
				for (LegacyGenericReportSchema.DataSetDefinition dataSetDef : dataSetDefs) {
					if ("PATIENT_DATA_SET".equals(dataSetDef.getType())) {
						PatientDataSetDefinition patientDataSetDef = expandPatientDataSetDefinition(dataSetDef, parameters);
						
						reportDefinition.addDataSetDefinition(dataSetDef.getName(),
						    Mapped.mapStraightThrough(patientDataSetDef));
						break; // Only support one dataset for now
					}
				}
			}
			
			log.info("Successfully expanded report definition: {}", reportDefinition.getName());
			return reportDefinition;
			
		}
		catch (Exception e) {
			log.error("Failed to expand report definition: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to expand report definition: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Expands parameters from the compiled config.
	 */
	private List<Parameter> expandParameters(LegacyGenericReportSchema.Parameter[] jsonParams) {
		List<Parameter> parameters = new ArrayList<Parameter>();
		if (jsonParams != null) {
			for (LegacyGenericReportSchema.Parameter jsonParam : jsonParams) {
				String name = jsonParam.getName();
				String label = jsonParam.getLabel();
				String type = jsonParam.getType();
				boolean required = jsonParam.isRequired();
				
				Class<?> paramClass = mapParameterTypeToClass(type);
				Parameter param = new Parameter(name, label, paramClass);
				// Mark as required if specified
				if (required) {
					// Parameter class doesn't have setRequired, assume all params are required for now
				}
				parameters.add(param);
			}
		}
		return parameters;
	}
	
	/**
	 * Expands a baseCohortDefinition from the compiled config.
	 */
	private CohortDefinition expandBaseCohortDefinition(LegacyGenericReportSchema.BaseCohortDefinition baseCohort,
	        List<Parameter> parameters) {
		if (baseCohort == null) {
			return null;
		}
		
		String type = baseCohort.getType();
		if (type == null) {
			return null;
		}
		
		// For SQL type, create a SqlCohortDefinition
		if ("SQL".equalsIgnoreCase(type)) {
			Map<String, Object> config = baseCohort.getConfig();
			if (config != null && config.containsKey("sql")) {
				String sql = (String) config.get("sql");
				SqlCohortDefinition cohortDef = new SqlCohortDefinition();
				cohortDef.setName(baseCohort.getName());
				cohortDef.setQuery(sql);
				
				// Add parameters to the cohort definition
				cohortDef.setParameters(parameters);
				
				return cohortDef;
			}
		}
		
		// For other types, use the GenericRowFilterResolver
		// Convert BaseCohortDefinition to RowFilter format
		LegacyGenericReportSchema.RowFilter rowFilter = new LegacyGenericReportSchema.RowFilter();
		rowFilter.setType(type);
		rowFilter.setName(baseCohort.getName());
		rowFilter.setConfig(baseCohort.getConfig());
		return rowFilterResolver.resolveRowFilter(rowFilter);
	}
	
	/**
	 * Expands a PatientDataSetDefinition from the compiled config.
	 */
	private PatientDataSetDefinition expandPatientDataSetDefinition(LegacyGenericReportSchema.DataSetDefinition jsonDsDef,
	        List<Parameter> parameters) {
		PatientDataSetDefinition patientDataSetDef = new PatientDataSetDefinition();
		patientDataSetDef.setName(jsonDsDef.getName());
		
		// Add parameters
		for (Parameter p : parameters) {
			patientDataSetDef.addParameter(p);
		}
		
		// Expand columns
		LegacyGenericReportSchema.Column[] columns = jsonDsDef.getColumns();
		if (columns != null && columns.length > 0) {
			for (LegacyGenericReportSchema.Column column : columns) {
				expandColumn(column, patientDataSetDef);
			}
		}
		
		// Expand rowFilter if present (base cohort definition)
		if (jsonDsDef.getRowFilter() != null) {
			// Row filter is the base cohort - we'll handle this separately
			// For now, the base cohort is already defined at the report level
		}
		
		return patientDataSetDef;
	}
	
	/**
	 * Expands a single column definition and adds it to the PatientDataSetDefinition.
	 */
	private void expandColumn(LegacyGenericReportSchema.Column jsonColumn, PatientDataSetDefinition patientDataSetDef) {
		String columnName = jsonColumn.getName();
		String columnKey = jsonColumn.getKey();
		
		// Use key if available, otherwise use name
		String key = (columnKey != null && !columnKey.isEmpty()) ? columnKey : columnName;
		
		// Expand dataDefinition
		LegacyGenericReportSchema.DataDefinition jsonDataDef = jsonColumn.getDataDefinition();
		DataDefinition dataDefinition = null;
		if (jsonDataDef != null) {
			dataDefinition = expandDataDefinition(jsonDataDef);
		}
		
		// Expand converter
		DataConverter converter = null;
		LegacyGenericReportSchema.Converter jsonConverter = jsonColumn.getConverter();
		if (jsonConverter != null) {
			converter = converterResolver.resolveConverter(jsonConverter);
		}
		
		// Add column to dataset definition
		// Parameter name is empty string for straight-through mapping
		if (converter != null) {
			patientDataSetDef.addColumn(key, dataDefinition, "", converter);
		} else {
			patientDataSetDef.addColumn(key, dataDefinition, "");
		}
	}
	
	/**
	 * Expands a data definition into a proper DataDefinition.
	 */
	private DataDefinition expandDataDefinition(LegacyGenericReportSchema.DataDefinition jsonDataDef) {
		return dataDefinitionResolver.resolveDataDefinition(jsonDataDef);
	}
	
	/**
	 * Maps a parameter type string to a Java Class.
	 */
	private Class<?> mapParameterTypeToClass(String type) {
		if (type == null) {
			return String.class;
		}
		
		switch (type.toUpperCase()) {
			case "DATE":
			case "DATETIME":
				return java.util.Date.class;
			case "INTEGER":
			case "INT":
				return Integer.class;
			case "BOOLEAN":
			case "BOOL":
				return Boolean.class;
			case "DOUBLE":
			case "DECIMAL":
				return Double.class;
			case "LOCATION":
				return org.openmrs.Location.class;
			case "PERSON":
				return org.openmrs.Person.class;
			case "PATIENT":
				return org.openmrs.Patient.class;
			case "CONCEPT":
				return org.openmrs.Concept.class;
			case "ENCOUNTER":
				return org.openmrs.Encounter.class;
			default:
				return String.class;
		}
	}
}
