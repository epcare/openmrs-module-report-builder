/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reportbuilder.legacyconfig.resolver;

import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredAddressDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reportbuilder.contract.LegacyGenericReportSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Simplified generic data definition resolver. Uses only standard OpenMRS data definitions and SQL
 * - no custom classes. Supports: person attributes, identifiers, basic person data, addresses,
 * names, and SQL queries.
 */
public class GenericDataDefinitionResolver {
	
	private static final Logger log = LoggerFactory.getLogger(GenericDataDefinitionResolver.class);
	
	/**
	 * Resolve a data definition from generic JSON configuration
	 */
	public DataDefinition resolveDataDefinition(LegacyGenericReportSchema.DataDefinition jsonDef) {
		if (jsonDef == null) {
			throw new IllegalArgumentException("Data definition cannot be null");
		}
		
		String type = jsonDef.getType();
		if (type == null) {
			throw new IllegalArgumentException("Data definition type cannot be null");
		}
		
		try {
			switch (type.toUpperCase()) {
				case "PERSON_ATTRIBUTE":
					return resolvePersonAttributeDefinition(jsonDef);
				case "IDENTIFIER":
					return resolveIdentifierDefinition(jsonDef);
				case "BIRTHDATE":
					return new BirthdateDataDefinition();
				case "GENDER":
					return new GenderDataDefinition();
				case "PREFERRED_ADDRESS":
					return resolvePreferredAddressDefinition(jsonDef);
				case "PERSON_ADDRESS":
					return resolvePersonAddressDefinition(jsonDef);
				case "PERSON_NAME":
					return resolvePersonNameDefinition(jsonDef);
				case "CALCULATION":
					return resolveCalculationDefinition(jsonDef);
				case "SQL":
					return resolveSqlDataDefinition(jsonDef);
				case "OBSERVATION":
					return resolveObservationDefinition(jsonDef);
				case "ENCOUNTER_DIAGNOSIS":
					return resolveEncounterDiagnosisDefinition(jsonDef);
				default:
					log.warn("Unknown data definition type: {}, attempting SQL as fallback", type);
					return resolveSqlDataDefinition(jsonDef);
			}
		}
		catch (Exception e) {
			log.error("Failed to resolve data definition of type: " + type, e);
			// Return null instead of throwing exception to avoid transaction issues
			return null;
		}
	}
	
	/**
	 * Resolve person attribute data definition
	 */
	private DataDefinition resolvePersonAttributeDefinition(LegacyGenericReportSchema.DataDefinition jsonDef) {
		Map<String, Object> config = jsonDef.getConfig();
		if (config == null) {
			log.warn("Person attribute definition missing config");
			return null;
		}
		
		// Check if this is actually a basic person field (GENDER, BIRTHDATE, etc.)
		if (config.containsKey("type")) {
			String type = (String) config.get("type");
			log.info("PERSON_ATTRIBUTE with type field, treating as basic person data: {}", type);
			
			// Map basic person types to their data definitions
			switch (type.toUpperCase()) {
				case "GENDER":
					return new GenderDataDefinition();
				case "BIRTHDATE":
					return new BirthdateDataDefinition();
				default:
					log.warn("Unknown person attribute type: {}, returning null", type);
					return null;
			}
		}
		
		// Handle actual person attributes with UUID
		if (!config.containsKey("attributeTypeUuid")) {
			log.warn("Person attribute definition missing both type and attributeTypeUuid");
			return null;
		}
		
		String attributeTypeUuid = (String) config.get("attributeTypeUuid");
		try {
			PersonAttributeType attributeType = Context.getPersonService().getPersonAttributeTypeByUuid(attributeTypeUuid);
			
			PersonAttributeDataDefinition def = new PersonAttributeDataDefinition();
			def.setPersonAttributeType(attributeType);
			
			log.info("Resolved person attribute definition: " + attributeTypeUuid);
			return def;
		}
		catch (Exception e) {
			log.error("Failed to resolve person attribute type: " + attributeTypeUuid, e);
			return null;
		}
	}
	
	/**
	 * Resolve patient identifier definition
	 */
	private DataDefinition resolveIdentifierDefinition(LegacyGenericReportSchema.DataDefinition jsonDef) {
		Map<String, Object> config = jsonDef.getConfig();
		if (config == null || !config.containsKey("identifierTypeUuid")) {
			log.warn("Identifier definition missing identifierTypeUuid");
			return null;
		}
		
		String identifierTypeUuid = (String) config.get("identifierTypeUuid");
		
		try {
			PatientIdentifierDataDefinition def = new PatientIdentifierDataDefinition();
			
			PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(
			    identifierTypeUuid);
			if (identifierType != null) {
				def.addType(identifierType);
			} else {
				log.warn("Identifier type not found for uuid: {}", identifierTypeUuid);
			}
			// Return a single identifier (preferred-first) rather than a list, so the evaluator can
			// unwrap it directly to the identifier string.
			def.setIncludeFirstNonNullOnly(Boolean.TRUE);
			
			log.info("Resolved identifier definition: " + identifierTypeUuid);
			return def;
		}
		catch (Exception e) {
			log.error("Failed to resolve identifier definition: " + identifierTypeUuid, e);
			return null;
		}
	}
	
	/**
	 * Resolve preferred address definition
	 */
	private DataDefinition resolvePreferredAddressDefinition(LegacyGenericReportSchema.DataDefinition jsonDef) {
		try {
			PreferredAddressDataDefinition def = new PreferredAddressDataDefinition();
			log.info("Resolved preferred address definition");
			return def;
		}
		catch (Exception e) {
			log.error("Failed to resolve preferred address definition", e);
			return null;
		}
	}
	
	/**
	 * Resolve person address definition Supports: ADDRESS_FIELD (specific field like address5),
	 * ADDRESS_FIELDS (multiple fields), FULL_ADDRESS
	 */
	private DataDefinition resolvePersonAddressDefinition(LegacyGenericReportSchema.DataDefinition jsonDef) {
		Map<String, Object> config = jsonDef.getConfig();
		if (config == null) {
			log.warn("Person address definition missing config");
			return null;
		}
		
		String addressType = (String) config.get("type");
		
		try {
			// For ADDRESS_FIELD, use preferred address which will extract the field
			if ("ADDRESS_FIELD".equals(addressType) || "ADDRESS_FIELDS".equals(addressType)) {
				PreferredAddressDataDefinition def = new PreferredAddressDataDefinition();
				log.info("Resolved person address definition for field: {}", config.get("field"));
				return def;
			}
			
			// For FULL_ADDRESS or ADDRESS_COMBINATION, use preferred address
			PreferredAddressDataDefinition def = new PreferredAddressDataDefinition();
			log.info("Resolved person address definition: {}", addressType);
			return def;
		}
		catch (Exception e) {
			log.error("Failed to resolve person address definition", e);
			return null;
		}
	}
	
	/**
	 * Resolve person name definition Supports: FULL_NAME, GIVEN_NAME, MIDDLE_NAME, FAMILY_NAME
	 */
	private DataDefinition resolvePersonNameDefinition(LegacyGenericReportSchema.DataDefinition jsonDef) {
		// PreferredNameDataDefinition evaluates to the patient's preferred PersonName; the line-list
		// evaluator unwraps it to a full-name string.
		log.info("Resolved person name definition via PreferredNameDataDefinition");
		return new PreferredNameDataDefinition();
	}
	
	/**
	 * Resolve calculation definition Used for calculated fields and transformations. Now
	 * implemented using SQL-based approach for common calculations like AGE and AGE_IN_RANGE.
	 */
	private DataDefinition resolveCalculationDefinition(LegacyGenericReportSchema.DataDefinition jsonDef) {
		Map<String, Object> config = jsonDef.getConfig();
		if (config == null) {
			log.warn("Calculation definition missing config");
			return null;
		}
		
		String calculation = (String) config.get("calculation");
		if (calculation == null) {
			log.warn("Calculation definition missing calculation type");
			return null;
		}
		
		try {
			// Handle AGE calculation
			if ("AGE".equals(calculation)) {
				String onDate = (String) config.get("onDate");
				if (onDate == null) {
					log.warn("AGE calculation missing onDate parameter");
					return null;
				}
				
				// Use SQL to calculate age
				String sql = "SELECT TIMESTAMPDIFF(YEAR, p.birthdate, " + onDate + ") "
				        + "FROM person p WHERE p.person_id = :patientId";
				
				SqlPatientDataDefinition sqlDef = new SqlPatientDataDefinition();
				sqlDef.setSql(sql);
				sqlDef.setName("Age Calculation");
				
				log.info("Successfully resolved AGE calculation");
				return sqlDef;
			}
			
			// Handle AGE_IN_RANGE calculation
			if ("AGE_IN_RANGE".equals(calculation)) {
				Object minAgeObj = config.get("minAge");
				Object maxAgeObj = config.get("maxAge");
				String onDate = (String) config.get("onDate");
				
				if (minAgeObj == null || maxAgeObj == null || onDate == null) {
					log.warn("AGE_IN_RANGE calculation missing required parameters");
					return null;
				}
				
				int minAge = Integer.parseInt(minAgeObj.toString());
				int maxAge = Integer.parseInt(maxAgeObj.toString());
				
				// Use SQL to check if age is in range
				String sql = String.format("SELECT CASE WHEN TIMESTAMPDIFF(YEAR, p.birthdate, %s) "
				        + "BETWEEN %d AND %d THEN 1 ELSE 0 END " + "FROM person p WHERE p.person_id = :patientId", onDate,
				    minAge, maxAge);
				
				SqlPatientDataDefinition sqlDef = new SqlPatientDataDefinition();
				sqlDef.setSql(sql);
				sqlDef.setName("Age Range Calculation (" + minAge + "-" + maxAge + ")");
				
				log.info("Successfully resolved AGE_IN_RANGE calculation: {}-{}", minAge, maxAge);
				return sqlDef;
			}
			
			// Handle AGE_IN_RANGE_EXCLUSIVE calculation (returns true if age is in range)
			if ("AGE_IN_RANGE_EXCLUSIVE".equals(calculation)) {
				Object minAgeObj = config.get("minAge");
				Object maxAgeObj = config.get("maxAge");
				String onDate = (String) config.get("onDate");
				
				if (minAgeObj == null || maxAgeObj == null || onDate == null) {
					log.warn("AGE_IN_RANGE_EXCLUSIVE calculation missing required parameters");
					return null;
				}
				
				int minAge = Integer.parseInt(minAgeObj.toString());
				int maxAge = Integer.parseInt(maxAgeObj.toString());
				
				// Use SQL to check if age is in range (exclusive)
				String sql = String.format("SELECT CASE WHEN TIMESTAMPDIFF(YEAR, p.birthdate, %s) "
				        + "> %d AND TIMESTAMPDIFF(YEAR, p.birthdate, %s) < %d THEN 1 ELSE 0 END "
				        + "FROM person p WHERE p.person_id = :patientId", onDate, minAge, onDate, maxAge);
				
				SqlPatientDataDefinition sqlDef = new SqlPatientDataDefinition();
				sqlDef.setSql(sql);
				sqlDef.setName("Age Range Exclusive Calculation (" + minAge + "-" + maxAge + ")");
				
				log.info("Successfully resolved AGE_IN_RANGE_EXCLUSIVE calculation: {}-{}", minAge, maxAge);
				return sqlDef;
			}
			
			// Unsupported calculation type
			log.warn("Unsupported calculation type: {}. Supported types: AGE, AGE_IN_RANGE, AGE_IN_RANGE_EXCLUSIVE",
			    calculation);
			return null;
		}
		catch (NumberFormatException e) {
			log.error("Failed to parse numeric parameters for calculation: " + calculation, e);
			return null;
		}
		catch (Exception e) {
			log.error("Failed to resolve calculation definition for type: " + calculation, e);
			return null;
		}
	}
	
	/**
	 * Resolve SQL data definition for complex queries This is the key to eliminating custom Java
	 * classes - use SQL for complex logic. Now properly implemented using SqlPatientDataDefinition.
	 */
	private DataDefinition resolveSqlDataDefinition(LegacyGenericReportSchema.DataDefinition jsonDef) {
		Map<String, Object> config = jsonDef.getConfig();
		if (config == null || !config.containsKey("sql")) {
			log.warn("SQL data definition missing sql query");
			return null;
		}
		
		String sql = (String) config.get("sql");
		
		try {
			// Use the standard OpenMRS SqlPatientDataDefinition class
			SqlPatientDataDefinition sqlDef = new SqlPatientDataDefinition();
			sqlDef.setSql(sql);
			
			// Set a descriptive name based on the SQL query
			String name = "SQL Data Definition";
			if (sql.length() > 50) {
				name = "SQL: " + sql.substring(0, 47) + "...";
			} else {
				name = "SQL: " + sql;
			}
			sqlDef.setName(name);
			
			log.info("Successfully resolved SQL data definition with query length: {}", sql.length());
			return sqlDef;
		}
		catch (Exception e) {
			log.error("Failed to create SQL data definition: " + e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * Resolve observation data definition Fetches observation values from the obs table based on
	 * concept UUID and column modifiers
	 */
	private DataDefinition resolveObservationDefinition(LegacyGenericReportSchema.DataDefinition jsonDef) {
		Map<String, Object> config = jsonDef.getConfig();
		if (config == null) {
			log.warn("Observation definition missing config");
			return null;
		}
		
		String conceptUuid = (String) config.get("conceptUuid");
		if (conceptUuid == null) {
			log.warn("Observation definition missing conceptUuid");
			return null;
		}
		
		String conceptName = (String) config.get("conceptName");
		String columnModifier = (String) config.getOrDefault("columnModifier", "MOST_RECENT");
		Object modifierCountObj = config.get("modifierCount");
		int modifierCount = modifierCountObj != null ? Integer.parseInt(modifierCountObj.toString()) : 1;
		Boolean returnDisplay = (Boolean) config.getOrDefault("returnDisplay", Boolean.TRUE);
		
		@SuppressWarnings("unchecked")
		List<String> extraValues = (List<String>) config.get("extraValues");
		
		try {
			String sql = buildObservationSql(conceptUuid, columnModifier, modifierCount, returnDisplay, extraValues);
			
			SqlPatientDataDefinition sqlDef = new SqlPatientDataDefinition();
			sqlDef.setSql(sql);
			
			String name = "Observation: " + (conceptName != null ? conceptName : conceptUuid);
			sqlDef.setName(name);
			
			log.info("Resolved observation definition for concept: {} with modifier: {}", conceptUuid, columnModifier);
			return sqlDef;
		}
		catch (Exception e) {
			log.error("Failed to resolve observation definition for concept: " + conceptUuid, e);
			return null;
		}
	}
	
	/**
	 * Resolve encounter diagnosis data definition Fetches diagnosis values from the
	 * encounter_diagnosis table based on filters
	 */
	private DataDefinition resolveEncounterDiagnosisDefinition(LegacyGenericReportSchema.DataDefinition jsonDef) {
		Map<String, Object> config = jsonDef.getConfig();
		if (config == null) {
			log.warn("Encounter diagnosis definition missing config");
			return null;
		}
		
		String conceptUuid = (String) config.get("conceptUuid");
		String conceptName = (String) config.get("conceptName");
		String rank = (String) config.getOrDefault("rank", "ANY");
		Boolean confirmedOnly = (Boolean) config.getOrDefault("confirmedOnly", Boolean.FALSE);
		String strategy = (String) config.getOrDefault("strategy", "LATEST");
		
		try {
			String sql = buildDiagnosisSql(conceptUuid, rank, confirmedOnly, strategy);
			
			SqlPatientDataDefinition sqlDef = new SqlPatientDataDefinition();
			sqlDef.setSql(sql);
			
			String name = "Diagnosis: " + (conceptName != null ? conceptName : (conceptUuid != null ? conceptUuid : "All"));
			sqlDef.setName(name);
			
			log.info("Resolved encounter diagnosis definition for concept: {} with rank: {}", conceptUuid, rank);
			return sqlDef;
		}
		catch (Exception e) {
			log.error("Failed to resolve encounter diagnosis definition for concept: " + conceptUuid, e);
			return null;
		}
	}
	
	/**
	 * Build SQL query for observation data retrieval
	 */
	private String buildObservationSql(String conceptUuid, String columnModifier, int modifierCount, Boolean returnDisplay,
	        List<String> extraValues) {
		return buildObservationSql(conceptUuid, columnModifier, modifierCount, returnDisplay, extraValues, 0);
	}
	
	/**
	 * Build SQL query for observation data retrieval with offset support Used for multi-column
	 * expansion (e.g., weight_1, weight_2, weight_3)
	 */
	private String buildObservationSql(String conceptUuid, String columnModifier, int modifierCount, Boolean returnDisplay,
	        List<String> extraValues, int offset) {
		
		StringBuilder sql = new StringBuilder();
		
		// Determine which fields to select based on extra values
		if (extraValues != null && !extraValues.isEmpty()) {
			// Build SELECT clause with extra values
			sql.append("SELECT ");
			
			for (int i = 0; i < extraValues.size(); i++) {
				String ev = extraValues.get(i);
				switch (ev) {
					case "obsDatetime":
						sql.append("o.obs_datetime");
						break;
					case "location":
						sql.append("l.name as location_name");
						break;
					case "comment":
						sql.append("o.comment");
						break;
					case "encounterType":
						sql.append("et.name as encounter_type_name");
						break;
					case "provider":
						sql.append("pr.name as provider_name");
						break;
					default:
						sql.append("NULL");
						break;
				}
				if (i < extraValues.size() - 1) {
					sql.append(", ");
				}
			}
			sql.append(", ");
		} else {
			sql.append("SELECT ");
		}
		
		// Build the main value expression
		sql.append(buildObservationValueExpression(returnDisplay));
		
		// Apply column modifier logic
		if ("ANY".equalsIgnoreCase(columnModifier)) {
			sql.append(" FROM obs o");
		} else if ("FIRST".equalsIgnoreCase(columnModifier)) {
			sql.append(" FROM obs o");
		} else if ("MOST_RECENT".equalsIgnoreCase(columnModifier)) {
			sql.append(" FROM obs o");
		} else if ("FIRST_N".equalsIgnoreCase(columnModifier)) {
			sql.append(" FROM obs o");
		} else if ("MOST_RECENT_N".equalsIgnoreCase(columnModifier)) {
			sql.append(" FROM obs o");
		} else {
			// Default to MOST_RECENT
			sql.append(" FROM obs o");
		}
		
		// Add joins for extra values
		if (extraValues != null
		        && (extraValues.contains("location") || extraValues.contains("encounterType") || extraValues
		                .contains("provider"))) {
			if (extraValues.contains("location")) {
				sql.append(" LEFT JOIN location l ON o.location_id = l.location_id");
			}
			if (extraValues.contains("encounterType") || extraValues.contains("provider")) {
				sql.append(" LEFT JOIN encounter e ON o.encounter_id = e.encounter_id");
			}
			if (extraValues.contains("encounterType")) {
				sql.append(" LEFT JOIN encounter_type et ON e.encounter_type_id = et.encounter_type_id");
			}
			if (extraValues.contains("provider")) {
				sql.append(" LEFT JOIN provider p ON o.provider_id = p.provider_id");
				sql.append(" LEFT JOIN person pr ON p.person_id = pr.person_id");
			}
		}
		
		// Add WHERE clause
		sql.append(" WHERE o.person_id = :patientId");
		sql.append(" AND o.concept_id = (SELECT concept_id FROM concept WHERE uuid = '").append(conceptUuid).append("')");
		sql.append(" AND o.voided = 0");
		
		// Add date filters only for ANY modifier
		// For MOST_RECENT/FIRST modifiers, we want the actual most recent/first observation regardless of date
		if ("ANY".equalsIgnoreCase(columnModifier)) {
			sql.append(" AND (:startDate IS NULL OR o.obs_datetime >= :startDate)");
			sql.append(" AND (:endDate IS NULL OR o.obs_datetime <= :endDate)");
		}
		
		// Add ORDER BY and LIMIT based on column modifier
		if ("FIRST".equalsIgnoreCase(columnModifier) || "MOST_RECENT".equalsIgnoreCase(columnModifier)
		        || "FIRST_N".equalsIgnoreCase(columnModifier) || "MOST_RECENT_N".equalsIgnoreCase(columnModifier)) {
			if ("FIRST".equalsIgnoreCase(columnModifier) || "FIRST_N".equalsIgnoreCase(columnModifier)) {
				sql.append(" ORDER BY o.obs_datetime ASC");
			} else {
				sql.append(" ORDER BY o.obs_datetime DESC");
			}
			// Use LIMIT 1 with OFFSET for multi-column expansion
			sql.append(" LIMIT 1");
			if (offset > 0) {
				sql.append(" OFFSET ").append(offset);
			}
		} else if ("ANY".equalsIgnoreCase(columnModifier)) {
			// Use GROUP_CONCAT to aggregate all values
			sql = new StringBuilder();
			sql.append("SELECT GROUP_CONCAT(");
			sql.append(buildObservationValueExpression(returnDisplay));
			sql.append(" ORDER BY o.obs_datetime DESC SEPARATOR ', ') as observation_value");
			sql.append(" FROM obs o");
			sql.append(" WHERE o.person_id = :patientId");
			sql.append(" AND o.concept_id = (SELECT concept_id FROM concept WHERE uuid = '").append(conceptUuid)
			        .append("')");
			sql.append(" AND o.voided = 0");
			sql.append(" AND (:startDate IS NULL OR o.obs_datetime >= :startDate)");
			sql.append(" AND (:endDate IS NULL OR o.obs_datetime <= :endDate)");
		}
		
		return sql.toString();
	}
	
	/**
	 * Build the value expression for observation SELECT clause
	 */
	private String buildObservationValueExpression(Boolean returnDisplay) {
		if (returnDisplay) {
			return "CASE "
			        + "WHEN o.value_coded IS NOT NULL THEN "
			        + "(SELECT name FROM concept_name WHERE concept_id = o.value_coded AND locale = 'en' AND concept_name_type = 'FULLY_SPECIFIED' LIMIT 1) "
			        + "WHEN o.value_numeric IS NOT NULL THEN CAST(o.value_numeric AS CHAR) "
			        + "ELSE COALESCE(o.value_text, '') " + "END";
		} else {
			return "CASE " + "WHEN o.value_coded IS NOT NULL THEN CAST(o.value_coded AS CHAR) "
			        + "WHEN o.value_numeric IS NOT NULL THEN CAST(o.value_numeric AS CHAR) "
			        + "ELSE COALESCE(o.value_text, '') " + "END";
		}
	}
	
	/**
	 * Build SQL query for diagnosis data retrieval
	 */
	private String buildDiagnosisSql(String conceptUuid, String rank, Boolean confirmedOnly, String strategy) {
		StringBuilder sql = new StringBuilder();
		
		// Build SELECT clause
		sql.append("SELECT ");
		
		if ("ANY".equalsIgnoreCase(rank) && conceptUuid == null) {
			// All diagnoses aggregated
			sql.append("GROUP_CONCAT(");
			sql.append("(SELECT name FROM concept_name WHERE concept_id = ed.diagnosis AND locale = 'en' AND concept_name_type = 'FULLY_SPECIFIED' LIMIT 1)");
			sql.append(" ORDER BY e.encounter_datetime DESC SEPARATOR ', ') as diagnosis_value");
		} else {
			// Single diagnosis or filtered by concept/rank
			sql.append("(SELECT name FROM concept_name WHERE concept_id = ed.diagnosis AND locale = 'en' AND concept_name_type = 'FULLY_SPECIFIED' LIMIT 1)");
			sql.append(" as diagnosis_value");
		}
		
		// Build FROM clause
		sql.append(" FROM encounter_diagnosis ed");
		sql.append(" INNER JOIN encounter e ON ed.encounter_id = e.encounter_id");
		
		// Build WHERE clause
		sql.append(" WHERE e.patient_id = :patientId");
		sql.append(" AND ed.voided = 0");
		
		// Add date filters only for non-LATEST/non-EARLIEST strategies
		// For LATEST/EARLIEST strategies, we want the actual latest/earliest diagnosis regardless of date
		if (!"LATEST".equalsIgnoreCase(strategy) && !"EARLIEST".equalsIgnoreCase(strategy)) {
			sql.append(" AND (:startDate IS NULL OR e.encounter_datetime >= :startDate)");
			sql.append(" AND (:endDate IS NULL OR e.encounter_datetime <= :endDate)");
		}
		
		// Add concept filter if specified
		if (conceptUuid != null) {
			sql.append(" AND ed.diagnosis = (SELECT concept_id FROM concept WHERE uuid = '").append(conceptUuid).append("'");
		}
		
		// Add rank filter
		if (!"ANY".equalsIgnoreCase(rank)) {
			sql.append(" AND ed.rank = '").append(rank).append("'");
		}
		
		// Add certainty filter
		if (confirmedOnly) {
			sql.append(" AND ed.certainty = 'CONFIRMED'");
		}
		
		// Add ORDER BY and LIMIT based on strategy
		if ("LATEST".equalsIgnoreCase(strategy)) {
			sql.append(" ORDER BY e.encounter_datetime DESC");
			sql.append(" LIMIT 1");
		} else if ("EARLIEST".equalsIgnoreCase(strategy)) {
			sql.append(" ORDER BY e.encounter_datetime ASC");
			sql.append(" LIMIT 1");
		}
		
		return sql.toString();
	}
	
	/**
	 * Create an observation data definition with a specific offset for multi-column expansion Used
	 * by LineListDataSetEvaluator to expand columns like weight_1, weight_2, etc.
	 */
	public DataDefinition createObservationDefinitionWithOffset(LegacyGenericReportSchema.DataDefinition jsonDef, int offset) {
		if (jsonDef == null || !"OBSERVATION".equalsIgnoreCase(jsonDef.getType())) {
			return null;
		}
		
		Map<String, Object> config = jsonDef.getConfig();
		if (config == null) {
			return null;
		}
		
		String conceptUuid = (String) config.get("conceptUuid");
		String conceptName = (String) config.get("conceptName");
		String columnModifier = (String) config.getOrDefault("columnModifier", "MOST_RECENT");
		Object modifierCountObj = config.get("modifierCount");
		int modifierCount = modifierCountObj != null ? Integer.parseInt(modifierCountObj.toString()) : 1;
		Boolean returnDisplay = (Boolean) config.getOrDefault("returnDisplay", Boolean.TRUE);
		
		@SuppressWarnings("unchecked")
		List<String> extraValues = (List<String>) config.get("extraValues");
		
		try {
			String sql = buildObservationSql(conceptUuid, columnModifier, modifierCount, returnDisplay, extraValues, offset);
			
			SqlPatientDataDefinition sqlDef = new SqlPatientDataDefinition();
			sqlDef.setSql(sql);
			
			String name = "Observation: " + (conceptName != null ? conceptName : conceptUuid) + " (offset " + offset + ")";
			sqlDef.setName(name);
			
			return sqlDef;
		}
		catch (Exception e) {
			log.error("Failed to create observation definition with offset {} for concept: {}", offset, conceptUuid, e);
			return null;
		}
	}
	
	/**
	 * Check if a data definition should be expanded into multiple columns
	 */
	public boolean shouldExpandColumn(LegacyGenericReportSchema.DataDefinition jsonDef) {
		if (jsonDef == null || !"OBSERVATION".equalsIgnoreCase(jsonDef.getType())) {
			return false;
		}
		
		Map<String, Object> config = jsonDef.getConfig();
		if (config == null) {
			return false;
		}
		
		String columnModifier = (String) config.get("columnModifier");
		if (!"FIRST_N".equalsIgnoreCase(columnModifier) && !"MOST_RECENT_N".equalsIgnoreCase(columnModifier)) {
			return false;
		}
		
		Object modifierCountObj = config.get("modifierCount");
		int modifierCount = modifierCountObj != null ? Integer.parseInt(modifierCountObj.toString()) : 1;
		
		return modifierCount > 1;
	}
	
	/**
	 * Get the modifier count from a data definition config
	 */
	public int getModifierCount(LegacyGenericReportSchema.DataDefinition jsonDef) {
		if (jsonDef == null) {
			return 1;
		}
		
		Map<String, Object> config = jsonDef.getConfig();
		if (config == null) {
			return 1;
		}
		
		Object modifierCountObj = config.get("modifierCount");
		return modifierCountObj != null ? Integer.parseInt(modifierCountObj.toString()) : 1;
	}
}
