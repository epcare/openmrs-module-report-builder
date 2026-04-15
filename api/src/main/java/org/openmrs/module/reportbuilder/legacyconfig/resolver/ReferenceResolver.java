package org.openmrs.module.reportbuilder.legacyconfig.resolver;

import com.fasterxml.jackson.core.type.TypeReference;
import org.openmrs.module.reportbuilder.legacyconfig.model.CohortConfig;
import org.openmrs.module.reportbuilder.legacyconfig.model.ParameterSetConfig;
import org.openmrs.module.reportbuilder.legacyconfig.parser.JsonConfigParser;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

@Component
public class ReferenceResolver {
	
	private final ApplicationContext applicationContext;
	
	private final JsonConfigParser parser;
	
	public ReferenceResolver(ApplicationContext applicationContext, JsonConfigParser parser) {
		this.applicationContext = applicationContext;
		this.parser = parser;
	}
	
	public Object resolveJavaReference(String ref) {
		if (ref == null || !ref.startsWith("java:")) {
			throw new IllegalArgumentException("Invalid java ref: " + ref);
		}
		
		String value = ref.substring("java:".length());
		int lastDot = value.lastIndexOf('.');
		if (lastDot < 0) {
			throw new IllegalArgumentException("Invalid java ref format: " + ref);
		}
		
		String beanName = value.substring(0, lastDot);
		String methodName = value.substring(lastDot + 1);
		
		Object bean = applicationContext.getBean(beanName);
		try {
			Method method = bean.getClass().getMethod(methodName);
			return method.invoke(bean);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to resolve java ref: " + ref, e);
		}
	}
	
	public ParameterSetConfig resolveParameterSet(File resourceBaseDir, String ref) throws IOException {
		File file = new File(resourceBaseDir, ref + ".json");
		return parser.parse(file, ParameterSetConfig.class);
	}
	
	public Map<String, CohortConfig> resolveCohortMap(File file) throws IOException {
		return parser.parse(file, new TypeReference<Map<String, CohortConfig>>() {});
	}
}
