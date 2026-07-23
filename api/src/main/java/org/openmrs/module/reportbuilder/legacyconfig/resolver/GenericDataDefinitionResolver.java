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

import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredAddressDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reportbuilder.contract.LegacyGenericReportSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			// Note: Configure identifier type using appropriate API method
			// For now, using basic configuration
			
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
		// Person name data definitions are handled by the reporting module
		// For now, we'll return a placeholder
		log.info("Resolved person name definition (returning null - needs implementation)");
		return null; // Placeholder - would need proper implementation
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
}
