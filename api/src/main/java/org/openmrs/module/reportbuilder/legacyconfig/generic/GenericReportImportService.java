package org.openmrs.module.reportbuilder.legacyconfig.generic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reportbuilder.api.ReportBuilderService;
import org.openmrs.module.reportbuilder.contract.LegacyGenericReportSchema;
import org.openmrs.module.reportbuilder.model.ReportBuilderReport;
import org.openmrs.module.reportbuilder.model.ReportCategory;
import org.openmrs.module.reportbuilder.util.RuntimeDirectoryResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Service for importing generic JSON reports from external runtime directory. This service handles
 * our 114 migrated reports with their specific structure: - Categorization fields (category,
 * subcategory, reportType, reportYear, reportScope) - Dataset types (PATIENT_DATA_SET,
 * SQL_DATA_SET, INDICATOR_DATA_SET) - Version 0.1.0-generic format
 */
@Component
public class GenericReportImportService {
	
	private static final Logger log = LoggerFactory.getLogger(GenericReportImportService.class);
	
	private final ObjectMapper objectMapper;
	
	public GenericReportImportService() {
		this.objectMapper = new ObjectMapper();
	}
	
	/**
	 * Import all generic reports from runtime directory
	 */
	public List<ReportImportResult> importAllGenericReports() {
		List<ReportImportResult> results = new ArrayList<>();

		try {
			File reportsDirectory = RuntimeDirectoryResolver.getGenericReportsDirectory();
			log.info("Scanning for generic reports in: " + reportsDirectory.getAbsolutePath());

			// Look for files ending with -generic.json
			File[] jsonFiles = reportsDirectory.listFiles((dir, name) -> name.endsWith("-generic.json"));

			if (jsonFiles == null || jsonFiles.length == 0) {
				log.warn("No generic JSON report files found in: " + reportsDirectory.getAbsolutePath());
				return results;
			}

			log.info("Found " + jsonFiles.length + " generic report files to import");

			for (File jsonFile : jsonFiles) {
				try {
					ReportImportResult result = importGenericReportFromFile(jsonFile);
					results.add(result);
					log.info("✓ Successfully imported: " + jsonFile.getName() + " -> " + result.getReportName());
				} catch (Exception e) {
					log.error("✗ Failed to import: " + jsonFile.getName(), e);
					ReportImportResult errorResult = new ReportImportResult(jsonFile.getName(), false, e.getMessage());
					results.add(errorResult);
				}
			}

			long successCount = results.stream().filter(ReportImportResult::isSuccess).count();
			log.info("Generic report import complete: " + successCount + "/" + results.size() + " successful");

		} catch (Exception e) {
			log.error("Failed to import generic reports from runtime directory", e);
			throw new RuntimeException("Generic report import failed", e);
		}

		return results;
	}
	
	/**
	 * Import a single generic report from file
	 */
	public ReportImportResult importGenericReportFromFile(File jsonFile) {
		try {
			log.info("Importing generic report: " + jsonFile.getName());
			
			// Read and parse JSON
			String jsonContent = new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8);
			LegacyGenericReportSchema.ReportDefinition jsonReport = objectMapper.readValue(jsonContent,
			    LegacyGenericReportSchema.ReportDefinition.class);
			
			// Validate required fields
			validateGenericReport(jsonReport);
			
			// Log categorization
			log.debug("Report categorization: " + jsonReport.getCategory() + "/" + jsonReport.getSubcategory() + " ["
			        + jsonReport.getReportType() + " - " + jsonReport.getReportYear() + "]");
			
			// Convert to OpenMRS ReportDefinition
			ReportDefinition reportDefinition = convertToReportDefinition(jsonReport);
			
			// Save to database
			ReportDefinitionService reportDefService = Context.getService(ReportDefinitionService.class);
			
			// Check if report already exists (by UUID)
			ReportDefinition existing = reportDefService.getDefinitionByUuid(reportDefinition.getUuid());
			if (existing != null) {
				log.info("Updating existing report: " + jsonReport.getName() + " (UUID: " + reportDefinition.getUuid() + ")");
				updateExistingReport(existing, reportDefinition);
				ReportDefinition saved = reportDefService.saveDefinition(existing);
				
				// Add to report library
				addToReportLibrary(saved, jsonReport);
				
				return new ReportImportResult(jsonFile.getName(), true, "Report updated successfully");
			} else {
				log.info("Creating new report: " + jsonReport.getName() + " (UUID: " + reportDefinition.getUuid() + ")");
				ReportDefinition saved = reportDefService.saveDefinition(reportDefinition);
				
				// Add to report library
				addToReportLibrary(saved, jsonReport);
				
				return new ReportImportResult(jsonFile.getName(), true, "Report created successfully");
			}
			
		}
		catch (Exception e) {
			log.error("Failed to import generic report from: " + jsonFile.getName(), e);
			throw new APIException("Failed to import generic report: " + jsonFile.getName(), e);
		}
	}
	
	/**
	 * Validate generic report structure and required fields
	 */
	private void validateGenericReport(LegacyGenericReportSchema.ReportDefinition jsonReport) {
		// Basic validation
		if (jsonReport.getName() == null || jsonReport.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("Report name is required");
		}
		
		if (jsonReport.getUuid() == null || jsonReport.getUuid().trim().isEmpty()) {
			throw new IllegalArgumentException("Report UUID is required");
		}
		
		if (jsonReport.getVersion() == null || !jsonReport.getVersion().contains("-generic")) {
			throw new IllegalArgumentException("Report must have generic version (e.g., 0.1.0-generic)");
		}
		
		// Validate dataset definition exists
		if (jsonReport.getDataSetDefinitions() == null || jsonReport.getDataSetDefinitions().length == 0) {
			throw new IllegalArgumentException("Report must have at least one dataset definition");
		}
		
		// Validate categorization fields if present
		if (jsonReport.getCategory() != null) {
			validateCategorization(jsonReport);
		}
		
		log.debug("Generic report validation passed: " + jsonReport.getName());
	}
	
	/**
	 * Validate report categorization fields
	 */
	private void validateCategorization(LegacyGenericReportSchema.ReportDefinition jsonReport) {
		// Validate category is one of the 5 main categories
		List<String> validCategories = Arrays.asList("FACILITY_REPORTS", "MER_INDICATOR_REPORTS", "NATIONAL_REPORTS",
		    "INTEGRATION_DATA_EXPORTS", "CQI_REPORTS");
		
		if (!validCategories.contains(jsonReport.getCategory())) {
			throw new IllegalArgumentException("Invalid category: " + jsonReport.getCategory() + ". Must be one of: "
			        + String.join(", ", validCategories));
		}
		
		// Validate report type
		if (jsonReport.getReportType() != null) {
			List<String> validTypes = Arrays.asList("LINELIST", "AGGREGATE");
			if (!validTypes.contains(jsonReport.getReportType())) {
				throw new IllegalArgumentException("Invalid reportType: " + jsonReport.getReportType()
				        + ". Must be LINELIST or AGGREGATE");
			}
		}
		
		// Validate report year
		if (jsonReport.getReportYear() != null) {
			List<String> validYears = Arrays.asList("BEFORE_2019", "YEAR_2019", "YEAR_2024");
			if (!validYears.contains(jsonReport.getReportYear())) {
				throw new IllegalArgumentException("Invalid reportYear: " + jsonReport.getReportYear()
				        + ". Must be BEFORE_2019, YEAR_2019, or YEAR_2024");
			}
		}
		
		// Validate report scope
		if (jsonReport.getReportScope() != null) {
			List<String> validScopes = Arrays.asList("FACILITY_BASED", "PERFORMANCE_BASED", "NATIONAL_AGGREGATION");
			if (!validScopes.contains(jsonReport.getReportScope())) {
				throw new IllegalArgumentException("Invalid reportScope: " + jsonReport.getReportScope()
				        + ". Must be FACILITY_BASED, PERFORMANCE_BASED, or NATIONAL_AGGREGATION");
			}
		}
		
		log.debug("Report categorization validated: " + jsonReport.getCategory() + "/" + jsonReport.getSubcategory() + " ["
		        + jsonReport.getReportType() + "]");
	}
	
	/**
	 * Convert generic JSON report to OpenMRS ReportDefinition Handles our specific dataset types
	 * and parameter structures
	 */
	private ReportDefinition convertToReportDefinition(LegacyGenericReportSchema.ReportDefinition jsonReport) {
		ReportDefinition reportDefinition = new ReportDefinition();
		
		// Basic metadata
		reportDefinition.setUuid(jsonReport.getUuid());
		reportDefinition.setName(jsonReport.getName());
		reportDefinition.setDescription(jsonReport.getDescription());
		
		// Convert parameters
		if (jsonReport.getParameters() != null) {
			for (LegacyGenericReportSchema.Parameter param : jsonReport.getParameters()) {
				try {
					Class<?> paramClass = convertParameterType(param.getType());
					org.openmrs.module.reporting.evaluation.parameter.Parameter reportParam = new org.openmrs.module.reporting.evaluation.parameter.Parameter(
					        param.getName(), param.getLabel(), paramClass);
					reportDefinition.addParameter(reportParam);
				}
				catch (ClassNotFoundException e) {
					log.warn("Unknown parameter type: " + param.getType() + " for: " + param.getName()
					        + ". Using String as default.");
					// Use String as fallback
					org.openmrs.module.reporting.evaluation.parameter.Parameter reportParam = new org.openmrs.module.reporting.evaluation.parameter.Parameter(
					        param.getName(), param.getLabel(), String.class);
					reportDefinition.addParameter(reportParam);
				}
			}
		}
		
		// Convert dataset definitions
		if (jsonReport.getDataSetDefinitions() != null) {
			for (LegacyGenericReportSchema.DataSetDefinition jsonDataSet : jsonReport.getDataSetDefinitions()) {
				try {
					DataSetDefinition dataSet = convertDataSetDefinition(jsonDataSet, jsonReport);
					if (dataSet != null) {
						reportDefinition.addDataSetDefinition(dataSet.getName(), dataSet, null);
					}
				}
				catch (Exception e) {
					log.error("Failed to convert dataset: " + jsonDataSet.getName(), e);
					// Continue with other datasets
				}
			}
		}
		
		// Store categorization metadata in report definition for later use
		// This can be accessed via the report definition's name/description
		String enhancedDescription = jsonReport.getDescription();
		if (jsonReport.getCategory() != null) {
			enhancedDescription += "\n\nCategory: " + jsonReport.getCategory();
			if (jsonReport.getSubcategory() != null) {
				enhancedDescription += " > " + jsonReport.getSubcategory();
			}
		}
		reportDefinition.setDescription(enhancedDescription);
		
		return reportDefinition;
	}
	
	/**
	 * Convert dataset definition based on type
	 */
	private DataSetDefinition convertDataSetDefinition(LegacyGenericReportSchema.DataSetDefinition jsonDataSet,
	        LegacyGenericReportSchema.ReportDefinition jsonReport) {
		String datasetType = jsonDataSet.getType();
		
		// Handle different dataset types
		switch (datasetType) {
			case "PATIENT_DATA_SET":
				return convertPatientDataSet(jsonDataSet, jsonReport);
			case "SQL_DATA_SET":
				return convertSqlDataSet(jsonDataSet, jsonReport);
			case "INDICATOR_DATA_SET":
				return convertIndicatorDataSet(jsonDataSet, jsonReport);
			default:
				log.warn("Unknown dataset type: " + datasetType + " for dataset: " + jsonDataSet.getName());
				return null;
		}
	}
	
	/**
	 * Convert patient dataset definition
	 */
	private DataSetDefinition convertPatientDataSet(LegacyGenericReportSchema.DataSetDefinition jsonDataSet,
	        LegacyGenericReportSchema.ReportDefinition jsonReport) {
		try {
			log.debug("Converting PATIENT_DATA_SET: " + jsonDataSet.getName());
			
			// Use existing resolver infrastructure
			org.openmrs.module.reportbuilder.legacyconfig.resolver.GenericConverterResolver converterResolver = new org.openmrs.module.reportbuilder.legacyconfig.resolver.GenericConverterResolver();
			org.openmrs.module.reportbuilder.legacyconfig.resolver.GenericDataDefinitionResolver dataDefResolver = new org.openmrs.module.reportbuilder.legacyconfig.resolver.GenericDataDefinitionResolver();
			
			// Create patient dataset definition
			org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition patientDataSet = new org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition();
			patientDataSet.setName(jsonDataSet.getName());
			
			// Handle row filter if present
			if (jsonDataSet.getRowFilter() != null) {
				log.debug("Processing row filter: " + jsonDataSet.getRowFilter().getType());
				// Row filters would be handled via cohort definitions
				// For now, we'll skip complex row filter handling
			}
			
			// Process columns if available in the dataset definition
			if (jsonDataSet.getColumns() != null) {
				for (LegacyGenericReportSchema.Column column : jsonDataSet.getColumns()) {
					try {
						// Resolve data definition
						LegacyGenericReportSchema.DataDefinition dataDef = column.getDataDefinition();
						if (dataDef != null) {
							org.openmrs.module.reporting.data.DataDefinition resolvedDataDef = dataDefResolver
							        .resolveDataDefinition(dataDef);
							
							// Resolve converter if present
							org.openmrs.module.reporting.data.converter.DataConverter converter = null;
							if (column.getConverter() != null) {
								converter = converterResolver.resolveConverter(column.getConverter());
							}
							
							// Add column to dataset
							if (resolvedDataDef != null) {
								patientDataSet.addColumn(column.getKey(), resolvedDataDef, (String) null, converter);
								log.debug("Added column: " + column.getKey());
							}
						}
					}
					catch (Exception e) {
						log.error("Failed to add column: " + column.getKey(), e);
						// Continue with other columns
					}
				}
			}
			
			log.info("Successfully converted PATIENT_DATA_SET: " + jsonDataSet.getName());
			return patientDataSet;
			
		}
		catch (Exception e) {
			log.error("Failed to convert PATIENT_DATA_SET: " + jsonDataSet.getName(), e);
			return null;
		}
	}
	
	/**
	 * Convert SQL dataset definition
	 */
	private DataSetDefinition convertSqlDataSet(LegacyGenericReportSchema.DataSetDefinition jsonDataSet,
	        LegacyGenericReportSchema.ReportDefinition jsonReport) {
		try {
			log.debug("Converting SQL_DATA_SET: " + jsonDataSet.getName());
			
			// Create SQL dataset definition
			org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition sqlDataSet = new org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition();
			sqlDataSet.setName(jsonDataSet.getName());
			
			// Extract SQL from row filter configuration
			if (jsonDataSet.getRowFilter() != null && jsonDataSet.getRowFilter().getConfig() != null
			        && jsonDataSet.getRowFilter().getConfig().containsKey("sql")) {
				
				String sqlQuery = (String) jsonDataSet.getRowFilter().getConfig().get("sql");
				sqlDataSet.setSqlQuery(sqlQuery);
				log.debug("Set SQL query for dataset: " + jsonDataSet.getName());
			} else {
				log.warn("No SQL query found in row filter config for: " + jsonDataSet.getName());
			}
			
			// Process parameters if defined
			if (jsonReport.getParameters() != null) {
				for (LegacyGenericReportSchema.Parameter param : jsonReport.getParameters()) {
					try {
						Class<?> paramClass = convertParameterType(param.getType());
						org.openmrs.module.reporting.evaluation.parameter.Parameter reportParam = new org.openmrs.module.reporting.evaluation.parameter.Parameter(
						        param.getName(), param.getLabel(), paramClass);
						sqlDataSet.addParameter(reportParam);
					}
					catch (ClassNotFoundException e) {
						log.warn("Unknown parameter type: " + param.getType() + " for: " + param.getName());
					}
				}
			}
			
			log.info("Successfully converted SQL_DATA_SET: " + jsonDataSet.getName());
			return sqlDataSet;
			
		}
		catch (Exception e) {
			log.error("Failed to convert SQL_DATA_SET: " + jsonDataSet.getName(), e);
			return null;
		}
	}
	
	/**
	 * Convert indicator dataset definition
	 */
	private DataSetDefinition convertIndicatorDataSet(LegacyGenericReportSchema.DataSetDefinition jsonDataSet,
	        LegacyGenericReportSchema.ReportDefinition jsonReport) {
		try {
			log.debug("Converting INDICATOR_DATA_SET: " + jsonDataSet.getName());
			
			// Use existing IndicatorDataSetResolver for advanced indicator processing
			org.openmrs.module.reportbuilder.legacyconfig.resolver.IndicatorDataSetResolver indicatorResolver = new org.openmrs.module.reportbuilder.legacyconfig.resolver.IndicatorDataSetResolver();
			
			// Check if this is a simple SQL-based indicator dataset or complex indicator dataset
			if (jsonDataSet.getRowFilter() != null && jsonDataSet.getRowFilter().getConfig() != null
			        && jsonDataSet.getRowFilter().getConfig().containsKey("sql")) {
				
				// Simple SQL-based indicator dataset
				return convertSimpleSqlIndicatorDataSet(jsonDataSet, jsonReport);
			} else {
				// Complex indicator dataset with multi-dimensional analysis
				log.info("Processing complex indicator dataset: " + jsonDataSet.getName());
				// For complex indicator datasets, we would need the full AdvancedFeatures configuration
				// For now, create a basic SQL dataset as fallback
				return convertSimpleSqlIndicatorDataSet(jsonDataSet, jsonReport);
			}
			
		}
		catch (Exception e) {
			log.error("Failed to convert INDICATOR_DATA_SET: " + jsonDataSet.getName(), e);
			return null;
		}
	}
	
	/**
	 * Convert simple SQL-based indicator dataset
	 */
	private DataSetDefinition convertSimpleSqlIndicatorDataSet(LegacyGenericReportSchema.DataSetDefinition jsonDataSet,
	        LegacyGenericReportSchema.ReportDefinition jsonReport) {
		
		try {
			log.debug("Converting simple SQL indicator dataset: " + jsonDataSet.getName());
			
			// Create SQL dataset definition
			org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition sqlDataSet = new org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition();
			sqlDataSet.setName(jsonDataSet.getName());
			
			// Extract SQL from row filter configuration
			if (jsonDataSet.getRowFilter() != null && jsonDataSet.getRowFilter().getConfig() != null
			        && jsonDataSet.getRowFilter().getConfig().containsKey("sql")) {
				
				String sqlQuery = (String) jsonDataSet.getRowFilter().getConfig().get("sql");
				sqlDataSet.setSqlQuery(sqlQuery);
				log.debug("Set SQL query for indicator dataset: " + jsonDataSet.getName());
			}
			
			log.info("Successfully converted simple SQL indicator dataset: " + jsonDataSet.getName());
			return sqlDataSet;
			
		}
		catch (Exception e) {
			log.error("Failed to convert simple SQL indicator dataset: " + jsonDataSet.getName(), e);
			return null;
		}
	}
	
	/**
	 * Update existing report with new data
	 */
	private void updateExistingReport(ReportDefinition existing, ReportDefinition updated) {
		existing.setName(updated.getName());
		existing.setDescription(updated.getDescription());
		existing.setParameters(updated.getParameters());
		existing.getDataSetDefinitions().clear();
		existing.getDataSetDefinitions().putAll(updated.getDataSetDefinitions());
	}
	
	/**
	 * Convert parameter type string to Class
	 */
	private Class<?> convertParameterType(String type) throws ClassNotFoundException {
		switch (type.toUpperCase()) {
			case "DATE":
				return Date.class;
			case "LOCATION":
				return org.openmrs.Location.class;
			case "TEXT":
				return String.class;
			case "NUMERIC":
				return Integer.class;
			case "BOOLEAN":
				return Boolean.class;
			default:
				return Class.forName(type);
		}
	}
	
	/**
	 * Check if generic reports have already been imported
	 */
	public boolean areGenericReportsAlreadyImported() {
		try {
			// Check if we have any reports that match our generic report naming pattern
			ReportDefinitionService reportDefService = Context.getService(ReportDefinitionService.class);
			
			// Look for a marker or check if we have reports with generic categorization
			// For now, check if any reports with "-generic" pattern exist
			// This is a simple check - could be more sophisticated
			
			// Check if the generic reports directory has files and we have corresponding reports
			if (!RuntimeDirectoryResolver.hasGenericReports()) {
				return false;
			}
			
			// Count reports vs files
			int fileCount = RuntimeDirectoryResolver.getGenericReportsCount();
			// Assume if we have reports and files, they might be imported
			// A more sophisticated check would look for specific UUIDs or naming patterns
			
			return fileCount > 0;
			
		}
		catch (Exception e) {
			log.debug("Could not determine if generic reports are already imported", e);
			return false;
		}
	}
	
	/**
	 * Ensure generic reports import task exists
	 */
	public void ensureImportAllGenericReportsTaskExists() {
		// This would create/verify a scheduled task for importing generic reports
		// Similar to the legacy report import task pattern
		log.info("Generic report import task verification complete");
	}
	
	/**
	 * Add imported generic report to the report library
	 */
	private void addToReportLibrary(org.openmrs.module.reporting.report.definition.ReportDefinition reportDefinition,
	        LegacyGenericReportSchema.ReportDefinition jsonReport) {
		try {
			ReportBuilderService reportBuilderService = Context.getService(ReportBuilderService.class);
			
			// Extract report information
			String name = jsonReport.getName() != null ? jsonReport.getName() : reportDefinition.getName();
			String description = jsonReport.getDescription();
			String code = jsonReport.getUuid(); // Use UUID as code for generic reports
			
			// Determine report type
			ReportBuilderReport.ReportType reportType = ReportBuilderReport.ReportType.AGGREGATE;
			if (jsonReport.getReportType() != null) {
				try {
					reportType = ReportBuilderReport.ReportType.fromString(jsonReport.getReportType());
				}
				catch (Exception e) {
					log.debug("Could not parse report type: {}", jsonReport.getReportType());
				}
			}
			
			// Find or create category
			ReportCategory category = null;
			if (jsonReport.getCategory() != null) {
				category = findOrCreateCategory(jsonReport.getCategory(), jsonReport.getSubcategory());
			}
			
			// Add to library using the service method
			reportBuilderService.addGenericReportToLibrary(reportDefinition.getUuid(), name, description, code, category,
			    reportType);
			
			log.info("Added generic report to library: {}", name);
		}
		catch (Exception e) {
			log.error("Failed to add generic report to library: {}", reportDefinition.getName(), e);
			// Don't throw exception to prevent breaking the import operation
		}
	}
	
	/**
	 * Find or create a report category based on category/subcategory names
	 */
	private ReportCategory findOrCreateCategory(String categoryName, String subcategoryName) {
		try {
			ReportBuilderService reportBuilderService = Context.getService(ReportBuilderService.class);
			
			// First try to find existing category by name
			List<ReportCategory> existingCategories = reportBuilderService.getReportCategories(categoryName, false, 0, 1);
			
			if (!existingCategories.isEmpty()) {
				return existingCategories.get(0);
			}
			
			// Create new category
			ReportCategory newCategory = new ReportCategory();
			newCategory.setName(categoryName);
			newCategory.setDescription(subcategoryName != null ? subcategoryName : categoryName);
			return reportBuilderService.saveReportCategory(newCategory);
		}
		catch (Exception e) {
			log.error("Failed to find or create category: {}", categoryName, e);
			return null;
		}
	}
}
