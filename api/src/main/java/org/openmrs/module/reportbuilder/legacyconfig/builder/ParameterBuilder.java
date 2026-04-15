package org.openmrs.module.reportbuilder.legacyconfig.builder;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Program;
import org.openmrs.Provider;
import org.openmrs.module.reportbuilder.legacyconfig.model.ParameterConfig;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ParameterBuilder {
	
	private final Map<String, Class<?>> aliases = new HashMap<String, Class<?>>();
	
	public ParameterBuilder() {
		register("date", Date.class);
		register("datetime", Date.class);
		register("location", Location.class);
		register("string", String.class);
		register("text", String.class);
		register("integer", Integer.class);
		register("int", Integer.class);
		register("long", Long.class);
		register("double", Double.class);
		register("number", Double.class);
		register("boolean", Boolean.class);
		register("bool", Boolean.class);
		register("patient", Patient.class);
		register("person", Person.class);
		register("program", Program.class);
		register("provider", Provider.class);
	}
	
	public Parameter build(ParameterConfig config) {
		if (config == null) {
			throw new IllegalArgumentException("Parameter config is required");
		}
		if (!hasText(config.getName())) {
			throw new IllegalArgumentException("Parameter name is required");
		}
		if (!hasText(config.getType())) {
			throw new IllegalArgumentException("Parameter type is required for parameter: " + config.getName());
		}
		try {
			Class<?> parameterType = resolveParameterType(config.getType());
			String label = hasText(config.getLabel()) ? config.getLabel() : config.getName();
			return new Parameter(config.getName(), label, parameterType);
		}
		catch (Exception e) {
			throw new RuntimeException("Unknown parameter type: " + config.getType(), e);
		}
	}
	
	private Class<?> resolveParameterType(String type) throws ClassNotFoundException {
		String value = type == null ? null : type.trim();
		if (!hasText(value)) {
			throw new IllegalArgumentException("Parameter type is required");
		}
		Class<?> aliasType = aliases.get(value.toLowerCase());
		if (aliasType != null) {
			return aliasType;
		}
		return Class.forName(value);
	}
	
	private void register(String alias, Class<?> type) {
		aliases.put(alias, type);
	}
	
	private boolean hasText(String value) {
		return value != null && value.trim().length() > 0;
	}
}
