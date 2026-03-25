package org.openmrs.module.reportbuilder.web.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.reportbuilder.api.ReportBuilderService;
import org.openmrs.module.reportbuilder.model.ReportCategory;
import org.openmrs.module.reportbuilder.model.ReportLibrary;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;

import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/reportbuilder/reportlibrary", supportedClass = ReportLibrary.class, supportedOpenmrsVersions = { "1.8 - 9.0.*" })
public class ReportLibraryResource extends DelegatingCrudResource<ReportLibrary> {
	
	@Override
	public ReportLibrary newDelegate() {
		return new ReportLibrary();
	}
	
	@Override
	public ReportLibrary save(ReportLibrary delegate) {
		return Context.getService(ReportBuilderService.class).saveReportLibrary(delegate);
	}
	
	@Override
	public ReportLibrary getByUniqueId(String uniqueId) {
		ReportBuilderService service = Context.getService(ReportBuilderService.class);
		
		ReportLibrary byUuid = service.getReportLibraryByUuid(uniqueId);
		if (byUuid != null) {
			return byUuid;
		}
		
		try {
			return service.getReportLibraryById(Integer.valueOf(uniqueId));
		}
		catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public void delete(ReportLibrary delegate, String reason, RequestContext context) {
		Context.getService(ReportBuilderService.class).retireReportLibrary(delegate, reason);
	}
	
	@Override
	public void purge(ReportLibrary delegate, RequestContext context) {
		Context.getService(ReportBuilderService.class).purgeReportLibrary(delegate);
	}
	
	public ReportLibrary getById(String id) {
		try {
			return Context.getService(ReportBuilderService.class).getReportLibraryById(Integer.valueOf(id));
		}
		catch (Exception e) {
			return null;
		}
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		ReportBuilderService service = Context.getService(ReportBuilderService.class);
		
		String q = context.getParameter("q");
		boolean includeAll = context.getIncludeAll();
		Integer startIndex = context.getStartIndex() != null ? context.getStartIndex() : 0;
		Integer limit = context.getLimit() != null ? context.getLimit() : 50;
		
		List<ReportLibrary> results = service.getReportLibraries(q, includeAll, startIndex, limit);
		long count = service.getReportLibrariesCount(q, includeAll);
		
		return new AlreadyPaged<ReportLibrary>(context, results, false, count);
	}
	
	@Override
	public NeedsPaging<ReportLibrary> doGetAll(RequestContext context) {
		ReportBuilderService service = Context.getService(ReportBuilderService.class);
		
		boolean includeAll = context.getIncludeAll();
		Integer startIndex = context.getStartIndex() != null ? context.getStartIndex() : 0;
		Integer limit = context.getLimit() != null ? context.getLimit() : 50;
		
		List<ReportLibrary> results = service.getReportLibraries(null, includeAll, startIndex, limit);
		return new NeedsPaging<ReportLibrary>(results, context);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription d = new DelegatingResourceDescription();
		
		if (rep instanceof DefaultRepresentation) {
			d.addProperty("uuid");
			d.addProperty("name");
			d.addProperty("description");
			d.addProperty("code");
			d.addProperty("sourceType");
			d.addProperty("reportType");
			d.addProperty("migrated");
			d.addProperty("retired");
			d.addProperty("category");
			d.addSelfLink();
			d.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return d;
		}
		
		if (rep instanceof FullRepresentation) {
			d.addProperty("uuid");
			d.addProperty("name");
			d.addProperty("description");
			d.addProperty("code");
			d.addProperty("sourceType");
			d.addProperty("reportDefinitionUuid");
			d.addProperty("reportBuilderReportUuid");
			d.addProperty("reportType");
			d.addProperty("migrated");
			d.addProperty("retired");
			d.addProperty("category");
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
		d.addProperty("description");
		d.addProperty("code");
		d.addProperty("sourceType");
		d.addProperty("reportDefinitionUuid");
		d.addProperty("reportBuilderReportUuid");
		d.addProperty("reportType");
		d.addProperty("migrated");
		d.addProperty("category");
		return d;
	}
	
	@PropertySetter("category")
	public static void setCategory(ReportLibrary instance, Object value) {
		if (value == null) {
			instance.setCategory(null);
			return;
		}
		
		ReportBuilderService service = Context.getService(ReportBuilderService.class);
		ReportCategory category = null;
		
		if (value instanceof ReportCategory) {
			category = (ReportCategory) value;
		} else {
			String asString = String.valueOf(value).trim();
			
			if (!asString.isEmpty()) {
				category = service.getReportCategoryByUuid(asString);
				if (category == null) {
					try {
						category = service.getReportCategoryById(Integer.valueOf(asString));
					}
					catch (Exception ignored) {}
				}
			}
		}
		
		instance.setCategory(category);
	}
}
