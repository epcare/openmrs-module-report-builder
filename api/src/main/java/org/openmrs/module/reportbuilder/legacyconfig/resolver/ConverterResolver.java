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

import org.openmrs.module.reporting.data.converter.*;
import org.openmrs.module.reportbuilder.contract.ReportSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Resolves data converters from JSON configuration to actual OpenMRS DataConverter objects. This
 * replaces the fragile alias-based converter system with explicit, type-safe resolution. Supports
 * built-in OpenMRS converters and custom UgandaEMRReports converters.
 */
public class ConverterResolver {
	
	private static final Logger log = LoggerFactory.getLogger(ConverterResolver.class);
	
	/**
	 * Resolve a data converter from JSON configuration
	 */
	public DataConverter resolveConverter(ReportSchema.Converter jsonConverter) {
		if (jsonConverter == null) {
			return null;
		}
		
		String type = jsonConverter.getType();
		if (type == null) {
			throw new IllegalArgumentException("Converter type cannot be null");
		}
		
		try {
			switch (type.toUpperCase()) {
				case "BUILTIN":
					return resolveBuiltinConverter(jsonConverter);
				case "CUSTOM":
					return resolveCustomConverter(jsonConverter);
				case "CHAINED":
					return resolveChainedConverter(jsonConverter);
				case "COLLECTION":
					return resolveCollectionConverter(jsonConverter);
				default:
					throw new IllegalArgumentException("Unknown converter type: " + type);
			}
		}
		catch (Exception e) {
			log.error("Failed to resolve converter of type: " + type, e);
			throw new RuntimeException("Failed to resolve converter: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Resolve built-in OpenMRS converters
	 */
	private DataConverter resolveBuiltinConverter(ReportSchema.Converter jsonConverter) {
		String propertyName = jsonConverter.getPropertyName();
		if (propertyName == null) {
			throw new IllegalArgumentException("Property name is required for builtin converters");
		}
		
		// Handle common built-in converters
		switch (propertyName.toLowerCase()) {
			case "objectformatter":
				return new ObjectFormatter();
			case "nullvalue":
				Object nullReplacement = jsonConverter.getConfig() != null ? jsonConverter.getConfig()
				        .get("nullReplacement") : "";
				return new NullValueConverter(nullReplacement);
			default:
				throw new IllegalArgumentException("Unknown builtin property: " + propertyName);
		}
	}
	
	/**
	 * Resolve custom converters (e.g., UgandaEMRReports specific)
	 */
	private DataConverter resolveCustomConverter(ReportSchema.Converter jsonConverter) throws Exception {
		String className = jsonConverter.getClassName();
		if (className == null) {
			throw new IllegalArgumentException("Class name is required for custom converters");
		}
		
		try {
			Class<?> clazz = Class.forName(className);
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			DataConverter converter = (DataConverter) constructor.newInstance();
			
			// Apply configuration if present
			if (jsonConverter.getConfig() != null && !jsonConverter.getConfig().isEmpty()) {
				applyConfiguration(converter, jsonConverter.getConfig());
			}
			
			log.info("Successfully resolved custom converter: " + className);
			return converter;
		}
		catch (ClassNotFoundException e) {
			log.error("Custom converter class not found: " + className);
			throw new RuntimeException("Custom converter class not found: " + className, e);
		}
		catch (Exception e) {
			log.error("Failed to instantiate custom converter: " + className, e);
			throw new RuntimeException("Failed to instantiate custom converter: " + className, e);
		}
	}
	
	/**
	 * Resolve chained converters
	 */
	private DataConverter resolveChainedConverter(ReportSchema.Converter jsonConverter) {
		ReportSchema.Converter[] converterArray = jsonConverter.getConverters();
		if (converterArray == null || converterArray.length == 0) {
			throw new IllegalArgumentException("Chained converter must have at least one sub-converter");
		}
		
		ChainedConverter chainedConverter = new ChainedConverter();
		for (ReportSchema.Converter subConverter : converterArray) {
			DataConverter resolved = resolveConverter(subConverter);
			if (resolved != null) {
				chainedConverter.addConverter(resolved);
			}
		}
		
		return chainedConverter;
	}
	
	/**
	 * Resolve collection converters
	 */
	private DataConverter resolveCollectionConverter(ReportSchema.Converter jsonConverter) {
		ReportSchema.Converter[] converterArray = jsonConverter.getConverters();
		if (converterArray == null || converterArray.length == 0) {
			throw new IllegalArgumentException("Collection converter must have at least one sub-converter");
		}
		
		DataConverter itemConverter = resolveConverter(converterArray[0]);
		
		// Get configuration options
		boolean includeNullElements = true;
		String nullReplacement = null;
		
		if (jsonConverter.getConfig() != null) {
			if (jsonConverter.getConfig().containsKey("includeNullElements")) {
				includeNullElements = (Boolean) jsonConverter.getConfig().get("includeNullElements");
			}
			if (jsonConverter.getConfig().containsKey("nullReplacement")) {
				nullReplacement = (String) jsonConverter.getConfig().get("nullReplacement");
			}
		}
		
		CollectionConverter collectionConverter = new CollectionConverter();
		return collectionConverter;
	}
	
	/**
	 * Apply configuration to a converter
	 */
	private void applyConfiguration(DataConverter converter, Map<String, Object> config) {
		// This would use reflection or specific setters to apply configuration
		// For now, it's a placeholder for the configuration application logic
		log.info("Applying configuration to converter: " + converter.getClass().getSimpleName());
	}
}
