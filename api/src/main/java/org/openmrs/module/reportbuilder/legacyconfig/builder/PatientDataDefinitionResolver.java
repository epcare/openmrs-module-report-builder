package org.openmrs.module.reportbuilder.legacyconfig.builder;

import org.openmrs.module.reportbuilder.legacyconfig.model.AliasMethodConfig;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.LegacyAliasResolver;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PatientDataDefinitionResolver {
	
	private final LegacyAliasResolver legacyAliasResolver;
	
	public PatientDataDefinitionResolver(LegacyAliasResolver legacyAliasResolver) {
		this.legacyAliasResolver = legacyAliasResolver;
	}
	
	public DataDefinition resolve(Object source) {
		if (source == null)
			throw new IllegalArgumentException("Patient-data source is required");
		DataDefinition resolved;
		if (source instanceof String) {
			resolved = legacyAliasResolver.resolvePatientDataSource((String) source);
		} else if (source instanceof Map<?, ?>) {
			resolved = legacyAliasResolver.resolvePatientDataSource(toAliasMethodConfig((Map<?, ?>) source));
		} else {
			throw new IllegalArgumentException("Unsupported patient-data source type: " + source.getClass().getName());
		}
		if (!(resolved instanceof PatientDataDefinition) && !(resolved instanceof PersonDataDefinition)) {
			throw new IllegalArgumentException("Resolved patient-data source to unsupported type "
			        + resolved.getClass().getName());
		}
		return resolved;
	}
	
	private AliasMethodConfig toAliasMethodConfig(Map<?, ?> map) {
		AliasMethodConfig cfg = new AliasMethodConfig();
		copy(map, "resolver", cfg::setResolver); copy(map, "value", cfg::setValue); copy(map, "type", cfg::setType); copy(map, "provider", cfg::setProvider); copy(map, "key", cfg::setKey); copy(map, "property", cfg::setProperty); copy(map, "ref", cfg::setRef); copy(map, "bean", cfg::setBean); copy(map, "beanClass", cfg::setBeanClass); copy(map, "method", cfg::setMethod); return cfg;
	}
	
	private interface Setter {
		
		void set(String value);
	}
	
	private void copy(Map<?, ?> map, String key, Setter setter) {
		Object v = map.get(key);
		if (v != null)
			setter.set(String.valueOf(v));
	}
}
