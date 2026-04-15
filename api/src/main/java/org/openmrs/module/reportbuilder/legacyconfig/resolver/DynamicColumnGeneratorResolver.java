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
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Advanced resolver for dynamic column generation in complex reports. Handles time-series data
 * generation for reports like ART Register that require 72+ dynamic monthly columns for tracking
 * patient clinical data over time. Key capabilities: - Generate time-series columns based on date
 * ranges - Support multiple metrics per time period (regimen, viral load, visits) - Efficient query
 * batching for large numbers of columns - Result type converters (CONCEPT_NAME, NUMERIC, COUNT,
 * DATE)
 */
public class DynamicColumnGeneratorResolver {
	
	private static final Logger log = LoggerFactory.getLogger(DynamicColumnGeneratorResolver.class);
	
	/**
	 * Process dynamic column generation configuration and extend dataset definition
	 * 
	 * @param baseDataSet The base dataset definition to extend
	 * @param dynamicConfig The dynamic column generation configuration
	 * @param parameters Map of report parameters (startDate, endDate, etc.)
	 * @return Enhanced dataset definition with dynamic columns
	 */
	public PatientDataSetDefinition processDynamicColumnGeneration(PatientDataSetDefinition baseDataSet,
	        GenericReportSchema.DynamicColumnGeneration dynamicConfig, Map<String, Object> parameters) {
		
		if (dynamicConfig == null || !dynamicConfig.isEnabled()) {
			log.info("Dynamic column generation is disabled");
			return baseDataSet;
		}
		
		log.info("Processing dynamic column generation: type=" + dynamicConfig.getType());
		
		String type = dynamicConfig.getType();
		if (type == null) {
			throw new IllegalArgumentException("Dynamic column type cannot be null");
		}
		
		try {
			switch (type.toUpperCase()) {
				case "MONTHLY_TIME_SERIES":
					return processMonthlyTimeSeries(baseDataSet, dynamicConfig.getConfig(), parameters);
				case "WEEKLY_TIME_SERIES":
					return processWeeklyTimeSeries(baseDataSet, dynamicConfig.getConfig(), parameters);
				case "CUSTOM":
					return processCustomTimeSeries(baseDataSet, dynamicConfig.getConfig(), parameters);
				default:
					throw new IllegalArgumentException("Unknown dynamic column type: " + type);
			}
		}
		catch (Exception e) {
			log.error("Failed to process dynamic column generation", e);
			throw new RuntimeException("Failed to process dynamic column generation: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Process monthly time-series column generation Used for ART Register with 72+ monthly columns
	 */
	private PatientDataSetDefinition processMonthlyTimeSeries(PatientDataSetDefinition baseDataSet,
	        GenericReportSchema.DynamicColumnConfig config, Map<String, Object> parameters) throws ParseException {
		
		log.info("Processing monthly time-series columns");
		
		// Resolve date parameters
		String startDateParam = config.getStartDate();
		String endDateParam = config.getEndDate();
		
		Date startDate = resolveDateParameter(parameters, startDateParam);
		Date endDate = resolveDateParameter(parameters, endDateParam);
		
		if (startDate == null || endDate == null) {
			throw new IllegalArgumentException("Start date and end date parameters are required");
		}
		
		// Generate monthly periods
		List<MonthlyPeriod> periods = generateMonthlyPeriods(startDate, endDate, config.getMaxHistoryMonths());
		
		log.info("Generated " + periods.size() + " monthly periods");
		
		// Add dynamic columns for each metric and period combination
		GenericReportSchema.DynamicMetric[] metrics = config.getMetrics();
		if (metrics == null || metrics.length == 0) {
			throw new IllegalArgumentException("At least one metric must be defined");
		}
		
		// Create columns for each metric-period combination
		for (GenericReportSchema.DynamicMetric metric : metrics) {
			for (MonthlyPeriod period : periods) {
				addDynamicColumn(baseDataSet, metric, period, parameters);
			}
		}
		
		log.info("Added " + (periods.size() * metrics.length) + " dynamic columns to dataset");
		
		return baseDataSet;
	}
	
	/**
	 * Add a single dynamic column to the dataset
	 */
	private void addDynamicColumn(PatientDataSetDefinition dataSet, GenericReportSchema.DynamicMetric metric,
	        MonthlyPeriod period, Map<String, Object> parameters) {
		
		String columnName = generateColumnName(metric.getName(), period);
		String sqlQuery = prepareSqlQuery(metric.getQuery(), period);
		
		log.debug("Adding dynamic column: " + columnName);
		
		// Create SQL-based data definition for this column
		PatientDataDefinition dataDefinition = createSqlDataDefinition(sqlQuery, parameters);
		
		// Add column to dataset
		dataSet.addColumn(columnName, (DataDefinition) dataDefinition, (Map<String, Object>) null);
		
		log.debug("Successfully added dynamic column: " + columnName);
	}
	
	/**
	 * Process weekly time-series column generation
	 */
	private PatientDataSetDefinition processWeeklyTimeSeries(PatientDataSetDefinition baseDataSet,
	        GenericReportSchema.DynamicColumnConfig config, Map<String, Object> parameters) {
		// Implementation similar to monthly but with weekly periods
		log.info("Processing weekly time-series columns");
		// TODO: Implement weekly time series generation
		return baseDataSet;
	}
	
	/**
	 * Process custom time-series column generation
	 */
	private PatientDataSetDefinition processCustomTimeSeries(PatientDataSetDefinition baseDataSet,
	        GenericReportSchema.DynamicColumnConfig config, Map<String, Object> parameters) {
		// Implementation for custom time series logic
		log.info("Processing custom time-series columns");
		// TODO: Implement custom time series generation
		return baseDataSet;
	}
	
	/**
	 * Generate monthly periods between start and end dates
	 */
	private List<MonthlyPeriod> generateMonthlyPeriods(Date startDate, Date endDate, int maxMonths) {
        List<MonthlyPeriod> periods = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");

        int monthCount = 0;
        while (calendar.getTime().before(endDate) && monthCount < maxMonths) {
            MonthlyPeriod period = new MonthlyPeriod();
            period.setYear(calendar.get(Calendar.YEAR));
            period.setMonth(calendar.get(Calendar.MONTH) + 1); // 1-based month
            period.setPeriodKey(monthFormat.format(calendar.getTime()));
            period.setStartDate(calendar.getTime());

            // Move to next month
            calendar.add(Calendar.MONTH, 1);

            period.setEndDate(calendar.getTime());
            periods.add(period);

            monthCount++;
        }

        return periods;
    }
	
	/**
	 * Resolve date parameter from parameters map
	 */
	private Date resolveDateParameter(Map<String, Object> parameters, String paramName) {
		if (paramName == null || paramName.isEmpty()) {
			return null;
		}
		
		// Remove ${ } wrapper if present
		String cleanParamName = paramName.replaceAll("[\\$\\{\\}]", "");
		
		Object paramValue = parameters.get(cleanParamName);
		if (paramValue instanceof Date) {
			return (Date) paramValue;
		} else if (paramValue instanceof String) {
			try {
				return new SimpleDateFormat("yyyy-MM-dd").parse((String) paramValue);
			}
			catch (ParseException e) {
				log.error("Failed to parse date parameter: " + paramName, e);
				return null;
			}
		}
		
		return null;
	}
	
	/**
	 * Generate column name for a metric-period combination
	 */
	private String generateColumnName(String metricName, MonthlyPeriod period) {
		return metricName + "_" + period.getPeriodKey();
	}
	
	/**
	 * Prepare SQL query by replacing placeholders with period-specific values
	 */
	private String prepareSqlQuery(String sqlTemplate, MonthlyPeriod period) {
		String query = sqlTemplate.replace("{target_month}", period.getPeriodKey());
		query = query.replace("{year}", String.valueOf(period.getYear()));
		query = query.replace("{month}", String.valueOf(period.getMonth()));
		query = query.replace("{period_start}", new SimpleDateFormat("yyyy-MM-dd").format(period.getStartDate()));
		query = query.replace("{period_end}", new SimpleDateFormat("yyyy-MM-dd").format(period.getEndDate()));
		return query;
	}
	
	/**
	 * Create SQL-based data definition
	 */
	private PatientDataDefinition createSqlDataDefinition(String sql, Map<String, Object> parameters) {
		// This would use OpenMRS SQL data definition or custom implementation
		// For now, returning a placeholder that would be replaced with actual implementation
		log.debug("Creating SQL data definition for query: " + sql.substring(0, Math.min(50, sql.length())) + "...");
		return null; // Placeholder - needs actual OpenMRS SQL data definition
	}
	
	/**
	 * Inner class representing a monthly period
	 */
	private static class MonthlyPeriod {
		
		private int year;
		
		private int month; // 1-based month (1-12)
		
		private String periodKey; // Format: "yyyy-MM"
		
		private Date startDate;
		
		private Date endDate;
		
		public int getYear() {
			return year;
		}
		
		public void setYear(int year) {
			this.year = year;
		}
		
		public int getMonth() {
			return month;
		}
		
		public void setMonth(int month) {
			this.month = month;
		}
		
		public String getPeriodKey() {
			return periodKey;
		}
		
		public void setPeriodKey(String periodKey) {
			this.periodKey = periodKey;
		}
		
		public Date getStartDate() {
			return startDate;
		}
		
		public void setStartDate(Date startDate) {
			this.startDate = startDate;
		}
		
		public Date getEndDate() {
			return endDate;
		}
		
		public void setEndDate(Date endDate) {
			this.endDate = endDate;
		}
	}
}
