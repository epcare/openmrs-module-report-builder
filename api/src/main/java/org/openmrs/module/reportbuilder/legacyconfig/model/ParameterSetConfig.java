package org.openmrs.module.reportbuilder.legacyconfig.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterSetConfig {
    private List<ParameterConfig> parameters = new ArrayList<>();

    public List<ParameterConfig> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterConfig> parameters) {
        this.parameters = parameters;
    }
}