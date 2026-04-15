package org.openmrs.module.reportbuilder.model;

import org.openmrs.BaseOpenmrsObject;

import javax.persistence.*;
import java.util.Date;

/**
 * Hibernate entity for legacy reports stored in the database.
 * This maps to the legacy_report table and stores the complete configuration as JSON.
 */
@Entity
@Table(name = "legacy_report")
public class LegacyReport extends BaseOpenmrsObject {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "legacy_report_id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "version")
    private String version;

    @Column(name = "category")
    private String category;

    @Column(name = "subcategory")
    private String subcategory;

    @Column(name = "report_type")
    private String reportType;

    @Column(name = "report_year")
    private String reportYear;

    @Column(name = "report_scope")
    private String reportScope;

    @Column(name = "status", length = 50)
    private String status = "ACTIVE";

    @Column(name = "config_json", nullable = false, columnDefinition = "TEXT")
    private String configJson;

    @Column(name = "date_changed")
    private Date dateChanged;

    // Constructors
    public LegacyReport() {
        this.dateChanged = new Date();
    }

    public LegacyReport(String uuid) {
        this();
        this.setUuid(uuid);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getReportYear() {
        return reportYear;
    }

    public void setReportYear(String reportYear) {
        this.reportYear = reportYear;
    }

    public String getReportScope() {
        return reportScope;
    }

    public void setReportScope(String reportScope) {
        this.reportScope = reportScope;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getConfigJson() {
        return configJson;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    public Date getDateChanged() {
        return dateChanged;
    }

    public void setDateChanged(Date dateChanged) {
        this.dateChanged = dateChanged;
    }

    @Override
    public String toString() {
        return "LegacyReport{" +
                "uuid='" + getUuid() + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public Integer getId() {
        return 0;
    }

    @Override
    public void setId(Integer integer) {

    }
}
