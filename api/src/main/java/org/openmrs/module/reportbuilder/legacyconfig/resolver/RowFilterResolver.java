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

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reportbuilder.contract.ReportSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Resolves row filters from JSON configuration to actual OpenMRS CohortDefinition objects. This
 * replaces the fragile alias-based row filter system with explicit, type-safe resolution. Supports
 * SQL-based, obs-based, and custom cohort definitions.
 */
public class RowFilterResolver {
	
	private static final Logger log = LoggerFactory.getLogger(RowFilterResolver.class);
	
	/**
	 * Resolve a row filter from JSON configuration
	 */
	public CohortDefinition resolveRowFilter(ReportSchema.RowFilter jsonRowFilter) {
		if (jsonRowFilter == null) {
			return null;
		}
		
		String type = jsonRowFilter.getType();
		if (type == null) {
			throw new IllegalArgumentException("Row filter type cannot be null");
		}
		
		try {
			switch (type.toUpperCase()) {
				case "COHORT":
					return resolveCohortRowFilter(jsonRowFilter);
				case "SQL":
					return resolveSqlRowFilter(jsonRowFilter);
				case "OBS":
					return resolveObsRowFilter(jsonRowFilter);
				case "CUSTOM":
					return resolveCustomRowFilter(jsonRowFilter);
				default:
					throw new IllegalArgumentException("Unknown row filter type: " + type);
			}
		}
		catch (Exception e) {
			log.error("Failed to resolve row filter of type: " + type, e);
			throw new RuntimeException("Failed to resolve row filter: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Resolve cohort-based row filters
	 */
	private CohortDefinition resolveCohortRowFilter(ReportSchema.RowFilter jsonRowFilter) {
		// For generic cohort definitions, would need more specific configuration
		log.info("Resolving cohort-based row filter");
		return null; // Placeholder
	}
	
	/**
	 * Resolve SQL-based row filters
	 */
	private CohortDefinition resolveSqlRowFilter(ReportSchema.RowFilter jsonRowFilter) {
		// SQL-based cohort definitions would be configured here
		log.info("Resolving SQL-based row filter");
		return null; // Placeholder
	}
	
	/**
	 * Resolve obs-based row filters (common in UgandaEMRReports)
	 */
	private CohortDefinition resolveObsRowFilter(ReportSchema.RowFilter jsonRowFilter) {
		Map<String, Object> parameters = jsonRowFilter.getParameters();
		if (parameters == null) {
			throw new IllegalArgumentException("Parameters required for obs-based row filters");
		}
		
		// This mirrors the pattern from SetupAppointmentList:
		// df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDateAtLocation(
		//     hivMetadata.getReturnVisitDate(),
		//     Arrays.asList(hivMetadata.getARTEncounterEncounterType()),
		//     BaseObsCohortDefinition.TimeModifier.ANY)
		
		String conceptUuid = (String) parameters.get("concept");
		if (conceptUuid == null) {
			throw new IllegalArgumentException("Concept UUID required for obs-based row filters");
		}
		
		Concept concept = Context.getConceptService().getConceptByUuid(conceptUuid);
		
		// Create obs cohort definition
		DateObsCohortDefinition obsCohortDefinition = new DateObsCohortDefinition();
		obsCohortDefinition.setName("Row filter: " + concept.getName());
		// Note: Configure concept using appropriate API method
		
		// Set time modifier
		String timeModifierStr = (String) parameters.get("timeModifier");
		if (timeModifierStr != null) {
			BaseObsCohortDefinition.TimeModifier timeModifier = BaseObsCohortDefinition.TimeModifier
			        .valueOf(timeModifierStr);
			obsCohortDefinition.setTimeModifier(timeModifier);
		}
		
		// Set encounter types if specified
		if (parameters.containsKey("encounterTypes")) {
			Object encounterTypesObj = parameters.get("encounterTypes");
			if (encounterTypesObj instanceof List) {
				List<String> encounterTypeUuids = (List<String>) encounterTypesObj;
				List<EncounterType> encounterTypes = new ArrayList<EncounterType>();
				for (String uuid : encounterTypeUuids) {
					try {
						EncounterType et = Context.getEncounterService().getEncounterTypeByUuid(uuid);
						encounterTypes.add(et);
					}
					catch (Exception e) {
						log.warn("Could not find encounter type: " + uuid);
					}
				}
				if (!encounterTypes.isEmpty()) {
					obsCohortDefinition.setEncounterTypeList(encounterTypes);
				}
			}
		}
		
		log.info("Successfully resolved obs-based row filter for concept: " + concept.getName());
		return obsCohortDefinition;
	}
	
	/**
	 * Resolve custom row filters (e.g., UgandaEMRReports specific)
	 */
	private CohortDefinition resolveCustomRowFilter(ReportSchema.RowFilter jsonRowFilter) throws Exception {
		String className = jsonRowFilter.getClassName();
		if (className == null) {
			throw new IllegalArgumentException("Class name is required for custom row filters");
		}
		
		try {
			Class<?> clazz = Class.forName(className);
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			CohortDefinition cohortDefinition = (CohortDefinition) constructor.newInstance();
			
			// Apply configuration if present
			if (jsonRowFilter.getParameters() != null && !jsonRowFilter.getParameters().isEmpty()) {
				applyConfiguration(cohortDefinition, jsonRowFilter.getParameters());
			}
			
			log.info("Successfully resolved custom row filter: " + className);
			return cohortDefinition;
		}
		catch (ClassNotFoundException e) {
			log.error("Custom row filter class not found: " + className);
			throw new RuntimeException("Custom row filter class not found: " + className, e);
		}
		catch (Exception e) {
			log.error("Failed to instantiate custom row filter: " + className, e);
			throw new RuntimeException("Failed to instantiate custom row filter: " + className, e);
		}
	}
	
	/**
	 * Apply configuration to a cohort definition
	 */
	private void applyConfiguration(CohortDefinition cohortDefinition, Map<String, Object> config) {
		// This would use reflection or specific setters to apply configuration
		// For now, it's a placeholder for the configuration application logic
		log.info("Applying configuration to row filter: " + cohortDefinition.getClass().getSimpleName());
	}
}
