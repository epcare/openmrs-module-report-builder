package org.openmrs.module.reportbuilder.util;

import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Utility class for resolving runtime directories for report seeding. This enables the Report
 * Builder module to load reports from external directories instead of bundling them within the
 * module JAR, providing better flexibility and easier maintenance.
 */
public class RuntimeDirectoryResolver {
	
	private static final Logger log = LoggerFactory.getLogger(RuntimeDirectoryResolver.class);
	
	/**
	 * Get the application data directory for OpenMRS
	 */
	public static File getApplicationDataDirectory() {
		String appDataDir = OpenmrsUtil.getApplicationDataDirectory();
		if (appDataDir == null || appDataDir.trim().isEmpty()) {
			throw new IllegalStateException("Application data directory not configured. "
			        + "Please set the 'application_data_directory' global property.");
		}
		
		File dir = new File(appDataDir);
		if (!dir.exists() || !dir.isDirectory()) {
			throw new IllegalStateException("Application data directory does not exist: " + appDataDir);
		}
		
		return dir;
	}
	
	/**
	 * Get the reports configuration directory Creates it if it doesn't exist
	 */
	public static File getReportsConfigurationDirectory() {
		File appDataDir = getApplicationDataDirectory();
		File reportsDir = new File(appDataDir, "configuration/reports");
		
		if (!reportsDir.exists()) {
			log.info("Creating reports configuration directory: " + reportsDir.getAbsolutePath());
			reportsDir.mkdirs();
		}
		
		return reportsDir;
	}
	
	/**
	 * Get the generic reports directory for our 114 migrated reports This is where all the
	 * *-generic.json files should be placed
	 */
	public static File getGenericReportsDirectory() {
		File reportsConfigDir = getReportsConfigurationDirectory();
		File genericReportsDir = new File(reportsConfigDir, "generic");
		
		if (!genericReportsDir.exists()) {
			log.info("Creating generic reports directory: " + genericReportsDir.getAbsolutePath());
			genericReportsDir.mkdirs();
		}
		
		return genericReportsDir;
	}
	
	/**
	 * Get the legacy reports directory for backward compatibility
	 */
	public static File getLegacyReportsDirectory() {
		File reportsConfigDir = getReportsConfigurationDirectory();
		File legacyReportsDir = new File(reportsConfigDir, "legacy/reports");
		
		if (!legacyReportsDir.exists()) {
			log.info("Creating legacy reports directory: " + legacyReportsDir.getAbsolutePath());
			legacyReportsDir.mkdirs();
		}
		
		return legacyReportsDir;
	}
	
	/**
	 * Validate that a directory exists and is readable
	 */
	public static void validateDirectory(File dir, String dirDescription) {
		if (dir == null) {
			throw new IllegalArgumentException(dirDescription + " cannot be null");
		}
		
		if (!dir.exists()) {
			throw new IllegalArgumentException(dirDescription + " does not exist: " + dir.getAbsolutePath());
		}
		
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dirDescription + " is not a directory: " + dir.getAbsolutePath());
		}
		
		if (!dir.canRead()) {
			throw new IllegalArgumentException(dirDescription + " is not readable: " + dir.getAbsolutePath());
		}
	}
	
	/**
	 * Check if generic reports directory exists and has reports
	 */
	public static boolean hasGenericReports() {
		try {
			File genericDir = getGenericReportsDirectory();
			File[] jsonFiles = genericDir.listFiles((dir, name) -> name.endsWith("-generic.json"));
			return jsonFiles != null && jsonFiles.length > 0;
		} catch (Exception e) {
			log.debug("No generic reports directory found or accessible", e);
			return false;
		}
	}
	
	/**
	 * Get count of generic reports available
	 */
	public static int getGenericReportsCount() {
		try {
			File genericDir = getGenericReportsDirectory();
			File[] jsonFiles = genericDir.listFiles((dir, name) -> name.endsWith("-generic.json"));
			return jsonFiles != null ? jsonFiles.length : 0;
		} catch (Exception e) {
			log.debug("Could not count generic reports", e);
			return 0;
		}
	}
}
