package org.openmrs.module.reportbuilder.util.data.evaluator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.reportbuilder.contract.LegacyGenericReportSchema;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.GenericConverterResolver;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.GenericDataDefinitionResolver;
import org.openmrs.module.reportbuilder.util.PatientDataHelper;
import org.openmrs.module.reportbuilder.util.data.definition.LineListDataSetDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.*;

/**
 * Evaluator for ETL-based line listing reports. Reads a JSON configuration file and builds a
 * patient dataset by: 1. Parsing the baseCohortDefinition SQL to get patient IDs 2. Creating
 * columns from the dataSetDefinitions 3. Evaluating each column for each patient
 */
@Handler(supports = { LineListDataSetDefinition.class })
public class LineListDataSetEvaluator implements DataSetEvaluator {
	
	private static final Logger log = LoggerFactory.getLogger(LineListDataSetEvaluator.class);
	
	@Autowired
	private EvaluationService evaluationService;
	
	private final GenericDataDefinitionResolver dataDefinitionResolver;
	
	private final GenericConverterResolver converterResolver;
	
	public LineListDataSetEvaluator() {
		this.dataDefinitionResolver = new GenericDataDefinitionResolver();
		this.converterResolver = new GenericConverterResolver();
	}
	
	@Override
	public SimpleDataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext)
	        throws EvaluationException {
		LineListDataSetDefinition definition = (LineListDataSetDefinition) dataSetDefinition;
		
		SimpleDataSet dataSet = new SimpleDataSet(definition, evaluationContext);
		
		File reportDesign = definition.getReportDesign();
		if (reportDesign == null) {
			throw new RuntimeException("Report design file is not configured");
		}
		if (!reportDesign.exists()) {
			throw new RuntimeException("Report design file not found: " + reportDesign.getAbsolutePath());
		}
		
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			LegacyGenericReportSchema.ReportDefinition reportConfig = objectMapper.readValue(reportDesign,
			    LegacyGenericReportSchema.ReportDefinition.class);
			
			// Get patient IDs from base cohort definition
			Set<Integer> patientIds = getPatientIdsFromBaseCohort(reportConfig, evaluationContext);
			
			log.debug("Found {} patients in line list", patientIds.size());
			
			// Get the first PATIENT_DATA_SET dataset definition
			LegacyGenericReportSchema.DataSetDefinition patientDataSet = findPatientDataSet(reportConfig);
			if (patientDataSet == null) {
				throw new RuntimeException("No PATIENT_DATA_SET found in report configuration");
			}
			
			// Build column definitions
			Map<String, ColumnDefinition> columns = buildColumnDefinitions(patientDataSet);
			
			PatientDataHelper pdh = new PatientDataHelper();
			
			// Evaluate each column for each patient and add rows
			for (Integer patientId : patientIds) {
				DataSetRow row = new DataSetRow();
				
				for (Map.Entry<String, ColumnDefinition> entry : columns.entrySet()) {
					String columnKey = entry.getKey();
					ColumnDefinition colDef = entry.getValue();
					
					try {
						Object value = evaluateColumnForPatient(colDef, patientId, evaluationContext);
						pdh.addCol(row, columnKey, value);
					}
					catch (Exception e) {
						log.error("Failed to evaluate column {} for patient {}: {}", columnKey, patientId, e.getMessage());
						pdh.addCol(row, columnKey, null);
					}
				}
				
				dataSet.addRow(row);
			}
			
			log.info("Evaluated line list with {} rows", dataSet.getRows().size());
			return dataSet;
			
		}
		catch (Exception e) {
			throw new EvaluationException("Failed to evaluate line list dataset: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Get patient IDs from the base cohort definition SQL
	 */
	private Set<Integer> getPatientIdsFromBaseCohort(LegacyGenericReportSchema.ReportDefinition reportConfig,
	        EvaluationContext context) {
		Set<Integer> patientIds = new HashSet<>();

		LegacyGenericReportSchema.BaseCohortDefinition baseCohort = reportConfig.getBaseCohortDefinition();
		if (baseCohort == null) {
			throw new RuntimeException("Base cohort definition is required");
		}

		if (!"SQL".equalsIgnoreCase(baseCohort.getType())) {
			throw new RuntimeException("Only SQL base cohort definitions are supported, got: " + baseCohort.getType());
		}

		Map<String, Object> config = baseCohort.getConfig();
		if (config == null || !config.containsKey("sql")) {
			throw new RuntimeException("Base cohort definition missing SQL query");
		}

		String sql = (String) config.get("sql");
		sql = applyDatePlaceholders(sql, context);

		try {
			SqlQueryBuilder queryBuilder = new SqlQueryBuilder(sql);
			List<Object[]> results = evaluationService.evaluateToList(queryBuilder, context);

			for (Object[] row : results) {
				if (row != null && row.length > 0) {
					Object id = row[0];
					if (id instanceof Number) {
						patientIds.add(((Number) id).intValue());
					}
					else if (id != null) {
						try {
							patientIds.add(Integer.parseInt(id.toString()));
						}
						catch (NumberFormatException e) {
							log.warn("Could not parse patient ID: {}", id);
						}
					}
				}
			}
		}
		catch (Exception e) {
			log.error("Failed to execute base cohort SQL: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to execute base cohort SQL: " + e.getMessage(), e);
		}

		return patientIds;
	}
	
	/**
	 * Find the PATIENT_DATA_SET dataset definition
	 */
	private LegacyGenericReportSchema.DataSetDefinition findPatientDataSet(
	        LegacyGenericReportSchema.ReportDefinition reportConfig) {
		if (reportConfig.getDataSetDefinitions() == null) {
			return null;
		}
		
		for (LegacyGenericReportSchema.DataSetDefinition dsd : reportConfig.getDataSetDefinitions()) {
			if ("PATIENT_DATA_SET".equalsIgnoreCase(dsd.getType())) {
				return dsd;
			}
		}
		
		return null;
	}
	
	/**
	 * Build column definitions from the dataset configuration
	 */
	private Map<String, ColumnDefinition> buildColumnDefinitions(
	        LegacyGenericReportSchema.DataSetDefinition patientDataSet) {
		Map<String, ColumnDefinition> columns = new LinkedHashMap<>();

		if (patientDataSet.getColumns() == null) {
			return columns;
		}

		for (LegacyGenericReportSchema.Column column : patientDataSet.getColumns()) {
			String key = column.getKey();
			if (key == null || key.trim().isEmpty()) {
				key = column.getName();
			}
			if (key == null || key.trim().isEmpty()) {
				log.warn("Column missing key and name, skipping");
				continue;
			}

			LegacyGenericReportSchema.DataDefinition dataDef = column.getDataDefinition();
			if (dataDef == null) {
				log.warn("Column {} missing data definition, skipping", key);
				continue;
			}

			DataDefinition resolvedDataDef = dataDefinitionResolver.resolveDataDefinition(dataDef);
			if (resolvedDataDef == null) {
				log.warn("Could not resolve data definition for column {}, skipping", key);
				continue;
			}

			DataConverter converter = null;
			if (column.getConverter() != null) {
				converter = converterResolver.resolveConverter(column.getConverter());
			}

			columns.put(key, new ColumnDefinition(key, column.getName(), resolvedDataDef, converter));
		}

		return columns;
	}
	
	/**
	 * Evaluate a single column for a specific patient
	 */
	private Object evaluateColumnForPatient(ColumnDefinition colDef, Integer patientId, EvaluationContext context) {
		try {
			DataDefinition dataDef = colDef.getDataDefinition();
			
			// For SqlPatientDataDefinition, extract and execute the SQL
			if (dataDef.getClass().getSimpleName().equals("SqlPatientDataDefinition")) {
				return evaluateSqlPatientDataDefinition(dataDef, patientId, context);
			}
			
			// For other data definitions, return null for now
			// TODO: Implement support for other data definition types
			log.warn("Unsupported data definition type: {}", dataDef.getClass().getSimpleName());
			return null;
			
		}
		catch (Exception e) {
			log.error("Failed to evaluate column {} for patient {}: {}", colDef.getKey(), patientId, e.getMessage());
			return null;
		}
	}
	
	/**
	 * Evaluate a SqlPatientDataDefinition for a specific patient
	 */
	private Object evaluateSqlPatientDataDefinition(DataDefinition dataDef, Integer patientId, EvaluationContext context) {
		try {
			// Use reflection to get the SQL from SqlPatientDataDefinition
			String sql = (String) dataDef.getClass().getMethod("getSql").invoke(dataDef);
			
			if (sql == null || sql.trim().isEmpty()) {
				return null;
			}
			
			// Replace :patientId placeholder with actual patient ID
			sql = sql.replace(":patientId", String.valueOf(patientId));
			
			// Apply date placeholders if present
			sql = applyDatePlaceholders(sql, context);
			
			// Execute the query
			SqlQueryBuilder queryBuilder = new SqlQueryBuilder(sql);
			List<Object[]> results = evaluationService.evaluateToList(queryBuilder, context);
			
			if (results == null || results.isEmpty()) {
				return null;
			}
			
			// Return the first column of the first row
			Object[] firstRow = results.get(0);
			if (firstRow != null && firstRow.length > 0) {
				return firstRow[0];
			}
			
			return null;
			
		}
		catch (Exception e) {
			log.error("Failed to evaluate SqlPatientDataDefinition for patient {}: {}", patientId, e.getMessage());
			return null;
		}
	}
	
	/**
	 * Apply date placeholders to SQL query
	 */
	private String applyDatePlaceholders(String sql, EvaluationContext context) {
		String result = sql;
		
		// Get date parameters from context
		Object startDate = context.getParameterValue("startDate");
		Object endDate = context.getParameterValue("endDate");
		
		if (startDate != null) {
			String startDateStr = formatDate(startDate);
			result = result.replace(":startDate", "'" + startDateStr + "'");
		}
		
		if (endDate != null) {
			String endDateStr = formatDate(endDate);
			result = result.replace(":endDate", "'" + endDateStr + "'");
		}
		
		return result;
	}
	
	/**
	 * Format a date object to YYYY-MM-DD format
	 */
	private String formatDate(Object date) {
		if (date instanceof Date) {
			return DateUtil.formatDate((Date) date, "yyyy-MM-dd");
		}
		if (date != null) {
			return date.toString();
		}
		return "";
	}
	
	/**
	 * Internal class to hold column definition metadata
	 */
	private static class ColumnDefinition {
		
		private final String key;
		
		private final String name;
		
		private final DataDefinition dataDefinition;
		
		private final DataConverter converter;
		
		public ColumnDefinition(String key, String name, DataDefinition dataDefinition, DataConverter converter) {
			this.key = key;
			this.name = name;
			this.dataDefinition = dataDefinition;
			this.converter = converter;
		}
		
		public String getKey() {
			return key;
		}
		
		public String getName() {
			return name;
		}
		
		public DataDefinition getDataDefinition() {
			return dataDefinition;
		}
		
		public DataConverter getConverter() {
			return converter;
		}
	}
}
