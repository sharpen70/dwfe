package org.guiiis.dwfe.core;

import java.util.ArrayList;
import java.util.List;

import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;

/**
 * The class for datalog program
 * Consists of datalog rules
 * 
 * @author sharpen
 */
public class DatalogProgram extends IndexedByHeadPredicatesRuleSet {
	public List<DefaultDatalogRule> rules;

	public DatalogProgram() {
		rules = new ArrayList<DefaultDatalogRule>();
	}
	
	public void add(DefaultDatalogRule r) {
		rules.add(r);
	}
	
	public void remove(DefaultDatalogRule r) {
		rules.remove(r);
	}
	
	boolean isRecursive() {
		return false;
	}
}
