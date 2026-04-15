package org.openmrs.module.reportbuilder.api;

import java.util.List;

import org.openmrs.module.reportbuilder.model.LegacyReportConfig;
import org.openmrs.module.reportbuilder.validation.ReportValidationResult;

/**
 * Service interface for managing legacy reports.
 * Provides CRUD operations and validation for legacy report configurations.
 */
public interface LegacyReportService {

    /**
     * Get all legacy reports.
     *
     * @return list of all legacy reports
     */
    List<LegacyReportConfig> getAllLegacyReports();

    /**
     * Get a legacy report by UUID.
     *
     * @param uuid the UUID of the report
     * @return the legacy report, or null if not found
     */
    LegacyReportConfig getLegacyReportByUuid(String uuid);

    /**
     * Get a legacy report by name.
     *
     * @param name the name of the report
     * @return the legacy report, or null if not found
     */
    LegacyReportConfig getLegacyReportByName(String name);

    /**
     * Create a new legacy report.
     *
     * @param config the report configuration to create
     * @return the created report configuration
     */
    LegacyReportConfig createLegacyReport(LegacyReportConfig config);

    /**
     * Update an existing legacy report.
     *
     * @param uuid the UUID of the report to update
     * @param config the updated report configuration
     * @return the updated report configuration
     */
    LegacyReportConfig updateLegacyReport(String uuid, LegacyReportConfig config);

    /**
     * Delete a legacy report by UUID (soft delete).
     *
     * @param uuid the UUID of the report to delete
     */
    void deleteLegacyReport(String uuid);

    /**
     * Validate a legacy report configuration.
     *
     * @param config the report configuration to validate
     * @return validation result with errors and warnings
     */
    ReportValidationResult validateLegacyReport(LegacyReportConfig config);

    /**
     * Get legacy reports by category.
     *
     * @param category the category to filter by
     * @return list of legacy reports in the category
     */
    List<LegacyReportConfig> getLegacyReportsByCategory(String category);

    /**
     * Get legacy reports by status.
     *
     * @param status the status to filter by
     * @return list of legacy reports with the status
     */
    List<LegacyReportConfig> getLegacyReportsByStatus(String status);

    /**
     * Search legacy reports by name or description.
     *
     * @param query the search query
     * @return list of matching legacy reports
     */
    List<LegacyReportConfig> searchLegacyReports(String query);

    /**
     * Get count of legacy reports.
     *
     * @return the count of legacy reports
     */
    int getLegacyReportCount();
}
