package org.openmrs.module.reportbuilder.legacyconfig.support;

import org.springframework.stereotype.Component;

@Component("legacySupportLibrary")
public class LegacySupportLibrary {
	
	public Object newInstance(String className) {
		if (className == null || className.trim().length() == 0) {
			throw new IllegalArgumentException("Class name is required");
		}
		try {
			return Class.forName(className).newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to instantiate class: " + className, e);
		}
	}
}
