package org.guiiis.dwfe.core;

import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.core.unifier.QueryUnifier;

public class ExtendedQueryUnifier {
	public String ruleLabel = "ERROR";
	private QueryUnifier unifier;
	private int aggregatednum;
	
	public ExtendedQueryUnifier(QueryUnifier u, int aggNum) {
		this.unifier = u;
		this.aggregatednum = aggNum;
	}
	
	public ExtendedQueryUnifier(String label, QueryUnifier u, int aggNum) {
		this(u, aggNum);
		this.ruleLabel = label;
	}
	
	public void setLabel(String label) {
		this.ruleLabel = label;
	}
	
	public int getAgg() {
		return this.aggregatednum;
	}
	
	public QueryUnifier getUnifier() {
		return this.unifier;
	}
	
	public String getRuleLabel() {
		return ruleLabel;
	}
}
