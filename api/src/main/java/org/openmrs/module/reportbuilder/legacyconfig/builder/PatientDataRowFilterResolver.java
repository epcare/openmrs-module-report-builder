package org.openmrs.module.reportbuilder.legacyconfig.builder;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.reportbuilder.legacyconfig.model.FactoryArgumentConfig;
import org.openmrs.module.reportbuilder.legacyconfig.model.FactoryMethodConfig;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.LegacyAliasResolver;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.LegacyDataFactoryRegistry;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.SpringBeanMethodInvoker;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PatientDataRowFilterResolver {
	
	private final LegacyDataFactoryRegistry registry;
	
	private final LegacyAliasResolver legacyAliasResolver;
	
	private final SpringBeanMethodInvoker beanMethodInvoker;
	
	public PatientDataRowFilterResolver(LegacyDataFactoryRegistry registry, LegacyAliasResolver legacyAliasResolver,
	    SpringBeanMethodInvoker beanMethodInvoker) {
		this.registry = registry;
		this.legacyAliasResolver = legacyAliasResolver;
		this.beanMethodInvoker = beanMethodInvoker;
	}
	
	public CohortDefinition resolve(Map<String, Object> rowFilter, List<Parameter> parameters) {
		if (rowFilter == null || rowFilter.isEmpty())
			return null;
		FactoryMethodConfig methodConfig = resolveMethodConfig(rowFilter);
		Object[] args = resolveArguments(methodConfig, rowFilter, parameters);
		try {
			Object resolved = beanMethodInvoker.invoke(methodConfig, args);
			if (!(resolved instanceof CohortDefinition))
				throw new IllegalArgumentException(
				        "Configured rowFilter did not resolve to a CohortDefinition. Resolved type: " + className(resolved));
			return (CohortDefinition) resolved;
		}
		catch (IllegalArgumentException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Failed resolving patient-data rowFilter", e);
		}
	}
	
	private FactoryMethodConfig resolveMethodConfig(Map<String, Object> rowFilter) {
		if (hasText(asString(rowFilter.get("bean"))) || hasText(asString(rowFilter.get("beanClass")))
		        || hasText(asString(rowFilter.get("method")))) {
			FactoryMethodConfig cfg = new FactoryMethodConfig();
			cfg.setBean(asString(rowFilter.get("bean")));
			cfg.setBeanClass(asString(rowFilter.get("beanClass")));
			cfg.setMethod(asString(rowFilter.get("method")));
			Object argDefs = rowFilter.get("arguments");
			if (argDefs instanceof List<?>) {
				List<FactoryArgumentConfig> args = new ArrayList<FactoryArgumentConfig>();
				for (Object item : (List<?>) argDefs)
					if (item instanceof Map<?, ?>)
						args.add(toFactoryArgument((Map<?, ?>) item));
				cfg.setArguments(args);
			}
			return cfg;
		}
		String type = asString(rowFilter.get("type"));
		if (!hasText(type))
			throw new IllegalArgumentException("rowFilter type is required");
		FactoryMethodConfig methodConfig = registry.getRowFilter(type);
		if (methodConfig == null)
			throw new IllegalArgumentException("Unsupported patient-data rowFilter type: " + type);
		return methodConfig;
	}
	
	@SuppressWarnings("unchecked")
	private Object[] resolveArguments(FactoryMethodConfig methodConfig, Map<String, Object> rowFilter,
	        List<Parameter> parameters) {
		List<Object> resolved = new ArrayList<Object>();
		if (methodConfig.getArguments() == null || methodConfig.getArguments().isEmpty())
			return new Object[0];
		Map<String, Object> argumentValues = rowFilter;
		Object nestedArguments = rowFilter.get("arguments");
		if (nestedArguments instanceof Map<?, ?>)
			argumentValues = (Map<String, Object>) nestedArguments;
		for (FactoryArgumentConfig argument : methodConfig.getArguments())
			resolved.add(resolveArgument(argument, argumentValues, parameters));
		return resolved.toArray(new Object[0]);
	}
	
	private Object resolveArgument(FactoryArgumentConfig argument, Map<String, Object> rowFilter, List<Parameter> parameters) {
		if (argument == null)
			return null;
		String resolver = hasText(argument.getResolver()) ? argument.getResolver().trim() : "raw";
		if ("parameters".equalsIgnoreCase(resolver))
			return parameters;
		Object raw = argument.getValue();
		if (raw == null && hasText(argument.getSource()))
			raw = rowFilter.get(argument.getSource());
		if (raw == null && hasText(argument.getName()))
			raw = rowFilter.get(argument.getName());
		try {
			if (argument.isMultiple())
				return resolveMultiple(raw, resolver, argument, parameters);
			return resolveSingle(raw, resolver, argument, parameters);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Failed resolving rowFilter argument '" + safeName(argument)
			        + "' using resolver '" + resolver + "' with value '" + String.valueOf(raw) + "'", e);
		}
	}
	
	private Object resolveMultiple(Object raw, String resolver, FactoryArgumentConfig argument, List<Parameter> parameters) {
		if (raw == null)
			return null;
		if (!(raw instanceof List))
			throw new IllegalArgumentException("Expected a list value for argument '" + safeName(argument) + "' but got "
			        + raw.getClass().getName());
		List<?> list = (List<?>) raw;
		List<Object> out = new ArrayList<Object>();
		for (Object item : list)
			out.add(resolveSingle(item, resolver, argument, parameters));
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private Object resolveSingle(Object raw, String resolver, FactoryArgumentConfig argument, List<Parameter> parameters) {
		if (raw instanceof Map<?, ?>) {
			Map<String, Object> map = (Map<String, Object>) raw;
			String nestedResolver = hasText(asString(map.get("resolver"))) ? asString(map.get("resolver")) : resolver;
			if ("parameter".equalsIgnoreCase(nestedResolver))
				return resolveParameterReference(asString(map.get("parameter")), parameters);
			FactoryArgumentConfig nested = new FactoryArgumentConfig();
			nested.setName(argument.getName());
			nested.setSource(argument.getSource());
			nested.setEnumClass(hasText(asString(map.get("enum"))) ? asString(map.get("enum")) : argument.getEnumClass());
			nested.setResolver(nestedResolver);
			nested.setValue(map.containsKey("value") ? map.get("value") : map.get("ref"));
			return resolveSingle(nested.getValue(), nestedResolver, nested, parameters);
		}
		if ("raw".equalsIgnoreCase(resolver))
			return raw;
		if ("string".equalsIgnoreCase(resolver))
			return asString(raw);
		if ("integer".equalsIgnoreCase(resolver))
			return raw == null ? null : Integer.valueOf(String.valueOf(raw).trim());
		if ("boolean".equalsIgnoreCase(resolver))
			return raw == null ? null : Boolean.valueOf(String.valueOf(raw).trim());
		if ("concept".equalsIgnoreCase(resolver) || "conceptUuid".equalsIgnoreCase(resolver)) {
			if (raw == null)
				return null;
			if ("conceptUuid".equalsIgnoreCase(resolver))
				return legacyAliasResolver.resolveConcept(newAlias("conceptUuid", asString(raw)));
			return legacyAliasResolver.resolveConcept(asString(raw));
		}
		if ("encounterType".equalsIgnoreCase(resolver) || "encounterTypeUuid".equalsIgnoreCase(resolver)) {
			if (raw == null)
				return null;
			if ("encounterTypeUuid".equalsIgnoreCase(resolver))
				return legacyAliasResolver.resolveEncounterType(newAlias("encounterTypeUuid", asString(raw)));
			return legacyAliasResolver.resolveEncounterType(asString(raw));
		}
		if ("location".equalsIgnoreCase(resolver))
			return resolveLocation(raw);
		if ("parameter".equalsIgnoreCase(resolver))
			return resolveParameterReference(asString(raw), parameters);
		if ("enum".equalsIgnoreCase(resolver))
			return resolveEnum(argument.getEnumClass(), asString(raw));
		throw new IllegalArgumentException("Unsupported rowFilter argument resolver: " + resolver);
	}
	
	private org.openmrs.module.reportbuilder.legacyconfig.model.AliasMethodConfig newAlias(String resolver, String value) {
		org.openmrs.module.reportbuilder.legacyconfig.model.AliasMethodConfig a = new org.openmrs.module.reportbuilder.legacyconfig.model.AliasMethodConfig();
		a.setResolver(resolver);
		a.setValue(value);
		return a;
	}
	
	private Parameter resolveParameterReference(String name, List<Parameter> parameters) {
		if (!hasText(name))
			return null;
		if (parameters != null)
			for (Parameter p : parameters)
				if (name.equals(p.getName()))
					return p;
		return null;
	}
	
	private Location resolveLocation(Object raw) {
		String value = asString(raw);
		if (!hasText(value))
			return null;
		Location byUuid = Context.getLocationService().getLocationByUuid(value);
		if (byUuid != null)
			return byUuid;
		try {
			Integer id = Integer.valueOf(value);
			Location byId = Context.getLocationService().getLocation(id);
			if (byId != null)
				return byId;
		}
		catch (NumberFormatException ignored) {}
		List<Location> matches = Context.getLocationService().getLocations(value);
		if (matches != null && !matches.isEmpty())
			return matches.get(0);
		throw new IllegalArgumentException("Unable to resolve location from value '" + value + "'");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object resolveEnum(String enumClass, String value) {
		if (!hasText(enumClass))
			throw new IllegalArgumentException("Enum class is required for rowFilter enum resolver");
		if (!hasText(value))
			return null;
		String normalizedValue = value.trim().toUpperCase();
		if ("org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition$TimeModifier".equals(enumClass)
		        || "BaseObsCohortDefinition.TimeModifier".equals(enumClass))
			return BaseObsCohortDefinition.TimeModifier.valueOf(normalizedValue);
		try {
			Class<?> clazz = Class.forName(enumClass);
			if (!clazz.isEnum())
				throw new IllegalArgumentException("Configured enum class '" + enumClass + "' is not an enum");
			return Enum.valueOf((Class<Enum>) clazz, normalizedValue);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Unable to load enum class '" + enumClass + "'", e);
		}
	}
	
	private FactoryArgumentConfig toFactoryArgument(Map<?, ?> map) {
		FactoryArgumentConfig cfg = new FactoryArgumentConfig();
		Object v;
		if ((v = map.get("name")) != null)
			cfg.setName(String.valueOf(v));
		if ((v = map.get("source")) != null)
			cfg.setSource(String.valueOf(v));
		if ((v = map.get("resolver")) != null)
			cfg.setResolver(String.valueOf(v));
		if ((v = map.get("multiple")) != null)
			cfg.setMultiple(Boolean.parseBoolean(String.valueOf(v)));
		if ((v = map.get("enum")) != null)
			cfg.setEnumClass(String.valueOf(v));
		if ((v = map.get("enumClass")) != null)
			cfg.setEnumClass(String.valueOf(v));
		if ((v = map.get("value")) != null)
			cfg.setValue(v);
		return cfg;
	}
	
	private String safeName(FactoryArgumentConfig argument) {
		if (argument == null)
			return "unknown";
		if (hasText(argument.getName()))
			return argument.getName().trim();
		if (hasText(argument.getSource()))
			return argument.getSource().trim();
		return "unknown";
	}
	
	private String asString(Object value) {
		return value == null ? null : String.valueOf(value).trim();
	}
	
	private boolean hasText(String value) {
		return value != null && value.trim().length() > 0;
	}
	
	private String className(Object value) {
		return value == null ? "null" : value.getClass().getName();
	}
}
