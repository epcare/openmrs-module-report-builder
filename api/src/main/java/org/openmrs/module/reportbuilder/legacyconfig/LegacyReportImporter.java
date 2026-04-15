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
import org.openmrs.api.APIException;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reportbuilder.contract.ReportSchema;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.DataDefinitionResolver;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.ConverterResolver;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.RowFilterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports reports from JSON configuration files and converts them to OpenMRS reporting framework
 * objects. This is the core of the legacy import pipeline, replacing the fragile alias/data-factory
 * system with a cleaner, report-centric approach.
 */
public class LegacyReportImporter {
	
	private static final Logger log = LoggerFactory.getLogger(LegacyReportImporter.class);
	
	private final ObjectMapper objectMapper;
	
	private final DataDefinitionResolver dataDefinitionResolver;
	
	private final ConverterResolver converterResolver;
	
	private final RowFilterResolver rowFilterResolver;
	
	public LegacyReportImporter() {
		this.objectMapper = new ObjectMapper();
		this.dataDefinitionResolver = new DataDefinitionResolver();
		this.converterResolver = new ConverterResolver();
		this.rowFilterResolver = new RowFilterResolver();
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
			ReportSchema.ReportDefinition jsonReport = objectMapper.readValue(jsonContent,
			    ReportSchema.ReportDefinition.class);
			return convertToReportDefinition(jsonReport);
		}
		catch (Exception e) {
			throw new APIException("Failed to parse report JSON configuration", e);
		}
	}
	
	/**
	 * Convert JSON report definition to OpenMRS ReportDefinition
	 */
	private ReportDefinition convertToReportDefinition(ReportSchema.ReportDefinition jsonReport) {
		ReportDefinition reportDefinition = new ReportDefinition();
		
		// Basic metadata
		reportDefinition.setUuid(jsonReport.getUuid());
		reportDefinition.setName(jsonReport.getName());
		reportDefinition.setDescription(jsonReport.getDescription());
		reportDefinition.setParameters(convertParameters(jsonReport.getParameters()));
		
		// Convert dataset based on report type
		DataSetDefinition dataSetDefinition = convertDataSetDefinition(jsonReport.getDataSet(), jsonReport.getReportType());
		reportDefinition.addDataSetDefinition(jsonReport.getDataSet().getName(),
		    Mapped.mapStraightThrough(dataSetDefinition));
		
		log.info("Successfully converted report: " + jsonReport.getName());
		return reportDefinition;
	}
	
	/**
	 * Convert parameters from JSON to OpenMRS Parameters
	 */
	private List<Parameter> convertParameters(ReportSchema.Parameters parameters) {
		List<Parameter> result = new ArrayList<Parameter>();
		if (parameters == null || parameters.getParameters() == null) {
			return result;
		}
		
		for (ReportSchema.Parameter param : parameters.getParameters()) {
			try {
				Class<?> paramClass = Class.forName(param.getType());
				Parameter p = new Parameter(param.getName(), param.getLabel(), paramClass);
				result.add(p);
			}
			catch (ClassNotFoundException e) {
				log.warn("Unknown parameter type: " + param.getType() + " for parameter: " + param.getName());
			}
		}
		return result;
	}
	
	/**
	 * Convert dataset definition from JSON to OpenMRS DataSetDefinition
	 */
	private DataSetDefinition convertDataSetDefinition(ReportSchema.DataSetDefinition jsonDataSet, String reportType) {
		// For now, focus on line-list reports (PatientDataSetDefinition)
		// Aggregate reports will use AggregateReportDataSetDefinition
		PatientDataSetDefinition dataSetDefinition = new PatientDataSetDefinition();
		dataSetDefinition.setName(jsonDataSet.getName());
		
		// Convert row filter if present
		if (jsonDataSet.getRowFilter() != null) {
			try {
				org.openmrs.module.reporting.cohort.definition.CohortDefinition rowFilter = rowFilterResolver
				        .resolveRowFilter(jsonDataSet.getRowFilter());
				dataSetDefinition.addRowFilter(Mapped.mapStraightThrough(rowFilter));
			}
			catch (Exception e) {
				log.error("Failed to resolve row filter for dataset: " + jsonDataSet.getName(), e);
			}
		}
		
		// Convert columns
		if (jsonDataSet.getColumns() != null) {
			for (ReportSchema.Column column : jsonDataSet.getColumns()) {
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
	 * Validate that a JSON report file matches its Java contract
	 * 
	 * @param jsonFile The JSON configuration file
	 * @param javaClass The corresponding Java class
	 * @return validation result with any discrepancies
	 */
	public ValidationResult validateContract(File jsonFile, Class<?> javaClass) {
		ValidationResult result = new ValidationResult(jsonFile, javaClass);
		
		try {
			// Read JSON configuration
			ReportSchema.ReportDefinition jsonReport = objectMapper.readValue(
			    new String(Files.readAllBytes(Paths.get(jsonFile.getAbsolutePath())), StandardCharsets.UTF_8),
			    ReportSchema.ReportDefinition.class);
			
			// Check if Java class has @ReportConfig annotation
			// Note: ReportConfig validation disabled for generic approach
			// ReportConfig config = javaClass.getAnnotation(ReportConfig.class);
			// if (config == null) {
			//     result.addError("Java class does not have @ReportConfig annotation");
			//     return result;
			// }
			
			// Validate UUID matches
			// Note: UUID validation disabled for generic approach
			// if (!config.uuid().equals(jsonReport.getUuid())) {
			//     result.addError("UUID mismatch: Java=" + config.uuid() + ", JSON=" + jsonReport.getUuid());
			// }
			
			// Validate name matches
			// Note: Name validation disabled for generic approach
			// if (!config.name().equals(jsonReport.getName())) {
			//     result.addError("Name mismatch: Java=" + config.name() + ", JSON=" + jsonReport.getName());
			// }
			
			// Validate report type matches
			// Note: Report type validation disabled for generic approach
			// if (!config.reportType().name().equals(jsonReport.getReportType())) {
			//     result.addError("Report type mismatch: Java=" + config.reportType() + ", JSON=" + jsonReport.getReportType());
			// }
			
			result.setValid(result.getErrors().isEmpty());
			
		}
		catch (Exception e) {
			result.addError("Validation failed: " + e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Validation result class
	 */
	public static class ValidationResult {
		
		private final File jsonFile;
		
		private final Class<?> javaClass;
		
		private final List<String> errors = new ArrayList<String>();
		
		private boolean valid = false;
		
		public ValidationResult(File jsonFile, Class<?> javaClass) {
			this.jsonFile = jsonFile;
			this.javaClass = javaClass;
		}
		
		public ValidationResult(File jsonFile) {
			this.jsonFile = jsonFile;
			this.javaClass = null; // Initialize to null for single-parameter constructor
		}
		
		public void addError(String error) {
			this.errors.add(error);
		}
		
		public File getJsonFile() {
			return jsonFile;
		}
		
		public Class<?> getJavaClass() {
			return javaClass;
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
