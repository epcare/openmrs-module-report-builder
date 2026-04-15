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

import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.ChainedConverter;
import org.openmrs.module.reporting.data.converter.CollectionConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.converter.NullValueConverter;
import org.openmrs.module.reportbuilder.contract.LegacyGenericReportSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Completely generic converter resolver. Uses only built-in OpenMRS converters - no custom
 * converters. Supports: birthdate, concept names, object formatting, null values, etc.
 */
public class GenericConverterResolver {
	
	private static final Logger log = LoggerFactory.getLogger(GenericConverterResolver.class);
	
	/**
	 * Resolve a converter from generic JSON configuration
	 */
	public DataConverter resolveConverter(LegacyGenericReportSchema.Converter jsonConverter) {
		if (jsonConverter == null) {
			return null;
		}
		
		String type = jsonConverter.getType();
		if (type == null) {
			throw new IllegalArgumentException("Converter type cannot be null");
		}
		
		try {
			switch (type.toUpperCase()) {
				case "BIRTHDATE_AGE":
					return resolveBirthdateConverter(jsonConverter);
				case "CONCEPT_NAME":
					return resolveConceptNameConverter(jsonConverter);
				case "OBJECT_FORMATTER":
					return new ObjectFormatter();
				case "NULL_VALUE":
					return resolveNullValueConverter(jsonConverter);
				case "PROPERTY":
					return resolvePropertyConverter(jsonConverter);
				case "COLLECTION":
					return resolveCollectionConverter(jsonConverter);
				case "CHAIN":
					return resolveChainedConverter(jsonConverter);
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
	 * Resolve birthdate converter Uses built-in OpenMRS BirthdateConverter
	 */
	private DataConverter resolveBirthdateConverter(LegacyGenericReportSchema.Converter jsonConverter) {
		Map<String, Object> config = jsonConverter.getConfig();
		String format = "age"; // default
		
		if (config != null && config.containsKey("format")) {
			format = (String) config.get("format");
		}
		
		// Use built-in OpenMRS BirthdateConverter
		// Note: BirthdateConverter for age calculation
		BirthdateConverter converter = new BirthdateConverter();
		
		// Format configuration not directly supported in this version
		// The converter will use default date formatting
		
		log.info("Resolved birthdate converter with format: " + format);
		return converter;
	}
	
	/**
	 * Resolve concept name converter
	 */
	private DataConverter resolveConceptNameConverter(LegacyGenericReportSchema.Converter jsonConverter) {
		Map<String, Object> config = jsonConverter.getConfig();
		boolean usePreferredName = true;
		
		if (config != null && config.containsKey("usePreferredName")) {
			usePreferredName = (Boolean) config.get("usePreferredName");
		}
		
		// Chained converter: get concept property, then format it
		ChainedConverter converter = new ChainedConverter();
		converter.addConverter(new PropertyConverter(org.openmrs.Concept.class, "name"));
		converter.addConverter(new ObjectFormatter());
		
		log.info("Resolved concept name converter");
		return converter;
	}
	
	/**
	 * Resolve null value converter
	 */
	private DataConverter resolveNullValueConverter(LegacyGenericReportSchema.Converter jsonConverter) {
		Map<String, Object> config = jsonConverter.getConfig();
		Object nullReplacement = "";
		
		if (config != null && config.containsKey("nullReplacement")) {
			nullReplacement = config.get("nullReplacement");
		}
		
		NullValueConverter converter = new NullValueConverter(nullReplacement);
		
		log.info("Resolved null value converter with replacement: " + nullReplacement);
		return converter;
	}
	
	/**
	 * Resolve property converter
	 */
	private DataConverter resolvePropertyConverter(LegacyGenericReportSchema.Converter jsonConverter) {
		Map<String, Object> config = jsonConverter.getConfig();
		if (config == null || !config.containsKey("property")) {
			throw new IllegalArgumentException("Property converter requires 'property' in config");
		}
		
		String property = (String) config.get("property");
		Class<?> targetClass = Object.class;
		
		if (config.containsKey("targetClass")) {
			String className = (String) config.get("targetClass");
			try {
				targetClass = Class.forName(className);
			}
			catch (ClassNotFoundException e) {
				log.warn("Target class not found: " + className + ", using Object.class");
			}
		}
		
		PropertyConverter converter = new PropertyConverter(targetClass, property);
		
		log.info("Resolved property converter for property: " + property);
		return converter;
	}
	
	/**
	 * Resolve collection converter
	 */
	private DataConverter resolveCollectionConverter(LegacyGenericReportSchema.Converter jsonConverter) {
		Map<String, Object> config = jsonConverter.getConfig();
		boolean includeNullElements = true;
		Object nullReplacement = null;
		
		if (config != null) {
			if (config.containsKey("includeNullElements")) {
				includeNullElements = (Boolean) config.get("includeNullElements");
			}
			if (config.containsKey("nullReplacement")) {
				nullReplacement = config.get("nullReplacement");
			}
		}
		
		// For collection converters, we need a sub-converter
		// This is a simplified version - using default constructor
		CollectionConverter converter = new CollectionConverter();
		
		log.info("Resolved collection converter");
		return converter;
	}
	
	/**
	 * Resolve chained converter
	 */
	private DataConverter resolveChainedConverter(LegacyGenericReportSchema.Converter jsonConverter) {
		Map<String, Object> config = jsonConverter.getConfig();
		if (config == null || !config.containsKey("converters")) {
			throw new IllegalArgumentException("Chained converter requires 'converters' array in config");
		}
		
		List<Map<String, Object>> converterConfigs = (List<Map<String, Object>>) config.get("converters");
		ChainedConverter chainedConverter = new ChainedConverter();
		
		for (Map<String, Object> converterConfig : converterConfigs) {
			LegacyGenericReportSchema.Converter subConverterJson = new LegacyGenericReportSchema.Converter();
			subConverterJson.setType((String) converterConfig.get("type"));
			subConverterJson.setConfig((Map<String, Object>) converterConfig.get("config"));
			
			DataConverter subConverter = resolveConverter(subConverterJson);
			if (subConverter != null) {
				chainedConverter.addConverter(subConverter);
			}
		}
		
		log.info("Resolved chained converter with " + chainedConverter.getConverters().size() + " sub-converters");
		return chainedConverter;
	}
}
