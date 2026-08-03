package org.openmrs.module.reportbuilder.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Renders linelist report data into HTML table format. Expected input format is the
 * LegacyGenericReportSchema with baseCohortDefinition and dataSetDefinitions containing
 * PATIENT_DATA_SET columns.
 */
public class LinelistHtmlRenderer {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	public static class Result {
		
		public final String html;
		
		public final String payloadJson;
		
		public final String renderedOutputJson;
		
		public Result(String html, String payloadJson, String renderedOutputJson) {
			this.html = html;
			this.payloadJson = payloadJson;
			this.renderedOutputJson = renderedOutputJson;
		}
	}
	
	/**
	 * Converts linelist report data to HTML table format.
	 * 
	 * @param reportData the evaluated report data containing the linelist dataset
	 * @param reportDesign the report design containing the compiled linelist config
	 * @return Result containing html, payloadJson, and renderedOutputJson
	 */
	public Result convert(ReportData reportData, ReportDesign reportDesign) {
		try {
			// Read the compiled linelist config from the report design
			String templateJson = readDesignResource(reportDesign);
			JsonNode config = MAPPER.readTree(templateJson);
			
			// Extract data rows from the report data FIRST
			List<Map<String, Object>> dataRows = extractDataRows(reportData);
			
			// Extract column definitions from actual data (handles expanded columns)
			List<ColumnDefinition> columns = extractColumnDefinitionsFromData(config, dataRows);
			
			// Build HTML
			String html = renderHtmlTable(config, columns, dataRows);
			
			// Build payload JSON
			String payloadJson = buildPayloadJson(config, columns, dataRows);
			
			// Build rendered output JSON
			String renderedOutputJson = buildRenderedOutputJson(config, columns, dataRows, html);
			
			return new Result(html, payloadJson, renderedOutputJson);
			
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to render linelist report", e);
		}
	}
	
	/**
	 * Reads the JSON template from the report design resource.
	 */
	private String readDesignResource(ReportDesign reportDesign) {
		if (reportDesign == null || reportDesign.getResources() == null) {
			throw new RuntimeException("Report design has no resources");
		}
		
		for (org.openmrs.module.reporting.report.ReportDesignResource resource : reportDesign.getResources()) {
			if ("template".equals(resource.getName())) {
				byte[] content = resource.getContents();
				if (content == null || content.length == 0) {
					throw new RuntimeException("Report design template is empty");
				}
				return new String(content, StandardCharsets.UTF_8);
			}
		}
		
		throw new RuntimeException("No template resource found in report design");
	}
	
	/**
	 * Extracts column definitions from the compiled linelist config. Returns columns from actual
	 * data rows (to handle expanded columns like weight_1, weight_2), with display names from the
	 * config where available.
	 */
	private List<ColumnDefinition> extractColumnDefinitions(JsonNode config) {
		List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
		
		JsonNode dataSetDefinitions = config.path("dataSetDefinitions");
		if (!dataSetDefinitions.isArray() || dataSetDefinitions.size() == 0) {
			return columns;
		}
		
		// First, extract columns from config for display name mapping
		Map<String, String> keyToDisplayName = new LinkedHashMap<String, String>();
		for (JsonNode dsd : dataSetDefinitions) {
			if ("PATIENT_DATA_SET".equals(dsd.path("type").asText())) {
				JsonNode columnsNode = dsd.path("columns");
				if (columnsNode.isArray()) {
					for (JsonNode col : columnsNode) {
						String name = col.path("name").asText("");
						String key = col.has("key") ? col.path("key").asText() : nameToKey(name);
						keyToDisplayName.put(key, name);
					}
				}
				break;
			}
		}
		
		return new ArrayList<ColumnDefinition>(columns);
	}
	
	/**
	 * Extracts column definitions from actual data rows. This handles expanded columns (e.g.,
	 * weight_1, weight_2) that aren't in the original config. Uses display names from config where
	 * available, otherwise formats the key for display.
	 */
	private List<ColumnDefinition> extractColumnDefinitionsFromData(JsonNode config, List<Map<String, Object>> dataRows) {
		List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
		
		if (dataRows.isEmpty()) {
			// Fallback to config columns if no data
			return extractColumnDefinitions(config);
		}
		
		// Extract display name mapping from config (in config order)
		Map<String, String> configDisplayNames = new LinkedHashMap<String, String>();
		List<String> configColumnOrder = new ArrayList<String>();
		JsonNode dataSetDefinitions = config.path("dataSetDefinitions");
		if (dataSetDefinitions.isArray()) {
			for (JsonNode dsd : dataSetDefinitions) {
				if ("PATIENT_DATA_SET".equals(dsd.path("type").asText())) {
					JsonNode columnsNode = dsd.path("columns");
					if (columnsNode.isArray()) {
						for (JsonNode col : columnsNode) {
							String name = col.path("name").asText("");
							String key = col.has("key") ? col.path("key").asText() : nameToKey(name);
							configDisplayNames.put(key, name);
							configColumnOrder.add(key);
						}
					}
					break;
				}
			}
		}
		
		// Get all column keys from data (including expanded columns)
		Map<String, Object> firstRow = dataRows.get(0);
		Set<String> dataColumnKeys = firstRow.keySet();
		
		// Build columns list by iterating through config columns in order
		// This ensures the primary order is the config's order
		for (String configKey : configColumnOrder) {
			if (dataColumnKeys.contains(configKey)) {
				columns.add(new ColumnDefinition(configKey, configDisplayNames.get(configKey)));
			}
		}
		
		// Add any expanded columns not in config (e.g., weight_1, weight_2) at the end
		for (String dataKey : dataColumnKeys) {
			if (!configDisplayNames.containsKey(dataKey)) {
				// Check if this is an expanded column
				String baseKey = findBaseKeyForExpandedColumn(dataKey, configDisplayNames.keySet());
				String displayName;
				if (baseKey != null && configDisplayNames.containsKey(baseKey)) {
					String baseName = configDisplayNames.get(baseKey);
					int occurrence = getOccurrenceNumber(dataKey);
					displayName = baseName + " " + occurrence;
				} else {
					displayName = formatKeyForDisplay(dataKey);
				}
				columns.add(new ColumnDefinition(dataKey, displayName));
			}
		}
		
		return columns;
	}
	
	/**
	 * Finds the base key for an expanded column. For example, given "weight_2" and base keys
	 * ["weight", "name"], returns "weight".
	 */
	private String findBaseKeyForExpandedColumn(String expandedKey, Set<String> baseKeys) {
		for (String baseKey : baseKeys) {
			if (expandedKey.startsWith(baseKey + "_")) {
				String suffix = expandedKey.substring(baseKey.length() + 1);
				try {
					// Check if suffix is a number
					Integer.parseInt(suffix);
					return baseKey;
				}
				catch (NumberFormatException e) {
					// Not a number suffix, continue
				}
			}
		}
		return null;
	}
	
	/**
	 * Extracts the occurrence number from an expanded column key. For example, "weight_2" returns
	 * 2.
	 */
	private int getOccurrenceNumber(String columnKey) {
		int lastUnderscore = columnKey.lastIndexOf('_');
		if (lastUnderscore > 0) {
			String suffix = columnKey.substring(lastUnderscore + 1);
			try {
				return Integer.parseInt(suffix);
			}
			catch (NumberFormatException e) {
				return 1;
			}
		}
		return 1;
	}
	
	/**
	 * Formats a column key for display. Converts "patient_name" to "Patient Name", "weight_1" to
	 * "Weight 1", etc.
	 */
	private String formatKeyForDisplay(String key) {
		if (key == null || key.isEmpty()) {
			return "";
		}
		
		// Split by underscore and capitalize each part
		String[] parts = key.split("_");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (i > 0) {
				sb.append(" ");
			}
			if (!parts[i].isEmpty()) {
				sb.append(Character.toUpperCase(parts[i].charAt(0)));
				if (parts[i].length() > 1) {
					sb.append(parts[i].substring(1).toLowerCase());
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * Extracts data rows from the evaluated report data.
	 */
	private List<Map<String, Object>> extractDataRows(ReportData reportData) {
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
		
		for (DataSet dataSet : reportData.getDataSets().values()) {
			Iterator<?> it = dataSet.iterator();
			while (it.hasNext()) {
				DataSetRow dataRow = (DataSetRow) it.next();
				Map<String, Object> row = new LinkedHashMap<String, Object>();
				for (Map.Entry<String, Object> entry : dataRow.getColumnValuesByKey().entrySet()) {
					row.put(entry.getKey(), entry.getValue());
				}
				rows.add(row);
			}
		}
		
		return rows;
	}
	
	/**
	 * Renders the linelist data as an HTML table.
	 */
	private String renderHtmlTable(JsonNode config, List<ColumnDefinition> columns, List<Map<String, Object>> dataRows) {
		StringBuilder sb = new StringBuilder();
		sb.append("<!doctype html><html><head><meta charset='utf-'/>");
		sb.append("<style>").append(getTableStyles()).append("</style></head><body>");
		
		// Report title
		String reportName = config.path("name").asText("");
		if (reportName != null && !reportName.isEmpty()) {
			sb.append("<div class='reportTitle'>").append(esc(reportName)).append("</div>");
		}
		
		// Report description
		String description = config.path("description").asText("");
		if (description != null && !description.isEmpty()) {
			sb.append("<div class='reportDescription'>").append(esc(description)).append("</div>");
		}
		
		if (columns.isEmpty()) {
			sb.append("<div>No columns defined in report.</div>");
			sb.append("</body></html>");
			return sb.toString();
		}
		
		sb.append("<table class='linelist-table'>");
		sb.append("<thead><tr>");
		
		// Header row
		for (ColumnDefinition col : columns) {
			sb.append("<th>").append(esc(col.displayName)).append("</th>");
		}
		sb.append("</tr></thead>");
		
		// Data rows
		sb.append("<tbody>");
		for (Map<String, Object> dataRow : dataRows) {
			sb.append("<tr>");
			for (ColumnDefinition col : columns) {
				Object value = dataRow.get(col.key);
				sb.append("<td>").append(value != null ? esc(value.toString()) : "").append("</td>");
			}
			sb.append("</tr>");
		}
		sb.append("</tbody>");
		
		sb.append("</table>");
		
		// Row count footer
		sb.append("<div class='rowCount'>").append(dataRows.size()).append(" rows</div>");
		
		sb.append("</body></html>");
		return sb.toString();
	}
	
	/**
	 * Builds the payload JSON containing just the data values.
	 */
	private String buildPayloadJson(JsonNode config, List<ColumnDefinition> columns, List<Map<String, Object>> dataRows) {
		try {
			ObjectNode root = MAPPER.createObjectNode();
			ObjectNode jsonData = MAPPER.createObjectNode();
			
			// Add metadata
			jsonData.put("name", config.path("name").asText(""));
			jsonData.put("description", config.path("description").asText(""));
			
			// Add columns
			ArrayNode columnsArray = MAPPER.createArrayNode();
			for (ColumnDefinition col : columns) {
				ObjectNode colNode = MAPPER.createObjectNode();
				colNode.put("key", col.key);
				colNode.put("name", col.displayName);
				columnsArray.add(colNode);
			}
			jsonData.set("columns", columnsArray);
			
			// Add data rows
			ArrayNode rowsArray = MAPPER.createArrayNode();
			for (Map<String, Object> dataRow : dataRows) {
				ObjectNode rowNode = MAPPER.createObjectNode();
				for (ColumnDefinition col : columns) {
					Object value = dataRow.get(col.key);
					rowNode.put(col.key, value != null ? value.toString() : "");
				}
				rowsArray.add(rowNode);
			}
			jsonData.set("data", rowsArray);
			jsonData.put("rowCount", dataRows.size());
			
			root.set("json", jsonData);
			return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
			
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to build payload JSON", e);
		}
	}
	
	/**
	 * Builds the rendered output JSON containing both json and html.
	 */
	private String buildRenderedOutputJson(JsonNode config, List<ColumnDefinition> columns,
	        List<Map<String, Object>> dataRows, String html) {
		try {
			ObjectNode root = MAPPER.createObjectNode();
			
			// Re-use the payload JSON structure
			String payloadJson = buildPayloadJson(config, columns, dataRows);
			JsonNode payloadNode = MAPPER.readTree(payloadJson);
			root.set("json", payloadNode.path("json"));
			
			// Add HTML
			root.put("html", html);
			
			// Add empty dhis2 node for consistency with aggregate reports
			ObjectNode dhis2Node = MAPPER.createObjectNode();
			dhis2Node.put("enabled", false);
			dhis2Node.set("rows", MAPPER.createArrayNode());
			root.set("dhis2", dhis2Node);
			
			return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
			
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to build rendered output JSON", e);
		}
	}
	
	private String getTableStyles() {
		return "body{font-family:Arial,Helvetica,sans-serif;margin:12px;color:#222;}"
		        + ".reportTitle{font-size:16px;font-weight:bold;margin-bottom:10px;}"
		        + ".reportDescription{font-size:13px;color:#666;margin-bottom:15px;}"
		        + ".rowCount{margin-top:10px;font-size:12px;color:#666;}"
		        + "table.linelist-table{border-collapse:collapse;width:100%;margin-bottom:20px;}"
		        + "table.linelist-table th,table.linelist-table td{border:1px solid #ddd;padding:8px;"
		        + "font-size:12px;text-align:left;}" + "table.linelist-table th{font-weight:bold;position:sticky;top:0;}"
		        + "table.linelist-table tbody tr:nth-child(even){background:#f9f9f9;}"
		        + "table.linelist-table tbody tr:hover{background:#f0f0f0;}";
	}
	
	private String nameToKey(String name) {
		if (name == null || name.isEmpty()) {
			return "";
		}
		return name.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
	}
	
	private String esc(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&#39;")
		        .replace("\"", "&quot;");
	}
	
	private static class ColumnDefinition {
		
		final String key;
		
		final String displayName;
		
		ColumnDefinition(String key, String displayName) {
			this.key = key;
			this.displayName = displayName;
		}
	}
}
