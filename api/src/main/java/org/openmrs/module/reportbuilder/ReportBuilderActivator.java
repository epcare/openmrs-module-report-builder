/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reportbuilder;

import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.reportbuilder.generic.GenericReportImportService;
import org.openmrs.module.reportbuilder.legacyconfig.LegacyReportImportService;
import org.openmrs.module.reportbuilder.util.RuntimeDirectoryResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced activator that supports both legacy and generic report seeding from external
 * directories. This activator enables automatic import of reports from the OpenMRS application data
 * directory, allowing reports to be managed as external files rather than bundled within the module
 * JAR.
 */
public class ReportBuilderActivator extends BaseModuleActivator {
	
	private static final Logger log = LoggerFactory.getLogger(ReportBuilderActivator.class);
	
	/**
	 * @see #started()
	 */
	public void started() {
		log.info("Starting Report Builder with external directory seeding support...");
		
		try {
			// Import generic reports from runtime directory
			importGenericReports();
		}
		catch (Exception e) {
			log.error("Error during generic report import", e);
			// Don't fail module startup if generic import fails
		}
		
		try {
			// Import legacy reports for backward compatibility
			importLegacyReports();
		}
		catch (Exception e) {
			log.error("Error during legacy report import", e);
			// Don't fail module startup if legacy import fails
		}
		
		log.info("Report Builder started successfully");
	}
	
	/**
	 * Import generic reports from external runtime directory
	 */
	private void importGenericReports() {
		try {
			log.info("Checking for generic reports in runtime directory...");

			// Check if generic reports directory exists and has files
			if (!RuntimeDirectoryResolver.hasGenericReports()) {
				log.info("No generic reports directory found or no generic reports present. Skipping import.");
				log.info("Expected location: " + RuntimeDirectoryResolver.getGenericReportsDirectory().getAbsolutePath());
				return;
			}

			int reportCount = RuntimeDirectoryResolver.getGenericReportsCount();
			log.info("Found " + reportCount + " generic report files in runtime directory");

			GenericReportImportService genericImportService = Context.getService(GenericReportImportService.class);
			if (genericImportService == null) {
				log.warn("Generic report import service not available");
				return;
			}

			// Import all generic reports
			var results = genericImportService.importAllGenericReports();

			long successCount = results.stream().filter(r -> r.isSuccess()).count();
			long failureCount = results.stream().filter(r -> !r.isSuccess()).count();

			log.info("Generic report import completed: " + successCount + " succeeded, " +
					failureCount + " failed out of " + results.size() + " total");

			if (successCount > 0) {
				log.info("Successfully imported generic reports from: " +
						RuntimeDirectoryResolver.getGenericReportsDirectory().getAbsolutePath());
			}

		} catch (Exception e) {
			log.error("Failed to import generic reports", e);
			throw e;
		}
	}
	
	/**
	 * Import legacy reports for backward compatibility
	 */
	private void importLegacyReports() {
		LegacyReportImportService legacyImportService = Context.getService(LegacyReportImportService.class);
		
		if (legacyImportService == null) {
			log.debug("Legacy report import service not available");
			return;
		}
		
		try {
			log.info("Importing legacy UgandaEMRReports...");
			legacyImportService.ensureLegacyReportsImported();
			log.info("Legacy report import completed");
		}
		catch (Exception e) {
			log.error("Failed to import legacy reports", e);
			throw e;
		}
	}
	
	/**
	 * @see #shutdown()
	 */
	public void shutdown() {
		log.info("Shutting down Report Builder");
	}
	
}
