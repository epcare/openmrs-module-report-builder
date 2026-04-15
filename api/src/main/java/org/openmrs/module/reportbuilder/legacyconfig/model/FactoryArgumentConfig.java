package org.openmrs.module.reportbuilder.legacyconfig.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FactoryArgumentConfig {
	
	private String name;
	
	private String source;
	
	private String resolver;
	
	private boolean multiple;
	
	@JsonAlias({ "enum" })
	private String enumClass;
	
	private Object value;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getResolver() {
		return resolver;
	}
	
	public void setResolver(String resolver) {
		this.resolver = resolver;
	}
	
	public boolean isMultiple() {
		return multiple;
	}
	
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}
	
	public String getEnumClass() {
		return enumClass;
	}
	
	public void setEnumClass(String enumClass) {
		this.enumClass = enumClass;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
}
