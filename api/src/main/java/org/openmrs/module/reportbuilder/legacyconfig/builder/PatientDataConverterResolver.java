package org.openmrs.module.reportbuilder.legacyconfig.builder;

import org.openmrs.module.reportbuilder.legacyconfig.model.AliasMethodConfig;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.LegacyAliasResolver;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PatientDataConverterResolver {
	
	private final LegacyAliasResolver legacyAliasResolver;
	
	public PatientDataConverterResolver(LegacyAliasResolver legacyAliasResolver) {
		this.legacyAliasResolver = legacyAliasResolver;
	}
	
	public DataConverter resolve(Object converter) {
		if (converter == null) {
			return null;
		}
		
		try {
			if (converter instanceof String) {
				return legacyAliasResolver.resolveConverter((String) converter);
			}
			
			if (converter instanceof Map<?, ?>) {
				return legacyAliasResolver.resolveConverter(toAliasMethodConfig((Map<?, ?>) converter));
			}
			
			throw new IllegalArgumentException("Unsupported patient-data converter type: " + converter.getClass().getName());
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to resolve patient-data converter: " + converter, e);
		}
	}
	
	private AliasMethodConfig toAliasMethodConfig(Map<?, ?> map) {
		AliasMethodConfig cfg = new AliasMethodConfig();

		copy(map, "resolver", cfg::setResolver);
		copy(map, "value", cfg::setValue);
		copy(map, "type", cfg::setType);
		copy(map, "provider", cfg::setProvider);
		copy(map, "key", cfg::setKey);
		copy(map, "property", cfg::setProperty);
		copy(map, "ref", cfg::setRef);
		copy(map, "bean", cfg::setBean);
		copy(map, "beanClass", cfg::setBeanClass);
		copy(map, "method", cfg::setMethod);

		return cfg;
	}
	
	private interface Setter {
		
		void set(String value);
	}
	
	private void copy(Map<?, ?> map, String key, Setter setter) {
		Object v = map.get(key);
		if (v != null) {
			setter.set(String.valueOf(v));
		}
	}
}
