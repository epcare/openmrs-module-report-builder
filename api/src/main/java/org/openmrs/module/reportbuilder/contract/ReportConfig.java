/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reportbuilder.contract;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark Java classes as report definitions that can be serialized to/from JSON
 * configuration. This establishes the contract between Java code and JSON configuration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReportConfig {
	
	/**
	 * Unique identifier for this report
	 */
	String uuid();
	
	/**
	 * Human-readable name
	 */
	String name();
	
	/**
	 * Detailed description
	 */
	String description() default "";
	
	/**
	 * Report type (LINE_LIST or AGGREGATE)
	 */
	ReportType reportType() default ReportType.LINE_LIST;
	
	/**
	 * Report category for grouping
	 */
	String category() default "general";
	
	/**
	 * Tags for searching and filtering
	 */
	String[] tags() default {};
	
	/**
	 * JSON configuration file path (optional, defaults to class name)
	 */
	String jsonFile() default "";
	
	/**
	 * Whether this report is retired/deprecated
	 */
	boolean retired() default false;
	
	/**
	 * Whether to use JSON configuration instead of Java construction
	 */
	boolean useJsonConfig() default true;
	
	/**
	 * Report type enumeration
	 */
	enum ReportType {
		LINE_LIST, AGGREGATE
	}
}
