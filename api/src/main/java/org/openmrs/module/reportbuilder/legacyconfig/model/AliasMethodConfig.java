package org.openmrs.module.reportbuilder.legacyconfig.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AliasMethodConfig {
	
	private String bean;
	
	private String beanClass;
	
	private String method;
	
	private List<Object> arguments = new ArrayList<Object>();
	
	/**
	 * Lightweight inline resolver support so aliases can be declared directly in JSON without
	 * requiring bean+method wiring.
	 */
	private String resolver;
	
	private String value;
	
	private String type;
	
	private String provider;
	
	private String key;
	
	private String property;
	
	private String ref;
	
	public String getBean() {
		return bean;
	}
	
	public void setBean(String bean) {
		this.bean = bean;
	}
	
	public String getBeanClass() {
		return beanClass;
	}
	
	public void setBeanClass(String beanClass) {
		this.beanClass = beanClass;
	}
	
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	public List<Object> getArguments() {
		return arguments;
	}
	
	public void setArguments(List<Object> arguments) {
		this.arguments = arguments;
	}
	
	public String getResolver() {
		return resolver;
	}
	
	public void setResolver(String resolver) {
		this.resolver = resolver;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getProvider() {
		return provider;
	}
	
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getProperty() {
		return property;
	}
	
	public void setProperty(String property) {
		this.property = property;
	}
	
	public String getRef() {
		return ref;
	}
	
	public void setRef(String ref) {
		this.ref = ref;
	}
}
