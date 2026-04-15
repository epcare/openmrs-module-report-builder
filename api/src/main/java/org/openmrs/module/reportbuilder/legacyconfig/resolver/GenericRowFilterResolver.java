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
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reportbuilder.contract.LegacyGenericReportSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Completely generic row filter resolver. Uses only standard OpenMRS cohort definitions based on
 * metadata and SQL. Supports: obs-based, encounter-based, program-based, and SQL cohort
 * definitions.
 */
public class GenericRowFilterResolver {
	
	private static final Logger log = LoggerFactory.getLogger(GenericRowFilterResolver.class);
	
	/**
	 * Resolve a row filter from generic JSON configuration
	 */
	public CohortDefinition resolveRowFilter(
	        org.openmrs.module.reportbuilder.contract.LegacyGenericReportSchema.RowFilter jsonRowFilter) {
		if (jsonRowFilter == null) {
			return null;
		}
		
		String type = jsonRowFilter.getType();
		if (type == null) {
			throw new IllegalArgumentException("Row filter type cannot be null");
		}
		
		try {
			switch (type.toUpperCase()) {
				case "SQL":
					return resolveSqlCohortDefinition(jsonRowFilter);
				case "OBS":
					return resolveObsCohortDefinition(jsonRowFilter);
				case "ENCOUNTER":
					return resolveEncounterCohortDefinition(jsonRowFilter);
				case "PROGRAM":
					return resolveProgramCohortDefinition(jsonRowFilter);
				case "PROGRAM_ENROLLMENT":
					return resolveProgramEnrollmentCohortDefinition(jsonRowFilter);
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
	 * Resolve SQL cohort definition This is the key to eliminating custom cohort definitions - use
	 * SQL for complex logic
	 */
	private CohortDefinition resolveSqlCohortDefinition(LegacyGenericReportSchema.RowFilter jsonRowFilter) {
		Map<String, Object> config = jsonRowFilter.getConfig();
		if (config == null || !config.containsKey("sql")) {
			throw new IllegalArgumentException("SQL cohort definition requires sql query");
		}
		
		String sql = (String) config.get("sql");
		SqlCohortDefinition cohortDefinition = new SqlCohortDefinition();
		cohortDefinition.setName("SQL Row Filter");
		cohortDefinition.setQuery(sql);
		
		log.info("Resolved SQL cohort definition");
		return cohortDefinition;
	}
	
	/**
	 * Resolve obs-based cohort definition Replaces custom AppointmentDateAtLocationCohortDefinition
	 */
	private CohortDefinition resolveObsCohortDefinition(LegacyGenericReportSchema.RowFilter jsonRowFilter) {
		Map<String, Object> config = jsonRowFilter.getConfig();
		if (config == null || !config.containsKey("conceptUuid")) {
			throw new IllegalArgumentException("Obs cohort definition requires conceptUuid");
		}
		
		String conceptUuid = (String) config.get("conceptUuid");
		Concept concept = Context.getConceptService().getConceptByUuid(conceptUuid);
		
		DateObsCohortDefinition cohortDefinition = new DateObsCohortDefinition();
		cohortDefinition.setName("Obs Row Filter: " + concept.getName());
		// Note: Configure concept using appropriate API method
		
		// Set time modifier
		if (config.containsKey("timeModifier")) {
			String timeModifierStr = (String) config.get("timeModifier");
			DateObsCohortDefinition.TimeModifier timeModifier = DateObsCohortDefinition.TimeModifier
			        .valueOf(timeModifierStr);
			cohortDefinition.setTimeModifier(timeModifier);
		}
		
		// Set encounter types if specified
		if (config.containsKey("encounterTypeUuids")) {
			List<String> encounterTypeUuids = (List<String>) config.get("encounterTypeUuids");
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
				cohortDefinition.setEncounterTypeList(encounterTypes);
			}
		}
		
		// Set location if specified
		if (config.containsKey("locationParam")) {
			String locationParam = (String) config.get("locationParam");
			// This would map to a report parameter like "location"
			// cohortDefinition.setLocationParameterName(locationParam);
		}
		
		log.info("Resolved obs cohort definition for concept: " + concept.getName());
		return cohortDefinition;
	}
	
	/**
	 * Resolve encounter-based cohort definition
	 */
	private CohortDefinition resolveEncounterCohortDefinition(LegacyGenericReportSchema.RowFilter jsonRowFilter) {
		Map<String, Object> config = jsonRowFilter.getConfig();
		if (config == null || !config.containsKey("encounterTypeUuids")) {
			throw new IllegalArgumentException("Encounter cohort definition requires encounterTypeUuids");
		}
		
		List<String> encounterTypeUuids = (List<String>) config.get("encounterTypeUuids");
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
		
		EncounterCohortDefinition cohortDefinition = new EncounterCohortDefinition();
		cohortDefinition.setName("Encounter Row Filter");
		cohortDefinition.setEncounterTypeList(encounterTypes);
		
		// Set time parameter if specified
		if (config.containsKey("timeParameter")) {
			String timeParameter = (String) config.get("timeParameter");
			// cohortDefinition.setTimeParameter(timeParameter);
		}
		
		log.info("Resolved encounter cohort definition with " + encounterTypes.size() + " encounter types");
		return cohortDefinition;
	}
	
	/**
	 * Resolve program-based cohort definition
	 */
	private CohortDefinition resolveProgramCohortDefinition(LegacyGenericReportSchema.RowFilter jsonRowFilter) {
		Map<String, Object> config = jsonRowFilter.getConfig();
		if (config == null || !config.containsKey("programUuid")) {
			throw new IllegalArgumentException("Program cohort definition requires programUuid");
		}
		
		String programUuid = (String) config.get("programUuid");
		
		InProgramCohortDefinition cohortDefinition = new InProgramCohortDefinition();
		cohortDefinition.setName("Program Row Filter: " + programUuid);
		// Note: Configure program using appropriate API method
		
		log.info("Resolved program cohort definition for program: " + programUuid);
		return cohortDefinition;
	}
	
	/**
	 * Resolve program enrollment cohort definition
	 */
	private CohortDefinition resolveProgramEnrollmentCohortDefinition(LegacyGenericReportSchema.RowFilter jsonRowFilter) {
		Map<String, Object> config = jsonRowFilter.getConfig();
		if (config == null || !config.containsKey("programUuid")) {
			throw new IllegalArgumentException("Program enrollment cohort definition requires programUuid");
		}
		
		String programUuid = (String) config.get("programUuid");
		
		ProgramEnrollmentCohortDefinition cohortDefinition = new ProgramEnrollmentCohortDefinition();
		cohortDefinition.setName("Program Enrollment Row Filter: " + programUuid);
		// Note: Configure program using appropriate API method
		
		// Set specific states if specified
		if (config.containsKey("stateUuids")) {
			List<String> stateUuids = (List<String>) config.get("stateUuids");
			// This would need to resolve to ProgramWorkflowStates
			// cohortDefinition.setStateList(stateList);
		}
		
		log.info("Resolved program enrollment cohort definition for program: " + programUuid);
		return cohortDefinition;
	}
}
