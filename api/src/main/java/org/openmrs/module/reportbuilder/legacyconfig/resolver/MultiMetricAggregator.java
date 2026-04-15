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

import org.openmrs.module.reportbuilder.contract.GenericReportSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Utility class for aggregating multiple metrics in dynamic column generation. Handles batch
 * processing of multiple metrics across time periods to optimize query performance for reports with
 * large numbers of dynamic columns.
 */
public class MultiMetricAggregator {
	
	private static final Logger log = LoggerFactory.getLogger(MultiMetricAggregator.class);
	
	/**
	 * Batch process multiple metrics for efficiency
	 * 
	 * @param metrics Array of metrics to process
	 * @param patientIds List of patient IDs to process
	 * @param parameters Report parameters
	 * @return Map of patient ID to metric results
	 */
	public static Map<Integer, Map<String, Object>> batchProcessMetrics(
            GenericReportSchema.DynamicMetric[] metrics,
            List<Integer> patientIds,
            Map<String, Object> parameters) {

        Map<Integer, Map<String, Object>> results = new HashMap<>();

        if (metrics == null || metrics.length == 0) {
            log.warn("No metrics to process");
            return results;
        }

        log.info("Batch processing " + metrics.length + " metrics for " + patientIds.size() + " patients");

        // Group metrics by query pattern for optimization
        Map<String, List<GenericReportSchema.DynamicMetric>> metricGroups = groupMetricsByPattern(metrics);

        log.info("Grouped metrics into " + metricGroups.size() + " query patterns");

        // Process each group
        for (Map.Entry<String, List<GenericReportSchema.DynamicMetric>> entry : metricGroups.entrySet()) {
            String pattern = entry.getKey();
            List<GenericReportSchema.DynamicMetric> groupMetrics = entry.getValue();

            Map<Integer, Map<String, Object>> groupResults = processMetricGroup(
                    groupMetrics, patientIds, parameters);

            // Merge results
            for (Map.Entry<Integer, Map<String, Object>> patientEntry : groupResults.entrySet()) {
                int patientId = patientEntry.getKey();
                Map<String, Object> patientMetrics = patientEntry.getValue();

                if (!results.containsKey(patientId)) {
                    results.put(patientId, new HashMap<>());
                }
                results.get(patientId).putAll(patientMetrics);
            }
        }

        log.info("Completed batch processing");

        return results;
    }
	
	/**
	 * Group metrics by query pattern for batch optimization
	 */
	private static Map<String, List<GenericReportSchema.DynamicMetric>> groupMetricsByPattern(
            GenericReportSchema.DynamicMetric[] metrics) {

        Map<String, List<GenericReportSchema.DynamicMetric>> groups = new HashMap<>();

        for (GenericReportSchema.DynamicMetric metric : metrics) {
            String query = metric.getQuery();
            String pattern = extractQueryPattern(query);

            if (!groups.containsKey(pattern)) {
                groups.put(pattern, new ArrayList<>());
            }
            groups.get(pattern).add(metric);
        }

        return groups;
    }
	
	/**
	 * Extract query pattern for grouping
	 */
	private static String extractQueryPattern(String query) {
		if (query == null) {
			return "unknown";
		}
		
		// Simple pattern extraction - remove specific values and placeholders
		String pattern = query.replaceAll("\\{[^}]+\\}", "?").replaceAll(":\\w+", "?").replaceAll("\\d+", "?")
		        .replaceAll("'[^']*'", "?");
		
		return pattern;
	}
	
	/**
	 * Process a group of metrics with similar query patterns
	 */
	private static Map<Integer, Map<String, Object>> processMetricGroup(
            List<GenericReportSchema.DynamicMetric> metrics,
            List<Integer> patientIds,
            Map<String, Object> parameters) {

        Map<Integer, Map<String, Object>> results = new HashMap<>();

        // For each metric in the group
        for (GenericReportSchema.DynamicMetric metric : metrics) {
            // Process this metric for all patients
            Map<Integer, Object> metricResults = processSingleMetric(metric, patientIds, parameters);

            // Add to results
            for (Map.Entry<Integer, Object> entry : metricResults.entrySet()) {
                int patientId = entry.getKey();
                Object value = entry.getValue();

                if (!results.containsKey(patientId)) {
                    results.put(patientId, new HashMap<>());
                }

                String metricKey = metric.getName();
                results.get(patientId).put(metricKey, value);
            }
        }

        return results;
    }
	
	/**
	 * Process a single metric for multiple patients
	 */
	private static Map<Integer, Object> processSingleMetric(
            GenericReportSchema.DynamicMetric metric,
            List<Integer> patientIds,
            Map<String, Object> parameters) {

        Map<Integer, Object> results = new HashMap<>();

        String resultType = metric.getResultType();
        String queryTemplate = metric.getQuery();

        // For each patient, execute the query and get result
        for (Integer patientId : patientIds) {
            try {
                Object result = executeQueryForPatient(queryTemplate, patientId, resultType, parameters);
                results.put(patientId, result);
            } catch (Exception e) {
                log.error("Failed to process metric " + metric.getName() + " for patient " + patientId, e);
                results.put(patientId, null); // Store null for failed queries
            }
        }

        return results;
    }
	
	/**
	 * Execute query for a single patient and convert result
	 */
	private static Object executeQueryForPatient(String queryTemplate, int patientId, String resultType,
	        Map<String, Object> parameters) {
		
		// Replace :patientId placeholder
		String query = queryTemplate.replace(":patientId", String.valueOf(patientId));
		
		// Replace other parameter placeholders
		if (parameters != null) {
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				String placeholder = ":" + entry.getKey();
				String value = String.valueOf(entry.getValue());
				query = query.replace(placeholder, value);
			}
		}
		
		// Execute query (placeholder - would use actual database execution)
		Object rawResult = executeSqlQuery(query);
		
		// Convert result based on result type
		return convertResult(rawResult, resultType);
	}
	
	/**
	 * Execute SQL query (placeholder implementation)
	 */
	private static Object executeSqlQuery(String query) {
		// Placeholder - in production, this would execute actual SQL
		// For now, return null to indicate no result
		log.debug("Executing SQL query: " + query.substring(0, Math.min(50, query.length())) + "...");
		return null;
	}
	
	/**
	 * Convert raw result to specified type
	 */
	private static Object convertResult(Object rawResult, String resultType) {
		if (rawResult == null) {
			return null;
		}
		
		try {
			switch (resultType.toUpperCase()) {
				case "CONCEPT_NAME":
					return convertToConceptName(rawResult);
				case "NUMERIC":
					return convertToNumeric(rawResult);
				case "DATE":
					return convertToDate(rawResult);
				case "COUNT":
					return convertToCount(rawResult);
				case "STRING":
					return convertToString(rawResult);
				default:
					log.warn("Unknown result type: " + resultType + ", returning raw result");
					return rawResult;
			}
		}
		catch (Exception e) {
			log.error("Failed to convert result to type: " + resultType, e);
			return rawResult;
		}
	}
	
	/**
	 * Convert result to concept name
	 */
	private static Object convertToConceptName(Object rawResult) {
		if (rawResult == null) {
			return null;
		}
		
		// If it's already a string, return it
		if (rawResult instanceof String) {
			return rawResult;
		}
		
		// If it's a number, assume it's a concept_id and look up name
		if (rawResult instanceof Number) {
			int conceptId = ((Number) rawResult).intValue();
			// Placeholder - would lookup actual concept name
			return "Concept_" + conceptId;
		}
		
		return String.valueOf(rawResult);
	}
	
	/**
	 * Convert result to numeric
	 */
	private static Object convertToNumeric(Object rawResult) {
		if (rawResult == null) {
			return null;
		}
		
		if (rawResult instanceof Number) {
			return rawResult;
		}
		
		if (rawResult instanceof String) {
			try {
				return Double.parseDouble((String) rawResult);
			}
			catch (NumberFormatException e) {
				log.warn("Failed to parse numeric from string: " + rawResult);
				return null;
			}
		}
		
		return null;
	}
	
	/**
	 * Convert result to date
	 */
	private static Object convertToDate(Object rawResult) {
		if (rawResult == null) {
			return null;
		}
		
		if (rawResult instanceof java.util.Date) {
			return rawResult;
		}
		
		if (rawResult instanceof String) {
			try {
				return TimeSeriesCalculator.parseDate((String) rawResult);
			}
			catch (Exception e) {
				log.warn("Failed to parse date from string: " + rawResult);
				return null;
			}
		}
		
		return null;
	}
	
	/**
	 * Convert result to count
	 */
	private static Object convertToCount(Object rawResult) {
		// Count is typically a numeric value
		return convertToNumeric(rawResult);
	}
	
	/**
	 * Convert result to string
	 */
	private static Object convertToString(Object rawResult) {
		if (rawResult == null) {
			return null;
		}
		
		return String.valueOf(rawResult);
	}
	
	/**
	 * Aggregate results across multiple time periods
	 */
	public static Map<String, Object> aggregateTimePeriodResults(
            Map<String, Object> periodResults,
            String aggregationType) {

        Map<String, Object> aggregated = new HashMap<>();

        if (periodResults == null || periodResults.isEmpty()) {
            return aggregated;
        }

        switch (aggregationType.toUpperCase()) {
            case "SUM":
                aggregated.put("sum", calculateSum(periodResults.values()));
                break;
            case "AVG":
                aggregated.put("average", calculateAverage(periodResults.values()));
                break;
            case "COUNT":
                aggregated.put("count", periodResults.size());
                break;
            case "LATEST":
                aggregated.put("latest", getLatestValue(periodResults));
                break;
            case "EARLIEST":
                aggregated.put("earliest", getEarliestValue(periodResults));
                break;
            default:
                log.warn("Unknown aggregation type: " + aggregationType);
                aggregated.putAll(periodResults);
        }

        return aggregated;
    }
	
	/**
	 * Calculate sum of numeric values
	 */
	private static Object calculateSum(Collection<Object> values) {
		double sum = 0;
		for (Object value : values) {
			if (value instanceof Number) {
				sum += ((Number) value).doubleValue();
			}
		}
		return sum;
	}
	
	/**
	 * Calculate average of numeric values
	 */
	private static Object calculateAverage(Collection<Object> values) {
		if (values.isEmpty()) {
			return null;
		}
		
		double sum = 0;
		int count = 0;
		
		for (Object value : values) {
			if (value instanceof Number) {
				sum += ((Number) value).doubleValue();
				count++;
			}
		}
		
		return count > 0 ? sum / count : null;
	}
	
	/**
	 * Get latest value from period results
	 */
	private static Object getLatestValue(Map<String, Object> periodResults) {
		// Find the latest period key (assuming sorted period keys)
		String latestKey = null;
		for (String key : periodResults.keySet()) {
			if (latestKey == null || key.compareTo(latestKey) > 0) {
				latestKey = key;
			}
		}
		
		return latestKey != null ? periodResults.get(latestKey) : null;
	}
	
	/**
	 * Get earliest value from period results
	 */
	private static Object getEarliestValue(Map<String, Object> periodResults) {
		// Find the earliest period key (assuming sorted period keys)
		String earliestKey = null;
		for (String key : periodResults.keySet()) {
			if (earliestKey == null || key.compareTo(earliestKey) < 0) {
				earliestKey = key;
			}
		}
		
		return earliestKey != null ? periodResults.get(earliestKey) : null;
	}
}
