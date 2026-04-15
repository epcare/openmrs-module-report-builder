package org.openmrs.module.reportbuilder.legacyconfig.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.module.reportbuilder.legacyconfig.model.ReportConfig;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.ExcelTemplateRenderer;
import org.openmrs.module.reporting.report.renderer.TextTemplateRenderer;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class DesignBuilder {
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	public List<ReportDesign> build(ReportConfig config, ReportDefinition reportDefinition, File reportDir) throws Exception {
		List<ReportDesign> designs = new ArrayList<ReportDesign>();
		
		if (config == null) {
			throw new IllegalArgumentException("Report config is required");
		}
		if (reportDefinition == null) {
			throw new IllegalArgumentException("Report definition is required");
		}
		if (reportDir == null) {
			throw new IllegalArgumentException("Report directory is required");
		}
		
		if (hasValue(config.getExcelTemplate())) {
			designs.add(buildExcelDesign(config, reportDefinition, reportDir));
		}
		
		if (hasEmbeddedJsonTemplate(config) || hasValue(config.getJsonTemplate())) {
			designs.add(buildJsonDesign(config, reportDefinition, reportDir));
		}
		
		return designs;
	}
	
	public ReportDesign buildExcelDesign(ReportConfig config, ReportDefinition reportDefinition, File reportDir)
	        throws Exception {
		ReportDesign design = new ReportDesign();
		design.setUuid(firstNonBlank(config.getExcelDesignUuid(), config.getDesignUuid()));
		design.setName(firstNonBlank(config.getExcelDesignName(), "Excel"));
		design.setReportDefinition(reportDefinition);
		design.setRendererType(ExcelTemplateRenderer.class);
		
		String templatePath = firstNonBlank(config.getExcelTemplate(), config.getTemplate());
		File templateFile = new File(reportDir, templatePath);
		
		ReportDesignResource resource = buildResource("template", getExtension(templateFile.getName()),
		    getExcelContentType(templateFile.getName()), templateFile, design);
		design.addResource(resource);
		
		return design;
	}
	
	public ReportDesign buildJsonDesign(ReportConfig config, ReportDefinition reportDefinition, File reportDir)
	        throws Exception {
		ReportDesign design = new ReportDesign();
		design.setUuid(config.getJsonDesignUuid());
		design.setName(firstNonBlank(config.getJsonDesignName(), "JSON"));
		design.setReportDefinition(reportDefinition);
		design.setRendererType(TextTemplateRenderer.class);
		design.addPropertyValue("templateType", "json");
		
		ReportDesignResource resource;
		if (hasEmbeddedJsonTemplate(config)) {
			String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config.getJsonTemplateConfig());
			resource = buildInlineResource("template", "json", "application/json", json, design);
		} else {
			File templateFile = new File(reportDir, config.getJsonTemplate());
			resource = buildResource("template", "json", "application/json", templateFile, design);
		}
		
		design.addResource(resource);
		return design;
	}
	
	private ReportDesignResource buildResource(String name, String extension, String contentType, File file,
	        ReportDesign design) throws Exception {
		if (file == null || !file.exists() || !file.isFile()) {
			throw new IllegalArgumentException("Template file does not exist: "
			        + (file == null ? null : file.getAbsolutePath()));
		}
		
		ReportDesignResource resource = new ReportDesignResource();
		resource.setName(name);
		resource.setExtension(extension);
		resource.setContentType(contentType);
		resource.setContents(readFile(file));
		resource.setReportDesign(design);
		return resource;
	}
	
	private ReportDesignResource buildInlineResource(String name, String extension, String contentType, String contents,
	        ReportDesign design) {
		ReportDesignResource resource = new ReportDesignResource();
		resource.setName(name);
		resource.setExtension(extension);
		resource.setContentType(contentType);
		resource.setContents(contents == null ? new byte[0] : contents.getBytes(StandardCharsets.UTF_8));
		resource.setReportDesign(design);
		return resource;
	}
	
	private byte[] readFile(File file) throws Exception {
		InputStream is = null;
		ByteArrayOutputStream os = null;
		
		try {
			is = new FileInputStream(file);
			os = new ByteArrayOutputStream();
			
			byte[] buffer = new byte[4096];
			int len;
			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
			
			return os.toByteArray();
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (Exception e) {
					// ignore
				}
			}
			if (os != null) {
				try {
					os.close();
				}
				catch (Exception e) {
					// ignore
				}
			}
		}
	}
	
	private String getExtension(String filename) {
		if (filename == null) {
			return "";
		}
		
		int dot = filename.lastIndexOf('.');
		return dot >= 0 ? filename.substring(dot + 1) : "";
	}
	
	private String getExcelContentType(String filename) {
		if (filename != null && filename.toLowerCase().endsWith(".xlsx")) {
			return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		}
		return "application/vnd.ms-excel";
	}
	
	private boolean hasValue(String value) {
		return value != null && value.trim().length() > 0;
	}
	
	private boolean hasEmbeddedJsonTemplate(ReportConfig config) {
		return config.getJsonTemplateConfig() != null && !config.getJsonTemplateConfig().isEmpty();
	}
	
	private String firstNonBlank(String first, String second) {
		if (hasValue(first)) {
			return first;
		}
		return second;
	}
}
