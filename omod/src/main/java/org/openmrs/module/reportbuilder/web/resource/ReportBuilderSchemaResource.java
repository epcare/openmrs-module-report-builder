package org.openmrs.module.reportbuilder.web.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.reportbuilder.api.ReportBuilderService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Listable schema resource for configured ETL source tables. GET /ws/rest/v1/reportbuilder/schema
 * GET /ws/rest/v1/reportbuilder/schema?q=dim
 */
@Resource(name = RestConstants.VERSION_1 + "/reportbuilder/schema", supportedClass = ReportBuilderSchemaResource.ETLTableRef.class, supportedOpenmrsVersions = { "1.8 - 9.0.*" })
public class ReportBuilderSchemaResource extends DelegatingCrudResource<ReportBuilderSchemaResource.ETLTableRef> {
	
	/**
	 * RESTWS likes delegates that have a uuid. This is not persisted; it's a synthetic uuid derived
	 * from table name.
	 */
	public static class ETLTableRef {
		
		private String uuid;
		
		private String name;
		
		public ETLTableRef() {
		}
		
		public ETLTableRef(String name) {
			this.name = name;
			this.uuid = UUID.nameUUIDFromBytes(("etl-table:" + name).getBytes()).toString();
		}
		
		public String getUuid() {
			return uuid;
		}
		
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
	}
	
	private ReportBuilderService service() {
		return Context.getService(ReportBuilderService.class);
	}
	
	@Override
	public ETLTableRef newDelegate() {
		return new ETLTableRef();
	}
	
	@Override
	public ETLTableRef save(ETLTableRef delegate) {
		throw new UnsupportedOperationException("Read-only resource");
	}
	
	@Override
	public ETLTableRef getByUniqueId(String uniqueId) {
		// Not needed for list usage; keep null or implement lookup by synthetic uuid if you want
		return null;
	}
	
	@Override
	protected void delete(ETLTableRef delegate, String reason, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Read-only resource");
	}
	
	@Override
	public void purge(ETLTableRef delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Read-only resource");
	}
	
	@Override
    public PageableResult doGetAll(RequestContext context) throws ResponseException {

        String q = context.getParameter("q"); // optional filter on table name
        List<String> tables = service().getETLTables();

        List<ETLTableRef> results = new ArrayList<>();
        for (String t : tables) {
            if (q == null || q.trim().isEmpty() || t.toLowerCase().contains(q.toLowerCase())) {
                results.add(new ETLTableRef(t));
            }
        }

        return new NeedsPaging<>(results, context);
    }
	
	@Override
	protected PageableResult doSearch(RequestContext context) throws ResponseException {
		// same as doGetAll for this utility
		return doGetAll(context);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription d = new DelegatingResourceDescription();
		d.addProperty("uuid");
		d.addProperty("name");
		return d;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		return null;
	}
	
	public String getDisplayString(ETLTableRef obj) {
		return obj.getName();
	}
}
