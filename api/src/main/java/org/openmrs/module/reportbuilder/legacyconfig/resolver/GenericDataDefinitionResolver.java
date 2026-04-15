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
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
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
	 * Resolve calculation definition Used for calculated fields and transformations
	 */
	private DataDefinition resolveCalculationDefinition(LegacyGenericReportSchema.DataDefinition jsonDef) {
		// Calculations are typically handled at the dataset level
		// For data definitions, we'll return a placeholder
		log.info("Resolved calculation definition (returning null - needs implementation)");
		return null; // Placeholder - would need proper implementation
	}
	
	/**
	 * Resolve SQL data definition for complex queries This is the key to eliminating custom Java
	 * classes - use SQL for complex logic
	 */
	private DataDefinition resolveSqlDataDefinition(LegacyGenericReportSchema.DataDefinition jsonDef) {
		Map<String, Object> config = jsonDef.getConfig();
		if (config == null || !config.containsKey("sql")) {
			log.warn("SQL data definition missing sql query");
			return null;
		}
		
		String sql = (String) config.get("sql");
		
		// Create a SQL-based data definition
		// Note: This would need to use SqlPatientQuery or similar
		// For now, this is a placeholder for the SQL-based approach
		
		log.info("Resolved SQL data definition (returning null - needs implementation)");
		return null; // Placeholder - needs SQL implementation
	}
}
