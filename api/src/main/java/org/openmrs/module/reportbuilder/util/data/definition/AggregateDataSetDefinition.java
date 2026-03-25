package org.openmrs.module.reportbuilder.util.data.definition;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.util.Date;

public class AggregateDataSetDefinition extends BaseDataSetDefinition {
	
	public static final String REPORTS_PATH = "configuration/reportbuilder";
	
	public static final String GP_TO_DIR_PATH = "reportbuilder.reports.directory";
	
	public static final String REPORT_DESIGNS_FOLDER = "report_designs";
	
	@ConfigurationProperty
	private Date startDate;
	
	@ConfigurationProperty
	private Date endDate;
	
	/**
	 * Backward-compatible storage of the report design location. Existing definitions may store an
	 * absolute file path. New definitions may store a relative file name/path such as:
	 * my_report.json or some/subfolder/my_report.json At runtime, relative paths are resolved under
	 * the configured reportbuilder report designs directory.
	 */
	@ConfigurationProperty
	private File reportDesign;
	
	public AggregateDataSetDefinition() {
		super();
	}
	
	public AggregateDataSetDefinition(String name, String description) {
		super(name, description);
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
	
	/**
	 * Returns the resolved report design file. Rules: - null -> null - absolute path -> return
	 * as-is - relative path -> resolve under the configured reportbuilder report designs directory
	 */
	public File getReportDesign() {
		if (reportDesign == null) {
			return null;
		}
		
		if (reportDesign.isAbsolute()) {
			return reportDesign;
		}
		
		return new File(getReportDesignDirectory(), reportDesign.getPath());
	}
	
	/**
	 * Stores the raw file reference. If you pass an absolute file, it will be used as-is. If you
	 * pass a relative file, it will resolve under the reportbuilder report designs directory.
	 */
	public void setReportDesign(File reportDesign) {
		this.reportDesign = reportDesign;
	}
	
	/**
	 * Convenience setter for storing a relative design path or file name.
	 */
	public void setReportDesignPath(String relativePath) {
		if (relativePath == null || relativePath.trim().isEmpty()) {
			this.reportDesign = null;
		} else {
			this.reportDesign = new File(relativePath.trim());
		}
	}
	
	/**
	 * Returns the raw configured value exactly as stored, without resolution.
	 */
	public File getRawReportDesign() {
		return reportDesign;
	}
	
	/**
	 * Returns the report designs directory under the configured reportbuilder root. If the global
	 * property reportbuilder.reports.directory is not set, the fallback path is:
	 * <OPENMRS_APPDATA>/configuration/reportbuilder/report_designs
	 */
	public static File getReportDesignDirectory() {
		AdministrationService administrationService = Context.getAdministrationService();
		String pathToDIR = administrationService.getGlobalProperty(GP_TO_DIR_PATH);
		
		if (pathToDIR == null || pathToDIR.trim().isEmpty()) {
			pathToDIR = REPORTS_PATH;
		}
		
		File dir = new File(new File(OpenmrsUtil.getApplicationDataDirectory(), pathToDIR), REPORT_DESIGNS_FOLDER);
		
		if (!dir.exists() && !dir.mkdirs()) {
			throw new RuntimeException("Failed to create report design directory: " + dir.getAbsolutePath());
		}
		
		return dir;
	}
	
	/**
	 * Resolves a report design file name under the standard reportbuilder folder.
	 */
	public static File resolveReportDesignFile(String fileName) {
		return new File(getReportDesignDirectory(), fileName);
	}
}
