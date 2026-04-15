package org.openmrs.module.reportbuilder.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of legacy report validation. Contains validation status, errors, warnings, and detailed
 * validation information.
 */
public class ReportValidationResult {
	
	private boolean valid = true;
	
	private List<String> errors = new ArrayList<String>();
	
	private List<String> warnings = new ArrayList<String>();
	
	private SQLValidationResult sqlValidation = new SQLValidationResult();
	
	public static class SQLValidationResult {
		
		private boolean passed = true;
		
		private List<String> sqlErrors = new ArrayList<String>();
		
		private List<String> sqlWarnings = new ArrayList<String>();
		
		public boolean isPassed() {
			return passed;
		}
		
		public void setPassed(boolean passed) {
			this.passed = passed;
		}
		
		public List<String> getSqlErrors() {
			return sqlErrors;
		}
		
		public void setSqlErrors(List<String> sqlErrors) {
			this.sqlErrors = sqlErrors;
		}
		
		public List<String> getSqlWarnings() {
			return sqlWarnings;
		}
		
		public void setSqlWarnings(List<String> sqlWarnings) {
			this.sqlWarnings = sqlWarnings;
		}
		
		public void addSqlError(String error) {
			this.sqlErrors.add(error);
			this.passed = false;
		}
		
		public void addSqlWarning(String warning) {
			this.sqlWarnings.add(warning);
		}
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	public List<String> getErrors() {
		return errors;
	}
	
	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
	
	public List<String> getWarnings() {
		return warnings;
	}
	
	public void setWarnings(List<String> warnings) {
		this.warnings = warnings;
	}
	
	public SQLValidationResult getSqlValidation() {
		return sqlValidation;
	}
	
	public void setSqlValidation(SQLValidationResult sqlValidation) {
		this.sqlValidation = sqlValidation;
	}
	
	public void addError(String error) {
		this.errors.add(error);
		this.valid = false;
	}
	
	public void addWarning(String warning) {
		this.warnings.add(warning);
	}
	
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	
	public boolean hasWarnings() {
		return !warnings.isEmpty();
	}
	
	public String getSummary() {
		StringBuilder summary = new StringBuilder();
		if (valid) {
			summary.append("Validation passed");
		} else {
			summary.append("Validation failed");
		}
		
		if (hasErrors()) {
			summary.append(" with ").append(errors.size()).append(" error(s)");
		}
		if (hasWarnings()) {
			summary.append(" and ").append(warnings.size()).append(" warning(s)");
		}
		
		return summary.toString();
	}
}
