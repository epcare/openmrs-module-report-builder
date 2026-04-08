package org.openmrs.module.reportbuilder.legacyconfig.resolver;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.module.reportbuilder.legacyconfig.model.AliasMethodConfig;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.springframework.stereotype.Component;

@Component
public class LegacyAliasResolver {
	
	private final LegacyDataFactoryRegistry registry;
	
	private final SpringBeanMethodInvoker beanMethodInvoker;
	
	private final MetadataResolver metadataResolver;
	
	private final ReferenceResolver referenceResolver;
	
	public LegacyAliasResolver(LegacyDataFactoryRegistry registry, SpringBeanMethodInvoker beanMethodInvoker,
	    MetadataResolver metadataResolver, ReferenceResolver referenceResolver) {
		this.registry = registry;
		this.beanMethodInvoker = beanMethodInvoker;
		this.metadataResolver = metadataResolver;
		this.referenceResolver = referenceResolver;
	}
	
	public Concept resolveConcept(String value) {
		String key = requireText(value, "Concept alias is required");
		if (isJavaReference(key)) {
			Object resolved = referenceResolver.resolveJavaReference(key);
			if (!(resolved instanceof Concept))
				throw new IllegalArgumentException("Java reference '" + key
				        + "' did not resolve to a Concept. Resolved type: " + className(resolved));
			return (Concept) resolved;
		}
		AliasMethodConfig alias = registry.getConcept(key);
		if (alias != null)
			return resolveConcept(alias);
		return metadataResolver.resolveConcept(key);
	}
	
	public Concept resolveConcept(AliasMethodConfig alias) {
		if (alias == null)
			return null;
		if (hasText(alias.getResolver())) {
			String resolver = alias.getResolver().trim();
			if ("conceptUuid".equalsIgnoreCase(resolver) || "concept".equalsIgnoreCase(resolver))
				return metadataResolver.resolveConcept(firstNonBlank(alias.getValue(), alias.getRef()));
			if ("javaReference".equalsIgnoreCase(resolver) || "java".equalsIgnoreCase(resolver))
				return (Concept) referenceResolver.resolveJavaReference(firstNonBlank(alias.getRef(), alias.getValue()));
			throw new IllegalArgumentException("Unsupported concept resolver '" + resolver + "'");
		}
		Object resolved = invoke(alias, "concept", alias.getValue());
		if (!(resolved instanceof Concept))
			throw new IllegalArgumentException("Configured concept alias did not resolve to a Concept. Resolved type: "
			        + className(resolved));
		return (Concept) resolved;
	}
	
	public EncounterType resolveEncounterType(String value) {
		String key = requireText(value, "Encounter type alias is required");
		if (isJavaReference(key)) {
			Object resolved = referenceResolver.resolveJavaReference(key);
			if (!(resolved instanceof EncounterType))
				throw new IllegalArgumentException("Java reference '" + key
				        + "' did not resolve to an EncounterType. Resolved type: " + className(resolved));
			return (EncounterType) resolved;
		}
		AliasMethodConfig alias = registry.getEncounterType(key);
		if (alias != null)
			return resolveEncounterType(alias);
		return metadataResolver.resolveEncounterType(key);
	}
	
	public EncounterType resolveEncounterType(AliasMethodConfig alias) {
		if (alias == null)
			return null;
		if (hasText(alias.getResolver())) {
			String resolver = alias.getResolver().trim();
			if ("encounterTypeUuid".equalsIgnoreCase(resolver) || "encounterType".equalsIgnoreCase(resolver))
				return metadataResolver.resolveEncounterType(firstNonBlank(alias.getValue(), alias.getRef()));
			if ("javaReference".equalsIgnoreCase(resolver) || "java".equalsIgnoreCase(resolver))
				return (EncounterType) referenceResolver
				        .resolveJavaReference(firstNonBlank(alias.getRef(), alias.getValue()));
			throw new IllegalArgumentException("Unsupported encounter type resolver '" + resolver + "'");
		}
		Object resolved = invoke(alias, "encounter type", alias.getValue());
		if (!(resolved instanceof EncounterType))
			throw new IllegalArgumentException(
			        "Configured encounter type alias did not resolve to an EncounterType. Resolved type: "
			                + className(resolved));
		return (EncounterType) resolved;
	}
	
	public DataDefinition resolvePatientDataSource(String value) {
		String key = requireText(value, "Patient-data source alias is required");
		if (isJavaReference(key)) {
			Object resolved = referenceResolver.resolveJavaReference(key);
			if (!(resolved instanceof DataDefinition))
				throw new IllegalArgumentException("Java reference '" + key
				        + "' did not resolve to a DataDefinition. Resolved type: " + className(resolved));
			return (DataDefinition) resolved;
		}
		AliasMethodConfig alias = registry.getPatientDataSource(key);
		if (alias != null)
			return resolvePatientDataSource(alias);
		throw new IllegalArgumentException("Unsupported patient-data source alias '" + key
		        + "'. No java reference or configured alias matched.");
	}
	
	public DataDefinition resolvePatientDataSource(AliasMethodConfig alias) {
		if (alias == null)
			return null;
		if (hasText(alias.getResolver())) {
			String resolver = alias.getResolver().trim();
			if ("builtinPatientData".equalsIgnoreCase(resolver)) {
				String type = requireText(alias.getType(), "Patient-data type is required");
				if ("preferred-name".equalsIgnoreCase(type))
					return (DataDefinition) instantiate("org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition");
				if ("gender".equalsIgnoreCase(type))
					return (DataDefinition) instantiate("org.openmrs.module.reporting.data.person.definition.GenderDataDefinition");
				if ("birthdate".equalsIgnoreCase(type))
					return (DataDefinition) instantiate("org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition");
				throw new IllegalArgumentException("Unsupported builtinPatientData type '" + type + "'");
			}
			if ("personAddressProperty".equalsIgnoreCase(resolver))
				return (DataDefinition) beanMethodInvoker.invoke((String) null,
				    "org.openmrs.module.ugandaemrreports.library.DataFactory", "getPreferredAddress", alias.getProperty());
			if ("providerKey".equalsIgnoreCase(resolver))
				return (DataDefinition) resolveUgandaEmrPatientData(alias.getProvider(), alias.getKey());
			if ("javaReference".equalsIgnoreCase(resolver) || "java".equalsIgnoreCase(resolver))
				return (DataDefinition) referenceResolver.resolveJavaReference(firstNonBlank(alias.getRef(),
				    alias.getValue()));
			throw new IllegalArgumentException("Unsupported patient-data resolver '" + resolver + "'");
		}
		Object resolved = invoke(alias, "patient-data source", alias.getKey());
		if (!(resolved instanceof DataDefinition))
			throw new IllegalArgumentException(
			        "Configured patient-data source did not resolve to a DataDefinition. Resolved type: "
			                + className(resolved));
		return (DataDefinition) resolved;
	}
	
	public DataConverter resolveConverter(String value) {
		if (!hasText(value))
			return null;
		String key = value.trim();
		if (isJavaReference(key)) {
			Object resolved = referenceResolver.resolveJavaReference(key);
			if (!(resolved instanceof DataConverter))
				throw new IllegalArgumentException("Java reference '" + key
				        + "' did not resolve to a DataConverter. Resolved type: " + className(resolved));
			return (DataConverter) resolved;
		}
		AliasMethodConfig alias = registry.getConverter(key);
		if (alias != null)
			return resolveConverter(alias);
		throw new IllegalArgumentException("Unsupported patient-data converter alias '" + key
		        + "'. No java reference or configured alias matched.");
	}
	
	public DataConverter resolveConverter(AliasMethodConfig alias) {
		if (alias == null)
			return null;
		if (hasText(alias.getResolver())) {
			String resolver = alias.getResolver().trim();
			if ("providerKey".equalsIgnoreCase(resolver) && "ugandaemr".equalsIgnoreCase(alias.getProvider())
			        && "converter.birth-date".equalsIgnoreCase(alias.getKey()))
				return (DataConverter) instantiate("org.openmrs.module.ugandaemrreports.definition.data.converter.BirthDateConverter");
			if ("javaReference".equalsIgnoreCase(resolver) || "java".equalsIgnoreCase(resolver))
				return (DataConverter) referenceResolver
				        .resolveJavaReference(firstNonBlank(alias.getRef(), alias.getValue()));
			throw new IllegalArgumentException("Unsupported converter resolver '" + resolver + "'");
		}
		Object resolved = invoke(alias, "patient-data converter", alias.getKey());
		if (!(resolved instanceof DataConverter))
			throw new IllegalArgumentException(
			        "Configured converter alias did not resolve to a DataConverter. Resolved type: " + className(resolved));
		return (DataConverter) resolved;
	}
	
	public CohortDefinitionDimension resolveDimension(String value) {
		String key = requireText(value, "Dimension alias is required");
		if (isJavaReference(key)) {
			Object resolved = referenceResolver.resolveJavaReference(key);
			if (!(resolved instanceof CohortDefinitionDimension))
				throw new IllegalArgumentException("Java reference '" + key
				        + "' did not resolve to a CohortDefinitionDimension. Resolved type: " + className(resolved));
			return (CohortDefinitionDimension) resolved;
		}
		AliasMethodConfig alias = registry.getDimension(key);
		if (alias != null) {
			Object resolved = invoke(alias, "dimension", key);
			if (!(resolved instanceof CohortDefinitionDimension))
				throw new IllegalArgumentException("Configured dimension alias '" + key
				        + "' did not resolve to a CohortDefinitionDimension. Resolved type: " + className(resolved));
			return (CohortDefinitionDimension) resolved;
		}
		throw new IllegalArgumentException("Unsupported dimension alias '" + key
		        + "'. No java reference or configured alias matched.");
	}
	
	private DataDefinition resolveUgandaEmrPatientData(String provider, String key) {
		if (!"ugandaemr".equalsIgnoreCase(provider))
			throw new IllegalArgumentException("Unsupported provider '" + provider + "'");
		String beanClass = null;
		String method = null;
		if ("patient.telephone".equals(key)) {
			beanClass = "org.openmrs.module.ugandaemrreports.library.BasePatientDataLibrary";
			method = "getTelephone";
		} else if (key != null && key.startsWith("hiv.")) {
			beanClass = "org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary";
			method = hivMethodForKey(key);
		}
		if (beanClass == null || method == null)
			throw new IllegalArgumentException("Unsupported ugandaemr provider key '" + key + "'");
		return (DataDefinition) beanMethodInvoker.invoke((String) null, beanClass, method);
	}
	
	private String hivMethodForKey(String key) {
		if ("hiv.vl_qualitative_by_end_date".equals(key))
			return "getVLQualitativeByEndDate";
		String suffix = key.substring("hiv.".length());
		StringBuilder sb = new StringBuilder("get");
		boolean up = true;
		for (char c : suffix.toCharArray()) {
			if (c == '_' || c == '.') {
				up = true;
				continue;
			}
			sb.append(up ? Character.toUpperCase(c) : c);
			up = false;
		}
		return sb.toString();
	}
	
	private Object invoke(AliasMethodConfig alias, String aliasType, String key) {
		try {
			return beanMethodInvoker.invoke(alias);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Failed resolving " + aliasType + " alias '" + key
			        + "' using configured factory method.", e);
		}
	}
	
	private Object instantiate(String className) {
		try {
			return Class.forName(className).newInstance();
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to instantiate class '" + className + "'", e);
		}
	}
	
	private String requireText(String value, String message) {
		if (!hasText(value))
			throw new IllegalArgumentException(message);
		return value.trim();
	}
	
	private boolean isJavaReference(String value) {
		return hasText(value) && value.startsWith("java:");
	}
	
	private boolean hasText(String value) {
		return value != null && value.trim().length() > 0;
	}
	
	private String className(Object value) {
		return value == null ? "null" : value.getClass().getName();
	}
	
	private String firstNonBlank(String a, String b) {
		return hasText(a) ? a.trim() : (hasText(b) ? b.trim() : null);
	}
}
