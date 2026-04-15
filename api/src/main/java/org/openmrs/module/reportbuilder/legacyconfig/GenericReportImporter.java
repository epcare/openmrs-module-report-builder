/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reportbuilder.legacyconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.module.reportbuilder.contract.LegacyGenericReportSchema;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.GenericDataDefinitionResolver;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.GenericConverterResolver;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.GenericRowFilterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Completely generic report importer. Imports reports from JSON configuration with NO custom Java
 * dependencies. Uses only standard OpenMRS metadata and SQL-based definitions. This eliminates the
 * dependency on UgandaEMRReports module completely.
 */
public class GenericReportImporter {
	
	private static final Logger log = LoggerFactory.getLogger(GenericReportImporter.class);
	
	private final ObjectMapper objectMapper;
	
	private final GenericDataDefinitionResolver dataDefinitionResolver;
	
	private final GenericConverterResolver converterResolver;
	
	private final GenericRowFilterResolver rowFilterResolver;
	
	public GenericReportImporter() {
		this.objectMapper = new ObjectMapper();
		this.dataDefinitionResolver = new GenericDataDefinitionResolver();
		this.converterResolver = new GenericConverterResolver();
		this.rowFilterResolver = new GenericRowFilterResolver();
	}
	
	/**
	 * Import a single report from a JSON file
	 * 
	 * @param jsonFile The JSON configuration file
	 * @return OpenMRS ReportDefinition
	 */
	public ReportDefinition importReportFromFile(File jsonFile) {
		try {
			log.info("Importing report from: " + jsonFile.getName());
			
			String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFile.getAbsolutePath())),
			        StandardCharsets.UTF_8);
			return importReportFromJson(jsonContent);
		}
		catch (IOException e) {
			throw new APIException("Failed to read report JSON file: " + jsonFile.getAbsolutePath(), e);
		}
	}
	
	/**
	 * Import a report from JSON string
	 * 
	 * @param jsonContent The JSON configuration
	 * @return OpenMRS ReportDefinition
	 */
	public ReportDefinition importReportFromJson(String jsonContent) {
		try {
			LegacyGenericReportSchema.ReportDefinition jsonReport = objectMapper.readValue(jsonContent,
			    LegacyGenericReportSchema.ReportDefinition.class);
			return convertToReportDefinition(jsonReport);
		}
		catch (Exception e) {
			throw new APIException("Failed to parse report JSON configuration", e);
		}
	}
	
	/**
	 * Convert JSON report definition to OpenMRS ReportDefinition
	 */
	private ReportDefinition convertToReportDefinition(LegacyGenericReportSchema.ReportDefinition jsonReport) {
		ReportDefinition reportDefinition = new ReportDefinition();
		
		// Basic metadata
		reportDefinition.setUuid(jsonReport.getUuid());
		reportDefinition.setName(jsonReport.getName());
		reportDefinition.setDescription(jsonReport.getDescription());
		reportDefinition.setParameters(convertParameters(jsonReport.getParameters()));
		
		// Convert datasets based on report type
		if (jsonReport.getDataSetDefinitions() != null && jsonReport.getDataSetDefinitions().length > 0) {
			for (LegacyGenericReportSchema.DataSetDefinition jsonDataSet : jsonReport.getDataSetDefinitions()) {
				DataSetDefinition dataSetDefinition = convertDataSetDefinition(jsonDataSet, jsonReport.getReportType());
				reportDefinition.addDataSetDefinition(dataSetDefinition.getName(), dataSetDefinition, null);
			}
		}
		
		log.info("Successfully converted report: " + jsonReport.getName());
		return reportDefinition;
	}
	
	/**
	 * Convert parameters from JSON to OpenMRS Parameters
	 */
	private List<org.openmrs.module.reporting.evaluation.parameter.Parameter> convertParameters(
	        LegacyGenericReportSchema.Parameter[] parameters) {
		List<org.openmrs.module.reporting.evaluation.parameter.Parameter> result = new ArrayList<org.openmrs.module.reporting.evaluation.parameter.Parameter>();
		if (parameters == null) {
			return result;
		}
		
		for (LegacyGenericReportSchema.Parameter param : parameters) {
			try {
				Class<?> paramClass = convertParameterType(param.getType());
				org.openmrs.module.reporting.evaluation.parameter.Parameter p = new org.openmrs.module.reporting.evaluation.parameter.Parameter(
				        param.getName(), param.getLabel(), paramClass);
				result.add(p);
			}
			catch (Exception e) {
				log.warn("Unknown parameter type: " + param.getType() + " for parameter: " + param.getName());
			}
		}
		return result;
	}
	
	/**
	 * Convert parameter type string to Java class
	 */
	private Class<?> convertParameterType(String type) {
		if (type == null) {
			return String.class;
		}
		
		switch (type.toUpperCase()) {
			case "DATE":
				return Date.class;
			case "LOCATION":
				return Location.class;
			case "PATIENT":
				return org.openmrs.Patient.class;
			case "ENCOUNTER":
				return org.openmrs.Encounter.class;
			case "CONCEPT":
				return org.openmrs.Concept.class;
			default:
				return String.class;
		}
	}
	
	/**
	 * Convert dataset definition from JSON to OpenMRS DataSetDefinition
	 */
	private DataSetDefinition convertDataSetDefinition(LegacyGenericReportSchema.DataSetDefinition jsonDataSet,
	        String reportType) {
		// For now, focus on line-list reports (PatientDataSetDefinition)
		// Aggregate reports will use a different approach
		PatientDataSetDefinition dataSetDefinition = new PatientDataSetDefinition();
		dataSetDefinition.setName(jsonDataSet.getName());
		
		// Convert row filter if present
		if (jsonDataSet.getRowFilter() != null) {
			try {
				org.openmrs.module.reporting.cohort.definition.CohortDefinition rowFilter = rowFilterResolver
				        .resolveRowFilter(jsonDataSet.getRowFilter());
				if (rowFilter != null) {
					dataSetDefinition.addRowFilter(Mapped.mapStraightThrough(rowFilter));
				}
			}
			catch (Exception e) {
				log.error("Failed to resolve row filter for dataset: " + jsonDataSet.getName(), e);
			}
		}
		
		// Convert columns
		if (jsonDataSet.getColumns() != null) {
			for (LegacyGenericReportSchema.Column column : jsonDataSet.getColumns()) {
				try {
					DataDefinition dataDefinition = dataDefinitionResolver.resolveDataDefinition(column.getDataDefinition());
					DataConverter converter = null;
					if (column.getConverter() != null) {
						converter = converterResolver.resolveConverter(column.getConverter());
					}
					
					dataSetDefinition.addColumn(column.getName(), dataDefinition, "", converter);
				}
				catch (Exception e) {
					log.error("Failed to resolve column: " + column.getName(), e);
				}
			}
		}
		
		return dataSetDefinition;
	}
	
	/**
	 * Import all reports from a directory
	 * 
	 * @param reportsDirectory Directory containing JSON report files
	 * @return List of imported ReportDefinitions
	 */
	public List<ReportDefinition> importReportsFromDirectory(File reportsDirectory) {
		List<ReportDefinition> importedReports = new ArrayList<ReportDefinition>();

		if (!reportsDirectory.exists() || !reportsDirectory.isDirectory()) {
			log.warn("Reports directory does not exist: " + reportsDirectory.getAbsolutePath());
			return importedReports;
		}

		File[] jsonFiles = reportsDirectory.listFiles((dir, name) -> name.endsWith(".json"));
		if (jsonFiles == null || jsonFiles.length == 0) {
			log.info("No JSON report files found in: " + reportsDirectory.getAbsolutePath());
			return importedReports;
		}

		for (File jsonFile : jsonFiles) {
			try {
				ReportDefinition reportDefinition = importReportFromFile(jsonFile);
				importedReports.add(reportDefinition);
			} catch (Exception e) {
				log.error("Failed to import report from: " + jsonFile.getName(), e);
			}
		}

		log.info("Imported " + importedReports.size() + " reports from " + reportsDirectory.getAbsolutePath());
		return importedReports;
	}
	
	/**
	 * Validate that a JSON report file is valid
	 * 
	 * @param jsonFile The JSON configuration file
	 * @return validation result with any errors
	 */
	public LegacyReportImporter.ValidationResult validateReport(File jsonFile) {
		LegacyReportImporter.ValidationResult result = new LegacyReportImporter.ValidationResult(jsonFile);
		
		try {
			// Read JSON configuration
			LegacyGenericReportSchema.ReportDefinition jsonReport = objectMapper.readValue(
			    new String(Files.readAllBytes(Paths.get(jsonFile.getAbsolutePath())), StandardCharsets.UTF_8),
			    LegacyGenericReportSchema.ReportDefinition.class);
			
			// Validate required fields
			if (jsonReport.getUuid() == null || jsonReport.getUuid().trim().isEmpty()) {
				result.addError("UUID is required");
			}
			if (jsonReport.getName() == null || jsonReport.getName().trim().isEmpty()) {
				result.addError("Name is required");
			}
			if (jsonReport.getReportType() == null || jsonReport.getReportType().trim().isEmpty()) {
				result.addError("Report type is required");
			}
			if (jsonReport.getDataSetDefinitions() == null || jsonReport.getDataSetDefinitions().length == 0) {
				result.addError("Dataset definition is required");
			}
			
			// Validate no custom class dependencies
			validateNoCustomClasses(result, jsonReport);
			
			result.setValid(result.getErrors().isEmpty());
			
		}
		catch (Exception e) {
			result.addError("Validation failed: " + e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Validate that report doesn't depend on custom classes
	 */
	private void validateNoCustomClasses(LegacyReportImporter.ValidationResult result,
	        LegacyGenericReportSchema.ReportDefinition jsonReport) {
		// Check for custom class references in data definitions
		if (jsonReport.getDataSetDefinitions() != null) {
			for (LegacyGenericReportSchema.DataSetDefinition dataSet : jsonReport.getDataSetDefinitions()) {
				if (dataSet.getColumns() != null) {
					for (LegacyGenericReportSchema.Column column : dataSet.getColumns()) {
						if (column.getDataDefinition() != null && column.getDataDefinition().getConfig() != null) {
							// Check for className references (which would indicate custom classes)
							if (column.getDataDefinition().getConfig().containsKey("className")) {
								result.addError("Column '" + column.getName() + "' uses custom class: "
								        + column.getDataDefinition().getConfig().get("className"));
							}
						}
					}
				}
			}
		}
		
		/**
		 * Validation result class
		 */
		class ValidationResult {
			
			private final File jsonFile;
			
			private final List<String> errors = new ArrayList<String>();
			
			private boolean valid = false;
			
			public ValidationResult(File jsonFile) {
				this.jsonFile = jsonFile;
			}
			
			public void addError(String error) {
				this.errors.add(error);
			}
			
			public File getJsonFile() {
				return jsonFile;
			}
			
			public List<String> getErrors() {
				return errors;
			}
			
			public boolean isValid() {
				return valid;
			}
			
			public void setValid(boolean valid) {
				this.valid = valid;
			}
		}
	}
}
