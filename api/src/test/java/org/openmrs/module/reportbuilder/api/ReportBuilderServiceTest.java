package org.openmrs.module.reportbuilder.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.reportbuilder.api.impl.ReportBuilderServiceImpl;
import org.openmrs.module.reportbuilder.model.ETLSource;
import org.openmrs.module.reportbuilder.model.ReportBuilderIndicator;
import org.openmrs.module.reportbuilder.model.ReportBuilderReport;
import org.openmrs.module.reportbuilder.model.ReportBuilderSection;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.List;

public class ReportBuilderServiceTest extends BaseModuleContextSensitiveTest {
	
	private ReportBuilderService service;
	
	@Before
	public void setup() throws Exception {
		service = Context.getService(ReportBuilderService.class);
	}
	
	@Test
	public void shouldLoadReportBuilderService() {
		Assert.assertNotNull(service);
	}
	
	@Test
	public void removeQuotesFromValues_shouldConvertQuotedNumericValueToNumber() {
		String input = "{\"value\":\"123\"}";
		String output = ReportBuilderServiceImpl.removeQuotesFromValues(input);
		Assert.assertEquals("{\"value\":123}", output);
	}
	
	@Test
	public void saveReportBuilderReport_shouldAssignUuidWhenMissing() {
		ReportBuilderReport report = new ReportBuilderReport();
		report.setName("Test Report");
		report.setCode("TEST_REPORT");
		
		ReportBuilderReport saved = service.saveReportBuilderReport(report);
		
		Assert.assertNotNull(saved);
		Assert.assertNotNull(saved.getUuid());
	}
	
	@Test
	public void saveReportBuilderReport_shouldKeepExistingUuid() {
		ReportBuilderReport report = new ReportBuilderReport();
		report.setUuid("existing-uuid");
		report.setName("Existing UUID Report");
		report.setCode("EXISTING_UUID_REPORT");
		
		ReportBuilderReport saved = service.saveReportBuilderReport(report);
		
		Assert.assertNotNull(saved);
		Assert.assertEquals("existing-uuid", saved.getUuid());
	}
	
	@Test
	public void retireETLSource_shouldMarkRetiredAndSave() {
		ETLSource source = new ETLSource();
		source.setName("Test ETL Source");
		source.setCode("TEST_ETL_SOURCE");
		source.setActive(true);
		source.setTablePatterns("etl_,obs_");
		
		ETLSource saved = service.saveETLSource(source);
		service.retireETLSource(saved, "test reason");
		
		ETLSource fetched = service.getETLSourceByUuid(saved.getUuid());
		
		Assert.assertNotNull(fetched);
		Assert.assertTrue(fetched.getRetired());
		Assert.assertEquals("test reason", fetched.getRetireReason());
	}
	
	@Test
	public void getAllowedTablePrefixes_shouldReturnOnlyActiveTrimmedPatterns() {
		ETLSource active = new ETLSource();
		active.setName("Active Source");
		active.setCode("ACTIVE_SOURCE");
		active.setActive(true);
		active.setTablePatterns("etl_, obs_ ,  patient_");
		service.saveETLSource(active);
		
		ETLSource inactive = new ETLSource();
		inactive.setName("Inactive Source");
		inactive.setCode("INACTIVE_SOURCE");
		inactive.setActive(false);
		inactive.setTablePatterns("ignore_");
		service.saveETLSource(inactive);
		
		List<String> prefixes = service.getAllowedTablePrefixes();
		
		Assert.assertTrue(prefixes.contains("etl_"));
		Assert.assertTrue(prefixes.contains("obs_"));
		Assert.assertTrue(prefixes.contains("patient_"));
		Assert.assertFalse(prefixes.contains("ignore_"));
	}
	
	@Test
	public void retireReportBuilderIndicator_shouldMarkRetiredAndSave() {
		ReportBuilderIndicator indicator = new ReportBuilderIndicator();
		indicator.setName("Test Indicator");
		indicator.setCode("TEST_INDICATOR");
		indicator.setKind(ReportBuilderIndicator.Kind.BASE);
		indicator.setConfigJson("{}");
		
		ReportBuilderIndicator saved = service.saveReportBuilderIndicator(indicator);
		service.retireReportBuilderIndicator(saved, "retired for test");
		
		ReportBuilderIndicator fetched = service.getReportBuilderIndicatorByUuid(saved.getUuid());
		
		Assert.assertNotNull(fetched);
		Assert.assertTrue(fetched.getRetired());
		Assert.assertEquals("retired for test", fetched.getRetireReason());
	}
	
	@Test
	public void unretireReportBuilderIndicator_shouldClearRetiredAndReasonAndSave() {
		ReportBuilderIndicator indicator = new ReportBuilderIndicator();
		indicator.setName("Indicator To Unretire");
		indicator.setCode("UNRETIRE_INDICATOR");
		indicator.setKind(ReportBuilderIndicator.Kind.BASE);
		indicator.setConfigJson("{}");
		
		ReportBuilderIndicator saved = service.saveReportBuilderIndicator(indicator);
		service.retireReportBuilderIndicator(saved, "old");
		service.unretireReportBuilderIndicator(saved);
		
		ReportBuilderIndicator fetched = service.getReportBuilderIndicatorByUuid(saved.getUuid());
		
		Assert.assertNotNull(fetched);
		Assert.assertFalse(fetched.getRetired());
		Assert.assertNull(fetched.getRetireReason());
	}
	
	@Test
	public void retireReportBuilderSection_shouldMarkRetiredAndSetReason() {
		ReportBuilderSection section = new ReportBuilderSection();
		section.setName("Test Section");
		section.setCode("TEST_SECTION");
		
		ReportBuilderSection saved = service.saveReportBuilderSection(section);
		service.retireReportBuilderSection(saved, "section retired");
		
		ReportBuilderSection fetched = service.getReportBuilderSectionByUuid(saved.getUuid());
		
		Assert.assertNotNull(fetched);
		Assert.assertTrue(fetched.getRetired());
		Assert.assertEquals("section retired", fetched.getRetireReason());
	}
}
