package org.openmrs.module.reportbuilder.legacyconfig.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetRefConfig extends DatasetConfig {
	
	private String key;
	
	private String file;
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getFile() {
		return file;
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public boolean isInlineDefinition() {
		return (getType() != null && getType().trim().length() > 0) || (getColumns() != null && !getColumns().isEmpty())
		        || (getRowFilter() != null && !getRowFilter().isEmpty())
		        || (getDimensions() != null && !getDimensions().isEmpty());
	}
}
