package com.newsbank.permuter.types;

import java.util.Map;

public class DocStore {
	
	int score;
	int tokCount;
	String unq;
	String query;
	String type;
	Map <String, Integer> evidence;

	//constructors
	public DocStore() {
		
	}
	public DocStore(int score, String unq, String query, String type, int tokCount, Map<String, Integer> evidence) {
		super();
		this.score = score;
		this.unq = unq;
		this.query = query;
		this.type = type;
		this.tokCount = tokCount;
		this.evidence = evidence;
	}
	
	//getters/setters
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public int getTokCount() {
		return tokCount;
	}
	public void setTokCount(int tokCount) {
		this.tokCount = tokCount;
	}
	public String getUnq() {
		return unq;
	}
	public void setUnq(String unq) {
		this.unq = unq;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Map<String, Integer> getEvidence() {
		return evidence;
	}
	public void setEvidence(Map<String, Integer> evidence) {
		this.evidence = evidence;
	}
	
	//methods; count how many 
	
}


