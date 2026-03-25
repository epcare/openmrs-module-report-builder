package org.openmrs.module.reportbuilder.web.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reportbuilder.api.ReportBuilderService;
import org.openmrs.module.reportbuilder.web.controller.dto.ReportBuilderReportCompileResult;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/reportbuilder/reportcompile", supportedClass = ReportBuilderReportCompileResult.class, supportedOpenmrsVersions = { "1.8 - 9.0.*" })
public class ReportBuilderReportCompileResource extends DelegatingCrudResource<ReportBuilderReportCompileResult> {
	
	@Override
	public ReportBuilderReportCompileResult newDelegate() {
		return new ReportBuilderReportCompileResult();
	}
	
	/**
	 * POST /ws/rest/v1/reportbuilder/reportcompile Request body: { "reportUuid": "..." }
	 */
	@Override
	public ReportBuilderReportCompileResult save(ReportBuilderReportCompileResult delegate) {
		if (delegate == null || delegate.getReportUuid() == null || delegate.getReportUuid().trim().isEmpty()) {
			throw new IllegalArgumentException("reportUuid is required");
		}
		
		ReportBuilderService ReportBuilderService = Context.getService(ReportBuilderService.class);
		ReportBuilderService.CompiledReportArtifacts result = ReportBuilderService.compileReport(delegate.getReportUuid());
		
		ReportDefinition rd = result.getReportDefinition();
		
		ReportBuilderReportCompileResult out = new ReportBuilderReportCompileResult();
		out.setReportUuid(result.getReportBuilderReport() != null ? result.getReportBuilderReport().getUuid() : delegate
		        .getReportUuid());
		out.setReportDefinitionUuid(rd != null ? rd.getUuid() : null);
		out.setReportDefinitionName(rd != null ? rd.getName() : null);
		out.setReportDesignPath(result.getReportDesignFile() != null ? result.getReportDesignFile().getAbsolutePath() : null);
		out.setCompiled(Boolean.TRUE);
		
		return out;
	}
	
	@Override
	public ReportBuilderReportCompileResult getByUniqueId(String uniqueId) {
		return null;
	}
	
	@Override
	protected void delete(ReportBuilderReportCompileResult delegate, String reason, RequestContext context)
	        throws ResponseException {
		throw new UnsupportedOperationException("Delete is not supported for reportcompile");
	}
	
	@Override
	public void purge(ReportBuilderReportCompileResult delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Purge is not supported for reportcompile");
	}
	
	@Override
	public PageableResult doGetAll(RequestContext context) throws ResponseException {
		return null;
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) throws ResponseException {
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription d = new DelegatingResourceDescription();
		
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			d.addProperty("reportUuid");
			d.addProperty("reportDefinitionUuid");
			d.addProperty("reportDefinitionName");
			d.addProperty("reportDesignPath");
			d.addProperty("compiled");
		}
		
		return d;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription d = new DelegatingResourceDescription();
		d.addRequiredProperty("reportUuid");
		return d;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		return null;
	}
}
