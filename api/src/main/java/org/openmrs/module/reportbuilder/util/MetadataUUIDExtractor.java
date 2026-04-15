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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts metadata UUIDs from UgandaEMRReports metadata source files. This reads the Java source
 * files directly and extracts UUIDs using regex patterns.
 */
public class MetadataUUIDExtractor {
	
	private Map<String, String> conceptUuids = new HashMap<String, String>();
	
	private Map<String, String> programUuids = new HashMap<String, String>();
	
	private Map<String, String> encounterTypeUuids = new HashMap<String, String>();
	
	private Map<String, String> identifierTypeUuids = new HashMap<String, String>();
	
	// Pattern to match UUID assignments in metadata classes
	private final Pattern UUID_PATTERN = Pattern
	        .compile("(?:setUuid\\s*\\(\\s*|\"\\s*)([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})");
	
	// Pattern to match method definitions that return metadata
	private final Pattern METHOD_PATTERN = Pattern
	        .compile("public\\s+(?:Concept|Program|EncounterType|PatientIdentifierType)\\s+(\\w+)\\s*\\(");
	
	/**
	 * Extract metadata from a Java source file
	 */
	public void extractFromFile(String filePath, String metadataType) {
		System.out.println("Extracting from: " + filePath);
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line;
			StringBuilder currentMethod = new StringBuilder();
			String currentMethodName = null;
			boolean inMethod = false;
			
			while ((line = reader.readLine()) != null) {
				// Check if we're entering a new method
				Matcher methodMatcher = METHOD_PATTERN.matcher(line);
				if (methodMatcher.find()) {
					currentMethodName = methodMatcher.group(1);
					inMethod = true;
					currentMethod = new StringBuilder();
				}
				
				if (inMethod) {
					currentMethod.append(line).append("\n");
					
					// Look for UUID in the current method
					String methodContent = currentMethod.toString();
					Matcher uuidMatcher = UUID_PATTERN.matcher(methodContent);
					
					if (uuidMatcher.find()) {
						String uuid = uuidMatcher.group(1);
						String key = currentMethodName;
						
						// Store UUID based on metadata type
						if (filePath.contains("HIVMetadata") || filePath.contains("TBMetadata")
						        || filePath.contains("CommonReportMetadata")) {
							// Determine type based on method name
							if (key.toLowerCase().contains("identifier")) {
								if (!identifierTypeUuids.containsKey(key)) {
									identifierTypeUuids.put(key, uuid);
									System.out.println("  IdentifierType: " + key + " -> " + uuid);
								}
							} else if (key.toLowerCase().contains("encounter")) {
								if (!encounterTypeUuids.containsKey(key)) {
									encounterTypeUuids.put(key, uuid);
									System.out.println("  EncounterType: " + key + " -> " + uuid);
								}
							} else if (key.toLowerCase().contains("program")) {
								if (!programUuids.containsKey(key)) {
									programUuids.put(key, uuid);
									System.out.println("  Program: " + key + " -> " + uuid);
								}
							} else {
								// Default to concept
								if (!conceptUuids.containsKey(key)) {
									conceptUuids.put(key, uuid);
									System.out.println("  Concept: " + key + " -> " + uuid);
								}
							}
						}
						
						// Reset for next method
						inMethod = false;
						currentMethod = new StringBuilder();
					}
					
					// End of method detection
					if (line.contains("}") && inMethod) {
						inMethod = false;
						currentMethod = new StringBuilder();
					}
				}
			}
			
			reader.close();
			
		}
		catch (IOException e) {
			System.err.println("Error reading file " + filePath + ": " + e.getMessage());
		}
	}
	
	/**
	 * Generate JSON metadata file for reference
	 */
	public void generateMetadataJsonFile(String outputFile) {
		try {
			FileWriter writer = new FileWriter(outputFile);
			writer.write("{\n");
			
			// Concepts section
			writer.write("  \"concepts\": {\n");
			boolean first = true;
			for (Map.Entry<String, String> entry : conceptUuids.entrySet()) {
				if (!first)
					writer.write(",\n");
				writer.write("    \"" + entry.getKey() + "\": \"" + entry.getValue() + "\"");
				first = false;
			}
			writer.write("\n  },\n\n");
			
			// Programs section
			writer.write("  \"programs\": {\n");
			first = true;
			for (Map.Entry<String, String> entry : programUuids.entrySet()) {
				if (!first)
					writer.write(",\n");
				writer.write("    \"" + entry.getKey() + "\": \"" + entry.getValue() + "\"");
				first = false;
			}
			// Add known programs if not found
			if (programUuids.isEmpty()) {
				writer.write("    \"ART_PROGRAM\": \"da5a7e66-1d5f-11e0-b929-000c29ad1d07\"");
			}
			writer.write("\n  },\n\n");
			
			// Encounter types section
			writer.write("  \"encounterTypes\": {\n");
			first = true;
			for (Map.Entry<String, String> entry : encounterTypeUuids.entrySet()) {
				if (!first)
					writer.write(",\n");
				writer.write("    \"" + entry.getKey() + "\": \"" + entry.getValue() + "\"");
				first = false;
			}
			writer.write("\n  },\n\n");
			
			// Identifier types section
			writer.write("  \"identifierTypes\": {\n");
			first = true;
			for (Map.Entry<String, String> entry : identifierTypeUuids.entrySet()) {
				if (!first)
					writer.write(",\n");
				writer.write("    \"" + entry.getKey() + "\": \"" + entry.getValue() + "\"");
				first = false;
			}
			writer.write("\n  }\n");
			
			writer.write("}\n");
			writer.close();
			
			System.out.println("\n✅ Metadata UUIDs extracted to: " + outputFile);
			
		}
		catch (IOException e) {
			System.err.println("Error writing metadata file: " + e.getMessage());
		}
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
		MetadataUUIDExtractor extractor = new MetadataUUIDExtractor();
		
		System.out.println("=== Extracting Metadata from UgandaEMRReports ===\n");
		
		// Extract from all metadata files
		String basePath = "/Users/lubwamasamuel/Projects/mets/ugandaemr/modules/openmrs-module-ugandaemr-reports";
		
		extractor.extractFromFile(basePath
		        + "/api/src/main/java/org/openmrs/module/ugandaemrreports/metadata/HIVMetadata.java", "HIV");
		extractor.extractFromFile(basePath
		        + "/api/src/main/java/org/openmrs/module/ugandaemrreports/metadata/TBMetadata.java", "TB");
		extractor.extractFromFile(basePath
		        + "/api/src/main/java/org/openmrs/module/ugandaemrreports/metadata/CommonReportMetadata.java", "COMMON");
		
		System.out.println("\n=== Summary ===");
		System.out.println("Total concepts: " + extractor.getAllConceptUuids().size());
		System.out.println("Total encounter types: " + extractor.getAllEncounterTypeUuids().size());
		System.out.println("Total identifier types: " + extractor.getAllIdentifierTypeUuids().size());
		
		// Generate the reference file
		extractor.generateMetadataJsonFile(basePath + "/ugandaemr-metadata-uuids.json");
	}
}
