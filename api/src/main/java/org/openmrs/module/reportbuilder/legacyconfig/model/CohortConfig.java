package org.openmrs.module.reportbuilder.legacyconfig.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CohortConfig {
	
	private String type;
	
	@JsonAlias({ "label", "name" })
	private String name;
	
	// sql
	@JsonAlias({ "sql", "query" })
	private String query;
	
	// coded obs
	private String question;
	
	@JsonAlias({ "encounterType", "encounterTypeRef" })
	private String encounterTypeRef;
	
	private List<String> answers = new ArrayList<String>();
	
	private String timeModifier;
	
	// numeric obs
	private String comparator;
	
	private Double value;
	
	// composition
	private String composition;
	
	private Map<String, String> searches = new HashMap<String, String>();
	
	// reference
	@JsonAlias({ "reference", "ref" })
	private String ref;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getQuestion() {
		return question;
	}
	
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public String getEncounterTypeRef() {
		return encounterTypeRef;
	}
	
	public void setEncounterTypeRef(String encounterTypeRef) {
		this.encounterTypeRef = encounterTypeRef;
	}
	
	public List<String> getAnswers() {
		return answers;
	}
	
	public void setAnswers(List<String> answers) {
		this.answers = answers;
	}
	
	public String getTimeModifier() {
		return timeModifier;
	}
	
	public void setTimeModifier(String timeModifier) {
		this.timeModifier = timeModifier;
	}
	
	public String getComparator() {
		return comparator;
	}
	
	public void setComparator(String comparator) {
		this.comparator = comparator;
	}
	
	public Double getValue() {
		return value;
	}
	
	public void setValue(Double value) {
		this.value = value;
	}
	
	public String getComposition() {
		return composition;
	}
	
	public void setComposition(String composition) {
		this.composition = composition;
	}
	
	public Map<String, String> getSearches() {
		return searches;
	}
	
	public void setSearches(Map<String, String> searches) {
		this.searches = searches;
	}
	
	public String getRef() {
		return ref;
	}
	
	public void setRef(String ref) {
		this.ref = ref;
	}
}
