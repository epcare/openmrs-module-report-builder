package org.openmrs.module.reportbuilder.util;

import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class PatientDataHelper {
	
	private static final Logger log = LoggerFactory.getLogger(PatientDataHelper.class);
	
	public void addCol(DataSetRow row, String label, Object value) {
		if (value == null) {
			value = "";
		}
		DataSetColumn c = new DataSetColumn(label, label, value.getClass());
		row.addColumnValue(c, value);
	}
}
