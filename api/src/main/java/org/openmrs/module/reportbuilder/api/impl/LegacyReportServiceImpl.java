package org.openmrs.module.reportbuilder.api.impl;

import org.openmrs.api.APIException;
import org.openmrs.module.reportbuilder.api.LegacyReportService;
import org.openmrs.module.reportbuilder.api.db.LegacyReportDAO;
import org.openmrs.module.reportbuilder.model.LegacyReportConfig;
import org.openmrs.module.reportbuilder.validation.ReportValidationResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of LegacyReportService.
 * Provides CRUD operations and validation for legacy report configurations.
 */
public class LegacyReportServiceImpl implements LegacyReportService {

    private LegacyReportDAO dao;

    public LegacyReportServiceImpl() {
    }

    public LegacyReportServiceImpl(LegacyReportDAO dao) {
        this.dao = dao;
    }

    public void setDao(LegacyReportDAO dao) {
        this.dao = dao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LegacyReportConfig> getAllLegacyReports() {
        return dao.getAll();
    }

    @Override
    @Transactional(readOnly = true)
    public LegacyReportConfig getLegacyReportByUuid(String uuid) {
        return dao.getByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public LegacyReportConfig getLegacyReportByName(String name) {
        return dao.getByName(name);
    }

    @Override
    @Transactional
    public LegacyReportConfig createLegacyReport(LegacyReportConfig config) {
        if (config == null) {
            throw new APIException("Report configuration cannot be null");
        }

        if (config.getName() == null || config.getName().trim().isEmpty()) {
            throw new APIException("Report name is required");
        }

        // Check for duplicate name
        LegacyReportConfig existing = dao.getByName(config.getName());
        if (existing != null) {
            throw new APIException("A report with this name already exists");
        }

        // Validate the configuration
        ReportValidationResult validation = validateLegacyReport(config);
        if (!validation.isValid()) {
            throw new APIException("Invalid report configuration: " + validation.getSummary());
        }

        // Generate UUID if not provided
        if (config.getUuid() == null || config.getUuid().trim().isEmpty()) {
            config.setUuid(UUID.randomUUID().toString());
        }

        // Set default values
        if (config.getStatus() == null || config.getStatus().trim().isEmpty()) {
            config.setStatus("ACTIVE");
        }

        return dao.saveOrUpdate(config);
    }

    @Override
    @Transactional
    public LegacyReportConfig updateLegacyReport(String uuid, LegacyReportConfig config) {
        if (uuid == null || uuid.trim().isEmpty()) {
            throw new APIException("Report UUID is required");
        }

        if (config == null) {
            throw new APIException("Report configuration cannot be null");
        }

        // Check if report exists
        LegacyReportConfig existing = dao.getByUuid(uuid);
        if (existing == null) {
            throw new APIException("Report not found with UUID: " + uuid);
        }

        // Check for duplicate name (if name changed)
        if (!existing.getName().equals(config.getName())) {
            LegacyReportConfig duplicate = dao.getByName(config.getName());
            if (duplicate != null && !duplicate.getUuid().equals(uuid)) {
                throw new APIException("A report with this name already exists");
            }
        }

        // Validate the configuration
        ReportValidationResult validation = validateLegacyReport(config);
        if (!validation.isValid()) {
            throw new APIException("Invalid report configuration: " + validation.getSummary());
        }

        // Set the UUID to ensure we're updating the correct report
        config.setUuid(uuid);

        return dao.saveOrUpdate(config);
    }

    @Override
    @Transactional
    public void deleteLegacyReport(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            throw new APIException("Report UUID is required");
        }

        // Check if report exists
        LegacyReportConfig existing = dao.getByUuid(uuid);
        if (existing == null) {
            throw new APIException("Report not found with UUID: " + uuid);
        }

        dao.delete(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportValidationResult validateLegacyReport(LegacyReportConfig config) {
        ReportValidationResult result = new ReportValidationResult();

        if (config == null) {
            result.addError("Report configuration cannot be null");
            return result;
        }

        // Validate basic fields
        if (config.getName() == null || config.getName().trim().isEmpty()) {
            result.addError("Report name is required");
        }

        if (config.getVersion() == null || config.getVersion().trim().isEmpty()) {
            result.addWarning("Report version is not specified");
        }

        // Validate parameters
        if (config.getParameters() != null) {
            for (int i = 0; i < config.getParameters().size(); i++) {
                LegacyReportConfig.Parameter param = config.getParameters().get(i);
                if (param.getName() == null || param.getName().trim().isEmpty()) {
                    result.addError("Parameter at index " + i + " is missing a name");
                }
                if (param.getType() == null || param.getType().trim().isEmpty()) {
                    result.addError("Parameter '" + param.getName() + "' is missing a type");
                }
            }
        }

        // Validate advanced features
        if (config.getAdvancedFeatures() != null &&
            config.getAdvancedFeatures().getIndicatorDataSet() != null &&
            config.getAdvancedFeatures().getIndicatorDataSet().isEnabled()) {

            validateIndicatorDataSet(config, result);
        }

        // Validate dataset definitions
        if (config.getDataSetDefinitions() != null) {
            for (int i = 0; i < config.getDataSetDefinitions().size(); i++) {
                LegacyReportConfig.DataSetDefinition dataset = config.getDataSetDefinitions().get(i);
                if (dataset.getName() == null || dataset.getName().trim().isEmpty()) {
                    result.addError("Dataset definition at index " + i + " is missing a name");
                }
                if (dataset.getType() == null || dataset.getType().trim().isEmpty()) {
                    result.addError("Dataset '" + dataset.getName() + "' is missing a type");
                }
            }
        }

        // Validate SQL
        validateSQLQueries(config, result);

        result.setValid(!result.hasErrors());

        return result;
    }

    private void validateIndicatorDataSet(LegacyReportConfig config, ReportValidationResult result) {
        LegacyReportConfig.IndicatorDataSet indicatorDataSet =
            config.getAdvancedFeatures().getIndicatorDataSet();

        // Validate indicators
        if (indicatorDataSet.getIndicators() != null) {
            for (int i = 0; i < indicatorDataSet.getIndicators().size(); i++) {
                LegacyReportConfig.Indicator indicator = indicatorDataSet.getIndicators().get(i);

                if (indicator.getKey() == null || indicator.getKey().trim().isEmpty()) {
                    result.addError("Indicator at index " + i + " is missing a key");
                }

                if (indicator.getType() == null || indicator.getType().trim().isEmpty()) {
                    result.addError("Indicator '" + indicator.getKey() + "' is missing a type");
                }

                // Validate BASE indicators
                if ("BASE".equalsIgnoreCase(indicator.getType())) {
                    if (indicator.getSqlQuery() == null || indicator.getSqlQuery().trim().isEmpty()) {
                        result.addError("BASE indicator '" + indicator.getKey() + "' is missing a SQL query");
                    }
                }

                // Validate COMPOSITE indicators
                if ("COMPOSITE".equalsIgnoreCase(indicator.getType())) {
                    if (indicator.getFormula() == null || indicator.getFormula().trim().isEmpty()) {
                        result.addError("COMPOSITE indicator '" + indicator.getKey() + "' is missing a formula");
                    }
                }

                // Validate TEMPORAL indicators
                if ("TEMPORAL".equalsIgnoreCase(indicator.getType())) {
                    if (indicator.getBaseIndicator() == null || indicator.getBaseIndicator().trim().isEmpty()) {
                        result.addError("TEMPORAL indicator '" + indicator.getKey() + "' is missing a base indicator reference");
                    }
                }
            }
        }

        // Validate dimension definitions
        if (indicatorDataSet.getDimensionDefinitions() != null) {
            for (int i = 0; i < indicatorDataSet.getDimensionDefinitions().size(); i++) {
                LegacyReportConfig.DimensionDefinition dimension =
                    indicatorDataSet.getDimensionDefinitions().get(i);

                if (dimension.getName() == null || dimension.getName().trim().isEmpty()) {
                    result.addError("Dimension definition at index " + i + " is missing a name");
                }

                if (dimension.getType() == null || dimension.getType().trim().isEmpty()) {
                    result.addError("Dimension '" + dimension.getName() + "' is missing a type");
                }

                // Validate dimension groups
                if (dimension.getGroups() != null && dimension.getGroups().isEmpty()) {
                    result.addWarning("Dimension '" + dimension.getName() + "' has no groups defined");
                }
            }
        }
    }

    private void validateSQLQueries(LegacyReportConfig config, ReportValidationResult result) {
        // Validate SQL queries in indicators
        if (config.getAdvancedFeatures() != null &&
            config.getAdvancedFeatures().getIndicatorDataSet() != null &&
            config.getAdvancedFeatures().getIndicatorDataSet().getIndicators() != null) {

            for (LegacyReportConfig.Indicator indicator :
                config.getAdvancedFeatures().getIndicatorDataSet().getIndicators()) {
                if ("BASE".equalsIgnoreCase(indicator.getType()) && indicator.getSqlQuery() != null) {
                    validateSQLQuery(indicator.getSqlQuery(), result,
                        "Indicator '" + indicator.getKey() + "'");
                }
            }
        }

        // Validate SQL queries in dataset definitions
        if (config.getDataSetDefinitions() != null) {
            for (LegacyReportConfig.DataSetDefinition dataset : config.getDataSetDefinitions()) {
                if ("SQL_DATA_SET".equalsIgnoreCase(dataset.getType()) &&
                    dataset.getConfig() != null &&
                    dataset.getConfig().getSql() != null) {
                    validateSQLQuery(dataset.getConfig().getSql(), result,
                        "Dataset '" + dataset.getName() + "'");
                }
            }
        }
    }

    private void validateSQLQuery(String sql, ReportValidationResult result, String context) {
        // Basic SQL validation
        if (sql.trim().isEmpty()) {
            result.addError(context + " has empty SQL query");
            return;
        }

        // Check for dangerous operations
        String upperSQL = sql.toUpperCase();
        String[] dangerousOperations = {"DROP", "DELETE", "TRUNCATE", "ALTER", "CREATE", "INSERT", "UPDATE"};

        for (String dangerous : dangerousOperations) {
            if (upperSQL.contains(dangerous)) {
                result.addError(context + " contains dangerous SQL operation: " + dangerous);
                result.getSqlValidation().addSqlError(context + ": " + dangerous + " operation not allowed");
            }
        }

        // Validate SQL syntax (basic check)
        if (!upperSQL.startsWith("SELECT")) {
            result.addWarning(context + " SQL query does not start with SELECT");
        }

        if (!upperSQL.contains("FROM")) {
            result.addError(context + " SQL query is missing FROM clause");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LegacyReportConfig> getLegacyReportsByCategory(String category) {
        return dao.getByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LegacyReportConfig> getLegacyReportsByStatus(String status) {
        return dao.getByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LegacyReportConfig> searchLegacyReports(String query) {
        return dao.search(query);
    }

    @Override
    @Transactional(readOnly = true)
    public int getLegacyReportCount() {
        return dao.getCount();
    }
}
