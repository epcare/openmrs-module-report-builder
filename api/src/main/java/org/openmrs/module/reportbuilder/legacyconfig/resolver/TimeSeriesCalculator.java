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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility class for time-series calculations in dynamic column generation. Provides date period
 * calculations, formatting, and time-based aggregations for reports with dynamic time-series
 * columns like ART Register.
 */
public class TimeSeriesCalculator {
	
	private static final Logger log = LoggerFactory.getLogger(TimeSeriesCalculator.class);
	
	/**
	 * Generate time periods for dynamic column generation
	 */
	public static List<TimePeriod> generateTimePeriods(String periodType, Date startDate, Date endDate, int maxPeriods) {
        List<TimePeriod> periods = new ArrayList<>();

        switch (periodType.toUpperCase()) {
            case "MONTHLY":
                periods = generateMonthlyPeriods(startDate, endDate, maxPeriods);
                break;
            case "WEEKLY":
                periods = generateWeeklyPeriods(startDate, endDate, maxPeriods);
                break;
            case "DAILY":
                periods = generateDailyPeriods(startDate, endDate, maxPeriods);
                break;
            case "QUARTERLY":
                periods = generateQuarterlyPeriods(startDate, endDate, maxPeriods);
                break;
            default:
                throw new IllegalArgumentException("Unknown period type: " + periodType);
        }

        return periods;
    }
	
	/**
	 * Generate monthly time periods
	 */
	private static List<TimePeriod> generateMonthlyPeriods(Date startDate, Date endDate, int maxMonths) {
        List<TimePeriod> periods = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");

        int monthCount = 0;
        while (calendar.getTime().before(endDate) && monthCount < maxMonths) {
            TimePeriod period = new TimePeriod();
            period.setType("MONTHLY");
            period.setYear(calendar.get(Calendar.YEAR));
            period.setMonth(calendar.get(Calendar.MONTH) + 1); // 1-based
            period.setPeriodKey(monthFormat.format(calendar.getTime()));
            period.setStartDate(calendar.getTime());

            calendar.add(Calendar.MONTH, 1);
            period.setEndDate(calendar.getTime());

            periods.add(period);
            monthCount++;
        }

        return periods;
    }
	
	/**
	 * Generate weekly time periods
	 */
	private static List<TimePeriod> generateWeeklyPeriods(Date startDate, Date endDate, int maxWeeks) {
        List<TimePeriod> periods = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        SimpleDateFormat weekFormat = new SimpleDateFormat("yyyy-'W'ww");

        int weekCount = 0;
        while (calendar.getTime().before(endDate) && weekCount < maxWeeks) {
            TimePeriod period = new TimePeriod();
            period.setType("WEEKLY");
            period.setYear(calendar.get(Calendar.YEAR));
            period.setWeek(calendar.get(Calendar.WEEK_OF_YEAR));
            period.setPeriodKey(weekFormat.format(calendar.getTime()));
            period.setStartDate(calendar.getTime());

            calendar.add(Calendar.WEEK_OF_YEAR, 1);
            period.setEndDate(calendar.getTime());

            periods.add(period);
            weekCount++;
        }

        return periods;
    }
	
	/**
	 * Generate daily time periods
	 */
	private static List<TimePeriod> generateDailyPeriods(Date startDate, Date endDate, int maxDays) {
        List<TimePeriod> periods = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

        int dayCount = 0;
        while (calendar.getTime().before(endDate) && dayCount < maxDays) {
            TimePeriod period = new TimePeriod();
            period.setType("DAILY");
            period.setYear(calendar.get(Calendar.YEAR));
            period.setMonth(calendar.get(Calendar.MONTH) + 1);
            period.setDay(calendar.get(Calendar.DAY_OF_MONTH));
            period.setPeriodKey(dayFormat.format(calendar.getTime()));
            period.setStartDate(calendar.getTime());

            calendar.add(Calendar.DAY_OF_MONTH, 1);
            period.setEndDate(calendar.getTime());

            periods.add(period);
            dayCount++;
        }

        return periods;
    }
	
	/**
	 * Generate quarterly time periods
	 */
	private static List<TimePeriod> generateQuarterlyPeriods(Date startDate, Date endDate, int maxQuarters) {
        List<TimePeriod> periods = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        SimpleDateFormat quarterFormat = new SimpleDateFormat("yyyy-'Q'Q");

        int quarterCount = 0;
        while (calendar.getTime().before(endDate) && quarterCount < maxQuarters) {
            TimePeriod period = new TimePeriod();
            period.setType("QUARTERLY");
            period.setYear(calendar.get(Calendar.YEAR));
            period.setQuarter((calendar.get(Calendar.MONTH) / 3) + 1);
            period.setPeriodKey(calendar.get(Calendar.YEAR) + "-Q" + period.getQuarter());
            period.setStartDate(calendar.getTime());

            calendar.add(Calendar.MONTH, 3);
            period.setEndDate(calendar.getTime());

            periods.add(period);
            quarterCount++;
        }

        return periods;
    }
	
	/**
	 * Calculate age in specific units at a given date
	 */
	public static long calculateAge(Date birthDate, Date asOfDate, String unit) {
		if (birthDate == null || asOfDate == null) {
			throw new IllegalArgumentException("Birth date and as-of date cannot be null");
		}
		
		long diffMs = asOfDate.getTime() - birthDate.getTime();
		long diffDays = diffMs / (1000 * 60 * 60 * 24);
		
		switch (unit.toUpperCase()) {
			case "DAYS":
				return diffDays;
			case "MONTHS":
				return diffDays / 30;
			case "YEARS":
				return diffDays / 365;
			default:
				throw new IllegalArgumentException("Unknown age unit: " + unit);
		}
	}
	
	/**
	 * Parse date string in various formats
	 */
	public static Date parseDate(String dateStr) {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			return null;
		}
		
		List<String> formats = Arrays.asList("yyyy-MM-dd", "yyyy/MM/dd", "dd-MM-yyyy", "dd/MM/yyyy", "yyyy-MM-dd HH:mm:ss",
		    "yyyy-MM-dd'T'HH:mm:ss");
		
		for (String format : formats) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				sdf.setLenient(false);
				return sdf.parse(dateStr);
			}
			catch (ParseException e) {
				// Try next format
			}
		}
		
		throw new IllegalArgumentException("Unable to parse date: " + dateStr);
	}
	
	/**
	 * Format date to standard string representation
	 */
	public static String formatDate(Date date, String format) {
		if (date == null) {
			return null;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}
	
	/**
	 * Check if a date falls within a time period
	 */
	public static boolean isDateInPeriod(Date date, TimePeriod period) {
		if (date == null || period == null) {
			return false;
		}
		
		return !date.before(period.getStartDate()) && !date.after(period.getEndDate());
	}
	
	/**
	 * Get period key for a specific date
	 */
	public static String getPeriodKey(Date date, String periodType) {
		if (date == null) {
			return null;
		}
		
		SimpleDateFormat format;
		switch (periodType.toUpperCase()) {
			case "MONTHLY":
				format = new SimpleDateFormat("yyyy-MM");
				break;
			case "WEEKLY":
				format = new SimpleDateFormat("yyyy-'W'ww");
				break;
			case "QUARTERLY":
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				int quarter = (cal.get(Calendar.MONTH) / 3) + 1;
				return cal.get(Calendar.YEAR) + "-Q" + quarter;
			case "DAILY":
				format = new SimpleDateFormat("yyyy-MM-dd");
				break;
			default:
				throw new IllegalArgumentException("Unknown period type: " + periodType);
		}
		
		return format.format(date);
	}
	
	/**
	 * Inner class representing a time period
	 */
	public static class TimePeriod {
		
		private String type; // "MONTHLY", "WEEKLY", "DAILY", "QUARTERLY"
		
		private int year;
		
		private int month; // 1-12 for monthly
		
		private int week; // 1-53 for weekly
		
		private int day; // 1-31 for daily
		
		private int quarter; // 1-4 for quarterly
		
		private String periodKey; // "2023-10", "2023-W42", "2023-Q4"
		
		private Date startDate;
		
		private Date endDate;
		
		// Getters and setters
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
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
		
		public int getWeek() {
			return week;
		}
		
		public void setWeek(int week) {
			this.week = week;
		}
		
		public int getDay() {
			return day;
		}
		
		public void setDay(int day) {
			this.day = day;
		}
		
		public int getQuarter() {
			return quarter;
		}
		
		public void setQuarter(int quarter) {
			this.quarter = quarter;
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
