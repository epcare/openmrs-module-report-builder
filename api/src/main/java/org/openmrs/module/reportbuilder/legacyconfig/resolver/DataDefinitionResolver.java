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

import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredAddressDataDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reportbuilder.contract.ReportSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * Resolves data definitions from JSON configuration to actual OpenMRS DataDefinition objects. This
 * replaces the fragile alias-based system with explicit, type-safe resolution. Supports built-in
 * OpenMRS data definitions and custom UgandaEMRReports definitions.
 */
public class DataDefinitionResolver {
	
	private static final Logger log = LoggerFactory.getLogger(DataDefinitionResolver.class);
	
	private final BuiltInPatientDataLibrary builtInPatientData = new BuiltInPatientDataLibrary();
	
	/**
	 * Resolve a data definition from JSON configuration
	 */
	public DataDefinition resolveDataDefinition(ReportSchema.DataDefinition jsonDef) {
		if (jsonDef == null) {
			throw new IllegalArgumentException("Data definition cannot be null");
		}
		
		String type = jsonDef.getType();
		if (type == null) {
			throw new IllegalArgumentException("Data definition type cannot be null");
		}
		
		try {
			switch (type.toUpperCase()) {
				case "BUILTIN":
					return resolveBuiltinDataDefinition(jsonDef);
				case "CUSTOM":
					return resolveCustomDataDefinition(jsonDef);
				case "PERSON":
					return resolvePersonDataDefinition(jsonDef);
				case "PATIENT":
					return resolvePatientDataDefinition(jsonDef);
				default:
					throw new IllegalArgumentException("Unknown data definition type: " + type);
			}
		}
		catch (Exception e) {
			log.error("Failed to resolve data definition of type: " + type, e);
			throw new RuntimeException("Failed to resolve data definition: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Resolve built-in OpenMRS data definitions
	 */
	private DataDefinition resolveBuiltinDataDefinition(ReportSchema.DataDefinition jsonDef) {
		String propertyName = jsonDef.getPropertyName();
		if (propertyName == null) {
			throw new IllegalArgumentException("Property name is required for builtin data definitions");
		}
		
		// Handle common built-in data definitions
		switch (propertyName.toLowerCase()) {
			case "preferredname":
				return new PreferredNameDataDefinition();
			case "gender":
				return new GenderDataDefinition();
			case "birthdate":
				return new BirthdateDataDefinition();
			case "preferredaddress":
				PreferredAddressDataDefinition addrDef = new PreferredAddressDataDefinition();
				// Apply any configuration
				if (jsonDef.getConfig() != null) {
					Map<String, Object> config = jsonDef.getConfig();
					if (config.containsKey("addressField")) {
						// Address field configuration would be applied here
						// For now, this is a placeholder for how config would be handled
					}
				}
				return addrDef;
			default:
				throw new IllegalArgumentException("Unknown builtin property: " + propertyName);
		}
	}
	
	/**
	 * Resolve custom data definitions (e.g., UgandaEMRReports specific)
	 */
	private DataDefinition resolveCustomDataDefinition(ReportSchema.DataDefinition jsonDef) throws Exception {
		String className = jsonDef.getClassName();
		if (className == null) {
			throw new IllegalArgumentException("Class name is required for custom data definitions");
		}
		
		try {
			Class<?> clazz = Class.forName(className);
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			DataDefinition dataDefinition = (DataDefinition) constructor.newInstance();
			
			// Apply configuration if present
			if (jsonDef.getConfig() != null && !jsonDef.getConfig().isEmpty()) {
				applyConfiguration(dataDefinition, jsonDef.getConfig());
			}
			
			log.info("Successfully resolved custom data definition: " + className);
			return dataDefinition;
		}
		catch (ClassNotFoundException e) {
			log.error("Custom data definition class not found: " + className);
			throw new RuntimeException("Custom data definition class not found: " + className, e);
		}
		catch (Exception e) {
			log.error("Failed to instantiate custom data definition: " + className, e);
			throw new RuntimeException("Failed to instantiate custom data definition: " + className, e);
		}
	}
	
	/**
	 * Resolve person data definitions
	 */
	private DataDefinition resolvePersonDataDefinition(ReportSchema.DataDefinition jsonDef) {
		// Delegate to builtin resolution for now
		return resolveBuiltinDataDefinition(jsonDef);
	}
	
	/**
	 * Resolve patient data definitions
	 */
	private DataDefinition resolvePatientDataDefinition(ReportSchema.DataDefinition jsonDef) {
		String propertyName = jsonDef.getPropertyName();
		if (propertyName == null) {
			throw new IllegalArgumentException("Property name is required for patient data definitions");
		}
		
		// Handle common patient data definitions
		switch (propertyName.toLowerCase()) {
			case "gender":
				return builtInPatientData.getGender();
			case "birthdate":
				return builtInPatientData.getBirthdate();
			case "preferredname":
				// Note: getPreferredName has protected access, skipping for now
				throw new UnsupportedOperationException("preferredname not supported in generic approach");
			default:
				throw new IllegalArgumentException("Unknown patient property: " + propertyName);
		}
	}
	
	/**
	 * Apply configuration to a data definition
	 */
	private void applyConfiguration(DataDefinition dataDefinition, Map<String, Object> config) {
		// This would use reflection or specific setters to apply configuration
		// For now, it's a placeholder for the configuration application logic
		log.info("Applying configuration to data definition: " + dataDefinition.getClass().getSimpleName());
	}
}
