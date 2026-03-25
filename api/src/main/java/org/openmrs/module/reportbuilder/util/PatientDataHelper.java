package org.openmrs.module.reportbuilder.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;

/**
 */
public class PatientDataHelper {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	public void addCol(DataSetRow row, String label, Object value) {
		if (value == null) {
			value = "";
		}
		DataSetColumn c = new DataSetColumn(label, label, value.getClass());
		row.addColumnValue(c, value);
	}
}
