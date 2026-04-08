package org.openmrs.module.reportbuilder.legacyconfig.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FactoryMethodConfig {
	
	private String bean;
	
	private String beanClass;
	
	private String method;
	
	private List<FactoryArgumentConfig> arguments = new ArrayList<FactoryArgumentConfig>();
	
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
	
	public List<FactoryArgumentConfig> getArguments() {
		return arguments;
	}
	
	public void setArguments(List<FactoryArgumentConfig> arguments) {
		this.arguments = arguments;
	}
}
