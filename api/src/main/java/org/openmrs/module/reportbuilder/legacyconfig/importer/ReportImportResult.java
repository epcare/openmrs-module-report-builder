package org.openmrs.module.reportbuilder.legacyconfig.importer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class ReportImportResult {
    private String reportName;
    private final List<String> messages = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public List<String> getMessages() {
        return messages;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }
}