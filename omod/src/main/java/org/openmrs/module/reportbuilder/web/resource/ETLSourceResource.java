package org.openmrs.module.reportbuilder.web.resource;

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.reportbuilder.api.ReportBuilderService;
import org.openmrs.module.reportbuilder.model.ETLSource;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

@Resource(name = RestConstants.VERSION_1 + "/reportbuilder/etlsource", supportedClass = ETLSource.class, supportedOpenmrsVersions = { "1.8 - 9.0.*" })
public class ETLSourceResource extends DelegatingCrudResource<ETLSource> {
	
	@Override
	public ETLSource newDelegate() {
		return new ETLSource();
	}
	
	@Override
	public ETLSource save(ETLSource delegate) {
		return Context.getService(ReportBuilderService.class).saveETLSource(delegate);
	}
	
	@Override
	public ETLSource getByUniqueId(String uniqueId) {
		ReportBuilderService service = Context.getService(ReportBuilderService.class);
		
		ETLSource etlSource = service.getETLSourceByUuid(uniqueId);
		if (etlSource != null) {
			return etlSource;
		}
		
		try {
			return service.getETLSourceById(Integer.valueOf(uniqueId));
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	
	public ETLSource getById(String id) {
		try {
			return Context.getService(ReportBuilderService.class).getETLSourceById(Integer.valueOf(id));
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) {
		List<ETLSource> etlSources = Context.getService(ReportBuilderService.class).getAllETLSources(false);
		return new AlreadyPaged<ETLSource>(context, etlSources, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		throw new UnsupportedOperationException("Searching not supported for etlsource");
	}
	
	@Override
	public void delete(ETLSource delegate, String reason, RequestContext context) {
		Context.getService(ReportBuilderService.class).retireETLSource(delegate, reason);
	}
	
	@Override
	public void purge(ETLSource delegate, RequestContext context) {
		throw new UnsupportedOperationException("Delete not supported for etlsource: " + delegate.getName());
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription d = new DelegatingResourceDescription();
		
		if (rep instanceof DefaultRepresentation) {
			d.addProperty("uuid");
			d.addProperty("name");
			d.addProperty("code");
			d.addProperty("description");
			d.addProperty("tablePatterns");
			d.addProperty("schemaName");
			d.addProperty("sourceType");
			d.addProperty("active");
			d.addProperty("retired");
			d.addSelfLink();
			d.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return d;
		}
		
		if (rep instanceof FullRepresentation) {
			d.addProperty("uuid");
			d.addProperty("name");
			d.addProperty("code");
			d.addProperty("description");
			d.addProperty("tablePatterns");
			d.addProperty("schemaName");
			d.addProperty("sourceType");
			d.addProperty("active");
			d.addProperty("retired");
			d.addProperty("auditInfo");
			d.addSelfLink();
			return d;
		}
		
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription d = new DelegatingResourceDescription();
		d.addRequiredProperty("name");
		d.addProperty("code");
		d.addProperty("description");
		d.addProperty("tablePatterns");
		d.addProperty("schemaName");
		d.addProperty("sourceType");
		d.addProperty("active");
		return d;
	}
}
