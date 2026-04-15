package org.openmrs.module.reportbuilder.legacyconfig.resolver;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

@Component
public class MetadataResolver {
	
	public Concept resolveConcept(String value) {
		if (value == null) {
			return null;
		}
		
		Concept byUuid = Context.getConceptService().getConceptByUuid(value);
		if (byUuid != null) {
			return byUuid;
		}
		
		Concept byMapping = Context.getConceptService().getConceptByName(value);
		if (byMapping != null) {
			return byMapping;
		}
		
		throw new IllegalArgumentException("Could not resolve concept: " + value);
	}
	
	public EncounterType resolveEncounterType(String value) {
		if (value == null) {
			return null;
		}
		
		EncounterType byUuid = Context.getEncounterService().getEncounterTypeByUuid(value);
		if (byUuid != null) {
			return byUuid;
		}
		
		throw new IllegalArgumentException("Could not resolve encounter type: " + value);
	}
}
