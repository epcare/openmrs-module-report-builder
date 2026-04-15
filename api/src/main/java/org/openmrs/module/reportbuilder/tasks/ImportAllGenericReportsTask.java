package org.openmrs.module.reportbuilder.tasks;

import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.module.reportbuilder.api.ReportBuilderService;
import org.openmrs.module.reportbuilder.legacyconfig.generic.ReportImportResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Scheduled task for importing all generic reports from runtime directory. This task can be
 * scheduled to run periodically or triggered manually to import/reimport generic reports from the
 * external directory.
 */
public class ImportAllGenericReportsTask extends AbstractTask {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	@Override
	public void execute() {
		try {
			log.info("Starting scheduled generic report import task");
			
			ReportBuilderService service = Context.getService(ReportBuilderService.class);
			if (service == null) {
				log.error("Report builder service not available");
				return;
			}
			
			List<ReportImportResult> results = service.importAllGenericReports();
			
			int successCount = 0;
			int failureCount = 0;
			int facilityReports = 0;
			int merReports = 0;
			int nationalReports = 0;
			
			for (ReportImportResult result : results) {
				if (result.isSuccess()) {
					successCount++;
					log.info("✓ Imported: " + result.getFileName() + " -> " + result.getReportName() + " ["
					        + result.getCategory() + "/" + result.getSubcategory() + "]");
					
					// Count by category
					if ("FACILITY_REPORTS".equals(result.getCategory())) {
						facilityReports++;
					} else if ("MER_INDICATOR_REPORTS".equals(result.getCategory())) {
						merReports++;
					} else if ("NATIONAL_REPORTS".equals(result.getCategory())) {
						nationalReports++;
					}
				} else {
					failureCount++;
					log.error("✗ Failed: " + result.getFileName() + " - " + result.getErrorMessage());
				}
			}
			
			log.info("Scheduled import complete: " + successCount + " succeeded, " + failureCount + " failed out of "
			        + results.size() + " total");
			log.info("Import breakdown: " + facilityReports + " facility, " + merReports + " MER, " + nationalReports
			        + " national reports");
			
		}
		catch (Exception e) {
			log.error("Failed to execute generic report import task", e);
			
			// Don't re-throw - let the task complete gracefully
			// Individual import failures are logged separately
		}
	}
}
