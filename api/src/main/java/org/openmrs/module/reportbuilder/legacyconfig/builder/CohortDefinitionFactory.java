package org.openmrs.module.reportbuilder.legacyconfig.builder;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reportbuilder.legacyconfig.model.CohortConfig;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.MetadataResolver;
import org.openmrs.module.reportbuilder.legacyconfig.resolver.ReferenceResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CohortDefinitionFactory {
	
	private final MetadataResolver metadataResolver;
	
	private final ReferenceResolver referenceResolver;
	
	public CohortDefinitionFactory(MetadataResolver metadataResolver, ReferenceResolver referenceResolver) {
		this.metadataResolver = metadataResolver;
		this.referenceResolver = referenceResolver;
	}
	
	public CohortDefinition build(String key, CohortConfig config, Map<String, CohortDefinition> builtCohorts, List<Parameter> parameters) {
        switch (config.getType()) {
            case "sql":
                SqlCohortDefinition sql = new SqlCohortDefinition();
                sql.setName(config.getName() != null ? config.getName() : key);
                sql.setQuery(config.getQuery());
                sql.setParameters(parameters);
                return sql;

            case "codedObsDuringPeriod":
                CodedObsCohortDefinition coded = new CodedObsCohortDefinition();
                coded.setName(config.getName() != null ? config.getName() : key);
                Concept question = metadataResolver.resolveConcept(config.getQuestion());
                EncounterType encounterType = metadataResolver.resolveEncounterType(config.getEncounterTypeRef());
                List<Concept> answers = new ArrayList<>();
                for (String answer : config.getAnswers()) {
                    answers.add(metadataResolver.resolveConcept(answer));
                }
                coded.setQuestion(question);
                coded.setEncounterTypeList(java.util.Collections.singletonList(encounterType));
                coded.setValueList(answers);
                coded.setTimeModifier(BaseObsCohortDefinition.TimeModifier.valueOf(config.getTimeModifier()));
                coded.setParameters(parameters);
                return coded;

            case "numericObsDuringPeriod":
                NumericObsCohortDefinition numeric = new NumericObsCohortDefinition();
                numeric.setName(config.getName() != null ? config.getName() : key);
                numeric.setQuestion(metadataResolver.resolveConcept(config.getQuestion()));
                numeric.setEncounterTypeList(java.util.Collections.singletonList(metadataResolver.resolveEncounterType(config.getEncounterTypeRef())));
                numeric.setOperator1(RangeComparator.valueOf(config.getComparator()));
                numeric.setValue1(config.getValue());
                numeric.setTimeModifier(BaseObsCohortDefinition.TimeModifier.valueOf(config.getTimeModifier()));
                numeric.setParameters(parameters);
                return numeric;

            case "composition":
                CompositionCohortDefinition composition = new CompositionCohortDefinition();
                composition.setName(config.getName() != null ? config.getName() : key);
                composition.setCompositionString(config.getComposition());
                composition.setParameters(parameters);
                for (Map.Entry<String, String> entry : config.getSearches().entrySet()) {
                    CohortDefinition cd = builtCohorts.get(entry.getValue());
                    if (cd == null) {
                        throw new IllegalArgumentException("Missing composed cohort ref: " + entry.getValue());
                    }
                    composition.addSearch(entry.getKey(), cd, null);
                }
                return composition;

            case "reference":
                return (CohortDefinition) referenceResolver.resolveJavaReference(config.getRef());

            default:
                throw new IllegalArgumentException("Unsupported cohort config type: " + config.getType());
        }
    }
}
