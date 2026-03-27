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
public interface ReportBuilderService extends OpenmrsService {
	
	/** Returns HTML as a string for preview/printing */
	public String renderHtmlFromJsonTemplate(ReportDesign reportDesign);
	
	/** Returns payload JSON as a string (no JsonNode leaks) */
	public String createPayloadJsonFromTemplate(ReportData reportData, ReportDesign reportDesign, String renderType,
	        Map<String, Object> flatValues, String remapJsonOptional);
	
	public String buildPayloadJson(ReportData reportData, ReportDesign reportDesign, String renderType);
	
	public String buildFinalPayloadJson(ReportData reportData, ReportDesign reportDesign, String renderType, Date endDate);
	
	public String buildPreviewHtml(ReportData reportData, ReportDesign reportDesign);
	
	public String buildRenderedOutput(ReportData reportData, ReportDesign reportDesign, String remapJsonOptional);
	
	// =========================
	// Indicator
	// =========================
	ReportBuilderIndicator saveReportBuilderIndicator(ReportBuilderIndicator indicator);
	
	ReportBuilderIndicator getReportBuilderIndicatorById(Integer id);
	
	ReportBuilderIndicator getReportBuilderIndicatorByUuid(String uuid);
	
	ReportBuilderIndicator getReportBuilderIndicatorByCode(String code);
	
	List<ReportBuilderIndicator> searchReportBuilderIndicators(String q, ReportBuilderIndicator.Kind kind,
	        boolean includeRetired, Integer startIndex, Integer limit);
	
	public List<ReportBuilderIndicator> getAllReportBuilderIndicator(Integer startIndex, Integer limit);
	
	public List<ReportBuilderIndicator> getReportBuilderIndicators(ReportBuilderIndicator.Kind kind, boolean includeRetired,
	        Integer startIndex, Integer limit);
	
	long getReportBuilderIndicatorsCount(String q, ReportBuilderIndicator.Kind kind, boolean includeRetired);
	
	void retireReportBuilderIndicator(ReportBuilderIndicator indicator, String reason);
	
	void unretireReportBuilderIndicator(ReportBuilderIndicator indicator);
	
	void purgeReportBuilderIndicator(ReportBuilderIndicator indicator);
	
	// =========================
	// Section
	// =========================
	ReportBuilderSection saveReportBuilderSection(ReportBuilderSection section);
	
	ReportBuilderSection getReportBuilderSectionById(Integer id);
	
	ReportBuilderSection getReportBuilderSectionByUuid(String uuid);
	
	ReportBuilderSection getReportBuilderSectionByCode(String code);
	
	List<ReportBuilderSection> getReportBuilderSections(String q, boolean includeRetired, Integer startIndex, Integer limit);
	
	long getReportBuilderSectionsCount(String q, boolean includeRetired);
	
	void retireReportBuilderSection(ReportBuilderSection section, String reason);
	
	void unretireReportBuilderSection(ReportBuilderSection section);
	
	void purgeReportBuilderSection(ReportBuilderSection section);
	
	// =========================
	// DataTheme
	// =========================
	ReportBuilderDataTheme saveReportBuilderDataTheme(ReportBuilderDataTheme theme);
	
	ReportBuilderDataTheme getReportBuilderDataThemeById(Integer id);
	
	ReportBuilderDataTheme getReportBuilderDataThemeByUuid(String uuid);
	
	ReportBuilderDataTheme getReportBuilderDataThemeByCode(String code);
	
	List<ReportBuilderDataTheme> getReportBuilderDataThemes(String q, boolean includeRetired, Integer startIndex,
	        Integer limit);
	
	long getReportBuilderDataThemesCount(String q, boolean includeRetired);
	
	void retireReportBuilderDataTheme(ReportBuilderDataTheme theme, String reason);
	
	void unretireReportBuilderDataTheme(ReportBuilderDataTheme theme);
	
	void purgeReportBuilderDataTheme(ReportBuilderDataTheme theme);
	
	List<String> getETLTables();
	
	public List<Map> getETLTableColumns(String tableName);
	
	// Categories
	ReportBuilderAgeCategory saveAgeCategory(ReportBuilderAgeCategory category);
	
	ReportBuilderAgeCategory getAgeCategoryByUuid(String uuid);
	
	ReportBuilderAgeCategory getAgeCategoryByCode(String code);
	
	List<ReportBuilderAgeCategory> getAgeCategories(String q, boolean includeRetired, Boolean activeOnly,
	        Integer startIndex, Integer limit);
	
	long getAgeCategoriesCount(String q, boolean includeRetired, Boolean activeOnly);
	
	void retireAgeCategory(ReportBuilderAgeCategory category, String reason);
	
	void unretireAgeCategory(ReportBuilderAgeCategory category);
	
	void purgeAgeCategory(ReportBuilderAgeCategory category);
	
	// Groups
	ReportBuilderAgeGroup saveAgeGroup(ReportBuilderAgeGroup group);
	
	ReportBuilderAgeGroup getAgeGroupById(Integer id);
	
	List<ReportBuilderAgeGroup> getAgeGroupsByCategoryUuid(String categoryUuid, Boolean activeOnly);
	
	List<ReportBuilderAgeGroup> getAgeGroupsByCategoryCode(String categoryCode, Boolean activeOnly);
	
	void purgeAgeGroup(ReportBuilderAgeGroup group);
	
	List<ReportBuilderAgeGroup> getAgeGroups(String q, ReportBuilderAgeCategory category, Boolean activeOnly,
	        Integer startIndex, Integer limit);
	
	SqlPreviewResult previewSql(String sql, Map<String, Object> params, Integer maxRows);
	
	@Transactional
	public ReportBuilderReport saveReportBuilderReport(ReportBuilderReport report);
	
	@Transactional(readOnly = true)
	public ReportBuilderReport getReportBuilderReportByUuid(String uuid);
	
	@Transactional(readOnly = true)
	public List<ReportBuilderReport> getReportBuilderReports(String q, boolean includeRetired, Integer startIndex,
	        Integer limit);
	
	@Transactional
	public void retireReportBuilderReport(ReportBuilderReport report, String reason);
	
	@Transactional
	public void purgeReportBuilderReport(ReportBuilderReport report);
	
	@Transactional
	CompiledReportArtifacts compileReport(String reportBuilderReportUuid);
	
	@Transactional
	public ReportCategory saveReportCategory(ReportCategory category);
	
	@Transactional(readOnly = true)
	public ReportCategory getReportCategoryById(Integer id);
	
	@Transactional(readOnly = true)
	public ReportCategory getReportCategoryByUuid(String uuid);
	
	@Transactional(readOnly = true)
	public List<ReportCategory> getReportCategories(String q, boolean includeRetired, Integer startIndex, Integer limit);
	
	@Transactional(readOnly = true)
	public long getReportCategoriesCount(String q, boolean includeRetired);
	
	@Transactional
	public void retireReportCategory(ReportCategory category, String reason);
	
	@Transactional
	public void unretireReportCategory(ReportCategory category);
	
	@Transactional
	public void purgeReportCategory(ReportCategory category);
	
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
	public List<String> getAllowedTablePrefixes();
	
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
