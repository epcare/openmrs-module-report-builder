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
			
			// Extract column definitions from the config
			List<ColumnDefinition> columns = extractColumnDefinitions(config);
			
			// Extract data rows from the report data
			List<Map<String, Object>> dataRows = extractDataRows(reportData);
			
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
	 * Extracts column definitions from the compiled linelist config.
	 */
	private List<ColumnDefinition> extractColumnDefinitions(JsonNode config) {
		List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
		
		JsonNode dataSetDefinitions = config.path("dataSetDefinitions");
		if (!dataSetDefinitions.isArray() || dataSetDefinitions.size() == 0) {
			return columns;
		}
		
		// Get the first PATIENT_DATA_SET
		for (JsonNode dsd : dataSetDefinitions) {
			if ("PATIENT_DATA_SET".equals(dsd.path("type").asText())) {
				JsonNode columnsNode = dsd.path("columns");
				if (columnsNode.isArray()) {
					for (JsonNode col : columnsNode) {
						String name = col.path("name").asText("");
						String key = col.has("key") ? col.path("key").asText() : nameToKey(name);
						columns.add(new ColumnDefinition(key, name));
					}
				}
				break;
			}
		}
		
		return columns;
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
		        + "font-size:12px;text-align:left;}"
		        + "table.linelist-table th{font-weight:bold;position:sticky;top:0;}"
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
