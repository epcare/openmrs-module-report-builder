package org.openmrs.module.reportbuilder.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.api.APIException;
import org.openmrs.module.reportbuilder.legacyconfig.LegacyReportImporter;
import org.openmrs.module.reportbuilder.legacyconfig.model.ReportConfig;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/reportbuilder/legacy")
public class LegacyReportController extends BaseRestController {

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final LegacyReportImporter legacyReportImporter = new LegacyReportImporter();

	private static final String TEMP_UPLOAD_DIR = System.getProperty("java.io.tmpdir") + "/reportbuilder_uploads";

	/**
	 * List all legacy reports
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public SimpleObject listLegacyReports() {
		SimpleObject response = new SimpleObject();
		try {
			// For now, return an empty list - in the future, this would scan the configured legacy report directory
			List<SimpleObject> reports = new ArrayList<>();
			response.put("results", reports);
			response.put("count", reports.size());
			return response;
		} catch (Exception e) {
			throw new APIException("Failed to list legacy reports", e);
		}
	}

	/**
	 * Get a specific legacy report by UUID
	 */
	@RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleObject getLegacyReport(@PathVariable("uuid") String uuid) {
		SimpleObject response = new SimpleObject();
		try {
			// TODO: Implement fetching specific legacy report
			response.put("uuid", uuid);
			response.put("status", "NOT_FOUND");
			return response;
		} catch (Exception e) {
			throw new APIException("Failed to get legacy report: " + uuid, e);
		}
	}

	/**
	 * Upload and validate a legacy report JSON file
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@ResponseBody
	public SimpleObject uploadLegacyReport(@RequestParam("file") MultipartFile file) {
		SimpleObject response = new SimpleObject();

		if (file.isEmpty()) {
			response.put("success", false);
			response.put("error", "File is empty");
			return response;
		}

		if (!file.getOriginalFilename().endsWith(".json")) {
			response.put("success", false);
			response.put("error", "Only JSON files are allowed");
			return response;
		}

		try {
			// Create upload directory if it doesn't exist
			File uploadDir = new File(TEMP_UPLOAD_DIR);
			if (!uploadDir.exists()) {
				uploadDir.mkdirs();
			}

			// Save the uploaded file
			Path tempFile = Files.createTempFile(uploadDir.toPath(), "legacy_report_", ".json");
			Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

			// Parse and validate the JSON
			String jsonContent = new String(Files.readAllBytes(tempFile));
			ReportConfig reportConfig = objectMapper.readValue(jsonContent, ReportConfig.class);

			// Validate the report
			LegacyReportImporter.ValidationResult validationResult =
				legacyReportImporter.validateContract(tempFile.toFile(), null);

			response.put("success", true);
			response.put("filename", file.getOriginalFilename());
			response.put("reportUuid", reportConfig.getUuid());
			response.put("reportName", reportConfig.getName());
			response.put("reportKey", reportConfig.getKey());
			response.put("description", reportConfig.getDescription());
			response.put("status", reportConfig.getStatus());
			response.put("parameters", reportConfig.getParameters());
			response.put("datasets", reportConfig.getDatasets());
			response.put("designs", reportConfig.getDesigns());
			response.put("jsonTemplateConfig", reportConfig.getJsonTemplateConfig());
			response.put("tempFilePath", tempFile.toString());
			response.put("valid", validationResult.isValid());

			if (!validationResult.getErrors().isEmpty()) {
				response.put("errors", validationResult.getErrors());
			}

		} catch (Exception e) {
			response.put("success", false);
			response.put("error", "Failed to parse JSON file: " + e.getMessage());
		}

		return response;
	}

	/**
	 * Import a validated legacy report into the report builder
	 */
	@RequestMapping(value = "/import", method = RequestMethod.POST)
	@ResponseBody
	public SimpleObject importLegacyReport(@RequestBody Map<String, Object> payload) {
		SimpleObject response = new SimpleObject();

		try {
			String tempFilePath = (String) payload.get("tempFilePath");
			String reportName = (String) payload.get("reportName");

			if (tempFilePath == null) {
				response.put("success", false);
				response.put("error", "tempFilePath is required");
				return response;
			}

			File jsonFile = new File(tempFilePath);
			if (!jsonFile.exists()) {
				response.put("success", false);
				response.put("error", "Temporary file not found");
				return response;
			}

			// Import the report using the legacy importer
			org.openmrs.module.reporting.report.definition.ReportDefinition reportDefinition =
				legacyReportImporter.importReportFromFile(jsonFile);

			response.put("success", true);
			response.put("reportName", reportName);
			response.put("reportDefinitionUuid", reportDefinition.getUuid());
			response.put("message", "Legacy report imported successfully");

			// Clean up temp file
			Files.deleteIfExists(jsonFile.toPath());

		} catch (Exception e) {
			response.put("success", false);
			response.put("error", "Failed to import legacy report: " + e.getMessage());
		}

		return response;
	}

	/**
	 * Validate a legacy report JSON file
	 */
	@RequestMapping(value = "/validate", method = RequestMethod.POST)
	@ResponseBody
	public SimpleObject validateLegacyReport(@RequestBody Map<String, Object> payload) {
		SimpleObject response = new SimpleObject();

		try {
			String jsonContent = (String) payload.get("jsonContent");

			if (jsonContent == null) {
				response.put("success", false);
				response.put("error", "jsonContent is required");
				return response;
			}

			// Parse the JSON
			ReportConfig reportConfig = objectMapper.readValue(jsonContent, ReportConfig.class);

			response.put("success", true);
			response.put("valid", true);
			response.put("reportUuid", reportConfig.getUuid());
			response.put("reportName", reportConfig.getName());
			response.put("reportKey", reportConfig.getKey());
			response.put("description", reportConfig.getDescription());
			response.put("status", reportConfig.getStatus());
			response.put("parametersCount", reportConfig.getParameters().size());
			response.put("datasetsCount", reportConfig.getDatasets().size());
			response.put("designsCount", reportConfig.getDesigns().size());
			response.put("hasJsonTemplateConfig", !reportConfig.getJsonTemplateConfig().isEmpty());

		} catch (Exception e) {
			response.put("success", false);
			response.put("valid", false);
			response.put("error", "Failed to validate JSON: " + e.getMessage());
		}

		return response;
	}
}
