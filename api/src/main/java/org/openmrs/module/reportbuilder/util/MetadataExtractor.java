/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reportbuilder.util;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;

/**
 * Extracts all metadata UUIDs from UgandaEMRReports metadata classes. This tool reads the existing
 * metadata classes and extracts all concept, program, encounter type, and identifier type UUIDs to
 * use in generic report definitions.
 */
public class MetadataExtractor {
	
	private Map<String, String> conceptUuids = new HashMap<String, String>();
	
	private Map<String, String> programUuids = new HashMap<String, String>();
	
	private Map<String, String> encounterTypeUuids = new HashMap<String, String>();
	
	private Map<String, String> identifierTypeUuids = new HashMap<String, String>();
	
	/**
	 * Extract metadata from HIVMetadata class
	 */
	public void extractHIVMetadata() {
		System.out.println("Extracting HIV Metadata...");
		try {
			Class<?> hivMetadataClass = Class.forName("org.openmrs.module.ugandaemrreports.metadata.HIVMetadata");
			Object hivMetadata = hivMetadataClass.newInstance();
			
			// Extract all concept UUIDs
			extractConceptsFromMethods(hivMetadata, getConceptMethodNames());
			
			// Extract encounter types
			extractEncounterTypesFromMethods(hivMetadata);
			
			// Extract identifier types
			extractIdentifierTypesFromMethods(hivMetadata);
			
		}
		catch (Exception e) {
			System.err.println("Error extracting HIV metadata: " + e.getMessage());
		}
	}
	
	/**
	 * Extract metadata from TBMetadata class
	 */
	public void extractTBMetadata() {
		System.out.println("Extracting TB Metadata...");
		try {
			Class<?> tbMetadataClass = Class.forName("org.openmrs.module.ugandaemrreports.metadata.TBMetadata");
			Object tbMetadata = tbMetadataClass.newInstance();
			
			extractConceptsFromMethods(tbMetadata, getConceptMethodNames());
			extractEncounterTypesFromMethods(tbMetadata);
			extractIdentifierTypesFromMethods(tbMetadata);
			
		}
		catch (Exception e) {
			System.err.println("Error extracting TB metadata: " + e.getMessage());
		}
	}
	
	/**
	 * Extract metadata from CommonReportMetadata class
	 */
	public void extractCommonReportMetadata() {
		System.out.println("Extracting Common Report Metadata...");
		try {
			Class<?> commonMetadataClass = Class
			        .forName("org.openmrs.module.ugandaemrreports.metadata.CommonReportMetadata");
			Object commonMetadata = commonMetadataClass.newInstance();
			
			extractConceptsFromMethods(commonMetadata, getConceptMethodNames());
			extractEncounterTypesFromMethods(commonMetadata);
			
		}
		catch (Exception e) {
			System.err.println("Error extracting common report metadata: " + e.getMessage());
		}
	}
	
	/**
	 * Extract all concepts using reflection
	 */
	private void extractConceptsFromMethods(Object metadataObj, List<String> methodNames) {
		for (String methodName : methodNames) {
			try {
				Method method = metadataObj.getClass().getMethod(methodName);
				method.setAccessible(true);
				Object result = method.invoke(metadataObj);
				
				if (result instanceof Concept) {
					Concept concept = (Concept) result;
					String uuid = concept.getUuid();
					String name = concept.getName().getName();
					
					if (!conceptUuids.containsKey(methodName) && uuid != null) {
						conceptUuids.put(methodName, uuid);
						System.out.println("  Concept: " + methodName + " -> " + name + " (" + uuid + ")");
					}
				} else if (result instanceof List) {
					List<?> concepts = (List<?>) result;
					int index = 0;
					for (Object item : concepts) {
						if (item instanceof Concept) {
							Concept concept = (Concept) item;
							String uuid = concept.getUuid();
							String name = concept.getName().getName();
							String key = methodName + "_" + index++;
							
							if (uuid != null) {
								conceptUuids.put(key, uuid);
								System.out.println("  Concept: " + key + " -> " + name + " (" + uuid + ")");
							}
						}
					}
				}
			}
			catch (Exception e) {
				// Method doesn't exist or can't invoke - skip it
			}
		}
	}
	
	/**
	 * Extract encounter types from methods
	 */
	private void extractEncounterTypesFromMethods(Object metadataObj) {
		List<String> methodNames = Arrays.asList("getARTSummaryPageEncounterType", "getARTEncounterPageEncounterType",
		    "getMissedAppointmentRegisterEncounterType", "getEIDSummaryPageEncounterType",
		    "getEIDEncounterPageEncounterType", "getHCTEncounterType", "getMissedAppointmentEncounterType",
		    "getArtEncounterTypes", "getMedicationDispensingEncounterType", "getARTRegimenChangeEncounterType",
		    "getBackToCareEncounterType");
		
		for (String methodName : methodNames) {
			try {
				Method method = metadataObj.getClass().getMethod(methodName);
				method.setAccessible(true);
				Object result = method.invoke(metadataObj);
				
				if (result instanceof List) {
					List<?> encounterTypes = (List<?>) result;
					for (Object item : encounterTypes) {
						if (item instanceof EncounterType) {
							EncounterType et = (EncounterType) item;
							String uuid = et.getUuid();
							String name = et.getName();
							String key = methodName + "_" + name;
							
							if (uuid != null) {
								encounterTypeUuids.put(key, uuid);
								System.out.println("  EncounterType: " + key + " -> " + name + " (" + uuid + ")");
							}
						}
					}
				} else if (result instanceof EncounterType) {
					EncounterType et = (EncounterType) result;
					String uuid = et.getUuid();
					String name = et.getName();
					
					if (uuid != null) {
						encounterTypeUuids.put(methodName, uuid);
						System.out.println("  EncounterType: " + methodName + " -> " + name + " (" + uuid + ")");
					}
				}
			}
			catch (Exception e) {
				// Method doesn't exist or can't invoke - skip it
			}
		}
	}
	
	/**
	 * Extract identifier types from methods
	 */
	private void extractIdentifierTypesFromMethods(Object metadataObj) {
		List<String> methodNames = Arrays.asList("getPatientsWithEIDIdentifier", "getHIVIdentifier");
		
		for (String methodName : methodNames) {
			try {
				Method method = metadataObj.getClass().getMethod(methodName);
				method.setAccessible(true);
				Object result = method.invoke(metadataObj);
				
				if (result instanceof PatientIdentifierType) {
					PatientIdentifierType pit = (PatientIdentifierType) result;
					String uuid = pit.getUuid();
					String name = pit.getName();
					
					if (uuid != null) {
						identifierTypeUuids.put(methodName, uuid);
						System.out.println("  IdentifierType: " + methodName + " -> " + name + " (" + uuid + ")");
					}
				}
			}
			catch (Exception e) {
				// Method doesn't exist or can't invoke - skip it
			}
		}
	}
	
	/**
	 * Get concept method names from HIVMetadata
	 */
	private List<String> getConceptMethodNames() {
		return Arrays.asList(
		    // Core concepts
		    "getReturnVisitDate", "getArtStartDate", "getViralLoadQualitative",
		    "getCurrentRegimen",
		    "getTransferIn",
		    "getYes",
		    "getEntryPoint",
		    
		    // EID concepts
		    "getFirstPCRTestDate", "getSecondPCRTestDate", "getRepeatPCRTestDate", "get18MonthsRapidPCRTestDate",
		    "getFirstPCRTestResults", "getSecondPCRTestResults",
		    "getRepeatPCRTestResults",
		    "get18MonthsRapidPCRTestResults",
		    
		    // Result concepts
		    "getPositiveResult", "getNegativeResult", "getFinalStatus",
		    "getFinalOutcome",
		    
		    // Clinical concepts
		    "getWHOClinicalStage", "getBaselineWHOClinicalStage", "getDateEligibleForART", "getCD4AtEnrollment",
		    "getBaselineCD4", "getAdherence", "getGoodAdherence", "getFairAdherence", "getPoorAdherence", "getTBStatus",
		    "getMalnutrition",
		    
		    // Patient status
		    "getPregnantAtEnrollment", "getLactatingAtEnrollment", "getEMTCTAtEnrollment");
	}
	
	/**
	 * Generate JSON metadata file for reference
	 */
	public void generateMetadataJsonFile(String outputFile) {
		try {
			FileWriter writer = new FileWriter(outputFile);
			writer.write("{\n");
			
			writer.write("  \"concepts\": {\n");
			for (Map.Entry<String, String> entry : conceptUuids.entrySet()) {
				writer.write("    \"" + entry.getKey() + "\": \"" + entry.getValue() + "\",\n");
			}
			writer.write("  },\n\n");
			
			writer.write("  \"programs\": {\n");
			// Add ART program UUID when we find it
			writer.write("    \"ART_PROGRAM\": \"da5a7e66-1d5f-11e0-b929-000c29ad1d07\"\n");
			writer.write("  },\n\n");
			
			writer.write("  \"encounterTypes\": {\n");
			for (Map.Entry<String, String> entry : encounterTypeUuids.entrySet()) {
				writer.write("    \"" + entry.getKey() + "\": \"" + entry.getValue() + "\",\n");
			}
			writer.write("  },\n\n");
			
			writer.write("  \"identifierTypes\": {\n");
			for (Map.Entry<String, String> entry : identifierTypeUuids.entrySet()) {
				writer.write("    \"" + entry.getKey() + "\": \"" + entry.getValue() + "\",\n");
			}
			writer.write("  }\n");
			
			writer.write("}\n");
			writer.close();
			
			System.out.println("\n✅ Metadata UUIDs extracted to: " + outputFile);
			
		}
		catch (IOException e) {
			System.err.println("Error writing metadata file: " + e.getMessage());
		}
	}
	
	/**
	 * Get UUID by concept name
	 */
	public String getConceptUuid(String conceptName) {
		return conceptUuids.get(conceptName);
	}
	
	/**
	 * Get UUID by encounter type name
	 */
	public String getEncounterTypeUuid(String encounterTypeName) {
		for (Map.Entry<String, String> entry : encounterTypeUuids.entrySet()) {
			if (entry.getKey().contains(encounterTypeName)
			        || entry.getKey().replaceAll("_", " ").equalsIgnoreCase(encounterTypeName)) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	/**
	 * Get all extracted concepts
	 */
	public Map<String, String> getAllConceptUuids() {
		return conceptUuids;
	}
	
	/**
	 * Get all extracted encounter types
	 */
	public Map<String, String> getAllEncounterTypeUuids() {
		return encounterTypeUuids;
	}
	
	/**
	 * Get all extracted identifier types
	 */
	public Map<String, String> getAllIdentifierTypeUuids() {
		return identifierTypeUuids;
	}
	
	/**
	 * Main method for testing extraction
	 */
	public static void main(String[] args) {
		MetadataExtractor extractor = new MetadataExtractor();
		
		System.out.println("=== Extracting Metadata from UgandaEMRReports ===\n");
		
		extractor.extractHIVMetadata();
		extractor.extractTBMetadata();
		extractor.extractCommonReportMetadata();
		
		System.out.println("\n=== Summary ===");
		System.out.println("Total concepts: " + extractor.getAllConceptUuids().size());
		System.out.println("Total encounter types: " + extractor.getAllEncounterTypeUuids().size());
		System.out.println("Total identifier types: " + extractor.getAllIdentifierTypeUuids().size());
		
		extractor.generateMetadataJsonFile("ugandaemr-metadata-uuids.json");
	}
}
