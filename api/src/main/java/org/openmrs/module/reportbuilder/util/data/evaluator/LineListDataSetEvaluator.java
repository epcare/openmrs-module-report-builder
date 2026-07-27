package org.openmrs.module.reportbuilder.util.data.evaluator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.Cohort;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.service.PersonDataService;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
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
 * Evaluator for ETL-based line listing reports. Reads a JSON configuration file
 * (LegacyGenericReportSchema format) and builds a patient dataset by:
 * <ol>
 * <li>Parsing the baseCohortDefinition SQL to get patient IDs</li>
 * <li>Creating columns from the dataSetDefinitions</li>
 * <li>Evaluating each column for each patient</li>
 * </ol>
 * SQL and CALCULATION columns are evaluated per-patient via direct SQL execution. Typed OpenMRS
 * data definitions (PERSON_NAME, PERSON_ATTRIBUTE, PERSON_ADDRESS, IDENTIFIER, GENDER, BIRTHDATE)
 * are evaluated once over the whole patient cohort via the reporting module's PatientDataService /
 * PersonDataService and looked up per row.
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
			
			// Scope all per-cohort data evaluation to this report's patient set.
			evaluationContext.setBaseCohort(new Cohort(patientIds));
			
			// Build column definitions
			Map<String, ColumnDefinition> columns = buildColumnDefinitions(patientDataSet);
			
			// Pre-evaluate typed data definitions once over the whole cohort (one service call each).
			// SQL / CALCULATION columns are intentionally excluded here; they run per-patient below.
			Map<String, Map<Integer, Object>> columnValueMaps = preEvaluateColumns(columns, evaluationContext);
			
			PatientDataHelper pdh = new PatientDataHelper();
			
			// Evaluate each column for each patient and add rows
			for (Integer patientId : patientIds) {
				DataSetRow row = new DataSetRow();
				
				for (Map.Entry<String, ColumnDefinition> entry : columns.entrySet()) {
					String columnKey = entry.getKey();
					ColumnDefinition colDef = entry.getValue();
					
					try {
						Object value = resolveColumnValue(colDef, patientId, evaluationContext,
						    columnValueMaps.get(columnKey));
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
	 * Resolves a single column's value for a patient. SQL / CALCULATION columns execute per-patient
	 * SQL; typed columns look up their pre-evaluated cohort value and unwrap it to a display value.
	 */
	private Object resolveColumnValue(ColumnDefinition colDef, Integer patientId, EvaluationContext context,
	        Map<Integer, Object> typedValues) {
		DataDefinition dataDef = colDef.getDataDefinition();
		if (isSqlPatientDataDefinition(dataDef)) {
			return evaluateSqlPatientDataDefinition(dataDef, patientId, context);
		}
		Object value = typedValues != null ? typedValues.get(patientId) : null;
		return unwrapValue(value, colDef);
	}
	
	/**
	 * Pre-evaluates every non-SQL column's data definition once over the cohort, returning a
	 * per-column map of patientId -&gt; value. Person-level definitions are evaluated via
	 * PersonDataService (patient ids are person ids); patient-level definitions via
	 * PatientDataService.
	 */
	private Map<String, Map<Integer, Object>> preEvaluateColumns(Map<String, ColumnDefinition> columns,
	        EvaluationContext context) {
		Map<String, Map<Integer, Object>> result = new HashMap<String, Map<Integer, Object>>();
		PersonDataService personDataService = null;
		PatientDataService patientDataService = null;
		
		for (Map.Entry<String, ColumnDefinition> entry : columns.entrySet()) {
			ColumnDefinition colDef = entry.getValue();
			DataDefinition dataDef = colDef.getDataDefinition();
			if (isSqlPatientDataDefinition(dataDef)) {
				continue; // handled per-patient by SQL execution
			}
			Map<Integer, Object> values;
			try {
				if (dataDef instanceof PersonDataDefinition) {
					if (personDataService == null) {
						personDataService = Context.getService(PersonDataService.class);
					}
					values = personDataService.evaluate((PersonDataDefinition) dataDef, context).getData();
				} else if (dataDef instanceof PatientDataDefinition) {
					if (patientDataService == null) {
						patientDataService = Context.getService(PatientDataService.class);
					}
					values = patientDataService.evaluate((PatientDataDefinition) dataDef, context).getData();
				} else {
					log.warn("Unsupported data definition type for cohort evaluation: {}", dataDef.getClass()
					        .getSimpleName());
					values = new HashMap<Integer, Object>();
				}
			}
			catch (Exception e) {
				log.error("Failed to evaluate column {} over cohort: {}", colDef.getKey(), e.getMessage(), e);
				values = new HashMap<Integer, Object>();
			}
			result.put(entry.getKey(), values);
		}
		return result;
	}
	
	/**
	 * Converts a resolved data value into a display value: applies the column converter when
	 * present, otherwise unwraps known OpenMRS value objects (PersonName, PatientIdentifier,
	 * PersonAttribute, PersonAddress) into plain scalar values.
	 */
	private Object unwrapValue(Object value, ColumnDefinition colDef) {
		if (value == null) {
			return null;
		}
		
		if (colDef.getConverter() != null) {
			try {
				return colDef.getConverter().convert(value);
			}
			catch (Exception e) {
				log.warn("Converter failed for column {}: {}", colDef.getKey(), e.getMessage());
			}
		}
		
		// Collapse single-element collections (e.g. identifier lists).
		if (value instanceof Collection) {
			Collection<?> collection = (Collection<?>) value;
			if (collection.isEmpty()) {
				return null;
			}
			value = collection.iterator().next();
			if (value == null) {
				return null;
			}
		}
		
		if (value instanceof PersonName) {
			return ((PersonName) value).getFullName();
		}
		if (value instanceof PatientIdentifier) {
			return ((PatientIdentifier) value).getIdentifier();
		}
		if (value instanceof PersonAttribute) {
			return ((PersonAttribute) value).getValue();
		}
		if (value instanceof PersonAddress) {
			return extractAddressField((PersonAddress) value, colDef.getAddressField());
		}
		return value;
	}
	
	/**
	 * Extracts a single address field (e.g. cityVillage, address5) from a PersonAddress, falling
	 * back to the full address string when the field cannot be read.
	 */
	private Object extractAddressField(PersonAddress address, String field) {
		if (field == null || field.trim().isEmpty()) {
			return address.toString();
		}
		try {
			return new PropertyConverter(PersonAddress.class, field).convert(address);
		}
		catch (Exception e) {
			log.warn("Could not extract address field '{}': {}", field, e.getMessage());
			return address.toString();
		}
	}
	
	private boolean isSqlPatientDataDefinition(DataDefinition dataDef) {
		return dataDef != null && "SqlPatientDataDefinition".equals(dataDef.getClass().getSimpleName());
	}
	
	/**
	 * Get patient IDs from the base cohort definition SQL
	 */
	private Set<Integer> getPatientIdsFromBaseCohort(LegacyGenericReportSchema.ReportDefinition reportConfig,
	        EvaluationContext context) {
		Set<Integer> patientIds = new HashSet<Integer>();
		
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
					} else if (id != null) {
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
	private Map<String, ColumnDefinition> buildColumnDefinitions(LegacyGenericReportSchema.DataSetDefinition patientDataSet) {
		Map<String, ColumnDefinition> columns = new LinkedHashMap<String, ColumnDefinition>();
		
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
			
			columns.put(key, new ColumnDefinition(key, column.getName(), resolvedDataDef, converter, dataDef.getConfig()));
		}
		
		return columns;
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
		
		private final Map<String, Object> rawConfig;
		
		public ColumnDefinition(String key, String name, DataDefinition dataDefinition, DataConverter converter,
		    Map<String, Object> rawConfig) {
			this.key = key;
			this.name = name;
			this.dataDefinition = dataDefinition;
			this.converter = converter;
			this.rawConfig = rawConfig;
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
		
		/**
		 * The address field to extract (e.g. cityVillage, address5) for PERSON_ADDRESS columns,
		 * read from the raw data definition config.
		 */
		public String getAddressField() {
			if (rawConfig != null) {
				Object field = rawConfig.get("field");
				if (field != null) {
					return field.toString();
				}
			}
			return null;
		}
	}
}
