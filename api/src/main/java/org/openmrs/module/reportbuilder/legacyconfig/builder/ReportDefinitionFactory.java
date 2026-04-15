package org.openmrs.module.reportbuilder.legacyconfig.builder;

import org.openmrs.module.reportbuilder.legacyconfig.model.ReportConfig;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ReportDefinitionFactory {
	
	public ReportDefinition build(ReportConfig config, List<Parameter> parameters,
	        Map<String, ? extends DataSetDefinition> datasets) {
		ReportDefinition rd = new ReportDefinition();
		rd.setUuid(config.getUuid());
		rd.setName(config.getName());
		rd.setDescription(config.getDescription());
		rd.setParameters(parameters);
		for (Map.Entry<String, ? extends DataSetDefinition> entry : datasets.entrySet())
			rd.addDataSetDefinition(entry.getKey(), Mapped.mapStraightThrough(entry.getValue()));
		return rd;
	}
}
