/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.reportbuilder.activator;

import org.openmrs.module.BaseModuleActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class ReportBuilderActivator extends BaseModuleActivator {
	
	private static final Logger log = LoggerFactory.getLogger(org.openmrs.module.reportbuilder.ReportBuilderActivator.class);
	
	@Override
	public void started() {
		log.info("Report builder module started - initializing...");
	}
	
	@Override
	public void stopped() {
		log.info("Report builder module stopped");
	}
	
}
