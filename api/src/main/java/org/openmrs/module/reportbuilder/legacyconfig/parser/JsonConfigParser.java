package org.openmrs.module.reportbuilder.legacyconfig.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
public class JsonConfigParser {
	
	private final ObjectMapper objectMapper;
	
	public JsonConfigParser() {
		this.objectMapper = new ObjectMapper();
		this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}
	
	public <T> T parse(File file, Class<T> clazz) throws IOException {
		return objectMapper.readValue(file, clazz);
	}
	
	public <T> T parse(File file, TypeReference<T> typeReference) throws IOException {
		return objectMapper.readValue(file, typeReference);
	}
	
	public <T> T parse(InputStream inputStream, Class<T> clazz) throws IOException {
		return objectMapper.readValue(inputStream, clazz);
	}
	
	public <T> T parse(InputStream inputStream, TypeReference<T> typeReference) throws IOException {
		return objectMapper.readValue(inputStream, typeReference);
	}
}
