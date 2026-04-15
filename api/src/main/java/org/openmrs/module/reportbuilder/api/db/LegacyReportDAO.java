package org.openmrs.module.reportbuilder.api.db;

import java.util.List;

import org.openmrs.module.reportbuilder.model.LegacyReportConfig;

/**
 * DAO interface for LegacyReport entities.
 */
public interface LegacyReportDAO {
	
	/**
	 * Get all legacy reports.
	 * 
	 * @return list of all legacy reports
	 */
	List<LegacyReportConfig> getAll();
	
	/**
	 * Get a legacy report by UUID.
	 * 
	 * @param uuid the UUID of the report
	 * @return the legacy report, or null if not found
	 */
	LegacyReportConfig getByUuid(String uuid);
	
	/**
	 * Get a legacy report by name.
	 * 
	 * @param name the name of the report
	 * @return the legacy report, or null if not found
	 */
	LegacyReportConfig getByName(String name);
	
	/**
	 * Save or update a legacy report.
	 * 
	 * @param config the report configuration to save
	 * @return the saved report configuration
	 */
	LegacyReportConfig saveOrUpdate(LegacyReportConfig config);
	
	/**
	 * Delete a legacy report by UUID.
	 * 
	 * @param uuid the UUID of the report to delete
	 */
	void delete(String uuid);
	
	/**
	 * Get legacy reports by category.
	 * 
	 * @param category the category to filter by
	 * @return list of legacy reports in the category
	 */
	List<LegacyReportConfig> getByCategory(String category);
	
	/**
	 * Get legacy reports by status.
	 * 
	 * @param status the status to filter by
	 * @return list of legacy reports with the status
	 */
	List<LegacyReportConfig> getByStatus(String status);
	
	/**
	 * Search legacy reports by name or description.
	 * 
	 * @param query the search query
	 * @return list of matching legacy reports
	 */
	List<LegacyReportConfig> search(String query);
	
	/**
	 * Get count of legacy reports.
	 * 
	 * @return the count of legacy reports
	 */
	int getCount();
}
