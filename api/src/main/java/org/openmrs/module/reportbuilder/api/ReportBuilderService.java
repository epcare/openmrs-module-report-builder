/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reportbuilder.api;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.reportbuilder.dto.SqlPreviewResult;
import org.openmrs.module.reportbuilder.model.*;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
@Transactional
public interface ReportBuilderService extends OpenmrsService {
	
	/** Returns HTML as a string for preview/printing */
	@Transactional(readOnly = true)
	String renderHtmlFromJsonTemplate(ReportDesign reportDesign);
	
	/** Returns payload JSON as a string (no JsonNode leaks) */
	@Transactional(readOnly = true)
	String createPayloadJsonFromTemplate(ReportData reportData, ReportDesign reportDesign, String renderType,
	        Map<String, Object> flatValues, String remapJsonOptional);
	
	@Transactional(readOnly = true)
	String buildPayloadJson(ReportData reportData, ReportDesign reportDesign, String renderType);
	
	@Transactional(readOnly = true)
	String buildFinalPayloadJson(ReportData reportData, ReportDesign reportDesign, String renderType, Date endDate);
	
	@Transactional(readOnly = true)
	String buildPreviewHtml(ReportData reportData, ReportDesign reportDesign);
	
	@Transactional(readOnly = true)
	String buildRenderedOutput(ReportData reportData, ReportDesign reportDesign, String remapJsonOptional);
	
	// =========================
	// Indicator
	// =========================
	@Transactional
	ReportBuilderIndicator saveReportBuilderIndicator(ReportBuilderIndicator indicator);
	
	@Transactional(readOnly = true)
	ReportBuilderIndicator getReportBuilderIndicatorById(Integer id);
	
	@Transactional(readOnly = true)
	ReportBuilderIndicator getReportBuilderIndicatorByUuid(String uuid);
	
	@Transactional(readOnly = true)
	ReportBuilderIndicator getReportBuilderIndicatorByCode(String code);
	
	@Transactional(readOnly = true)
	List<ReportBuilderIndicator> searchReportBuilderIndicators(String q, ReportBuilderIndicator.Kind kind,
	        boolean includeRetired, Integer startIndex, Integer limit);
	
	@Transactional(readOnly = true)
	List<ReportBuilderIndicator> getAllReportBuilderIndicator(Integer startIndex, Integer limit);
	
	@Transactional(readOnly = true)
	List<ReportBuilderIndicator> getReportBuilderIndicators(ReportBuilderIndicator.Kind kind, boolean includeRetired,
	        Integer startIndex, Integer limit);
	
	@Transactional(readOnly = true)
	long getReportBuilderIndicatorsCount(String q, ReportBuilderIndicator.Kind kind, boolean includeRetired);
	
	@Transactional
	void retireReportBuilderIndicator(ReportBuilderIndicator indicator, String reason);
	
	@Transactional
	void unretireReportBuilderIndicator(ReportBuilderIndicator indicator);
	
	@Transactional
	void purgeReportBuilderIndicator(ReportBuilderIndicator indicator);
	
	// =========================
	// Section
	// =========================
	@Transactional
	ReportBuilderSection saveReportBuilderSection(ReportBuilderSection section);
	
	@Transactional(readOnly = true)
	ReportBuilderSection getReportBuilderSectionById(Integer id);
	
	@Transactional(readOnly = true)
	ReportBuilderSection getReportBuilderSectionByUuid(String uuid);
	
	@Transactional(readOnly = true)
	ReportBuilderSection getReportBuilderSectionByCode(String code);
	
	@Transactional(readOnly = true)
	List<ReportBuilderSection> getReportBuilderSections(String q, boolean includeRetired, Integer startIndex, Integer limit);
	
	@Transactional(readOnly = true)
	long getReportBuilderSectionsCount(String q, boolean includeRetired);
	
	@Transactional
	void retireReportBuilderSection(ReportBuilderSection section, String reason);
	
	@Transactional
	void unretireReportBuilderSection(ReportBuilderSection section);
	
	@Transactional
	void purgeReportBuilderSection(ReportBuilderSection section);
	
	// =========================
	// DataTheme
	// =========================
	@Transactional
	ReportBuilderDataTheme saveReportBuilderDataTheme(ReportBuilderDataTheme theme);
	
	@Transactional(readOnly = true)
	ReportBuilderDataTheme getReportBuilderDataThemeById(Integer id);
	
	@Transactional(readOnly = true)
	ReportBuilderDataTheme getReportBuilderDataThemeByUuid(String uuid);
	
	@Transactional(readOnly = true)
	ReportBuilderDataTheme getReportBuilderDataThemeByCode(String code);
	
	@Transactional(readOnly = true)
	List<ReportBuilderDataTheme> getReportBuilderDataThemes(String q, boolean includeRetired, Integer startIndex,
	        Integer limit);
	
	@Transactional(readOnly = true)
	long getReportBuilderDataThemesCount(String q, boolean includeRetired);
	
	@Transactional
	void retireReportBuilderDataTheme(ReportBuilderDataTheme theme, String reason);
	
	@Transactional
	void unretireReportBuilderDataTheme(ReportBuilderDataTheme theme);
	
	@Transactional
	void purgeReportBuilderDataTheme(ReportBuilderDataTheme theme);
	
	@Transactional(readOnly = true)
	List<String> getETLTables();
	
	@Transactional(readOnly = true)
	List<Map> getETLTableColumns(String tableName);
	
	// Categories
	@Transactional
	ReportBuilderAgeCategory saveAgeCategory(ReportBuilderAgeCategory category);
	
	@Transactional(readOnly = true)
	ReportBuilderAgeCategory getAgeCategoryByUuid(String uuid);
	
	@Transactional(readOnly = true)
	ReportBuilderAgeCategory getAgeCategoryByCode(String code);
	
	@Transactional(readOnly = true)
	List<ReportBuilderAgeCategory> getAgeCategories(String q, boolean includeRetired, Boolean activeOnly,
	        Integer startIndex, Integer limit);
	
	@Transactional(readOnly = true)
	long getAgeCategoriesCount(String q, boolean includeRetired, Boolean activeOnly);
	
	@Transactional
	void retireAgeCategory(ReportBuilderAgeCategory category, String reason);
	
	@Transactional
	void unretireAgeCategory(ReportBuilderAgeCategory category);
	
	@Transactional
	void purgeAgeCategory(ReportBuilderAgeCategory category);
	
	// Groups
	@Transactional
	ReportBuilderAgeGroup saveAgeGroup(ReportBuilderAgeGroup group);
	
	@Transactional(readOnly = true)
	ReportBuilderAgeGroup getAgeGroupById(Integer id);
	
	@Transactional(readOnly = true)
	List<ReportBuilderAgeGroup> getAgeGroupsByCategoryUuid(String categoryUuid, Boolean activeOnly);
	
	@Transactional(readOnly = true)
	List<ReportBuilderAgeGroup> getAgeGroupsByCategoryCode(String categoryCode, Boolean activeOnly);
	
	@Transactional
	void purgeAgeGroup(ReportBuilderAgeGroup group);
	
	@Transactional(readOnly = true)
	List<ReportBuilderAgeGroup> getAgeGroups(String q, ReportBuilderAgeCategory category, Boolean activeOnly,
	        Integer startIndex, Integer limit);
	
	@Transactional(readOnly = true)
	SqlPreviewResult previewSql(String sql, Map<String, Object> params, Integer maxRows);
	
	@Transactional
	ReportBuilderReport saveReportBuilderReport(ReportBuilderReport report);
	
	@Transactional(readOnly = true)
	ReportBuilderReport getReportBuilderReportByUuid(String uuid);
	
	@Transactional(readOnly = true)
	List<ReportBuilderReport> getReportBuilderReports(String q, boolean includeRetired, Integer startIndex, Integer limit);
	
	@Transactional
	void retireReportBuilderReport(ReportBuilderReport report, String reason);
	
	@Transactional
	void purgeReportBuilderReport(ReportBuilderReport report);
	
	@Transactional
	CompiledReportArtifacts compileReport(String reportBuilderReportUuid);
	
	@Transactional
	ReportCategory saveReportCategory(ReportCategory category);
	
	@Transactional(readOnly = true)
	ReportCategory getReportCategoryById(Integer id);
	
	@Transactional(readOnly = true)
	ReportCategory getReportCategoryByUuid(String uuid);
	
	@Transactional(readOnly = true)
	List<ReportCategory> getReportCategories(String q, boolean includeRetired, Integer startIndex, Integer limit);
	
	@Transactional(readOnly = true)
	long getReportCategoriesCount(String q, boolean includeRetired);
	
	@Transactional
	void retireReportCategory(ReportCategory category, String reason);
	
	@Transactional
	void unretireReportCategory(ReportCategory category);
	
	@Transactional
	void purgeReportCategory(ReportCategory category);
	
	@Transactional
	ReportLibrary saveReportLibrary(ReportLibrary reportLibrary);
	
	@Transactional(readOnly = true)
	ReportLibrary getReportLibraryById(Integer id);
	
	@Transactional(readOnly = true)
	ReportLibrary getReportLibraryByUuid(String uuid);
	
	@Transactional(readOnly = true)
	List<ReportLibrary> getReportLibraries(String q, boolean includeRetired, Integer startIndex, Integer limit);
	
	@Transactional(readOnly = true)
	long getReportLibrariesCount(String q, boolean includeRetired);
	
	@Transactional
	void retireReportLibrary(ReportLibrary reportLibrary, String reason);
	
	@Transactional
	void unretireReportLibrary(ReportLibrary reportLibrary);
	
	@Transactional
	void purgeReportLibrary(ReportLibrary reportLibrary);
	
	@Transactional
	ETLSource saveETLSource(ETLSource etlSource);
	
	@Transactional(readOnly = true)
	ETLSource getETLSourceByUuid(String uuid);
	
	@Transactional(readOnly = true)
	ETLSource getETLSourceById(Integer id);
	
	@Transactional(readOnly = true)
	List<ETLSource> getAllETLSources(boolean includeRetired);
	
	@Transactional
	void retireETLSource(ETLSource etlSource, String retireReason);
	
	@Transactional(readOnly = true)
	List<String> getAllowedTablePrefixes();
	
	class CompiledReportArtifacts {
		
		private ReportBuilderReport reportBuilderReport;
		
		private ReportDefinition reportDefinition;
		
		private File reportDesignFile;
		
		private String compiledJson;
		
		public ReportBuilderReport getReportBuilderReport() {
			return reportBuilderReport;
		}
		
		public void setReportBuilderReport(ReportBuilderReport reportBuilderReport) {
			this.reportBuilderReport = reportBuilderReport;
		}
		
		public ReportDefinition getReportDefinition() {
			return reportDefinition;
		}
		
		public void setReportDefinition(ReportDefinition reportDefinition) {
			this.reportDefinition = reportDefinition;
		}
		
		public File getReportDesignFile() {
			return reportDesignFile;
		}
		
		public void setReportDesignFile(File reportDesignFile) {
			this.reportDesignFile = reportDesignFile;
		}
		
		public String getCompiledJson() {
			return compiledJson;
		}
		
		public void setCompiledJson(String compiledJson) {
			this.compiledJson = compiledJson;
		}
	}
}
