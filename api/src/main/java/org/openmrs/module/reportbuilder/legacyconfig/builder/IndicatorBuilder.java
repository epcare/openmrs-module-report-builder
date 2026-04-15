package org.openmrs.module.reportbuilder.legacyconfig.builder;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reportbuilder.legacyconfig.model.IndicatorConfig;
import org.springframework.stereotype.Component;

@Component
public class IndicatorBuilder {
	
	public CohortIndicator build(IndicatorConfig config, CohortDefinition cohortDefinition) {
		if (!"cohort".equals(config.getType())) {
			throw new IllegalArgumentException("Unsupported indicator type: " + config.getType());
		}
		
		String name = config.getName();
		if (name == null || name.trim().length() == 0) {
			name = "Cohort Indicator";
		}
		
		CohortIndicator indicator = new CohortIndicator(name);
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		indicator.setCohortDefinition(cohortDefinition, mappings);
		
		return indicator;
	}
}
