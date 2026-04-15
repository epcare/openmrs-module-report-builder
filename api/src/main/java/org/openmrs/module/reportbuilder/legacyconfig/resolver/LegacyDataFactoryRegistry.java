package org.openmrs.module.reportbuilder.legacyconfig.resolver;

import org.openmrs.module.reportbuilder.legacyconfig.model.AliasMethodConfig;
import org.openmrs.module.reportbuilder.legacyconfig.model.FactoryMethodConfig;
import org.openmrs.module.reportbuilder.legacyconfig.model.LegacyDataFactoryConfig;
import org.openmrs.module.reportbuilder.legacyconfig.parser.JsonConfigParser;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Component
public class LegacyDataFactoryRegistry {
	
	private static final String CONFIG_RELATIVE_PATH = "configuration/reports/legacy/datafactory/data-factory-config.json";
	
	private final LegacyDataFactoryConfig config;
	
	public LegacyDataFactoryRegistry(JsonConfigParser parser) {
		this.config = loadConfig(parser);
	}
	
	public AliasMethodConfig getConcept(String key) {
		return key == null ? null : config.getConcepts().get(key);
	}
	
	public AliasMethodConfig getEncounterType(String key) {
		return key == null ? null : config.getEncounterTypes().get(key);
	}
	
	public AliasMethodConfig getPatientDataSource(String key) {
		return key == null ? null : config.getPatientDataSources().get(key);
	}
	
	public AliasMethodConfig getConverter(String key) {
		return key == null ? null : config.getConverters().get(key);
	}
	
	public AliasMethodConfig getDimension(String key) {
		return key == null ? null : config.getDimensions().get(key);
	}
	
	public FactoryMethodConfig getRowFilter(String type) {
		return type == null ? null : config.getRowFilters().get(type);
	}
	
	private LegacyDataFactoryConfig loadConfig(JsonConfigParser parser) {
		File configFile = resolveConfigFile();
		if (configFile == null || !configFile.exists() || !configFile.isFile()) {
			return new LegacyDataFactoryConfig();
		}
		
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(configFile);
			LegacyDataFactoryConfig loaded = parser.parse(inputStream, LegacyDataFactoryConfig.class);
			return loaded == null ? new LegacyDataFactoryConfig() : loaded;
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to load legacy data factory config from " + configFile.getAbsolutePath(), e);
		}
		finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				}
				catch (Exception ignore) {
					// ignore
				}
			}
		}
	}
	
	private File resolveConfigFile() {
		String appDataDir = OpenmrsUtil.getApplicationDataDirectory();
		if (appDataDir == null || appDataDir.trim().length() == 0) {
			return null;
		}
		return new File(appDataDir, CONFIG_RELATIVE_PATH);
	}
}
