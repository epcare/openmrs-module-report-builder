package org.openmrs.module.reportbuilder.web.controller;

import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/reportbuilder")
public class ReportBuilderResourceController extends MainResourceController {
	
	@Override
	public String getNamespace() {
		return RestConstants.VERSION_1 + "/reportbuilder";
	}
}
