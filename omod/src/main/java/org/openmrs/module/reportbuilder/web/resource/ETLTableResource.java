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

@Resource(name = RestConstants.VERSION_1 + "/etltable", supportedClass = ETLTableResource.ETLTable.class, supportedOpenmrsVersions = {
        "2.*", "3.*" })
public class ETLTableResource extends DelegatingCrudResource<ETLTableResource.ETLTable> {
	
	public static class ETLTable {
		
		private String uuid; // synthetic, not persisted
		
		private String name;
		
		public ETLTable() {
		}
		
		public ETLTable(String name) {
			this.name = name;
			// stable-enough synthetic uuid derived from name
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
	public ETLTable newDelegate() {
		return new ETLTable();
	}
	
	@Override
	public ETLTable save(ETLTable delegate) {
		// Read-only resource
		throw new UnsupportedOperationException("etltable is read-only");
	}
	
	@Override
	public ETLTable getByUniqueId(String uniqueId) {
		// optional: not needed for UI; return null for now
		return null;
	}
	
	@Override
	protected void delete(ETLTable delegate, String reason, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("etltable is read-only");
	}
	
	@Override
	public void purge(ETLTable delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("etltable is read-only");
	}
	
	@Override
    public PageableResult doGetAll(RequestContext context) throws ResponseException {
        List<String> names = service().getETLTables(); // from DAO query INFORMATION_SCHEMA

        List<ETLTable> rows = new ArrayList<>();
        for (String n : names) {
            rows.add(new ETLTable(n));
        }

        // NeedsPaging will apply startIndex & limit automatically
        return new NeedsPaging<>(rows, context);
    }
	
	@Override
    protected PageableResult doSearch(RequestContext context) throws ResponseException {
        // optional: allow q filter on table name
        String q = context.getParameter("q");
        List<String> names = service().getETLTables();

        List<ETLTable> rows = new ArrayList<>();
        for (String n : names) {
            if (q == null || q.trim().isEmpty() || n.toLowerCase().contains(q.toLowerCase())) {
                rows.add(new ETLTable(n));
            }
        }
        return new NeedsPaging<>(rows, context);
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
		return null; // read-only
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		return null; // read-only
	}
	
	public String getDisplayString(ETLTable obj) {
		return obj.getName();
	}
}
