package org.openmrs.module.reportbuilder.legacyconfig.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IndicatorConfig {
	
	private String type;
	
	@JsonAlias({ "cohort" })
	private String cohortRef;
	
	private String name;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getCohortRef() {
		return cohortRef;
	}
	
	public void setCohortRef(String cohortRef) {
		this.cohortRef = cohortRef;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
