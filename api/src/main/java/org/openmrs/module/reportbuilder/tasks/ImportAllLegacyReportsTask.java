package org.openmrs.module.reportbuilder.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.module.reportbuilder.api.ReportBuilderService;
import org.openmrs.module.reportbuilder.legacyconfig.importer.ReportImportResult;

import java.util.List;

public class ImportAllLegacyReportsTask extends AbstractTask {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Override
	public void execute() {
		try {
			ReportBuilderService service = Context.getService(ReportBuilderService.class);
			List<ReportImportResult> results = service.importAllRuntimeLegacyReportPackages();
			
			int i;
			for (i = 0; i < results.size(); i++) {
				ReportImportResult result = results.get(i);
				log.info("Legacy report import result for " + result.getReportName() + ": " + result.getMessages());
			}
			
			log.info("Completed import of legacy report packages. Total packages: " + results.size());
		}
		catch (Exception e) {
			log.error("Failed to import legacy report packages", e);
			throw new RuntimeException(e);
		}
	}
}
